var MongoClient = require('mongodb').MongoClient;
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var existsInContactList = require(__dirname+'/../library/db/existsInContactList.js');

module.exports = function(app) {

	app.post('/isUser', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.contact) {

			try{
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var contact = req.body.contact.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
				var contact = "0";
			}

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.variousMobileFormatValidation(contact)) {

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

						//console.log(contact);

						MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

							if(err) {
								console.log(err);
								res.status(200).json({statusCode:'500', message:'internal server error'});
							}
							else {
								var usersCollection = db.collection('users');
								usersCollection.findOne({$and:[{mobile: contact}, {verified: true}]}, function(err, item) {

									if(err) {
										db.close();
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else if(item) {

										res.status(200).json({statusCode:'200', message:'found'});

										//######################################################
										//######################################################

										//Firebase token exchange

										existsInContactList.existsInContactList(mobile, contact, function(val) {

											if(val == 1) {
												db.close();
												console.log(err);
											}
											else if(val == 2) {

												//console.log("Mobile is present in contact's contacts collection now checking vice versa");
												
												existsInContactList.existsInContactList(contact, mobile, function(val) {

													if(val == 1) {

													}
													else if(val == 2) {

														//console.log("Contact is present in mobile's contacts collection and now going for further process");

														//Checking if contact is present in mobile's notifications collection
														//but first checking if mobile has it's object present in notifications collection

														var cloudMessagesCollection = db.collection('cloudMessages');
														cloudMessagesCollection.findOne({mobile: mobile}, function(err, item) {

															if(err) {
																db.close();
																console.log(err);
															}
															else if(item) {

																//Update, but first check if contact exists in the list

																cloudMessagesCollection.findOne({mobile: mobile, squad:{$elemMatch:{$eq: contact}}}, function(err, item) {

																	if(err) {
																		db.close();
																		console.log(err);
																	}
																	else if(!item) {

																		//Add

																		cloudMessagesCollection.updateOne({mobile: mobile},{$push:{squad: contact}}, function(err, result) {

																			if(err) {
																				db.close();
																				console.log(err);
																			}
																			else {
																				//console.log("Contact added to receive notifications, now reversing the process for contact");

																				//##############################################
																				//##############################################

																				cloudMessagesCollection.findOne({mobile: contact}, function(err, item) {

																					if(err) {
																						db.close();
																						console.log(err);
																					}
																					else if(item) {

																						//Update, but first check if contact exists in the list

																						cloudMessagesCollection.findOne({mobile: contact, squad:{$elemMatch:{$eq: mobile}}}, function(err, item) {

																							if(err) {
																								db.close();
																								console.log(err);
																							}
																							else if(!item) {

																								//Add

																								cloudMessagesCollection.updateOne({mobile: contact},{$push:{squad: mobile}}, function(err, result) {

																									if(err) {
																										db.close();
																										console.log(err);
																									}
																									else {
																										db.close();
																										//console.log("Mobile added to receive notifications, Firebase token exchange is done");
																									}

																								});

																							}
																							else {
																								//console.log("Contact is already present in the list");
																							}

																						});


																					}
																					else {

																						//Insert - since this is first time created we do not have to check if contact is present in the list

																						cloudMessagesCollection.insert({mobile: contact, squad:[mobile]}, function(err, result) {

																							if(err) {
																								db.close();
																								console.log(err);
																							}
																							else {
																								db.close();
																								//console.log("Mobile added to receive notifications, Firebase token exchange is done");
																							}

																						});

																					}

																				});

																				//##############################################
																				//##############################################

																			}

																		});

																	}
																	else {
																		//console.log("Contact is already present in the list");
																	}

																});


															}
															else {

																//Insert - since this is first time created we do not have to check if contact is present in the list

																cloudMessagesCollection.insert({mobile: mobile, squad:[contact]}, function(err, result) {

																	if(err) {
																		db.close();
																		console.log(err);
																	}
																	else {
																		//console.log("Contact added to receive notifications, now reversing the process for contact");

																		//##############################################
																		//##############################################

																		cloudMessagesCollection.findOne({mobile: contact}, function(err, item) {

																			if(err) {
																				db.close();
																				console.log(err);
																			}
																			else if(item) {

																				//Update, but first check if contact exists in the list

																				cloudMessagesCollection.findOne({mobile: contact, squad:{$elemMatch:{$eq: mobile}}}, function(err, item) {

																					if(err) {
																						db.close();
																						console.log(err);
																					}
																					else if(!item) {

																						//Add

																						cloudMessagesCollection.updateOne({mobile: contact},{$push:{squad: mobile}}, function(err, result) {

																							if(err) {
																								db.close();
																								console.log(err);
																							}
																							else {
																								db.close();
																								//console.log("Mobile added to receive notifications, Firebase token exchange is done");
																							}

																						});

																					}
																					else {
																						//console.log("Contact is already present in the list");
																					}

																				});


																			}
																			else {

																				//Insert - since this is first time created we do not have to check if contact is present in the list

																				cloudMessagesCollection.insert({mobile: contact, squad:[mobile]}, function(err, result) {

																					if(err) {
																						db.close();
																						console.log(err);
																					}
																					else {
																						db.close();
																						//console.log("Mobile added to receive notifications, Firebase token exchange is done");
																					}

																				});

																			}

																		});

																		//##############################################
																		//##############################################

																	}

																});

															}

														});

													}
													else if(val == 3) {
														//console.log("Contact is not present in mobile's contacts collection");
													}

												});
												

											}
											else if(val == 3) {
												//console.log("Mobile is not present in contact's contacts collection");
											}

										});

										//######################################################
										//######################################################

									}
									else {
										db.close();
										res.status(200).json({statusCode:'404', message:'user not found'});
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