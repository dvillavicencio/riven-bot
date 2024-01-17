package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import com.danielvm.destiny2bot.factory.creator.MessageComponentSource;
import com.danielvm.destiny2bot.factory.creator.RaidDiagramMessageCreator;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MessageComponentFactory {

  private final Map<String, MessageComponentSource> messageFactory;

  public MessageComponentFactory(
      RaidDiagramMessageCreator raidDiagramMessageCreator) {
    this.messageFactory = Map.of(
        "select_raid_encounter", raidDiagramMessageCreator
    );
  }

  public MessageComponentSource messageCreator(String componentId) {
    MessageComponentSource creator = messageFactory.get(componentId);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(componentId));
    }
    return creator;
  }
}
