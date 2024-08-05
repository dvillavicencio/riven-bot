package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.enums.SlashCommand;

public interface SlashCommandHandler extends Handler {

  /**
   * Retrieve the slash command this handler is for
   *
   * @return {@link SlashCommand}
   */
  SlashCommand getSlashCommand();
}
