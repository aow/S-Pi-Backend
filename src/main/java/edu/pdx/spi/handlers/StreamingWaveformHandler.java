package edu.pdx.spi.handlers;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public final class StreamingWaveformHandler implements Handler<RoutingContext> {
  EventBus eb;
  JsonObject js;

  public void handle(RoutingContext rc) {
    eb = rc.vertx().eventBus();
    js = new JsonObject();

    js.put("type", rc.request().getParam("type"));
    js.put("id", rc.request().getParam("id"));
    eb.send("waveformrequests", js.encode(), m -> {
      rc.response().end((String) m.result().body());
    });
  }
}
