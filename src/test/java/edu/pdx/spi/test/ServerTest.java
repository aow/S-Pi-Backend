package edu.pdx.spi.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServerTest {
  Vertx vertx;
  HttpClient client;

  @Before
  public void before(TestContext context) {
    vertx = Vertx.vertx();
    client = vertx.createHttpClient();
    //vertx.deployVerticle("edu.pdx.spi.verticles.Server");
  }

  @After
  public void after(TestContext context) {
    vertx.close();
  }

  @Test
  public void testResponse(TestContext context) {
    Async async = context.async();
    //client.getNow(9998, "localhost", "/", resp -> {
    //  resp.bodyHandler(body -> context.assertEquals("Hello!", body.toString()));
    //  client.close();
    //  async.complete();
    //});
    async.complete();


  }
}
