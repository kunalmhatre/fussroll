var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');

module.exports = function(app) {

	app.post('/delete', function(req, res) {

		if(req.body.mobile && req.body.uid) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
			}

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid)) {

						checkVerification.mobileUIDVerification(mobile, uid, function(val) {
							if(val == 1) {
								
								MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

									if(err) {
										console.log(err);
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else {

										var usersCollection = db.collection('users');
										var contactsCollection = db.collection('contacts');
										var logsCollection = db.collection('logs');
										var cloudMessagesCollection = db.collection('cloudMessages');

										usersCollection.deleteOne({mobile: mobile}, function(err, result) {

											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else {

												logsCollection.deleteOne({mobile: mobile}, function(err, result) {

													if(err) {
														db.close();
														res.status(200).json({statusCode:'500', message:'internal server error'});
													}
													else {

														contactsCollection.deleteOne({mobile: mobile}, function(err, result) {

															if(err) {
																db.close();
																res.status(200).json({statusCode:'500', message:'internal server error'});
															}
															else {

																cloudMessagesCollection.deleteOne({mobile: mobile}, function(err, result) {

																	if(err) {
																		db.close();
																		res.status(200).json({statusCode:'500', message:'internal server error'});
																	}
																	else {
																		db.close();
																		res.status(200).json({statusCode:'200', message:'done'});				
																	}

																});
																
															}

														});

													}

												});

											}

										});
									}

								});

							}
							else if(val == 2)
								res.status(200).json({statusCode:'401', message:'unauthorized'});
							else if(val == 3)
								res.status(200).json({statusCode:'404', message:'not found'});
							else if(val == 4)
								res.status(200).json({statusCode:'500', message:'internal server error'});
						});

			}

		} 

	});

};