package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDetailsRepositoryRx extends ReactiveMongoRepository<UserDetails, Long> {

  /**
   * Get user details based on the discord id of the current user
   *
   * @param discordId The discordId of the user
   * @return {@link UserDetails}
   */
  Mono<UserDetails> getUserDetailsByDiscordId(String discordId);

  /**
   * Returns whether there exists a database entry with the passed DiscordId
   *
   * @param discordId The discordId to verify
   * @return True if it exists, false otherwise
   */
  Mono<Boolean> existsByDiscordId(String discordId);
}
