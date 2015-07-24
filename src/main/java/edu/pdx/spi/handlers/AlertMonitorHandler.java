package edu.pdx.spi.handlers;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class AlertMonitorHandler implements Handler<RoutingContext> {
  EventBus eb;
  JsonObject js;

  public void handle(RoutingContext rc) {
    eb = rc.vertx().eventBus();
    if (rc.request().method() == HttpMethod.GET) {
      js = new JsonObject();

      js.put("ip", rc.request().headers().get("X-Real-IP"));
      if (Objects.nonNull(rc.request().getParam("id"))) {
        js.put("id", rc.request().getParam("id"));
      }

      eb.send("alertrequest", js, m -> {
        rc.response().end((String) m.result().body());
      });
    } else if (rc.request().method() == HttpMethod.POST) {
      //TODO: Figure out what data we're actually being sent here then forward it over the eventbus.
      System.out.println("Got: " + rc.getBodyAsJson().encodePrettily());
      // For now, assume we are being posted json and send it out
      eb.send("alertresponse", rc.getBodyAsJson());
      rc.response().end();
    }
  }
}
