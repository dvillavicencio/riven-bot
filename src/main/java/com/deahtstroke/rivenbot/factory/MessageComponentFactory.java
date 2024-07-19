package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.handler.MessageComponentSource;
import com.deahtstroke.rivenbot.handler.RaidStatsButtonHandler;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MessageComponentFactory implements MessageComponentHandler<MessageComponentSource> {

  private static final String STATS_COMPREHENSION_ID = "raid_stats_comprehension";

  private final Map<String, MessageComponentSource> componentFactory;

  public MessageComponentFactory(
      RaidStatsButtonHandler raidStatsButtonHandler) {
    componentFactory = Map.of(STATS_COMPREHENSION_ID, raidStatsButtonHandler);
  }

  @Override
  public MessageComponentSource getHandler(String componentId) {
    return componentFactory.get(componentId);
  }
}
