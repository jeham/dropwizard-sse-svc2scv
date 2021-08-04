package eu.hammarback;

public interface EventProducer<T> {

  void produceTo(EventQueue<T> eventQueue);

}
