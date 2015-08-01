package edu.pdx.spi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.verticles.Deploy;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.*;


final class Options {
  @JsonProperty
  String vertxHost;
  @JsonProperty
  Integer vertxPort;
  @JsonProperty
  Boolean sstore;
  @JsonProperty
  String sstoreClientHost;
  @JsonProperty
  Integer sstoreClientPort;
  @JsonProperty
  Boolean bigDawg;
  @JsonProperty
  String bigDawgRequestUrl;
  @JsonProperty
  int bigDawgRequestPort;
  @JsonProperty
  String bigDawgPollUrl;
  @JsonProperty
  int bigDawgPollPort;

  public Options() {
    this.vertxHost = "localhost";
    this.vertxPort = 9999;
    this.sstore = false;
    this.sstoreClientHost = "localhost";
    this.sstoreClientPort = 6000;
    this.bigDawg = false;
    this.bigDawgRequestUrl = "";
    this.bigDawgRequestPort = 8080;
    this.bigDawgPollUrl = "";
    this.bigDawgPollPort = 8080;
  }

  @Override
  public String toString() {
    return "Options{" +
        "vertxHost='" + vertxHost + '\'' +
        ", vertxPort=" + vertxPort +
        ", sstore=" + sstore +
        ", sstoreClientHost='" + sstoreClientHost + '\'' +
        ", sstoreClientPort=" + sstoreClientPort +
        ", bigDawg=" + bigDawg +
        ", bigDawgRequestUrl='" + bigDawgRequestUrl + '\'' +
        ", bigDawgRequestPort=" + bigDawgRequestPort +
        ", bigDawgPollUrl='" + bigDawgPollUrl + '\'' +
        ", bigDawgPollPort=" + bigDawgPollPort +
        '}';
  }
}

public class Runner {

  public static void main(final String... args) {
    ObjectMapper om = new ObjectMapper();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    Options options = null;

    Path configFile = Paths.get("settings.json");

    if (Files.exists(configFile)) {
      System.out.println("Found config");
      try {
        options = om.readValue(Files.readAllBytes(configFile), Options.class);
        System.out.println(options);
      } catch (IOException e) {
        System.out.println("Error reading from local config. Using default.");
      }
    } else {
      options = new Options();
      try {
        Files.write(configFile, om.writeValueAsBytes(options), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      } catch (IOException e) {
        System.out.println("Error writing new default config file.");
      }
    }

    Vertx vtx = Vertx.vertx();
    Verticle deploy = new Deploy();

    System.out.println("Starting deployment...");
    try {
      vtx.deployVerticle(deploy, deploymentOptions.setConfig(new JsonObject(om.writeValueAsString(options))));
    } catch (JsonProcessingException e) {
      System.out.println(e);
    }
  }
}
