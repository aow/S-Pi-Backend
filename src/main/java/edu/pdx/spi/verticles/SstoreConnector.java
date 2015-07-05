package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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
      JsonObject jmsg = new JsonObject().mergeIn((JsonObject)msg.body());

      eb.send(jmsg.getString("channel"), query());
    });
  }

  private JsonObject query() {
    JsonObject resp = new JsonObject();
    String queryText = new JsonObject().put("proc", "GetData").put("args", new JsonArray()).encode();
    System.out.println("Sending: " + queryText);
    socketWriter.println(queryText);
    System.out.println("Sent query.");

    try {
      String response = socketReader.readLine();
      resp = new JsonObject(response);
    } catch (IOException e) {
      System.out.println("Error reading from socket: " + e.getMessage());
    }

    return resp;
  }
}
