package edu.pdx.spi.handlers;

import edu.pdx.spi.ValidWaveforms;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

public class StreamingHandler implements Handler<RoutingContext> {
  EventBus eb;
  JsonObject js;

  public void handle(RoutingContext rc) {
    eb = rc.vertx().eventBus();
    js = new JsonObject();

    // Get the requested parameters.
    String type = rc.request().getParam("type");
    String id = rc.request().getParam("id");

    if (!ValidWaveforms.contains(type)) {
      rc.response().end("Invalid waveform");
      return;
    }

    // Put them in the JSON object we'll be sending.
    js.put("type", type);
    js.put("id", id);
    js.put("ip", rc.request().headers().get("X-Real-IP"));
    // Send it out and return the channel name.
    eb.send("streamrequest", js, m -> {
      String result = (String) m.result().body();
      rc.response().end(result);
    });
  }
}
