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

      System.out.println("Got alert request");
      js.put("ip", rc.request().headers().get("X-Real-IP"));
      //if (Objects.nonNull(rc.request().getParam("id"))) {
        js.put("id", rc.request().getParam("id"));
      //}

      eb.send("alertrequest", js, m -> {
        rc.response().end((String) m.result().body());
      });
    }
  }
}
