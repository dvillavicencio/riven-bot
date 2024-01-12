package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AuthorizedMessageFactory {

  private final Map<CommandEnum, AuthorizedMessage> autocompleteMessageFactory;

  public AuthorizedMessageFactory(
      UserCharacterMessageCreator userCharacterMessageCreator) {
    this.autocompleteMessageFactory = Map.of(
        CommandEnum.RAID_STATS, userCharacterMessageCreator
    );
  }

  /**
   * Return the corresponding message-creator associated with a Command in {@link CommandEnum}
   *
   * @param command The command to get the factory for
   * @return an implementation of {@link MessageResponse}
   * @throws ResourceNotFoundException If no creator is found for the given command
   */
  public AuthorizedMessage messageCreator(CommandEnum command) {
    AuthorizedMessage creator = autocompleteMessageFactory.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }

}
