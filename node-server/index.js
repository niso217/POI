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
 let interest = req.body.interest;
 let event_id = req.body.event_id;
 let mode = req.body.mode;


let payload = {
 data:{
 title : title,
 body: body,
 }

 };


 admin.auth().verifyIdToken(id_token)
   .then(function(decodedToken) {
     var uid = decodedToken.uid;

      var radius = 60;
        geoQuery = geoFire.query({
          center: [lat, lon],
          radius: radius
        });

  geoQuery.on("key_entered",

  function(key, location, distance) {
    console.log(key + " is located at [" + location + "] which is within the query (" + distance.toFixed(2) + " km from center)");
    usersRef.orderByKey().equalTo(key).once("child_added",
    function(snapshot) {
          var token = snapshot.val().notify_token;
          var notify_radius = snapshot.val().notify_radius;
          var user_lat = snapshot.val().l[0];
          var user_lon = snapshot.val().l[1];

          console.log("notify_radius" + notify_radius);

            var notificationArr = snapshot.val().notification;
            for (var key in notificationArr) {
              if (key==interest && notificationArr[key]==true && getDistanceFromLatLonInKm(lat,lon,user_lat,user_lon)<=notify_radius){
                console.log("notify");
                  {
                    admin.messaging().sendToDevice(token, payload)
                     .then(function(response){
                       res.json(response);
                     console.log("response  "+response);
                     })
                     .catch(function(error){
                       res.json(error);
                     console.log("error  "+error);
                     });
                  }
              }
            };

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

function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
  var R = 6371; // Radius of the earth in km
  var dLat = deg2rad(lat2-lat1);  // deg2rad below
  var dLon = deg2rad(lon2-lon1);
  var a =
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
    Math.sin(dLon/2) * Math.sin(dLon/2)
    ;
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  var d = R * c; // Distance in km
  return d;
}

function deg2rad(deg) {
  return deg * (Math.PI/180)
}

app.listen(PORT,function(){
 console.log("Server running on port "+PORT);
})
