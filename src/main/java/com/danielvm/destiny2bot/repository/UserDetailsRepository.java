package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends ReactiveMongoRepository<UserDetails, String> {

}
