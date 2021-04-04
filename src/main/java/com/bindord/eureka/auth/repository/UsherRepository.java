package com.bindord.eureka.auth.repository;

import com.bindord.eureka.auth.domain.Usher;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsherRepository extends ReactiveMongoRepository<Usher, UUID> {
}
