package edu.pdx.spi.handlers;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public final class PatientsHandler implements Handler<RoutingContext> {
  public void handle(RoutingContext rc) {
    EventBus eb = rc.vertx().eventBus();
    String id;
    try {
      id = rc.request().getParam("id");
      if (Objects.nonNull(id)) {
        Integer.parseInt(id);
      }
    } catch (Exception e) {
      rc.response().end();
      return;
    }
    if (id == null) {
      eb.send("patients", "", m -> {
        rc.response().end((String) m.result().body());
      });
    } else {
      eb.send("patients", rc.request().getParam("id"), m -> {
        rc.response().end((String) m.result().body());
      });
    }
  }
}
