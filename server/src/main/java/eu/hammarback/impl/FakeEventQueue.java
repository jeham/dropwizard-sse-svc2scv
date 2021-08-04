package eu.hammarback.impl;

import com.google.common.collect.Iterables;
import eu.hammarback.EventListener;
import eu.hammarback.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FakeEventQueue implements EventQueue<Integer> {

  private final List<EventListener<Integer>> eventListeners = new ArrayList<>();

  private final List<Integer> events = new ArrayList<>();

  @Override
  public void register(EventListener<Integer> eventListener) {
    eventListeners.add(eventListener);
  }

  @Override
  public void offer(Integer event) {
    events.add(event);
    eventListeners.forEach(e -> e.onEvent(event));
  }

  @Override
  public Integer head() {
    return Optional.ofNullable(Iterables.getLast(events, -1)).orElse(-1);
  }

  @Override
  public Stream<Integer> stream() {
    return events.stream();
  }

}
