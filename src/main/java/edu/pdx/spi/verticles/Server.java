package edu.pdx.spi.verticles;

import edu.pdx.spi.handlers.UnsubHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.*;

import edu.pdx.spi.handlers.PatientsHandler;
import edu.pdx.spi.handlers.StreamingNumericalHandler;
import edu.pdx.spi.handlers.StreamingWaveformHandler;

public class Server extends AbstractVerticle {
  EventBus eb;

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
    eb = vertx.eventBus();

    // Patient data routes
    PatientsHandler ph = new PatientsHandler();
    router.route("/api/patients/:id").handler(ph);
    router.route("/api/patients").handler(ph);

    // Numerical stream endpoints
    StreamingNumericalHandler sn = new StreamingNumericalHandler();
    router.route("/api/stream/numerical/:type/:id").handler(sn);

    // Waveform stream endpoints
    StreamingWaveformHandler sw = new StreamingWaveformHandler();
    router.route("/api/stream/waveform/:type/:id").handler(sw);

    router.route("/api/unsub/:name").handler(new UnsubHandler());

    // Bridged eventbus permissions
    BridgeOptions options = new BridgeOptions();
    options.addOutboundPermitted(new PermittedOptions().setAddressRegex(".+\\..+"));

    router.route("/api/streambus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEvent.Type.SOCKET_CREATED) {
        System.out.println("Socket created");
        System.out.println(event.socket().remoteAddress().host());
      }
      else if (event.type() == BridgeEvent.Type.SOCKET_CLOSED) {
        System.out.println("Socket closed ");
        eb.send("unsub", event.socket().remoteAddress().host());
      }

      event.complete(true);
    }));

    vertx.createHttpServer().requestHandler(router::accept).listen(9999);
    System.out.println("Server listening on port 9999.");
  }
}
