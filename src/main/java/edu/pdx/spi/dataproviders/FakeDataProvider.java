package edu.pdx.spi.dataproviders;

import edu.pdx.spi.utils.QueryCache;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;

public class FakeDataProvider implements DataProvider {
  Vertx vertx;
  QueryCache cache;
  Random rn;
  EventBus eb;

  public FakeDataProvider(Vertx vertx, QueryCache cache) {
    this.vertx = vertx;
    this.cache = new QueryCache(this.vertx);
    this.rn = new Random();
    this.eb = this.vertx.eventBus();
  }

  public void startStream(String responseChannel, String type, String id, String ip) {
    if (cache.cacheIfPresent(responseChannel, ip)) {
      return;
    }

    long timerId = vertx.setPeriodic(1000, t -> {
      long startTime = System.currentTimeMillis();
      JsonObject data = new JsonObject();
      JsonArray jsa = new JsonArray();
      for (int i = 0; i < 125; i++) {
        JsonObject json = new JsonObject();
        json.put("TS", startTime + 8 * i);
        json.put("SIGNAL", Math.abs(rn.nextGaussian()) * 25);
        jsa.add(json);
      }
      data.put("data", jsa);
      eb.publish(responseChannel, data);
    });

    cache.cache(responseChannel, timerId, ip);
  }

  public void startAlert(String responseChannel, String id, String ip) {
    if (cache.cacheIfPresent(responseChannel, ip)) {
      return;
    }

    long timerId = vertx.setPeriodic(10000, t -> {
      if (rn.nextBoolean()) {
        JsonObject js = new JsonObject();
        js.put("patient_id", rn.nextInt(4));
        js.put("ts", System.currentTimeMillis());
        js.put("signame", "blue");
        js.put("interval", 2);
        js.put("alert_msg", "It is an alert!");
        js.put("action_msg", "Do something!");
        eb.publish(responseChannel, js);
      }
    });

    cache.cache(responseChannel, timerId, ip);
  }

  public QueryCache getCache() {
    return this.cache;
  }
}
