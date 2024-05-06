package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.handler.MessageComponentSource;
import com.deahtstroke.rivenbot.handler.RaidStatsButtonHandler;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MessageComponentFactory implements MessageComponentHandler<MessageComponentSource> {

  private final Map<String, MessageComponentSource> messageComponentFactory;

  public MessageComponentFactory(
      RaidStatsButtonHandler raidStatsButtonHandler) {
    messageComponentFactory = Map.of("raid_stats_comprehension", raidStatsButtonHandler);
  }

  @Override
  public MessageComponentSource handle(String componentId) {
    return messageComponentFactory.get(componentId);
  }
}
