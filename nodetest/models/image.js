// models/image.js
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var imageSchema =new Schema({
  title: String,
  picture: String,
  tag: String
});

module.exports = mongoose.model('image', imageSchema);