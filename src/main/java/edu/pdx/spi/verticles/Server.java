package edu.pdx.spi.verticles;

import edu.pdx.spi.handlers.AlertMonitorHandler;
import edu.pdx.spi.handlers.StreamingHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;

import edu.pdx.spi.handlers.PatientsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.ArrayList;

public class Server extends AbstractVerticle {
  EventBus eb;

  @Override
  public void start() {
    int port = this.config().getInteger("port");
    String hostname = this.config().getString("hostname");

    SessionStore sessStore = LocalSessionStore.create(vertx);

    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(sessStore).setNagHttps(false).setSessionCookieName("cook"));
    eb = vertx.eventBus();

    // Patient data routes
    PatientsHandler ph = new PatientsHandler();
    router.route("/patients/:id").handler(ph);
    router.route("/patients").handler(ph);

    // Alerts
    router.route("/alerts/:id").handler(new AlertMonitorHandler());

    // Numerical stream endpoints
    StreamingHandler streamHandle = new StreamingHandler();
    router.route("/stream/numerical/:type/:id").handler(streamHandle);
    // Waveform stream endpoints
    router.route("/stream/waveform/:type/:id").handler(streamHandle);

    // Bridged eventbus permissions
    BridgeOptions options = new BridgeOptions();
    options.addOutboundPermitted(new PermittedOptions().setAddressRegex(".+\\..+"));
    options.addOutboundPermitted(new PermittedOptions().setAddress("alerts"));

    router.route("/streambus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
      switch (event.type()) {
        case SOCKET_CREATED:
          System.out.println("Socket created.");
          break;
        case SOCKET_CLOSED:
          System.out.println("Socket closed.");
          JsonObject js = new JsonObject()
              .put("channels",
                  new JsonArray(new ArrayList<String>(event.socket().webSession().data().keySet())));
          eb.send("ended", js);
          break;
        case RECEIVE:
          event.socket().webSession().data().putIfAbsent(event.rawMessage().getString("address"), 1);
          break;
        case SEND:
          break;
      }

      event.complete(true);
    }));

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(port, hostname);

    System.out.println("Server started on " + hostname + ":" + port);
  }
}
