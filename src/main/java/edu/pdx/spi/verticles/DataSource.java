package edu.pdx.spi.verticles;

import static edu.pdx.spi.ChannelNames.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.fakedata.models.Patient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

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
  Map<String, Long> activeClientTimers;
  Map<String, List<String>> activeListeners;
  boolean SSTORE;
  boolean BD;
  HttpClient requestClient;
  HttpClientOptions clientOptions;
  String baseBigDogUrl;

  public void start() {
    SSTORE = this.config().getBoolean("sstore");
    BD = this.config().getBoolean("bigDawg");
    rn = new Random();
    eb = vertx.eventBus();
    activeClientTimers = new HashMap<>();
    activeListeners = new HashMap<>();

    if (BD) {
      clientOptions = new HttpClientOptions()
          .setDefaultHost(this.config().getString("bigDawgUrl"))
          .setDefaultPort(8080);
      requestClient = vertx.createHttpClient(clientOptions);
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
   *  Listens for requests for streams and handles setting up the response channel
   *  as well as starting the stream itself. Replies with the requested stream's outgoing
   *  channel.
   */
  private void registerStreamRequestEventBusHandler() {
    eb.consumer(WAVEFORM_REQ, m-> {
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
      // For every channel's listener list
      activeListeners.entrySet().forEach(e -> {
        // Try and remove a single count for the IP that just disconnected
        // Don't remove all instances, as the client can have multiple windows open
        e.getValue().remove(m.body());
        // If the client list is empty, that means the channel is no longer needed
        if (e.getValue().isEmpty()) {
          // So kill the timer
          killTimer(e.getKey());
        }
      });
      // Then clear out the reference to it in the cache.
      activeListeners.entrySet().removeIf(e -> e.getValue().isEmpty());
      System.out.println("Killed timer");
    });
  }

  private void startAlerts(String responseChannel, String id, String ip) {
    // Same deal as startStreamingQuery.
    long timerId = 1L;

    if (Objects.nonNull(activeClientTimers.get(responseChannel))) {
      activeListeners.compute(responseChannel, (k,v) -> {
        v.add(ip);
        return v;
      });
      return;
    }

    if (BD) {
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
    } else if (SSTORE) {
      //TODO: Don't think this needs to be here anymore, but good to implement in case of future need.
      timerId = startSstoreTimer(responseChannel, "alert", id);
    } else {
      // Local alerts
      timerId = startFakeAlertTimer(responseChannel);
    }

    activeClientTimers.put(responseChannel, timerId);
    activeListeners.compute(responseChannel, (k,v) -> {
      if (Objects.isNull(v)) v = new ArrayList<>();
      v.add(ip);
      return v;
    });
  }

  private void startStreamingQuery(String responseChannel, String type, String id, String ip) {
    long timerId;

    // If an entry exists, there is a timer running already, so just add the ip for tracking
    if (Objects.nonNull(activeClientTimers.get(responseChannel))) {
      activeListeners.compute(responseChannel, (k,v) -> {
        v.add(ip);
        return v;
      });
      return;
    }

    // Otherwise, start the timer
    if (SSTORE) {
      timerId = startSstoreTimer(responseChannel, type, id);
    } else {
      timerId = startFakeTimer(responseChannel);
    }

    // And store the reference to the timer for caching
    activeClientTimers.put(responseChannel, timerId);

    System.out.println("Num active timers: " + activeClientTimers.size());

    // Then init the array and add the ip for tracking activity
    activeListeners.compute(responseChannel, (k, v) -> {
      if (Objects.isNull(v)) v = new ArrayList<>();
      v.add(ip);
      return v;
    });
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
        json.put("TS", startTime + 8*i);
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

  private void killTimer(String name) {
    vertx.cancelTimer(activeClientTimers.get(name));
    activeClientTimers.remove(name);
  }
}
