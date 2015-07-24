package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class SstoreConnector extends AbstractVerticle {
  Socket socket;
  InputStream inputStream;
  OutputStream outputStream;
  PrintWriter socketWriter;
  BufferedReader socketReader;
  EventBus eb;
  String host;
  int port;

  @Override
  public void start() {
    host = this.config().getString("sstoreClientHost");
    port = this.config().getInteger("sstoreClientPort");
    openSocket(host, port);
    if (!socket.isConnected()) {
      vertx.setPeriodic(1000, h -> {
        openSocket(host, port);
        if (socket.isConnected()) {
          vertx.cancelTimer(h);
        }
      });
    }

    eb = vertx.eventBus();

    eb.consumer("sstore", msg -> {
      JsonObject jmsg = (JsonObject)msg.body();
      JsonObject outMsg;
      long startTime = System.nanoTime();
      if (socket.isConnected()) {
        outMsg = query(jmsg.getString("query"), jmsg.getString("id"));
      } else {
        outMsg = null;
        vertx.setPeriodic(1000, h -> {
          openSocket(host, port);
          if (socket.isConnected()) {
            vertx.cancelTimer(h);
          }
        });
      }
      long endTime = System.nanoTime();
      System.out.println("Query took " + (endTime-startTime) + "seconds.");
      if (Objects.nonNull(outMsg)) {
        eb.publish(jmsg.getString("channel"), outMsg);
      }
    });
  }

  private void openSocket(String hostname, int port) {
    try {
      socket = new Socket(hostname, port);
      System.out.println("Connected to S-Store client.");
    } catch (UnknownHostException e) {
      throw new RuntimeException("Error resolving S-Store client hostname: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("Connection refused to S-Store client. Trying again in 1 second.");
    }

    try {
      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException("Error getting input/output stream from socket: " + e.getMessage());
    }

    socketWriter = new PrintWriter(outputStream, true);
    socketReader = new BufferedReader(new InputStreamReader(inputStream));
  }


  private JsonObject query(String type, String userId) {
    JsonObject resp = new JsonObject();
    String queryText;
    // Figure out which query to run.
    //TODO: Fix this for more query types once they are added.
    if (!type.equals("alert")) {
      // Handle a waveform request.
      //TODO: What proc should we be calling here?
      //TODO: Fix these constants once the extra data is there.
      JsonArray args = new JsonArray().add(type).add(Integer.valueOf(userId));
      queryText = new JsonObject().put("proc", "GetSingleWaveform").put("args", args).encode();
    } else {
      // Handle alerts
      JsonArray args = new JsonArray().add(Integer.valueOf(userId));
      queryText = new JsonObject().put("proc", "GetRecentAlerts").put("args", args).encode();
    }
    socketWriter.println(queryText);
    System.out.println("Sent s-store query.");

    try {
      String response = socketReader.readLine();
      if (!type.equals("alert")) {
        JsonArray unsorted = new JsonObject(response).getJsonArray("data");
        // Sort the timestamps. Temp solution until s-store returns the data presorted.
        Collections.sort(unsorted.getList(), comp);
        resp = new JsonObject(response).put("data", unsorted);
      } else {
        resp = new JsonObject(response);
      }
    } catch (IOException e) {
      System.out.println("Error reading from socket: " + e.getMessage());
    }

    return resp;
  }
  Comparator<LinkedHashMap<String, Object>> comp = (o1, o2) -> Integer.compare((Integer)o1.get("TS"), (Integer)o2.get("TS"));
}
