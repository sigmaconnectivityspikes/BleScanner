#command to send sample fcm information about infection
curl -X POST --header "Authorization: key=AAAAuyaOjSg:APA91bHPVcFp7Wh2O_B4JMujo5aeNEoBjXaSWbkwag6Qf8Qgo-WHFYPwXjECiPmswgVoGR-CpsZPRZo7N_tBWBvj5UBevqhw94f4lNBkQg9a-5gam_kjhztv9XC0STNQ5x-B-mys3n0z"     --Header "Content-Type: application/json"     https://fcm.googleapis.com/fcm/send     -d '{"to":"/topics/virus",
"notification":{
"title":"New infection",
      "body":"New infection detected",
  },
  "data": {
    "hash" : "0222201"
   }
}'
