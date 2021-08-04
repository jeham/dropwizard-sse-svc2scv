package eu.hammarback.impl;

import eu.hammarback.EventProducer;
import eu.hammarback.EventQueue;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeEventProducer implements EventProducer<Integer> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final AtomicInteger counter = new AtomicInteger();

  @Override
  public void produceTo(EventQueue<Integer> eventQueue) {
    new Thread(() -> {
      logger.info("Starting producer thread...");

      while (true) {
        try {
          Thread.sleep(Duration.ofSeconds(RandomUtils.nextInt(2, 10)).toMillis());
          int cnt = counter.incrementAndGet();
          eventQueue.offer(cnt);
          logger.info("Event {} produced", cnt);

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

}
