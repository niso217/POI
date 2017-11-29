var express = require('express');
var bodyParser = require('body-parser');
var cors = require('cors');
var helmet = require('helmet');
var app = express();
var admin = require("firebase-admin");
var serviceAccount = require("./firebase.json");
var PORT = process.env.PORT || 3000;
app.use(bodyParser.json());
app.use(cors());
app.use(bodyParser.urlencoded({
 extended: false
}));
app.use(helmet());
admin.initializeApp({
 credential: admin.credential.cert(serviceAccount),
 databaseURL: "https://pointerest-ce04f.firebaseio.com"
});
app.put('/noti/sendToToken',function(req,res){
let title = req.body.title;
 let body = req.body.body;
 let type = req.body.type;
 let token = req.body.token;
let payload = {
 data:{
 title : title,
 body: body,
 type: type
 }
 };
admin.messaging().sendToDevice(token, payload)
 .then(function(response){
 res.json(response);
 })
 .catch(function(error){
 res.json(error);
 });
});
app.put('/noti/sendToTopic', function(req,res){
let title = req.body.title;
 let body = req.body.body;
 let type =req.body.type;
 let topic = req.body.topic;
let payload = {
 data:{
 title : title,
 body: body,
 type: type
 }
 };
admin.messaging().sendToTopic(topic, payload)
 .then(function(response){
 res.json(response);
 })
 .catch(function(error){
 res.json(error);
 });
});
app.listen(PORT,function(){
 console.log("Server running on port "+PORT);
})
