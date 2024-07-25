package com.deahtstroke.rivenbot.repository;

import com.deahtstroke.rivenbot.entity.UserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDetailsRepository extends ReactiveMongoRepository<UserDetails, Long> {

  Mono<Boolean> existsByUsernameAndUserTag(String username, String userTag);

  Mono<UserDetails> findUserDetailsByUsernameAndUserTag(String username, String userTag);
}
