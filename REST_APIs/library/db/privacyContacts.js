var MongoClient = require('mongodb').MongoClient;

exports.privacyContacts = function(mobile, contact, callback) {

	var flag = 0;

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

	//Check if not from the same country
	if(mobile.slice(0, -10) != contact.slice(0,-10))
		flag = 1;

	MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {

		if(err) throw err;
		else {

			var usersCollection = db.collection('users');
			usersCollection.findOne({mobile: contact, privacyContacts: false}, function(err, item) {
				
				if(err) {
					db.close();
					callback(1);
				}
				else if(item){
					//For everyone
					db.close();
					callback(2);
				}
				else {
					//Only contacts
					var contactsCollection = db.collection('contacts');

					if(flag == 1) {
						//Not from same country so just make two versions of mobile because that will be the format used by other user (i.e, contact)
						var mobile_1 = mobile.slice(1, mobile.length); //19867740461

						contactsCollection.findOne({mobile: contact, contacts:{$elemMatch:{$in:[mobile, mobile_1]}}}, function(err, item) {

							if(err) {
								db.close();
								callback(1);
							}
							else if(item){
								db.close();
								callback(2);
							}
							else {
								db.close();
								callback(3);
							}

						});
					}
					else {
						//From same country so following possibilities might exists at user's end (i.e, contact)
						var mobile_1 = mobile.slice(-10); //9867740461
						var mobile_2 = mobile.slice(1, mobile.length); //919867740461
						var mobile_3 = '0'+mobile.slice(-10); //09867740461
						//console.log("Same country!"+mobile+" "+mobile_1+" "+mobile_2+" "+mobile_3);
						contactsCollection.findOne({mobile: contact, contacts:{$elemMatch:{$in:[mobile, mobile_1, mobile_2, mobile_3]}}}, function(err, item) {

							if(err) {
								db.close();
								callback(1);
							}
							else if(item){
								db.close();
								callback(2);
							}
							else {
								db.close();
								callback(3);
							}

						});
					}
				}

			});

		}

	});
}