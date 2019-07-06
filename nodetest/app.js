// app.js

// [LOAD PACKAGES]
var express     = require('express');
var app         = express();
var bodyParser  = require('body-parser');
var mongoose    = require('mongoose');
// [CONFIGURE APP TO USE bodyParser]
//app.use(bodyParser.urlencoded({ extended: true }));
//app.use(bodyParser.json());
app.use(bodyParser.json({limit: '30mb', extended: true}));
app.use(bodyParser.urlencoded({limit: '30mb', extended: true}));

// [CONFIGURE SERVER PORT]
var port = process.env.PORT || 8080;

var Contact = require('./models/contact');
var Image = require('./models/image');
// [CONFIGURE ROUTER]
var router = require('./routes/index1')(app, Contact);
var router = require('./routes/index2')(app, Image);

// [ CONFIGURE mongoose ]

// CONNECT TO MONGODB SERVER
var db = mongoose.connection;
db.on('error', console.error);
db.once('open', function(){
    // CONNECTED TO MONGODB SERVER
    console.log("Connected to mongod server");
});

mongoose.connect('mongodb://localhost/Second');

// [RUN SERVER]
var server = app.listen(port, function(){
 console.log("Express server has started on port " + port)
});