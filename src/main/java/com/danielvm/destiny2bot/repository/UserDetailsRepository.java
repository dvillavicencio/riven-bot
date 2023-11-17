package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDetailsRepository extends MongoRepository<UserDetails, Long> {

    UserDetails getUserDetailsByDiscordId(String discordId);
}
