package edu.pdx.spi.handlers;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;

public class UnsubHandler implements Handler<RoutingContext> {
  EventBus eb;

  public void handle(RoutingContext rc) {
    eb = rc.vertx().eventBus();
    System.out.println("got cancel req for: " + rc.request().getParam("name"));
    eb.send("unsub", rc.request().getParam("name"));
  }
}
