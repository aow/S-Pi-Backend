package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
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

  @Override
  public void start() {
    try {
      socket = new Socket("localhost", 6000);
      System.out.println("Connected to S-Store client.");
    } catch (UnknownHostException e) {
      throw new RuntimeException("Error resolving S-Store client hostname: " + e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException("Error with initializing socket I/O: " + e.getMessage());
    }

    try {
      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException("Error getting input/output stream from socket: " + e.getMessage());
    }

    socketWriter = new PrintWriter(outputStream, true);
    socketReader = new BufferedReader(new InputStreamReader(inputStream));

    eb = vertx.eventBus();

    eb.consumer("sstore", msg -> {
      JsonObject jmsg = (JsonObject)msg.body();

      eb.send(jmsg.getString("channel"), query(jmsg.getString("query"), jmsg.getString("id")));
    });
  }

  private JsonObject query(String type, String userId) {
    JsonObject resp = new JsonObject();
    String queryText;
    if (!type.equals("alert")) {
      queryText = new JsonObject().put("proc", "GetData").put("args", new JsonArray()).encode();
    } else {
      JsonArray args = new JsonArray().add(Integer.valueOf(userId));
      queryText = new JsonObject().put("proc", "GetRecentAlerts").put("args", args).encode();
    }
    System.out.println("Sending: " + queryText);
    socketWriter.println(queryText);
    System.out.println("Sent query.");

    try {
      String response = socketReader.readLine();
      System.out.println(response);
      if (!type.equals("alert")) {
        JsonArray unsorted = new JsonObject(response).getJsonArray("data");
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
