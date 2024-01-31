package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.BotUser;
import reactor.core.publisher.Mono;

public interface BotUserRepository {

  /**
   * Retrieve a {@link BotUser} by a DiscordId
   *
   * @param discordId The DiscordId to find by
   * @return {@link BotUser}
   */
  Mono<BotUser> findBotUserByDiscordId(Long discordId);

  /**
   * Save a {@link BotUser} to the corresponding table
   *
   * @param user The {@link BotUser} to save
   * @return The saved {@link BotUser}
   */
  Mono<BotUser> save(BotUser user);
}
