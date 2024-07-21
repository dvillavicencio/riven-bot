package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.MessageComponentId;

public interface MessageComponentHandler<T> {

  /**
   * Return a message component of type T based on a Message Component ID
   *
   * @param componentId The MessageComponentId of the button
   * @return Type of the button handler
   */
  T getHandler(MessageComponentId componentId);
}
