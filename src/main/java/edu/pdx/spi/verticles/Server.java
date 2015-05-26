package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Route;

public class Server extends AbstractVerticle {
  Router router = Router.router(vertx);

  @Override
  public void start() {
    router.route("/").handler(routingContext -> {
      HttpServerResponse resp = routingContext.response();

      resp.putHeader("content-type", "text/plain");
      resp.end("Hello!");
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(9999);
    System.out.println("Server listening on port 9999.");
  }
}
