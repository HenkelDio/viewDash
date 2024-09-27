package com.viewdash.service.repository;

import com.viewdash.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByDocument(String document);

    Optional<User> findByEmail(String email);

    @Query("{'document': ?0}")
    @Transactional
    void updateUserByDocument(String document, @Param("user") User user);
}
