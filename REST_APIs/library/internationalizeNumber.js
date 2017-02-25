exports.internationalizeNumber = function(mobile, contact) {

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

	return contact;

}