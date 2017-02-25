var MongoClient = require('mongodb').MongoClient;
var checkVerification = require(__dirname+'/../library/db/checkVerification.js');
var fieldsValidation = require(__dirname+'/../library/fieldsValidation.js');
var internationalizeNumber = require(__dirname+'/../library/internationalizeNumber.js');

module.exports = function(app) {

	app.post('/block', function(req, res) {

		if(req.body.mobile && req.body.uid && req.body.contact && req.body.block) {

			try {
				var mobile = req.body.mobile.trim();
				var uid = req.body.uid.trim();
				var contact = req.body.contact.trim();
				var block = req.body.block.trim();
			}
			catch(e) {
				var mobile = "0";
				var uid = "0";
				var contact = "0";
				var block = "0";
			}

			

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

			if(fieldsValidation.mobileValidation(mobile) && fieldsValidation.uidValidation(uid) && fieldsValidation.variousMobileFormatValidation(contact) && (block === 'y' || block === 'n' || block === 'oc' || block === 'nc')) {

				checkVerification.mobileUIDVerification(mobile, uid, function(val) {

					MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

						if(err) {
							console.log(err);
							res.status(200).json({statusCode:'500', message:'internal server error'});
						}
						else {
							
							var usersCollection = db.collection('users');

							if(val == 1) {

								if(block === 'y') {
									//Blocking process
									if(mobile != contactForCheck && mobile != contact) {
										usersCollection.findOne({mobile: mobile, block:{$elemMatch:{$eq: contact}}}, function(err, result) {
											if(err) {
												db.close();
												res.status(200).json({statusCode:'500', message:'internal server error'});
											}
											else if(result) {
												db.close();
												res.status(200).json({statusCode:'403', message:'forbidden'});
											}	
											else {
												usersCollection.updateOne({mobile: mobile},{$push:{block: contact}}, function(err, result) {

													if(err) {
														db.close();
														res.status(200).json({statusCode:'500', message:'internal server error'});
													}
													else {

														contact = internationalizeNumber.internationalizeNumber(mobile, contact);

														var blockedByCollection = db.collection('blockedBy');
														blockedByCollection.findOne({mobile: contact}, function(err, item) {

															if(err) {
																db.close();
																res.status(200).json({statusCode:'500', message:'internal server error'});
															}
															else if(item) {
																blockedByCollection.findOne({mobile: contact, people:{$elemMatch:{$eq: mobile}}}, function(err, item) {
																	
																	if(err) {
																		db.close();
																		res.status(200).json({statusCode:'500', message:'internal server error'});
																	}
																	else if(!item) {

																		blockedByCollection.updateOne({mobile: contact},{$push:{people: mobile}}, function(err, result) {

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
																		res.status(200).json({statusCode:'200', message:'done'});
																	}

																});
															}
															else {

																blockedByCollection.insert({mobile: contact, people:[mobile]}, function(err, result) {

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
									else {
										db.close();
										res.status(200).json({statusCode:'403', message:'forbidden'});
									}

								}
								else if(block === 'n') {
									//Unblocking process
									usersCollection.findOne({mobile: mobile, block:{$elemMatch:{$eq: contact}}}, function(err, result) {
										if(err) {
											db.close();
											res.status(200).json({statusCode:'500', message:'internal server error'});
										}
										else if(result) {
											usersCollection.updateOne({mobile: mobile},{$pull:{block: contact}}, function(err, result) {

												if(err) {
													db.close();
													res.status(200).json({statusCode:'500', message:'internal server error'});
												}
												else {
													
													contact = internationalizeNumber.internationalizeNumber(mobile, contact);

													var blockedByCollection = db.collection('blockedBy');
													blockedByCollection.findOne({mobile: contact, people:{$elemMatch:{$eq:mobile}}}, function(err, item) {

														if(err) {
															db.close();
															res.status(200).json({statusCode:'500', message:'internal server error'});
														}
														else if(item) {

															blockedByCollection.updateOne({mobile: contact},{$pull:{people: mobile}}, function(err, result) {

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
									});
								}
								else if(block === 'oc') {
									//Make privacyContacts : true
									usersCollection.updateOne({mobile: mobile},{$set:{privacyContacts : true}}, function(err, result) {

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
								else if(block === 'nc'){
									//Make privacyContacts : false
									usersCollection.updateOne({mobile: mobile},{$set:{privacyContacts : false}}, function(err, result) {

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

							}
							else if(val == 2)
								res.status(200).json({statusCode:'401', message:'unauthorized'});
							else if(val == 3)
								res.status(200).json({statusCode:'404', message:'not found'});
							else if(val == 4)
								res.status(200).json({statusCode:'500', message:'internal server error'});
						}

					});
					
				});

			}
			else 
				res.status(200).json({statusCode:'400', message:'bad request'});
		}
		else
			res.status(200).json({statusCode:'400', message:'bad request'});
	});
};