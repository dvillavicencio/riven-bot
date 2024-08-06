package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.enums.SlashCommand;

/**
 * Implementation of this interface are responsible for responding with autocomplete responses
 */
public interface AutocompleteHandler extends Handler {

  /**
   * Get the slash command this autocomplete handler serves for
   *
   * @return {@link SlashCommand}
   */
  SlashCommand getSlashCommand();
}
