var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var logsValidation = require(__dirname+'/../library/logsValidation.js');
var moment = require('moment');
var internationalizeNumber = require(__dirname+'/../library/internationalizeNumber.js');
var sendCloudMessages = require(__dirname+'/../library/sendCloudMessages.js');

module.exports = function(app) {

	app.post('/updateLog', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.category && req.body.log) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var category = req.body.category.trim();
				var log = req.body.log.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
				var category = "0";
				var log = "0";
			}

			//Reason for using try-catch block below: req.body.meta is optional and triming it if parameter is not provided in request will crash the functioning.
			try {
				//Since the category named activity does not require meta parameter as of now.
				if(category === "activity")
					var meta = '';
				else
					var meta = req.body.meta.trim();
			}
			catch(e) {
				var meta = '';
			}

			if(logsValidation.checkLogs(category, log, meta) && fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid)) {
				
				checkVerification.mobileUIDVerification(mobile, uid, function(val) {
					if(val == 1) {

						MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {
							if(err) {
								console.log(err);
								res.status(200).json({statusCode:'500', message:'internal server error'});
							}
							else {

								var logsCollection = db.collection('logs');

								logsCollection.findOne({mobile: mobile}, function(err, item) {
									if(err) {
										db.close();
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else if(item) {
										//Update operation with some more condition
										var date = moment().utc().format("YYYY-MM-DD");
										var time = moment().utc().format("HH:mm:ss.SSS");

										logsCollection.updateOne({mobile: mobile},{$set:{latestUpdate: date}, $push:{logs:{$each:[{date:date, time: time, category: category, log: log, meta: meta}],$sort:{date:-1,time:-1}}}}, function(err, result){
											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else {
												
												res.status(200).json({statusCode:'201', message:'created', date: date, time: time});

												//Sending notifications

												var cloudMessagesCollection = db.collection('cloudMessages');
												cloudMessagesCollection.findOne({mobile: mobile}, function(err, item) {

													if(err) {
														db.close();
														console.log(err);
													}
													else if(item) {

														var squadContacts = item.squad;

														if(squadContacts.length > 0) {
															
															var usersCollection = db.collection('users');
															usersCollection.findOne({mobile: mobile}, function(err, item) {

																if(err) {
																	db.close();
																	console.log(err);
																}
																else if(item) {
																	
																	var blockedContactsOG = item.block;
																	var blockedContacts = [];
																	var finalContacts = [];

																	//Converting all blocked contacts to internationalized format
																	for(var k = 0; k < blockedContactsOG.length; k++) {
																		blockedContacts.push(internationalizeNumber.internationalizeNumber(mobile, blockedContactsOG[k]));
																	}

																	if(blockedContacts.length > 0) {

																		for(var i = 0; i < squadContacts.length; i++) {

																			for(var j = 0; j < blockedContacts.length; j++) {

																				if(squadContacts[i] != blockedContacts[j]) {

																					if(finalContacts.indexOf(squadContacts[i]) == -1 && blockedContacts.indexOf(squadContacts[i]) == -1)
																						finalContacts.push(squadContacts[i]);

																				}

																			}

																		}

																	}
																	else if(blockedContacts.length == 0) {

																		for(var i = 0; i < squadContacts.length; i++) {
																			console.log("finalContacts push "+squadContacts[i]);
																			finalContacts.push(squadContacts[i]);

																		}	

																	}

																	if(finalContacts.length > 0) {
																		
																		var finalList = [];
																		var blockedByCollection = db.collection('blockedBy');
																		blockedByCollection.findOne({mobile: mobile}, function(err, item) {

																			if(err) {
																				db.close();
																				console.log(err);
																			}
																			else if(item) {

																				console.log("If item");

																				var blockedByContacts = item.people;

																				if(blockedByContacts.length > 0) {

																					console.log("Greater than 0");

																					for(var i = 0; i < finalContacts.length; i++) {

																						for(var j = 0; j < blockedByContacts.length; j++) {

																							if(finalContacts[i] != blockedByContacts[j]) {

																								if(finalList.indexOf(finalContacts[i]) == -1 && blockedByContacts.indexOf(finalContacts[i]) == -1)
																									finalList.push(finalContacts[i]);

																							}

																						}

																					}

																				}
																				else if(blockedByContacts.length == 0){

																					console.log("Greater == 0");

																					for(var i = 0; i < finalContacts.length; i++) {

																						finalList.push(finalContacts[i]);

																					}

																				}

																			}
																			else {
																				finalList = finalContacts;
																			}

																			console.log("finalist "+finalList);

																			if(finalList.length > 0) {

																				var fbtokens = [];

																				function getFBTokens(i) {

																					if(i < finalList.length) {

																						usersCollection.findOne({mobile: finalList[i]}, function(err, item) {

																							if(err) {
																								db.close();
																								console.log(err);
																							}
																							else if(item) {
																								if(item.fbtoken) {
																									console.log("pushing "+item.fbtoken)
																									fbtokens.push(item.fbtoken);
																								}
																							}
																							getFBTokens(i+1);

																						});

																					}
																					else {
																						if(fbtokens.length > 0) {
																							console.log(fbtokens);
																							console.log(fbtokens.length);
																							sendCloudMessages.sendCloudMessages(fbtokens, mobile, category, meta, log, date, time);
																						}
																						else {
																							console.log("No fbtokens were added in the ultra final list");
																						}
																					}

																				}
																				getFBTokens(0);

																			}
																			else {
																				console.log("No one is present in the final list");
																			}

																		});

																	}
																	else {
																		console.log("All contacts are blocked");
																	}

																} 

															});

														}
														else {
															console.log("There are no contacts to send notifications to");
														}

													}
													else {
														console.log("Mobile does not exists in notifications collection");
													}

												});

											}
										});
									}
									else {
										//Insert operation
										var date = moment().utc().format("YYYY-MM-DD");
										var time = moment().utc().format("HH:mm:ss.SSS");

										logsCollection.insert({mobile: mobile, latestUpdate: date, logs:[{date: date, time: time, category: category, log: log, meta: meta}]}, function(err, result){
											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else {
												db.close();
												res.status(200).json({statusCode:'201', message:'created', date: date, time: time});
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
			else 
				res.status(200).json({statusCode:'400', message:'bad request'});

		}
		else
			res.status(200).json({statusCode:'400', message:'bad request'});

	});

}