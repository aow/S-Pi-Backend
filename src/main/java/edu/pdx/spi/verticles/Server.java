package edu.pdx.spi.verticles;

import edu.pdx.spi.handlers.UnsubHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import edu.pdx.spi.handlers.PatientsHandler;
import edu.pdx.spi.handlers.StreamingNumericalHandler;
import edu.pdx.spi.handlers.StreamingWaveformHandler;

public class Server extends AbstractVerticle {
  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
    // Patient data routes
    PatientsHandler ph = new PatientsHandler();
    router.route("/patients/:id").handler(ph);
    router.route("/patients").handler(ph);

    // Numerical stream endpoints
    StreamingNumericalHandler sn = new StreamingNumericalHandler();
    router.route("/stream/numerical/:type/:id").handler(sn);

    // Waveform stream endpoints
    StreamingWaveformHandler sw = new StreamingWaveformHandler();
    router.route("/stream/waveform/:type/:id").handler(sw);

    router.route("/unsub/:name").handler(new UnsubHandler());

    // Bridged eventbus permissions
    BridgeOptions options = new BridgeOptions();
    options.addOutboundPermitted(new PermittedOptions().setAddressRegex(".+\\..+"));
    options.addInboundPermitted(new PermittedOptions().setAddress("unsub"));

    router.route("/streambus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEvent.Type.SOCKET_CREATED) {
        System.out.println("Socket created");
      }
      else if (event.type() == BridgeEvent.Type.SOCKET_CLOSED) {
        System.out.println("Socket closed ");
        System.out.println(event.rawMessage());
      }

      event.complete(true);
    }));

    vertx.createHttpServer().requestHandler(router::accept).listen(9999);
    System.out.println("Server listening on port 9999.");
  }
}
