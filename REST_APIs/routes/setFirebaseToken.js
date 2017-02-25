var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');

module.exports = function(app) {

	app.post('/setFirebaseToken', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.fbtoken) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var fbtoken = req.body.fbtoken.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
				var fbtoken = "0";
			}


			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.firebaseTokenValidation(fbtoken)) {

				checkVerification.mobileUIDVerification(mobile, uid, function(val) {

					if(val == 1) {

						MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

							if(err) {
								console.log(err);
								res.status(200).json({statusCode:'500', message:'internal server error'});
							}
							else {
								
								var usersCollection = db.collection('users');
								usersCollection.updateOne({mobile: mobile},{$set:{fbtoken: fbtoken}}, function(err, result) {

									if(err) {
										console.log(err);
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
					else if(val == 2)
						res.status(200).json({statusCode:'401', message:'unauthorized'});
					else if(val == 3)
						res.status(200).json({statusCode:'404', message:'not found'});
					else if(val == 4)
						res.status(200).json({statusCode:'500', message:'internal server error'});

				});

			}
			else
				res.status(200).json({statusCode:'400', message:'bad request'});
		}
		else
			res.status(200).json({statusCode:'400', message:'bad request'});

	});

};