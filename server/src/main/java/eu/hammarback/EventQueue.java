package eu.hammarback;

import java.util.stream.Stream;

public interface EventQueue<T> {

  void register(EventListener<T> eventListener);

  void offer(T event);

  T head();

  Stream<T> stream();

}
