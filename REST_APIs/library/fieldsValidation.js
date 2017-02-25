/******
Below are the functions for various API related fields validation.
******/

//Name validation
exports.nameValidation = function(name) {

	if(name.length > 0 && name.length <= 64) {
		var regexName = /^[A-Za-z ]+$/;
		return regexName.test(name)? true : false;
	} 
	else 
		return false;
	
};

//Date validation
exports.dateValidation =  function(date) {

	if(date.length >= 6 && date.length <=10) {
		var regexDate = /^(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]))\1|(?:(?:29|30)(\/|-|\.)(?:0?[1,3-9]|1[0-2])\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)0?2\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9])|(?:1[0-2]))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})$/;
		return regexDate.test(date)? true : false;
	}
	else {
		//console.log("Date validation failed.");
		return false;
	}

};

//Firebase device registration token validation - have no idea about proper validation, so did only length validation
exports.firebaseTokenValidation = function(token) {

	if(token.length > 0 && token.length <= 300)
		return true;
	else
		return false;

}

//Mobile number validation
exports.mobileValidation = function(mobile) {

	var regexMobile = /^(\+\d{1,3}?)\d{10}$/;
	if(regexMobile.test(mobile))
		return true;
	else
		return false;

};

//Time validation
exports.timeValidation = function(time) {

	var regexTime = /^[0-9:.]+$/;
	if(time.length > 0 && time.length <= 12) 
		return regexTime.test(time)? true : false;
	else 
		return false;

};

//Mobile number in various formats validation
exports.variousMobileFormatValidation = function(mobile) {

	var regexMobile = /^(\+?\d{1,3}?)?\d{10}$/;
	if(regexMobile.test(mobile))
		return true;
	else
		return false;

};

//List of mobile numbers in various formats
exports.listVariousMobileFormatValidation = function(mobile) {

	var regexMobile = /^((\+?\d{1,3}?)?\d{10})(,(\+?\d{1,3}?)?\d{10})*$/;
	if(regexMobile.test(mobile))
		return true;
	else
		return false;

};


//OTP number validation
exports.otpValidation = function(otp) {

	var regexOTP = /^\d{3}[-]\d{3}$/;
	if(regexOTP.test(otp))
		return true;
	else 
		return false;

};

//UID number validation
exports.uidValidation = function(UID) {

	var regexUID = /^\d{6}$/;
	if(regexUID.test(UID))
		return true;
	else 
		return false;

};

function sameNumberCheck(contact, callback) {
	//Country Specific --- ######### (India, US, UK, Canada)
	if(contact.length == 11 && contact.slice(0, -10) === '0') {
		//Same country, I presume
		callback(mobile.slice(0, -10)+contact.slice(-10));
	}
	else if(contact.length > 10 && contact.length <=13 && contact.indexOf('+') == -1) {
		//User didn't add the '+' symbol in front of country code
		callback('+'+contact);
	}
	else if(contact.length == 10) {
		//Probably contact exists in the same country as the user does
		callback(mobile.slice(0, -10)+contact);
	}
	else 
		callback(contact);
}

/*//Username validation
exports.usernameValidation = function(username) {

	if(username.length > 0 && username.length <= 64) {
		var regexUsername = /^[A-Za-z0-9]+$/;
		return regexUsername.test(username)? true : false;
	} 
	else 
		return false;
	
};

//Password validation
exports.passwordValidation = function(password) {

	if(password.length >= 6 && password.length <= 64) 
		return true;
	else 
		return false;
	
};

//Category validation
exports.categoryValidation = function(category) {
	
	if(category.length > 0 && category.length <= 32) {
		var regexCategory = /^[A-Za-z]+$/;
		return regexCategory.test(category)? true : false;
	}
	else {
		console.log("Category validation failed.");
		return false;
	}

};

//Log validation
exports.logValidation = function(log) {

	if(log.length > 0 && log.length <= 32) {
		var regexLog = /^[A-Za-z ]+$/;
		return regexLog.test(log)? true : false;
	}
	else {
		console.log("Log validation failed.");
		return false;
	}

};

//Meta validation
exports.metaValidation = function(meta) {

	if(meta.length <= 32) {
		var regexMeta = /^[A-Za-z ]+$/;
		return regexMeta.test(meta)? true : false;
	}
	else {
		console.log("Meta validation failed.");
		return false;
	}

};*/

