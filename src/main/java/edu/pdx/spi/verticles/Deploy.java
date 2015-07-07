package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;

public class Deploy extends AbstractVerticle {
  @Override
  public void start() {
    System.out.println("Starting server...");

    vertx.deployVerticle("edu.pdx.spi.verticles.DataSource");
    vertx.deployVerticle("edu.pdx.spi.verticles.Server");
    vertx.deployVerticle("edu.pdx.spi.verticles.Alerts");
  }
}
