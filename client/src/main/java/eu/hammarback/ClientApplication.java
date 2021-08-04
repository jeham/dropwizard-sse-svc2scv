package eu.hammarback;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Map;

public class ClientApplication extends Application<Configuration> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void run(Configuration configuration, Environment environment) {

    environment.jersey().disable();

    environment.lifecycle().executorService("sse-listener").build().submit(
        () -> consumePush(URI.create("http://localhost:8080/events"))
    );

  }

  private void consumePush(URI uri) {
    Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
    WebTarget target = client.target(uri);
    EventSource eventSource = EventSource.target(target).build();

    EventListener eventListener = inboundEvent -> {
      String eventName = inboundEvent.getName();
      Map map = inboundEvent.readData(Map.class);
      logger.info("Got event [{}]: {}", eventName, map);
    };

    eventSource.register(eventListener, "OrderEvent");
    eventSource.open();

    logger.info("Listening...");

  }

  public static void main(String[] args) throws Exception {
    new ClientApplication().run(args);
  }

}
