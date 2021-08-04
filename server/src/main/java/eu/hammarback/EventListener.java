package eu.hammarback;

public interface EventListener<T> {

  void onEvent(T t);

}
