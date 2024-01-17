package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.SlashCommand;

public interface InteractionFactory<T> {

  /**
   * Return a message creator of type T based on a slash-command
   *
   * @param slashCommand The slash command that is invoked
   * @return Message creator of type T
   */
  T messageCreator(SlashCommand slashCommand);
}
