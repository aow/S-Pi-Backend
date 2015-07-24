# S-Pi-Backend
Backend component for the S-Store MIMIC ICU Monitoring Demo

# Requirements
- Java 8
- Maven (3.0+)
- Nginx (optional)

# Building and running

1. Clone the repository
2. Run `mvn package` in the main directory (the one with pom.xml)
3. Look for `target/s-pi-x.x.x.jar`, where the xs are whatever version we're at.
4. Run that jar with `java -jar ...`
5. Profit

## Config

We support a config file named `settings.json`, which should be located in the same directory you run the jar file from. It has the following parameters:

```
{
    "vertxHost": "localhost",
    "vertxPort": 9999,
    "sstore": false,
    "sstoreClientHost": "localhost",
    "sstoreClientPort": 6000,
    "bigDawg": false,
    "bigDawgUrl": "http://noidea.com/"
}
```

The default values are those listed, and if no config file is present and overriding them, those are the values that will be used. If you want to change a value, you only have to put that value in the settings file. Not all parameters need to be present, only those you want to change.

### Parameters
`vertxHost`: String containing the hostname vertx will listen on.

`vertxPort`: Port vertx will listen on.

`sstore`: Whether we are using s-store backend for streaming data.

`sstoreClientHost`: Hostname the SimpleMimicClient is listening on.

`sstoreClientPort`: Port the SimpleMimicClient is listening on.

`bigDawg`: Whether to use BigDawg for alerts.

`bigDawgUrl`: The BigDawg URL we submit queries to.

# Integrating with the frontend

## Deploying with the frontend

1. Follow the instruction above for building, so you have the jar file.
2. Clone the S-Pi-Web repo into a directory.
3. Have Nginx installed.
4. Create 2 nginx virtual servers with the following configs. The API config is optional and only needed if you want to run vertx locally and not query off the main API we are hosting.

```
# API config
server {
        listen 80;
        # The api doesn't need the root, as it doesn't serve any html.
        root /var/www/html/api;
        # You'll want to change this to some local domain.
        server_name api.s-pi-demo.com;

        location / {
                proxy_set_header        X-Real-IP       $remote_addr;
                proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_http_version 1.1;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection "upgrade";
                proxy_set_header Host $host;
                # Change this if you run vertx on a different port/host.
                proxy_pass http://localhost:9999/;
        }
}

```

```
# HTML config
server {
        listen 80;
        # Make this the path to where you cloned S-Pi-Web
        root /var/www/html/S-Pi-Web;
        index overview.html;
        # Change this to something local.
        server_name s-pi-demo.com;

        location / {
                try_files $uri $uri/ =404;
        }
}

```

5. If you are running vert.x locally, you'll need to edit the frontend's code in overview.html and patient.html. Look for the call to `var eb = new vertx.EventBus("http://api.s-pi-demo.com/streambus")` and replace the `api.s-pi-demo.com` portion to the `server_name` you set in the nginx config.
6. Hopefully everything should work now.

## Developing for the frontend

You need to include https://github.com/vert-x3/vertx-web/blob/3.0.0/src/client/vertxbus.js library.

After that, you connect with the eventbus by doing `var eb = new vertx.EventBus("http://.../streambus")`, where the ... is whatever hostname the vertx server is listening on. You only need one EventBus connection.

Once that is set up, whenever you need to start streaming data, simply issue an AJAX request to the previous endpoints. They return the channel you have to listen on to get the streaming data. You use that as follows:

```
eb.registerHandler(channelName, function(msg) {
  // do something with the data in msg. right now the server only returns x,y pairs.
  var dmsg = JSON.parse(msg);
  console.log("got a message: " + dmsg.x + ", " + dmsg.y);
  }
```
This will register a callback that is run whenever a message on that channel is received over the eventbus.

Recommended reading:
http://vertx.io/docs/vertx-core/java/#event_bus

http://vertx.io/docs/vertx-web/java/#_sockjs_event_bus_bridge

# Current available REST queries
## Clinical data
#### GET /patients
- Parameters: None
- Success: Status 200 OK
- Data returned:
```
{  
  "1":{  
    "id":1,
    "bed":"121",
    "name":"Mary Kennedy",
    "age":45,
    "status":"Critical",
    "patient_id":"9114-182-88",
    "hospital_admission_id":30510,
    "case_id":9907,
    "weight":33,
    "height":127,
    "temperature":68.664779558408,
    "heart-rate":186,
    "allergies":"Aciclovir",
    "cardiac":false,
    "blood_pressure":"194/9"
  },
  "2":{  
    "id":2,
    "bed":"151",
    "name":"Mary Johnson",
    "age":27,
    "status":"Serious",
    "patient_id":"3094-822-96",
    "hospital_admission_id":455,
    "case_id":6305,
    "weight":65,
    "height":125,
    "temperature":121.23162046630166,
    "heart-rate":7,
    "allergies":"Latex",
    "cardiac":true,
    "blood_pressure":"116/46"
  },
  "3":{  
    "id":3,
    "bed":"147",
    "name":"Jane Johnson",
    "age":62,
    "status":"Serious",
    "patient_id":"9005-47-3",
    "hospital_admission_id":91775,
    "case_id":8037,
    "weight":96,
    "height":132,
    "temperature":58.98972987996298,
    "heart-rate":147,
    "allergies":"Latex",
    "cardiac":true,
    "blood_pressure":"237/76"
  },
  "4":{  
    "id":4,
    "bed":"62",
    "name":"Sally Johnson",
    "age":44,
    "status":"Stable",
    "patient_id":"6951-256-14",
    "hospital_admission_id":67324,
    "case_id":7049,
    "weight":186,
    "height":119,
    "temperature":13.421750498170805,
    "heart-rate":21,
    "allergies":"Penicillin",
    "cardiac":false,
    "blood_pressure":"148/65"
  }
}
```

#### GET /patients/[id]
- Parameters: 
	- id[Integer]: [Required] Integer id of a specific patient
- Success: 200 OK
- Data returned:

```
{  
  "id":1,
  "bed":"121",
  "name":"Mary Kennedy",
  "age":45,
  "status":"Critical",
  "patient_id":"9114-182-88",
  "hospital_admission_id":30510,
  "case_id":9907,
  "weight":33,
  "height":127,
  "temperature":68.664779558408,
  "heart-rate":186,
  "allergies":"Aciclovir",
  "cardiac":false,
  "blood_pressure":"194/9"
}
```

## Streaming data

#### GET /stream/waveform/[type]/[id]
- Parameters:
	- type[String]: [Required] Waveform type wanted. Valid options: `RESP, ECG, PAP, ABP`
	- id[Integer]: [Required] Id of the patient.
- Success: 200 OK
- Data returned: `RESP.1`
- Notes: This returns a string that is the eventbus channel name in which the waveform data will be broadcasted. You need to register an eventbus handler to process the data.

#### GET /stream/numerical/[type]/[id]
- This is for any possible numerical stream (average 5 min, etc). Nothing implemented for now.

## Alerts

#### GET /alerts/[id]
- Parameters:
	- id[Integer]: [Required] Id of the patient for whom you want to receive alerts.
- Success: 200 OK
- Data returned: `alerts.1`
- Notes: Similar to waveforms, this returns a string that is the eventbus channel name you want to register a handler for.

### Server specific stuff

/streambus - The eventbus endpoint that you connect to.
