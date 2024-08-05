package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.enums.MessageComponentId;

public interface MessageComponentHandler extends Handler {

  /**
   * Retrieve the componentID this handler is serving
   *
   * @return {@link MessageComponentId}
   */
  MessageComponentId getComponentId();
}
