// models/image.js
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var imageSchema =new Schema({
  title: String,
  photo: String,
  tag: String,
  email: String
});

module.exports = mongoose.model('image', imageSchema);