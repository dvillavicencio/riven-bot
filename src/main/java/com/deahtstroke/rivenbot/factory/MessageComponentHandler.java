package com.deahtstroke.rivenbot.factory;

public interface MessageComponentHandler<T> {

  /**
   * Return a message component of type T based on a componentId
   *
   * @param componentId The componentId of the button
   * @return Type of the button handler
   */
  T handle(String componentId);
}
