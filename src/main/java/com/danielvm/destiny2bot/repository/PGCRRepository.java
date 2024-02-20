package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.PGCRDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PGCRRepository extends ReactiveMongoRepository<PGCRDetails, Long> {

}
