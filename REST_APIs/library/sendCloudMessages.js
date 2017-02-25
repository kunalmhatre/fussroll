var FCM = require('fcm-node');
var serverKey = 'AAAA_pSczh8:APA91bEbalTNhyc2nEPi25O7q8zr2NrkuyrCVoPfkG-UQqQt53bSUa2FYz5ysvOZgmS9bn1M0UJRK3rhHrzLsQcY8YPs0-Af4t9ds62szZl0ovTtfgKkz23uD-_Zk79WBNiXMoOhmnaOWU7PGS8Cq_MKTfnahPJP9Q';
var fcm = new FCM(serverKey);

exports.sendCloudMessages = function(fbtokens, mobile, category, meta, log, date, time) {

if(category === "activity")
	meta = "RESERVED";

var message = {

	registration_ids: fbtokens,
	data: {
		mobile: mobile,
		category: category,
		meta : meta,
		log : log,
		date : date,
		time : time
	}

};

console.log(message);

fcm.send(message, function(err, response) {

	if(err)
		console.log(err);
	else
		console.log("Cloud messages are sent successfully");

});

};