// app.js

// [LOAD PACKAGES]
var express     = require('express');
var app         = express();
var bodyParser  = require('body-parser');
var fs          = require('fs');
var mongoose    = require('mongoose');
// [CONFIGURE APP TO USE bodyParser]
//app.use(bodyParser.urlencoded({ extended: true }));
//app.use(bodyParser.json());
app.use(bodyParser.json({limit: '30mb', extended: true}));
app.use(bodyParser.urlencoded({limit: '30mb', extended: true}));

var Contact = require('./models/contact');
var Image = require('./models/image');

// [CONFIGURE SERVER PORT]
var port = process.env.PORT || 8080;

// [CONFIGURE ROUTER]
var router = require('./routes/index1')(app, Contact);
var router = require('./routes/index2')(app, Image);

// [ CONFIGURE mongoose ]

var db = mongoose.connection;

// CONNECT TO MONGODB SERVER
db.on('error', console.error);
db.once('open', function(){
    // CONNECTED TO MONGODB SERVER
    console.log("Connected to mongod server");
});

mongoose.connect('mongodb://localhost/Second');

// [RUN SERVER]
var server = app.listen(port, function(){
 console.log("Express server has started on port " + port);
});
