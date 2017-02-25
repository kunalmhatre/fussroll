var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var privacyContacts = require(__dirname+'/../library/db/privacyContacts.js');
var blockContacts = require(__dirname+'/../library/db/blockContacts.js');

module.exports = function(app) {

	app.post('/api/logs', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.contact && req.body.opt) {

			var mobile = req.body.mobile.trim();
			var uid = req.body.uid.trim();
			var contact = req.body.contact.trim();
			var opt = req.body.opt.trim();

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.variousMobileFormatValidation(contact) && fieldsValidation.dateValidation(date)) {

				checkVerification.mobileUIDVerification(mobile, uid, function(val) {
					if(val == 1) {

						//Country Specific --- ######### (India, US, UK, Canada)
						if(contact.length == 11 && contact.slice(0, -10) === '0') {
							//Same country, I presume
							contact = mobile.slice(0, -10)+contact.slice(-10);
						}
						else if(contact.length > 10 && contact.length <=13 && contact.indexOf('+') == -1) {
							//User didn't add the '+' symbol in front of country code
							contact = '+'+contact;
						}
						else if(contact.length == 10) {
							//Probably contact exists in the same country as the user does
							contact = mobile.slice(0, -10)+contact;
						}
						
						MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

							if(err) throw err;
							else {

								var logsCollection = db.collection('logs');

								if(mobile == contact) {

									logsCollection.aggregate([{$match:{mobile: mobile}},{$project:{logs:{$filter:{input:'$logs',as:'logs',cond:{$eq:['$$logs.date',date]}}}, latestUpdate:1, _id:0}}], function(err, item) {
										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else if(Object.keys(item).length != 0 && Object.keys(item[0].logs).length != 0){
											db.close();
											res.status(200).json({statusCode:'200', activities: item});
											console.log("200");
										}
										else {
											db.close();
											//We won't get the latestUpdate date if user didn't update anything yet
											try {
												var latestUpdateDate = item[0].latestUpdate;
											} catch(e) {
												var latestUpdateDate = "false";
											}
											res.status(200).json({statusCode:'204', message:'no content', latestUpdate: latestUpdateDate});
											console.log("204");
										}
									});

								}
								else {

									var usersCollection = db.collection('users');
									usersCollection.findOne({mobile: contact}, function(err, item) {

										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else if(item){

											privacyContacts.privacyContacts(mobile, contact, function(val) {

												if(val == 2) {
													//console.log("Checking for blocked");
													blockContacts.blockContacts(mobile, contact, function(val) {

														if(val == 2) {

															logsCollection.aggregate([{$match:{mobile: contact}},{$project:{logs:{$filter:{input:'$logs',as:'logs',cond:{$eq:['$$logs.date',date]}}}, latestUpdate:1, _id:0}}], function(err, item) {
																if(err) {
																	db.close();
																	res.status(200).json({statusCode:'500', message:'internal server error'});
																}
																else if(Object.keys(item).length != 0 && Object.keys(item[0].logs).length != 0){
																	db.close();
																	res.status(200).json({statusCode:'200', activities: item});
																}
																else {
																	db.close();
																	//We won't get the latestUpdate date if user didn't update anything yet
																	try {
																		var latestUpdateDate = item[0].latestUpdate;
																	} catch(e) {
																		var latestUpdateDate = "false";
																	}
																	res.status(200).json({statusCode:'204', message:'no content', latestUpdate: latestUpdateDate});
																}
															});

														}
														else if(val == 3) {
															db.close();
															res.status(200).json({statusCode:'403', message:'forbidden'});
														}
														else if(val == 1) {
															db.close();
															res.status(200).json({statusCode:'500', message:'internal server error'});
														}

													});

												}
												else if(val == 3) {
													db.close();
													res.status(200).json({statusCode:'403', message:'forbidden'});
												}
												else if(val == 1){
													db.close();
													res.status(200).json({statusCode:'500', message:'internal server error'});
												}

											});

										}
										else {
											res.status(200).json({statusCode:'410', message:'user not found'});
										}

									});

								}

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

}