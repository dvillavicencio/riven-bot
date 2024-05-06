package com.deahtstroke.rivenbot.repository;

import com.deahtstroke.rivenbot.entity.PGCRDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PGCRRepository extends ReactiveMongoRepository<PGCRDetails, Long> {

}
