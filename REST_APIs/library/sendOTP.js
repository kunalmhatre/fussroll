var request = require('request');
var authKey = "[YOUR_AUTHKEY]";
var senderID = "FSROLL";

exports.otpToNumber = function(otp, mobile) {
	
	otp = otp+ " is your verification code for Fussroll.";
	var query = "http://api.msg91.com/api/sendhttp.php?authkey="+authKey+"&mobiles="+mobile+"&message="+otp+"&sender="+senderID+"&route=4"
	request(query, function(err, res, body) {
		//console.log(body);
	});

}