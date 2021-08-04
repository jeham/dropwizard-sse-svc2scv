package eu.hammarback;

import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.BroadcasterListener;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.UUID;

@Path("events")
public class SseResource implements EventListener<Integer> {

  private final Logger logger = LoggerFactory.getLogger(getClass().getName());
  private final SseBroadcaster broadcaster = new SseBroadcaster();

  private final EventQueue<Integer> eventQueue;

  public SseResource(EventQueue<Integer> eventQueue) {

    this.eventQueue = eventQueue;
    this.eventQueue.register(this);

    this.broadcaster.add(new BroadcasterListener<OutboundEvent>() {
      @Override
      public void onException(ChunkedOutput<OutboundEvent> chunkedOutput, Exception ex) {
        logger.warn("Exception during broadcasting: " + ex);
      }

      @Override
      public void onClose(ChunkedOutput<OutboundEvent> chunkedOutput) {
        logger.info("Output has been closed: " + chunkedOutput);
      }
    });
  }

  @GET
  @Produces(SseFeature.SERVER_SENT_EVENTS)
  public EventOutput getEvents(@HeaderParam(SseFeature.LAST_EVENT_ID_HEADER) @DefaultValue("-1") int lastReceivedId) {

    Integer lastProducedId = eventQueue.head();

    logger.info("lastReceivedId = " + lastReceivedId);
    logger.info("lastProducedId = " + lastProducedId);

    EventOutput eventOutput = new EventOutput();

    if (lastReceivedId < lastProducedId) {
      replayMissedEvents(lastReceivedId, eventOutput);
    }

    // NOTE! This is not bullet-proof as more events might have been emitted after the replay was done but before
    // the eventOutput was registered in the broadcaster.

    if (!broadcaster.add(eventOutput)) {
      throw new ServiceUnavailableException(5L); // 503 -> 5s delayed client reconnect attempt.
    }

    return eventOutput;
  }

  @Override
  public void onEvent(Integer integer) {
    broadcaster.broadcast(newEvent(integer));
  }

  private void replayMissedEvents(int lastReceivedId, EventOutput eventOutput) {
    eventQueue.stream()
        .skip(lastReceivedId + 1)
        .forEach(event -> {
          try {
            OutboundEvent sseEvent = newEvent(event);
            eventOutput.write(sseEvent);

          } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
          }
        });
  }

  private OutboundEvent newEvent(Integer event) {
    return new OutboundEvent.Builder()
        .name("OrderEvent")
        .id(String.valueOf(event))
        .mediaType(MediaType.APPLICATION_JSON_TYPE)
        .data(Map.class, ImmutableMap.of(
            "sequenceNumber", event,
            "orderId", UUID.randomUUID().toString(),
            "customerId", UUID.randomUUID().toString()
        ))
        .build();
  }

}
