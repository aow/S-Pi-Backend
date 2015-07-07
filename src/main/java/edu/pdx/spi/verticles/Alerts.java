package edu.pdx.spi.verticles;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Alerts extends AbstractVerticle {
  public static final String API_KEY = "AIzaSyBe-3d2hooDYXHHQt5Rb8Qo4wn0vRVRMNE";
  EventBus eb;

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
    eb = vertx.eventBus();

    System.out.println("running alerts simulation");

    // retrieve and save registration token
    // TODO: send registration token from mobile if we want to keep track of users

    // send message to client at specified token
    sendMessage( "ALERT:watch GCM testing message for S-PI", "/topics/global");
  }

  private void sendMessage(String toSend, String destination) {
    try {
      // Prepare JSON containing the GCM message content. What to send and where to send.
      JSONObject jGcmData = new JSONObject();
      JSONObject jData = new JSONObject();

        try {
            // message to send to watch
            jData.put("message", toSend);

            // send GCM message to destination passed in by arg
            jGcmData.put("to", destination);
            //  jGcmData.put("to", "/topics/global");

            // add data from message json obj to gcm data obj (correct formatting for GCM)
            jGcmData.put("data", jData);
        } catch (JSONException e) {
            System.out.println("JSON exception occurred," + e);
            System.out.println("check that JSON func use is correct");
        }

      // Create connection to send GCM Message request.
      URL url = new URL("https://android.googleapis.com/gcm/send");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Authorization", "key=" + API_KEY);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);

      // Send GCM message to Watch
      OutputStream outputStream = conn.getOutputStream();
      outputStream.write(jGcmData.toString().getBytes());

      // Read GCM response.
      InputStream inputStream = conn.getInputStream();
      String resp = IOUtils.toString(inputStream);
      System.out.println(resp);
      System.out.println("message above ^^ is from Watch, if correct, GCM is working correctly");
    }
    // catch errors in GCM process
    catch (IOException e) {
        System.out.println("Unable to send GCM message.");
        System.out.println("check that the device's registration token is correct.");
        e.printStackTrace();
    }
  }
}
