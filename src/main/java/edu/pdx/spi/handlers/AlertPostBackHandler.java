package edu.pdx.spi.handlers;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AlertPostBackHandler implements Handler<RoutingContext> {
  EventBus eb;
  JsonObject js;

  public void handle(RoutingContext rc) {
    eb = rc.vertx().eventBus();
    if (rc.request().method() == HttpMethod.POST) {
      // This happens when we get an alert posted from BigDawg's stuff.
      // We provide the alertId param to BigDawg, so we can just retrieve it
      // and push the data out over the eventbus. Assume we are getting JSON.
      //TODO: Fix this if we don't get JSON from BigDawg.

      eb.publish(rc.request().getParam("alertName"), rc.getBodyAsJson());
      System.out.println("Got alert postback");
      rc.response().end();
    }
  }
}
