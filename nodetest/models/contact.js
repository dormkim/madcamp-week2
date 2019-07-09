// models/contact.js
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var contactSchema =new Schema({
  name: String,
  phonenumber: String,
  tag: String,
  icon: String,
  contact_id: String,
  email: String
});

module.exports = mongoose.model('contact', contactSchema);