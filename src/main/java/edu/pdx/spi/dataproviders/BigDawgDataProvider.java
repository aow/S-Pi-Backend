package edu.pdx.spi.dataproviders;

import edu.pdx.spi.utils.QueryCache;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

public class BigDawgDataProvider implements DataProvider {
  Vertx vertx;
  QueryCache cache;
  Random rn;
  EventBus eb;
  HttpClient requestClient;
  HttpClientOptions clientOptions;
  // Config fields
  JsonObject config;
  String baseBigDogUrl;
  String bigDawgPollUrl;
  int bigDawgPollPort;
  boolean isAlertsTimerActive;

  public BigDawgDataProvider(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.cache = new QueryCache(this.vertx);
    this.rn = new Random();
    this.eb = this.vertx.eventBus();
    this.config = config;


    clientOptions = new HttpClientOptions()
        .setDefaultHost(this.config.getString("bigDawgRequestUrl"))
        .setDefaultPort(this.config.getInteger("bigDawgRequestPort"));
    requestClient = vertx.createHttpClient(clientOptions);
    bigDawgPollUrl = this.config.getString("bigDawgPollUrl");
    bigDawgPollPort = this.config.getInteger("bigDawgPollPort");
    // Change this if you need to test locally. Make sure it points to the vertx url
    // not the main site url.
    baseBigDogUrl = "http://api.s-pi-demo.com/incoming/";
  }

  public void startStream(String responseChannel, String type, String id, String ip) {
    if (cache.cacheIfPresent(responseChannel, ip)) {
      return;
    }

    JsonObject outData = new JsonObject();
    outData.put("channel", responseChannel).put("query", type).put("id", id);
    long timerId = vertx.setPeriodic(1000, t -> eb.publish("sstore", outData));

    cache.cache(responseChannel, timerId, ip);
  }

  public void startAlert(String responseChannel, String id, String ip) {
    if (cache.cacheIfPresent(responseChannel, ip)) {
      return;
    }

    requestBigDawgAlert(responseChannel);
    long timerId = startBigDawgTimer();

    cache.cache(responseChannel, timerId, ip);
  }

  public QueryCache getCache() {
    return this.cache;
  }

  private void requestBigDawgAlert(String responseChannel) {
    if (cache.isChannelActive(responseChannel)) {
      return;
    }

    JsonObject query = new JsonObject();
    query.put("query", "checkHeartRate");
    query.put("notifyURL", baseBigDogUrl + responseChannel);
    query.put("authorization", new JsonObject());
    query.put("pushNotify", false);
    query.put("oneTime", false);

    HttpClientRequest request = requestClient.post("/bigdawg/registeralert", handler -> {
      handler.bodyHandler(resp -> {
        URI statusUrl;
        try {
          statusUrl = new URI(new JsonObject(resp.toString()).getString("statusURL"));
          System.out.println(statusUrl);
        } catch (DecodeException | URISyntaxException e) {
          System.out.println(resp.toString());
          return;
        }
        if (bigDawgPollUrl.equals("")) {
          cache.addChannel(responseChannel, statusUrl.toString());
        } else {
          cache.addChannel(responseChannel, statusUrl.getPath());
        }
      });
    });
    request.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
    request.end(query.encode());
  }

  private long startBigDawgTimer() {
    return vertx.setPeriodic(2000, t -> cache.getStatusUrls().entrySet().forEach(e -> {
      String absUrl;
      if (bigDawgPollUrl.equals("")) {
        absUrl = e.getValue();
      } else {
        absUrl = "http://" + bigDawgPollUrl + ":" + bigDawgPollPort + e.getValue();
      }
      System.out.println("Querying: " + absUrl);
      HttpClientRequest getData = requestClient.getAbs(absUrl, dataResp -> dataResp.bodyHandler(d -> {
        System.out.println(d.toString());
        if (!d.toString().equals("None")) {
          eb.publish(e.getKey(), new JsonObject(d.toString()));
          System.out.println(d.toString());
        }
      }));
      getData.end();
      System.out.println(cache.getStatusCount());
    }));
  }
}
