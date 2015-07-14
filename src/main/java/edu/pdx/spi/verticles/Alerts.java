package edu.pdx.spi.verticles;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pdx.spi.GcmContent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;


public class Alerts extends AbstractVerticle {
  public static final String API_KEY = "AIzaSyBe-3d2hooDYXHHQt5Rb8Qo4wn0vRVRMNE";
    String UserKey;
  int port = 9996;
  EventBus eb;

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
    eb = vertx.eventBus();

    System.out.println("running alerts simulation");

    // get registration code from app
    getRegistrationCode();

    // Sends a new alert every 20 seconds
    vertx.setPeriodic(20000, id -> {
      // send message to client at specified token
      System.out.println("Sending GCM Message");

        // create message to send to watch user
        GcmContent content = createContent();

        // send message to user
        sendMessage(content, API_KEY);
    });
  }
  private void getRegistrationCode() {
      String msg;
      try {
          ServerSocket client = new ServerSocket(port);
          // TODO: implement this as a while loop or some construct to constantly listen for new users
          if(true)   {
              Socket connectionSocket = client.accept();
              BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
              DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
              msg = inFromClient.readLine();
              System.out.println("Received: " + msg);
              String resp = msg.toUpperCase() + '\n';
              outToClient.writeBytes("thanks for the registration token!");

              // TODO: reimplement this as a mapping for multiple users
              // save registration token to global var for future use
              UserKey = msg;
          }

      } catch (Exception e) {
          System.out.print("getRegistrationCode ERROR:" + e);
          System.exit(1);
      }

  }

    // creates message content to be sent to watch
    public GcmContent createContent(){

        GcmContent c = new GcmContent();

        c.addRegId(UserKey);
        c.createData("SPI ALERT!", "***WATCH DEMO:This is the message section of the GCM message***");

        return c;
    }
    private void sendMessage(GcmContent content, String apiKey) {
        try{

            // 1. URL
            URL url = new URL("https://android.googleapis.com/gcm/send");

            // 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key="+apiKey);

            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body

            // 5.1 Use Jackson object mapper to convert Contnet object into JSON
            ObjectMapper mapper = new ObjectMapper();

            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            // 5.3 Copy Content "JSON" into
            mapper.writeValue(wr, content);

            // 5.4 Send the request
            wr.flush();

            // 5.5 close
            wr.close();

            // 6. Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. Print result
            System.out.println(response.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
