package eu.hammarback;

import eu.hammarback.impl.FakeEventProducer;
import eu.hammarback.impl.FakeEventQueue;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class ServerApplication extends Application<Configuration> {

  @Override
  public void run(Configuration configuration, Environment environment) {
    EventProducer<Integer> eventProducer = new FakeEventProducer();
    EventQueue<Integer> eventQueue = new FakeEventQueue();

    SseResource sseResource = new SseResource(eventQueue);
    environment.jersey().register(sseResource);

    eventProducer.produceTo(eventQueue);
  }

  public static void main(String[] args) throws Exception {
    new ServerApplication().run(args);
  }

}
