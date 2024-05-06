package com.deahtstroke.rivenbot.repository;

import com.deahtstroke.rivenbot.entity.UserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends ReactiveMongoRepository<UserDetails, String> {

}
