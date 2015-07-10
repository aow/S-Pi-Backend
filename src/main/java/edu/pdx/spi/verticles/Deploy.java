package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class Deploy extends AbstractVerticle {
  @Override
  public void start() {
    System.out.println("Starting server...");
    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true).setConfig(this.config());
    DeploymentOptions normalOptions = new DeploymentOptions().setConfig(this.config());


    if (!this.config().getBoolean("debug")) {
      vertx.deployVerticle("edu.pdx.spi.verticles.SstoreConnector", workerOptions);
    }

    vertx.deployVerticle("edu.pdx.spi.verticles.DataSource", normalOptions);
    vertx.deployVerticle("edu.pdx.spi.verticles.Alerts", workerOptions);
    vertx.deployVerticle("edu.pdx.spi.verticles.Server", normalOptions);
  }
}
