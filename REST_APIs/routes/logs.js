var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var privacyContacts = require(__dirname+'/../library/db/privacyContacts.js');
var blockContacts = require(__dirname+'/../library/db/blockContacts.js');
var internationalizeNumber = require(__dirname+'/../library/internationalizeNumber.js');

module.exports = function(app) {

	app.post('/logs', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.contact && req.body.opt) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var contact = req.body.contact.trim();
				var opt = req.body.opt.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
				var contact = "0";
				var opt = "0";
			}

			//For optional parameters
			try {
				var utcTime = req.body.utcTime.trim();
				var utcDate = req.body.utcDate.trim();
			}
			catch(e) {
				var utcTime = "";
				var utcDate = "";
			}

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.variousMobileFormatValidation(contact) && (opt === "1" || opt === "2" || opt === "3" || opt === "4")) {

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

							if(err) {
								console.log(err);
								res.status(200).json({statusCode:'500', message:'internal server error'});
							}
							else {

								var logsCollection = db.collection('logs');

								if(mobile == contact && opt === "2") {

									logsCollection.findOne({mobile: contact},{mobile: 0, _id: 0}, function(err, item) {

										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else if(item){
											db.close();
											res.status(200).json({statusCode:'200', activities: item});
										}
										else {
											db.close();
											res.status(200).json({statusCode:'204', message:'no content'});
										}

									});

								}
								else if(mobile != contact && (opt === "1" || opt === "3")) {

									if(opt === "3" && !fieldsValidation.timeValidation(utcTime) && !fieldsValidation.dateValidation(utcDate)) 
										res.status(200).json({statusCode:'400', message:'bad request'});
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

																blockContacts.blockContacts(internationalizeNumber.internationalizeNumber(mobile, contact), mobile, function(val) {

																	if(val == 2) {

																		logsCollection.findOne({mobile: contact},{logs: 0}, function(err, item) {
																			if(err) {
																				db.close();
																				res.status(200).json({statusCode:'500', message:'internal server error'});
																			}
																			else if(item) {
																				//There are logs of some date - get the date and query the logs from the database for that particular date 
																				//Code snippet to check if there are logs present in the logs array - Object.keys(item).length != 0 && Object.keys(item[0].logs).length != 0
																				var date = item.latestUpdate;

																				if(opt === "1") {

																					//When only latest logs are needed - irrespective of the utcDate parameter

																					logsCollection.aggregate([{$match:{mobile: contact}},{$project:{logs:{$filter:{input:'$logs',as:'logs',cond:{$eq:['$$logs.date', date]}}}, latestUpdate: 1, _id: 0}}], function(err, item) {
																						if(err) {
																							db.close();
																							res.status(200).json({statusCode:'500', message:'internal server error'});
																						}
																						else {
																							db.close();
																							res.status(200).json({statusCode:'200', activities: item});
																						}
																					});

																				}
																				else if(opt === "3") {

																					//When new logs of the latest date is needed - if no new logs are found of the latest date the 304 is given, 
																					//which means user did not update anything yet (for e.g, after evening) - if user latest update date is of the
																					//next day (or more than that) 301 will be given, which means call should be made to opt = 1 for getting updates
																					//of the latest date

																					if(date === utcDate) {

																						logsCollection.aggregate([{$match:{mobile: contact, latestUpdate: utcDate}},{$project:{logs:{$filter:{input:'$logs',as:'logs',cond:{$and:[{$gt:['$$logs.time', utcTime]}, {$eq:['$$logs.date', date]}]}}}, latestUpdate: 1, _id: 0}}], function(err, item) {
																							if(err) {
																								db.close();
																								res.status(200).json({statusCode:'500', message:'internal server error'});
																							}
																							else if(item) {
																								//console.log(item);
																								if(Object.keys(item).length != 0 && Object.keys(item[0].logs).length != 0) {
																									db.close();
																									res.status(200).json({statusCode:'200', activities: item});
																								}
																								else {
																									db.close();
																									res.status(200).json({statusCode:'304', message:'not modified'});
																								}
																							}
																						});

																					}
																					else {
																						db.close();														
																						res.status(200).json({statusCode:'301', message:'check for new updates'});
																					}

																				}
																			}
																			else {
																				//Nothing updated yet
																				db.close();														
																				res.status(200).json({statusCode:'204', message:'no content'});
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
								else 
									res.status(200).json({statusCode:'400', message:'bad request'});
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