package edu.pdx.spi.verticles;


import edu.pdx.spi.handlers.StreamingHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;

public class GcmRegistrationServer extends AbstractVerticle {
    public static final String API_KEY = "AIzaSyBe-3d2hooDYXHHQt5Rb8Qo4wn0vRVRMNE";
    String UserKey;
    int port = 9996;
    EventBus eb;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        eb = vertx.eventBus();

        System.out.println("Google Cloud listening server starting");

        // create listening server
        NetServer gcmServer = vertx.createNetServer();

        // handle incoming data from clients
        gcmServer.connectHandler(new Handler<NetSocket>() {
            @Override
            public void handle(NetSocket sock) {
                sock.handler(new Handler<Buffer>(){
                    @Override
                    public void handle(Buffer buffer) {
                        String regToken = buffer.getString(0, buffer.length());
                        System.out.println(regToken);
                        sock.close();

                        // send registration token to alerts verticle
                        eb.send("newGcmToken", regToken);

                    }
                });
            }
        }).listen(port, "0.0.0.0");


    }
}
