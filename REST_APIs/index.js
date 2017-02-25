//Get express
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var moment = require('moment');
var morgan = require('morgan');

//For checking all incoming requests
app.use(morgan('dev'));

//For security purpose
app.disable('x-powered-by');

//Setting the port present in environment variable PORT or 1337 if there is nothing
app.set('port', process.env.PORT || 1337);

//A new body object containing the parsed data is populated on the request object 
//after the middleware (i.e. req.body). 
//This object will contain key-value pairs, 
//where the value can be a string or array (when extended is false), 
//or any type (when extended is true)
app.use(bodyParser.urlencoded({extended:false}));

//Deploying routes
var registerAPI = require('./routes/register.js')(app);
var updateLogAPI = require('./routes/updateLog.js')(app);
var logsAPI = require('./routes/logs.js')(app);
var verifyOTPAPI = require('./routes/verifyOTP.js')(app);
var contactsAPI = require('./routes/contacts.js')(app);
var isUserAPI = require('./routes/isUser.js')(app);
var blockAPI = require('./routes/block.js')(app);
var logsAPI = require('./routes/logs.js')(app);
var deleteAPI = require('./routes/delete.js')(app);
var setFirebaseTokenAPI = require('./routes/setFirebaseToken.js')(app);
/*var follow = require('./routes/follow.js')(app);
var unfollow = require('./routes/unfollow.js')(app);
var followers = require('./routes/followers.js')(app);
var following = require('./routes/following.js')(app);*/

app.listen(app.get('port'), function() {
	console.log("Server is started : 127.0.0.1:"+app.get('port'));
});
