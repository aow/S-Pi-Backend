package edu.pdx.spi;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.pdx.spi.verticles.Deploy;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

class RuntimeOptions {
  public boolean isDebug() {
    return debug;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  @Parameter(names = "-debug", description = "Enable debug mode, which doesn't use S-Store")
  private boolean debug = false;

  @Parameter(names = "-host", description = "Change the hostname to listen on, default is localhost")
  private String hostname = "localhost";

  @Parameter(names = "-port", description = "Change the port, default is 9999")
  private int port = 9999;
}

public class Runner {

  public static void main(final String... args) {
    JsonObject vertxConfig = new JsonObject();
    DeploymentOptions deploymentOptions = new DeploymentOptions();

    Vertx vtx = Vertx.vertx();
    Verticle deploy = new Deploy();

    RuntimeOptions clOptions = new RuntimeOptions();
    new JCommander(clOptions, args);

    vertxConfig.put("debug", clOptions.isDebug())
        .put("hostname", clOptions.getHostname())
        .put("port", clOptions.getPort());

    System.out.println("Starting deployment...");
    vtx.deployVerticle(deploy, deploymentOptions.setConfig(vertxConfig));
  }
}
