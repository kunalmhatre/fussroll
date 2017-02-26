var FCM = require('fcm-node');
var serverKey = '[YOUR_SERVERKEY]';
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