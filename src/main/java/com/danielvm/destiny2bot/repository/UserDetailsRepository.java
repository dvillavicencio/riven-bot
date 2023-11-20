package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserDetailsRepository extends MongoRepository<UserDetails, Long> {

    /**
     * Get user details by their DiscordId
     *
     * @param discordId The user's DiscordId
     * @return {@link Optional} of {@link UserDetails}
     */
    Optional<UserDetails> getUserDetailsByDiscordId(String discordId);

    /**
     * Evaluates whether exists an entry by their DiscordId
     *
     * @param discordId The user's DiscordId
     * @return True if the entry exists, else False
     */
    boolean existsByDiscordId(String discordId);
}
