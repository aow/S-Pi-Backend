package edu.pdx.spi.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.fakedata.models.Patient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

final class Patients {
  Map<Integer, Patient> patients = new HashMap<>();

  public Patients() {
    patients.put(1, new Patient(1, 100, "Jane", "Doe"));
    patients.put(2, new Patient(2, 101, "John", "Doe"));
    patients.put(3, new Patient(3, 102, "Lego", "Man"));
    patients.put(4, new Patient(4, 103, "Suzy", "Doe"));
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
  Map<String, Integer> listenerCounts;
  Map<String, List<String>> clientChannels;
  Map<String, List<String>> activeListeners;
  boolean DEBUG;

  public void start() {
    DEBUG = this.config().getBoolean("debug");
    rn = new Random();
    eb = vertx.eventBus();
    activeClientTimers = new HashMap<>();
    listenerCounts = new HashMap<>();
    clientChannels = new HashMap<>();
    activeListeners = new HashMap<>();

    eb.consumer("patients", m -> {
      if (((String) m.body()).isEmpty()) {
        try {
          m.reply(om.writeValueAsString(patientData.getAllPatients()));
        } catch (Exception e) {
          m.reply("error parsing patient data");
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

    eb.consumer("streamrequest", m -> {
      JsonObject requestData = (JsonObject) m.body();

      String responseChannel = requestData.getString("type") + "." + requestData.getString("id");

      startStreamingQuery(responseChannel, requestData.getString("type"), requestData.getString("id"), requestData.getString("ip"));

      m.reply(responseChannel);
    });

    eb.consumer("alertrequest", m -> {
      JsonObject req = (JsonObject) m.body();
      startAlerts("alerts", req.getString("id"), req.getString("ip"));
      m.reply("alerts");
    });

    eb.consumer("ended", m -> {
      activeListeners.entrySet().forEach(e -> {
        e.getValue().remove(m.body());
        if (e.getValue().isEmpty()) {
          killTimer(e.getKey());
        }
      });
      activeListeners.entrySet().removeIf(e -> e.getValue().isEmpty());
    });
  }

  private void startAlerts(String responseChannel, String id, String ip) {
    long timerId;

    if (Objects.nonNull(activeClientTimers.get(responseChannel))) {
      activeListeners.compute(responseChannel, (k,v) -> {
        v.add(ip);
        return v;
      });
      return;
    }

    if (DEBUG) {
      timerId = startFakeAlertTimer();
    } else {
      timerId = startSstoreTimer(responseChannel, "alert", id);
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
    if (DEBUG) {
      timerId = startFakeTimer(responseChannel);
    } else {
      timerId = startSstoreTimer(responseChannel, type, id);
    }

    activeClientTimers.put(responseChannel, timerId);

    // If the mapping doesn't exist, set it to one, otherwise increment the value.
    activeListeners.compute(responseChannel, (k,v) -> {
      if (Objects.isNull(v)) v = new ArrayList<>();
      v.add(ip);
      return v;
    });
  }

  private long startFakeAlertTimer() {
    long id = vertx.setPeriodic(10000, t -> {
      if (rn.nextBoolean()) {
        JsonObject js = new JsonObject();
        js.put("patient_id", rn.nextInt(4));
        js.put("ts", System.currentTimeMillis());
        js.put("signame", "blue");
        js.put("interval", 2);
        js.put("alert_msg", "It is an alert!");
        js.put("action_msg", "Do something!");
        eb.publish("alerts", js);
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
    return vertx.setPeriodic(1000, t -> eb.send("sstore", outData));
  }

  private void killTimer(String name) {
    vertx.cancelTimer(activeClientTimers.get(name));
    activeClientTimers.remove(name);
  }
}
