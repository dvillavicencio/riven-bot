package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.handler.MessageComponentSource;
import com.danielvm.destiny2bot.handler.RaidStatsButtonHandler;
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
