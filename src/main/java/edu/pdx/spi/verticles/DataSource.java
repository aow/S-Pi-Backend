package edu.pdx.spi.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.fakedata.models.Patient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
  boolean DEBUG;

  public void start() {
    DEBUG = this.config().getBoolean("debug");
    rn = new Random();
    eb = vertx.eventBus();
    activeClientTimers = new HashMap<>();
    listenerCounts = new HashMap<>();

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

      startStreamingQuery(responseChannel, requestData.getString("type"), requestData.getString("id"));

      m.reply(responseChannel);
    });

    eb.consumer("alertrequest", m -> {
      startAlerts((String)m.body());
      m.reply("alerts");
    });

    eb.consumer("ended", m -> {
      JsonObject data = (JsonObject) m.body();
      data.getJsonArray("channels").forEach((k) -> {
        listenerCounts.computeIfPresent((String) k, (x, y) -> {
          y -= y;
          if (y <= 0) {
            killTimer(x);
            return null;
          }
          return y;
        });
      });
    });
  }

  private void startAlerts(String id) {
    long timerId;

    if (activeClientTimers.get("alerts") != null) return;

    if (DEBUG) {
      timerId = startFakeAlertTimer();
    } else {
      timerId = startSstoreTimer("alerts", "alert", id);
    }

    activeClientTimers.put("alerts", timerId);
    listenerCounts.merge("alerts", 1, (k,v) -> v++);
  }

  private void startStreamingQuery(String responseChannel, String type, String id) {
    long timerId;

    // If an entry exists, there is a timer running already, so just do no work
    if (activeClientTimers.get(responseChannel) != null)
      return;

    // Otherwise, start the timer
    if (DEBUG) {
      timerId = startFakeTimer(responseChannel);
    } else {
      timerId = startSstoreTimer(responseChannel, type, id);
    }

    activeClientTimers.put(responseChannel, timerId);

    // If the mapping doesn't exist, set it to zero, otherwise increment the value.
    listenerCounts.merge(responseChannel, 1, (k,v) -> v++);
  }

  private long startFakeAlertTimer() {
    long id = vertx.setPeriodic(10000, t -> {
      if (rn.nextBoolean()) {
        JsonObject js = new JsonObject();
        js.put("patient_id", rn.nextInt(5));
        js.put("ts", System.currentTimeMillis());
        js.put("signame", "blue");
        js.put("interval", 2);
        js.put("alert_msg", "It is an alert!");
        js.put("action_msg", "Do something!");
        eb.send("alerts", js);
      }
    });

    return id;
  }

  private long startFakeTimer(String responseChannel) {
    return vertx.setPeriodic(1000, t -> {
      JsonObject json = new JsonObject();
      json.put("x", System.currentTimeMillis());
      json.put("y", rn.nextDouble() * 100);
      eb.send(responseChannel, json);
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
