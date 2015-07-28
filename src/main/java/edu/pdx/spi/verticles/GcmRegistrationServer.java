package edu.pdx.spi.verticles;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class GcmRegistrationServer extends AbstractVerticle {
    int port = 9996;
    EventBus eb;

    @Override
    public void start() {
        eb = vertx.eventBus();

        System.out.println("Google Cloud listening server starting");

        // create listening server
        NetServer gcmServer = vertx.createNetServer();

        // handle incoming GCM Registration tokens from clients
        gcmServer.connectHandler(sock -> sock.handler(buffer -> {
            // save as string and close socket
            String regToken = buffer.getString(0, buffer.length());
            sock.close();

            // send registration token to alerts verticle
            eb.send("newGcmToken", regToken);

        })).listen(port, "0.0.0.0");


    }
}
