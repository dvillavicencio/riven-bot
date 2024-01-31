package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserCharacter;
import reactor.core.publisher.Mono;

public interface UserCharacterRepository {

  /**
   * Save a single user character to the corresponding table
   *
   * @param userCharacter The {@link UserCharacter} object to save
   * @return The saved {@link UserCharacter}
   */
  Mono<UserCharacter> save(UserCharacter userCharacter);

}
