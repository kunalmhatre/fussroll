var MongoClient = require('mongodb').MongoClient;
var crypto = require('crypto');
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');

module.exports = function(app) {

	app.post('/verifyOTP', function(req, res) {

		if(req.body.mobile && req.body.otp && req.body.uid) {

			try{
				var mobile = req.body.mobile.trim();
				var otp = req.body.otp.trim();
				var uid = req.body.uid.trim();
			}
			catch(e) {
				var mobile = "0";
				var otp = "0";
				var uid = "0";
			}

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.otpValidation(otp) && fieldsValidation.uidValidation(uid)) {

				MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

					if(err) {
						console.log(err);
						res.status(200).json({statusCode:'500', message:'internal server error'});
					}
					else {
						
						var uidHash = crypto.createHash('sha256').update(uid).digest('base64');

						var query = {
							mobile: mobile,
							otp: otp,
							uid: uidHash
						};

						var usersCollection = db.collection('users');
						usersCollection.findOne(query, function(err, item) {
							
							if(err) {
								db.close();
								res.status(200).json({statusCode:'500', message:'internal server error'});
							}
							else if(item){

								//For first-time registration
								usersCollection.updateOne(item, {$set: {verified: true, otp: 0}}, function(err, result) {
									if(err) {
										db.close();
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else {
										db.close();
										res.status(200).json({statusCode:'201', message:'created'});
									}
								});

							}
							else {

								var query = {
									mobile: mobile,
									otp: otp,
									nuid: uidHash
								};

								usersCollection.findOne(query, function(err, item) {
									if(err) {
										db.close();
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else if(item) {

										//For re-registration/login on another device
										var uidItem = item.uid;
										usersCollection.updateOne(item, {$set: {uid: uidHash, ouid: uidItem, verified: true, otp: 0, privacyContacts: false, block: []}}, function(err, result) {
											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else {
												var contactsCollection = db.collection('contacts');
												contactsCollection.remove({mobile: mobile}, function(err, result) {

													if(err) {
														db.close();
														res.status(200).json({statusCode:'500', message:'internal server error'});
													}
													else {
														db.close();
														res.status(200).json({statusCode:'201', message:'created'});
													}

												});

											}
										});
										
									}
									else {
										db.close();
										res.status(200).json({statusCode:'404', message:'not found'});
									}
								});
							}
						});

					}

				});

			}
			else
				res.status(200).json({statusCode:'400', message:'bad request'});
		}
		else
			res.status(200).json({status:'400', message:'bad request'});
		
	});

};