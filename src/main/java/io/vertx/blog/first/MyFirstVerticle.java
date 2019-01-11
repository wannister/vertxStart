package io.vertx.blog.first;

import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.BodyHandler;

public class MyFirstVerticle extends AbstractVerticle {

  // Store our product
  private Map<Integer, Whisky> products = new LinkedHashMap<>();

  // Create some product
  private void createSomeData() {
    Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
    products.put(bowmore.getId(), bowmore);
    Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
    products.put(talisker.getId(), talisker);
  }

  @Override
  public void start(Future<Void> fut) {

    createSomeData();

    // Create a router object.
    Router router = Router.router(vertx);

    // Bind "/" to our hello message - so we are still compatible.
    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
    });
    router.route("/assets/*").handler(StaticHandler.create("assets"));

    router.get("/api/whiskies").handler(this::getAll);

    router.route("/api/whiskies*").handler(BodyHandler.create());
    router.post("/api/whiskies").handler(this::addOne);
    router.delete("/api/whiskies/:id").handler(this::deleteOne);


    vertx.createHttpServer().requestHandler(router::accept).listen(
        // Retrieve the port from the configuration,
        // default to 8080.
        config().getInteger("http.port", 8080), result -> {
          if (result.succeeded()) {
            fut.complete();
          } else {
            fut.fail(result.cause());
          }
        });
  }

  private void getAll(RoutingContext routingContext) {
    routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(products.values()));
  }

  private void addOne(RoutingContext routingContext) {
    final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
        Whisky.class);
    products.put(whisky.getId(), whisky);
    routingContext.response()
        .setStatusCode(201)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(whisky));
  }
  private void deleteOne(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      Integer idAsInteger = Integer.valueOf(id);
      products.remove(idAsInteger);
    }
    routingContext.response().setStatusCode(204).end();
  }
}