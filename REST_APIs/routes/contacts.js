var MongoClient = require('mongodb').MongoClient;
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var internationalizeNumber = require(__dirname+'/../library/internationalizeNumber.js');

module.exports = function(app) {
	app.post('/contacts', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.contact && req.body.opt) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var contact = req.body.contact.trim();
				var opt = req.body.opt;
			}
			catch(e){
				var mobile = "0";
				var uid = "0";
				var contact = "0";
				var opt = "0";
			}
			
			
			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.variousMobileFormatValidation(contact) && (opt === "1" || opt === "2")) {

				checkVerification.mobileUIDVerification(mobile, uid, function(val) {

					if(val == 1) {

					//Country Specific --- ######### (India, US, UK, Canada)
					if(contact.length == 11 && contact.slice(0, -10) === '0') {
						//Same country, I presume
						var contactForCheck = mobile.slice(0, -10)+contact.slice(-10);
					}
					else if(contact.length > 10 && contact.length <=13 && contact.indexOf('+') == -1) {
						//User didn't add the '+' symbol in front of country code
						var contactForCheck = '+'+contact;
					}
					else if(contact.length == 10) {
						//Probably contact exists in the same country as the user does
						var contactForCheck = mobile.slice(0, -10)+contact;
					}

					MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

						var contactsCollection = db.collection('contacts');

						if(err) {
							console.log(err);
							res.status(200).json({statusCode:'500', message:'internal server error'});
						}
						else {

							if(opt === "1") {
								if(mobile != contactForCheck && mobile != contact) {
									//Add contacts
									contactsCollection.findOne({mobile: mobile}, function(err, item) {
										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else if(item){
											//Update but, first check if the contact number exists
											contactsCollection.findOne({mobile: mobile, contacts:{$elemMatch:{$eq: contact}}}, function(err, item) {
												if(err) {
													db.close();
													res.status(200).json({statusCode:'500', message:'internal server error'});
												}
												else if(!item){
													contactsCollection.updateOne({mobile: mobile},{$push:{contacts: contact}}, function(err, result) {
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
												else {
													db.close();
													res.status(200).json({statusCode:'403', message:'forbidden'});
												}
											});
										}
										else {
											//Insert
											contactsCollection.insert({mobile: mobile, contacts: [contact]}, function(err, result) {
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
								else {
									db.close();
									res.status(200).json({statusCode:'403', message:'forbidden'});
								}
							} else if(opt === "2") {
								//Remove contacts
								contactsCollection.findOne({mobile: mobile, contacts:{$elemMatch:{$eq: contact}}}, function(err, item) {
									if(err) {
										db.close();
										res.status(200).json({statusCode:'500', message:'internal server error'});
									}
									else if(item){
										contactsCollection.updateOne({mobile: mobile},{$pull:{contacts: contact}}, function(err, result) {
											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else {

												res.status(200).json({statusCode:'200', message:'done'});

												//Remove from notifications collection

												contact = internationalizeNumber.internationalizeNumber(mobile, contact);

												var cloudMessagesCollection = db.collection('cloudMessages');
												cloudMessagesCollection.findOne({mobile: mobile, squad:{$elemMatch:{$eq: contact}}}, function(err, item) {

													if(err) {
														db.close();
														console.log(err);
													}
													else if(item) {
														cloudMessagesCollection.updateOne({mobile: mobile},{$pull:{squad: contact}}, function(err, result) {

															if(err) {
																db.close();
																console.log(err);
															}
															else {
																//console.log("Contact has been removed from mobile's cloudMessages collection, now removing from contact's cloudMessages collection");

																cloudMessagesCollection.findOne({mobile: contact, squad:{$elemMatch:{$eq: mobile}}}, function(err, item) {

																	if(err) {
																		db.close();
																		console.log(err);
																	}
																	else if(item){

																		cloudMessagesCollection.updateOne({mobile: contact},{$pull:{squad: mobile}}, function(err, result) {

																			if(err) {
																				db.close();
																				console.log(err);
																			}
																			else {
																				//console.log("Mobile has been removed from the contact's cloudMessages collection, Firebase token removal is done");
																			}

																		});

																	}
																	else {
																		//console.log("Mobile is not present in contact's cloudMessages collection");
																	}

																});

															}

														});
													}
													else {
														//console.log("Contact is not in mobile's cloudMessages collection, checking if mobile is present in contact's cloudMessages collection");

														cloudMessagesCollection.findOne({mobile: contact, squad:{$elemMatch:{$eq: mobile}}}, function(err, item) {

															if(err) {
																db.close();
																console.log(err);
															}
															else if(item){

																cloudMessagesCollection.updateOne({mobile: contact},{$pull:{squad: mobile}}, function(err, result) {

																	if(err) {
																		db.close();
																		console.log(err);
																	}
																	else {
																		//console.log("Mobile has been removed from the contact's cloudMessages collection, Firebase token removal is done");
																	}

																});

															}
															else {
																//console.log("Mobile is not present in contact's cloudMessages collection");
															}

														});

													}

												});

											}
										});
									}
									else {
										db.close();
										res.status(200).json({statusCode:'403', message:'forbidden'});
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
};