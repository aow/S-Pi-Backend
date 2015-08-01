package edu.pdx.spi.dataproviders;

import edu.pdx.spi.utils.QueryCache;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.Random;

public class SstoreDataProvider implements DataProvider {
  Vertx vertx;
  QueryCache cache;
  Random rn;
  EventBus eb;

  public SstoreDataProvider(Vertx vertx) {
    this.vertx = vertx;
    this.cache = new QueryCache(this.vertx);
    this.rn = new Random();
    this.eb = this.vertx.eventBus();
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

    JsonObject outData = new JsonObject();
    outData.put("channel", responseChannel).put("query", "alert").put("id", id);
    long timerId = vertx.setPeriodic(1000, t -> eb.publish("sstore", outData));

    cache.cache(responseChannel, timerId, ip);
  }

  public QueryCache getCache() {
    return this.cache;
  }
}
