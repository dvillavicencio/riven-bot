package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;

public interface SlashCommandHandler<T> {

  /**
   * Return a handler of type T based on a slash-command
   *
   * @param slashCommand The slash command that is invoked
   * @return Message creator of type T
   */
  T getHandler(SlashCommand slashCommand) throws NoSuchHandlerException;
}
