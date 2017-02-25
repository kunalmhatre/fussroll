var MongoClient = require('mongodb').MongoClient;
var moment = require('moment');
var crypto = require('crypto');
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var sendOTP = require(__dirname+'/../library/sendOTP.js');

module.exports = function(app) {

	app.post('/register', function(req, res) {

		if(req.body.mobile) {

			try{
				var mobile = req.body.mobile.trim();
			}
			catch(e) {
				var mobile = "0";
			}

			if(fieldsValidation.mobileValidation(mobile)) {
				if(mobile.slice(0, -10) === "+91" || mobile.slice(0, -10) === "+1" || mobile.slice(0, -10) === "+44") {
					MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {
						if(err) {
							console.log(err);
							res.status(200).json({statusCode:'500', message:'internal server error'});
						}
						else {
							var usersCollection = db.collection('users');
							usersCollection.findOne({mobile: mobile}, function(err, item) {
								if(err) {
									db.close();
									res.status(200).json({statusCode:'500', message:'internal server error'});
								}
								else if(!item) {
									//Insert
									var otp = Math.floor(Math.random()*(999-100+1)+100).toString()+"-"+Math.floor(Math.random()*(999-100+1)+100).toString();
									var uid = Math.floor(Math.random()*(999999-100000+1)+100000).toString();
									var uidHash = crypto.createHash('sha256').update(uid).digest('base64');

									var query = {
										mobile: mobile,
										smobile: mobile.slice(-10), 
										verified: false, 
										otp: otp, 
										uid: uidHash, 
										nuid: 0, 
										ouid: 0,
										privacyContacts: false,
										block : [],
										joined: moment().utc().format("YYYY-MM-DD")
									};
									usersCollection.insert(query, function(err, result){
										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else {
											//Send the otp and uid to the registered number
											sendOTP.otpToNumber(otp, mobile);
											db.close();
											res.status(200).json({statusCode:'201', message:'created', uid: uid});
										}
									});
								}
								else {
									//Update the otp and the uid
									var otp = Math.floor(Math.random()*(999-100+1)+100).toString()+"-"+Math.floor(Math.random()*(999-100+1)+100).toString();
									var nuid = Math.floor(Math.random()*(999999-100000+1)+100000).toString();
									var nuidHash = crypto.createHash('sha256').update(nuid).digest('base64');

									usersCollection.updateOne({mobile: mobile},{$set:{otp: otp, nuid: nuidHash}}, function(err, result){
										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else {
											//Send the otp and nuid to the new number
											sendOTP.otpToNumber(otp, mobile);
											db.close();
											//console.log(nuid);
											res.status(200).json({statusCode:'201', message:'created', uid: nuid});
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
				res.status(200).json({statusCode:'400', message:'bad request'});
		}
		else 
			res.status(200).json({statusCode:'400', message:'bad request'});
	});

};