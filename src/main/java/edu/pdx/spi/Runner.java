package edu.pdx.spi;

import edu.pdx.spi.verticles.Deploy;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

public class Runner {
  public static void main(String... args) {
    Vertx vtx = Vertx.vertx();
    Verticle deploy = new Deploy();

    System.out.println("Starting deployment...");
    vtx.deployVerticle(deploy);
  }
}
