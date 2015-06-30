# S-Pi-Backend
Backend component for the S-Store MIMIC ICU Monitoring Demo

# Requirements
- Java 8
- Maven (3.0+)

# Building and running

1. Clone the repository
2. Run `mvn package` in the main directory (the one with pom.xml)
3. Look for `target/s-pi-x.x.x.jar`, where the xs are whatever version we're at.
4. Run that jar with `java -jar ...`
5. Profit

# Current available REST queries
### Clinical data
/api/patients - Returns all available patients

/api/patients/[id] - Returns a specific patient with the given id.

### Streaming data

/api/stream/waveform/[type]/[id] - Returns an eventbus channel that you can register a handler for.

/api/stream/numerical/[type]/[id] - Same as waveform, returns an eventbus channel.

Right now, the types supported are `hr, bp`, which are just randomly generated. Any id value is valid. This will change once we integrate with S-Store.

### Server specific stuff

/api/streambus - The eventbus endpoint that you connect to.

# Integrating with the frontend

You need to include https://github.com/vert-x3/vertx-web/blob/3.0.0/src/client/vertxbus.js library.

After that, you connect with the eventbus by doing `var eb = new vertx.EventBus("http://.../api/streambus")`, where the ... is whatever hostname the vertx server is listening on.

Once that is set up, whenever you need to start streaming data, simply issue an XHR request to the previous endpoints. They return the channel you have to listen on to get the streaming data. You use that as follows:

```
eb.registerHandler(channelName, function(msg) {
  // do something with the data in msg. right now the server only returns x,y pairs.
  var dmsg = JSON.parse(msg);
  console.log("got a message: " + dmsg.x + ", " + dmsg.y);
  }
```
