package edu.pdx.spi.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import static edu.pdx.spi.ChannelNames.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
  boolean reconnecting;

  @Override
  public void start() {
    eb = vertx.eventBus();
    host = this.config().getString("sstoreClientHost");
    port = this.config().getInteger("sstoreClientPort");
    reconnecting = true;
    openSocket(host, port);
    retryOpen();

    eb.consumer("sstore", msg -> {
      JsonObject jmsg = (JsonObject)msg.body();
      String queryType = jmsg.getString("query");
      Optional<JsonObject> outMsg;
      //long startTime = System.nanoTime();
      outMsg = !reconnecting ? query(jmsg.getString("query"), jmsg.getString("id")) : Optional.empty();
      //long endTime = System.nanoTime();
      //System.out.println("Query took " + (endTime-startTime)/1000000 + " ms.");
      if (outMsg.isPresent() && !queryType.equals("alert")) {
        // Send to specific channels for web.
        eb.publish(jmsg.getString("channel"), outMsg.get());
      } else if (outMsg.isPresent()) {
        eb.publish(jmsg.getString("channel"), outMsg.get());
        eb.publish(ALL_ALERTS, outMsg.get());
      }
    });
  }

  private void openSocket(String hostname, int port) {
    try {
      if (Objects.nonNull(socket)) {
        socket.close();
      }
      socket = new Socket(hostname, port);
      socket.setSoTimeout(5000);
      System.out.println("Connected to S-Store client.");
      reconnecting = false;
      eb.publish(STREAM_RESTART, System.currentTimeMillis());
    } catch (UnknownHostException e) {
      throw new RuntimeException("Error resolving S-Store client hostname: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("Connection refused to S-Store client. Trying again in 1 second.");
      reconnecting = false;
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

  private void retryOpen() {
    if (!socket.isConnected() && !reconnecting) {
      reconnecting = true;
      vertx.setPeriodic(1000, h -> {
        openSocket(host, port);
        if (socket.isConnected()) {
          vertx.cancelTimer(h);
        }
      });
    }
  }

  private Optional<JsonObject> query(String type, String userId) {
    JsonObject resp;
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

    try {
      JsonObject response = new JsonObject(socketReader.readLine());
      if (!type.equals("alert")) {
        // Sort the timestamps. Temp solution until s-store returns the data presorted.
        Collections.sort(response.getJsonArray("data").getList(), comp);
        resp = response;
      } else {
        resp = response.getJsonArray("data").getList().isEmpty() ? null : response;
      }
    } catch (IOException e) {
      reconnecting = true;
      openSocket(host, port);
      retryOpen();
      System.out.println("Error reading from socket: " + e.getMessage());
      resp = null;
    } catch (ClassCastException e) {
      resp = null;
    }
    catch (Exception e) {
      reconnecting = true;
      openSocket(host, port);
      retryOpen();
      System.out.println("Some error: " + e);
      resp = null;
    }

    return Optional.ofNullable(resp);
  }
  Comparator<LinkedHashMap<String, Object>> comp = (o1, o2) -> Integer.compare((Integer)o1.get("TS"), (Integer)o2.get("TS"));
}
