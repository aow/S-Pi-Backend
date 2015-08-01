package edu.pdx.spi.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.fakedata.models.Patient;
import edu.pdx.spi.utils.QueryCache;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static edu.pdx.spi.ChannelNames.*;

final class Patients {
  Map<Integer, Patient> patients = new HashMap<>();

  public Patients() {
    patients.put(1, new Patient(1));
    patients.put(2, new Patient(2));
    patients.put(3, new Patient(3));
    patients.put(4, new Patient(4));
  }

  public Patient getPatient(int id) {
    return patients.get(id);
  }

  public Map<Integer, Patient> getAllPatients() {
    return patients;
  }
}

public final class DataSource extends AbstractVerticle {
  Patients patientData = new Patients();
  ObjectMapper om = new ObjectMapper();
  Random rn;
  EventBus eb;
  boolean SSTORE;
  boolean BD;
  HttpClient requestClient;
  HttpClientOptions clientOptions;
  String baseBigDogUrl;
  String bigDawgPollUrl;
  int bigDawgPollPort;
  QueryCache cache;


  public void start() {
    SSTORE = this.config().getBoolean("sstore");
    BD = this.config().getBoolean("bigDawg");
    rn = new Random();
    eb = vertx.eventBus();
    cache = new QueryCache(this.getVertx());

    if (BD) {
      clientOptions = new HttpClientOptions()
          .setDefaultHost(this.config().getString("bigDawgRequestUrl"))
          .setDefaultPort(this.config().getInteger("bigDawgRequestPort"));
      requestClient = vertx.createHttpClient(clientOptions);
      bigDawgPollUrl = this.config().getString("bigDawgPollUrl");
      bigDawgPollPort = this.config().getInteger("bigDawgPollPort");
      // Change this if you need to test locally. Make sure it points to the vertx url
      // not the main site url.
      baseBigDogUrl = "http://api.s-pi-demo.com/incoming/";
    }

    eb.consumer("patients", m -> {
      if (((String) m.body()).isEmpty()) {
        try {
          m.reply(om.writeValueAsString(patientData.getAllPatients()));
        } catch (Exception e) {
          m.reply("error parsing patient data" + e.getMessage());
        }
      } else {
        Patient p = patientData.getPatient(Integer.parseInt((String) m.body()));
        if (p != null) {
          try {
            m.reply(om.writeValueAsString(p));
          } catch (Exception e) {
            m.reply("error parsing patient data");
          }
        } else {
          m.reply("Invalid patient");
        }
      }
    });

    registerStreamRequestEventBusHandler();
    registerAlertsRequestEventBusHandler();
    registerTerminateStreamSessionEventBusHandler();
  }

  /**
   * Listens for requests for streams and handles setting up the response channel
   * as well as starting the stream itself. Replies with the requested stream's outgoing
   * channel.
   */
  private void registerStreamRequestEventBusHandler() {
    eb.consumer(WAVEFORM_REQ, m -> {
      JsonObject requestData = (JsonObject) m.body();
      String type = requestData.getString("type");
      String id = requestData.getString("id");

      String responseChannel = type + "." + id;
      startStreamingQuery(responseChannel, type, id, requestData.getString("ip"));
      m.reply(responseChannel);
    });
  }

  private void registerAlertsRequestEventBusHandler() {
    eb.consumer(ALERTS_REQ, m -> {
      JsonObject req = (JsonObject) m.body();
      String responseChannel = "alerts" + "." + req.getString("id");
      startAlerts(responseChannel, req.getString("id"), req.getString("ip"));
      m.reply(responseChannel);
    });
  }

  private void registerTerminateStreamSessionEventBusHandler() {
    eb.consumer(STREAM_END, m -> {
      cache.removeClient(m.body().toString());
    });
  }

  private void startAlerts(String responseChannel, String id, String ip) {
    // Same deal as startStreamingQuery.
    long timerId;


    if (cache.isChannelActive(responseChannel)) {
      cache.addClient(responseChannel, ip);
      return;
    }

    if (BD) {
      requestBigDawgAlert(responseChannel);
      //TODO: Fix this so only one BD timer runs.
      timerId = startBigDawgTimer();
    } else if (SSTORE) {
      //TODO: Don't think this needs to be here anymore, but good to implement in case of future need.
      timerId = startSstoreTimer(responseChannel, "alert", id);
    } else {
      // Local alerts
      timerId = startFakeAlertTimer(responseChannel);
    }

    cache.addChannel(responseChannel, timerId);
    cache.addClient(responseChannel, ip);
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

  private void startStreamingQuery(String responseChannel, String type, String id, String ip) {
    long timerId;

    // If an entry exists, there is a timer running already, so just add the ip for tracking

    if (cache.isChannelActive(responseChannel)) {
      cache.addClient(responseChannel, ip);
      return;
    }

    // Otherwise, start the timer
    if (SSTORE) {
      timerId = startSstoreTimer(responseChannel, type, id);
    } else {
      timerId = startFakeTimer(responseChannel);
    }

    // And store the reference to the timer for caching
    cache.addChannel(responseChannel, timerId);
    // Then init the array and add the ip for tracking activity
    cache.addClient(responseChannel, ip);
  }

  private long startFakeAlertTimer(String responseChannel) {
    long id = vertx.setPeriodic(10000, t -> {
      if (rn.nextBoolean()) {
        JsonObject js = new JsonObject();
        js.put("patient_id", rn.nextInt(4));
        js.put("ts", System.currentTimeMillis());
        js.put("signame", "blue");
        js.put("interval", 2);
        js.put("alert_msg", "It is an alert!");
        js.put("action_msg", "Do something!");
        eb.publish(responseChannel, js);
        System.out.println("sent msg");
      }
    });

    return id;
  }

  private long startFakeTimer(String responseChannel) {
    return vertx.setPeriodic(1000, t -> {
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
  }

  private long startSstoreTimer(String responseChannel, String queryType, String patientId) {
    JsonObject outData = new JsonObject();
    outData.put("channel", responseChannel).put("query", queryType).put("id", patientId);
    return vertx.setPeriodic(1000, t -> eb.publish("sstore", outData));
  }

  // Unused
  private void registerBigDawgPushAlerts(String responseChannel) {
    JsonObject query = new JsonObject();
    //TODO: Figure out what proc to call.
    query.put("query", "checkHeartRate");
    query.put("notifyURL", baseBigDogUrl + responseChannel);
    query.put("authorization", new JsonObject());
    query.put("pushNotify", true);
    query.put("oneTime", false);
    //TODO: do something here for bigdawg alerts
    // Our routes that BigDawg will post back to should be in the form /incoming/[alertId]
    HttpClientRequest request = requestClient.post("/bigdawg/registeralert", handler -> {
      System.out.println(handler.statusMessage());
      handler.bodyHandler(System.out::println);
    });
    System.out.println("Sending BD post");
    request.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
    request.end(query.encode());
    System.out.println("Sent BD post");
  }
}
