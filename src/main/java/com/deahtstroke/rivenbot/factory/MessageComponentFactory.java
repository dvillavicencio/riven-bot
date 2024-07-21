package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.MessageComponentId;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;
import com.deahtstroke.rivenbot.handler.MessageComponentSource;
import com.deahtstroke.rivenbot.handler.RaidStatsButtonHandler;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MessageComponentFactory implements MessageComponentHandler<MessageComponentSource> {

  private final Map<MessageComponentId, MessageComponentSource> componentFactory;

  public MessageComponentFactory(
      RaidStatsButtonHandler raidStatsButtonHandler) {
    componentFactory = Map.of(MessageComponentId.RAID_STATS_COMPREHENSION, raidStatsButtonHandler);
  }

  @Override
  public MessageComponentSource getHandler(MessageComponentId componentId) {
    MessageComponentSource handler = componentFactory.get(componentId);
    if (Objects.isNull(handler)) {
      throw new NoSuchHandlerException(
          "No handler found for component [%s] with ID [%s]".formatted(componentId,
              componentId.getId()));
    }
    return handler;
  }
}
