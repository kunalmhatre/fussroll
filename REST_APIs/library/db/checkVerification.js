var MongoClient = require('mongodb').MongoClient;
var crypto = require('crypto');

exports.mobileUIDVerification = function(mobile, uid, callback) {

	uidHash = crypto.createHash('sha256').update(uid).digest('base64');

	MongoClient.connect('mongodb://localhost:27017/fussroll', function(err, db) {
		if(err) {
			console.log(err);
			callback(4);
		}
		else {
			var usersCollection = db.collection('users');
			usersCollection.findOne({mobile: mobile, uid: uidHash, verified: true}, function(err, item) {

				if(err) throw err;
				else if(item) {
					db.close();
					callback(1);
				}
				else {
					usersCollection.findOne({mobile: mobile, ouid: uidHash, verified: true}, function(err, item) {
						if(err) throw err;
						else if(item) {
							db.close();
							callback(2);
						}
						else {
							db.close();
							callback(3);
						}
					});
				}

			});
		}
	});

};