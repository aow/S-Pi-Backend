package edu.pdx.spi.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.pdx.spi.dataproviders.BigDawgDataProvider;
import edu.pdx.spi.dataproviders.DataProvider;
import edu.pdx.spi.dataproviders.SstoreDataProvider;
import edu.pdx.spi.fakedata.PatientStore;
import edu.pdx.spi.fakedata.models.Patient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static edu.pdx.spi.ChannelNames.*;

public final class DataSource extends AbstractVerticle {
  final PatientStore patientData;
  final ObjectMapper om;
  final Random rn;
  EventBus eb;
  boolean SSTORE;
  boolean BD;
  DataProvider dataProvider;

  public DataSource() {
    patientData = new PatientStore();
    om = new ObjectMapper();
    om.enable(SerializationFeature.INDENT_OUTPUT);
    rn = new Random();
  }

  public void start() {
    eb = vertx.eventBus();

    SSTORE = this.config().getBoolean("sstore");
    BD = this.config().getBoolean("bigDawg");
    dataProvider = BD ? new BigDawgDataProvider(this.getVertx(), this.config()) : new SstoreDataProvider(this.getVertx());

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
      dataProvider.startStream(responseChannel, type, id, requestData.getString("ip"));
      m.reply(responseChannel);
    });
  }

  private void registerAlertsRequestEventBusHandler() {
    eb.consumer(ALERTS_REQ, m -> {
      JsonObject req = (JsonObject) m.body();
      String responseChannel = "alerts" + "." + req.getString("id");
      dataProvider.startAlert(responseChannel, req.getString("id"), req.getString("ip"));
      m.reply(responseChannel);
    });
  }

  private void registerTerminateStreamSessionEventBusHandler() {
    eb.consumer(STREAM_END, m -> {
      dataProvider.getCache().removeClient(m.body().toString());
    });
  }
}
