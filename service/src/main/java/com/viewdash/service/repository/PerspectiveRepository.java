package com.viewdash.service.repository;

import com.viewdash.document.Perspective;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PerspectiveRepository extends MongoRepository<Perspective, String> {

    @Query("{'status': ?0}")
    List<Perspective> findAllByStatus(String status);

}
