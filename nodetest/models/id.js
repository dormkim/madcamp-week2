// models/id.js
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var idSchema = new Schema({
  id : String
});

module.exports = mongoose.model('logininfo', idSchema);
