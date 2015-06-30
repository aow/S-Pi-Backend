package edu.pdx.spi.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.fakedata.models.Patient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
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
  Map<String, ArrayList<Long>> activeClientTimers;

  public void start() {
    rn = new Random();
    eb = vertx.eventBus();
    activeClientTimers = new HashMap<>();

    eb.consumer("patients", m -> {
      if (((String)m.body()).isEmpty()) {
        try {
          m.reply(om.writeValueAsString(patientData.getAllPatients()));
        } catch (Exception e) {
          m.reply("error parsing patient data");
        }
      } else {
        Patient p = patientData.getPatient(Integer.parseInt((String)m.body()));
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

    eb.consumer("numericalrequest", m -> {
      JsonObject js = new JsonObject((String)m.body());
      String type = js.getString("type");
      String id = js.getString("id");
      String responseChannel = type + "." + id + UUID.randomUUID();

      startPeriodicNumericalQuery(type, responseChannel, js.getString("remoteclient"));
      m.reply(responseChannel);
    });

    eb.consumer("waveformrequest", m -> {
      JsonObject js = new JsonObject((String)m.body());
      String type = js.getString("type");
      String id = js.getString("id");
      String responseChannel = type + "." + id + UUID.randomUUID();

      startPeriodicWaveformQuery(type, responseChannel, js.getString("remoteclient"));
      m.reply(responseChannel);
    });

    eb.consumer("unsub", m -> {
      ArrayList<Long> ids = activeClientTimers.get(m.body());

      for (Long id : ids) {
        vertx.cancelTimer(id);
      }

      activeClientTimers.remove(m.body());
    });
  }

  private void startPeriodicWaveformQuery(String queryType, String responseChannel, String remoteClient) {
    long id = 0L;
    switch (queryType) {
      case "hr":
        id = vertx.setPeriodic(1000, t -> {
          JsonObject json = new JsonObject();
          json.put("x", System.currentTimeMillis() / 1000L);
          json.put("y", rn.nextInt(200));
          eb.send(responseChannel, json.encode());
        });
        break;
      case "bp":
        id = vertx.setPeriodic(1000, t -> {
          JsonObject json = new JsonObject();
          json.put("x", System.currentTimeMillis() / 1000L);
          json.put("y", rn.nextInt(200));
          eb.send(responseChannel, json.encode());
        });
        break;
    }

    ArrayList<Long> ids = activeClientTimers.get(remoteClient);
    if (ids != null) {
      ids.add(id);
    } else {
      ids = new ArrayList<>();
      ids.add(id);
      activeClientTimers.put(remoteClient, ids);
    }
  }

  private void startPeriodicNumericalQuery(String queryType, String responseChannel, String remoteClient) {
    long id = 0L;
    switch (queryType) {
      case "hr":
        id = vertx.setPeriodic(1000, t -> {
          JsonObject json = new JsonObject();
          json.put("x", System.currentTimeMillis() / 1000L);
          json.put("y", rn.nextInt(200));
          eb.send(responseChannel, json.encode());
          System.out.println("Fired a timer: " + responseChannel);
        });
        break;
      case "bp":
        id = vertx.setPeriodic(1000, t -> {
          JsonObject json = new JsonObject();
          json.put("x", System.currentTimeMillis() / 1000L);
          json.put("y", rn.nextInt(200));
          eb.send(responseChannel, json.encode());
        });
        break;
    }

    ArrayList<Long> ids = activeClientTimers.get(remoteClient);
    if (ids != null) {
      ids.add(id);
    } else {
      ids = new ArrayList<>();
      ids.add(id);
      activeClientTimers.put(remoteClient, ids);
    }
  }
}
