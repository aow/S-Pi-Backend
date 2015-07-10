package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class Deploy extends AbstractVerticle {
  @Override
  public void start() {
    System.out.println("Starting server...");
    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true).setConfig(this.config());
    DeploymentOptions normalOptions = new DeploymentOptions().setConfig(this.config());

<<<<<<< HEAD
    vertx.deployVerticle("edu.pdx.spi.verticles.DataSource");
    vertx.deployVerticle("edu.pdx.spi.verticles.Server");
    vertx.deployVerticle("edu.pdx.spi.verticles.Alerts");
=======
    if (!this.config().getBoolean("debug")) {
      vertx.deployVerticle("edu.pdx.spi.verticles.SstoreConnector", workerOptions);
    }

    vertx.deployVerticle("edu.pdx.spi.verticles.DataSource", normalOptions);
    vertx.deployVerticle("edu.pdx.spi.verticles.Server", normalOptions);
>>>>>>> 3006ebe918c2dc96aa965fb9c9a75e49204376a8
  }
}
