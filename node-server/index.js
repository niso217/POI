var express = require('express');
var bodyParser = require('body-parser');
var cors = require('cors');
var helmet = require('helmet');
var app = express();
var admin = require("firebase-admin");
var geofire = require("geofire");
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

// Generate a random Firebase location
var eventsRef = admin.database().ref().child('events');
var usersRef = admin.database().ref().child('users');

// Create a new GeoFire instance at the random Firebase location
var geoFire = new geofire(usersRef);

app.put('/noti/sendToToken',function(req,res){

let title = req.body.title;
 let body = req.body.body;
 let lat = parseFloat(req.body.lat);
 let lon = parseFloat(req.body.lon);
 let id_token = req.body.id_token;

let payload = {
 data:{
 title : title,
 body: body,
 }

 };

 admin.auth().verifyIdToken(id_token)
   .then(function(decodedToken) {
     var uid = decodedToken.uid;

var radius = 10;

  geoQuery = geoFire.query({
    center: [lat, lon],
    radius: radius
  });

  geoQuery.on("key_entered", function(key, location, distance) {
    console.log(key + " is located at [" + location + "] which is within the query (" + distance.toFixed(2) + " km from center)");


    usersRef.orderByKey().equalTo(key).once("child_added", function(snapshot) {
          var token = snapshot.val().notify_token;
          console.log("token  "+token);
          console.log("payload  "+payload);



          admin.messaging().sendToDevice(token, payload)
           .then(function(response){
           return res.json(response);
           console.log("response  "+response);
           })
           .catch(function(error){
           res.json(error);
           console.log("error  "+error);
           });





});


  });

  geoQuery.on("key_exited", function(key, location, distance) {
    console.log(key, location, distance);
    console.log(key + " is located at [" + location + "] which is no longer within the query (" + distance.toFixed(2) + " km from center)");
  });


console.log(" the query: centered at [" + lat + "," + lon + "] with radius of " + radius + "km")

   }).catch(function(error) {
     console.log("Error  "+error);

   });



});

app.put('/noti/sendToTopic', function(req,res){
let title = req.body.title;
 let body = req.body.body;
 let type =req.body.type;
 let token = req.body.token;
let payload = {
 data:{
 title : title,
 body: body,
 type: type
 }
 };
admin.messaging().sendToToken(token, payload)
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
