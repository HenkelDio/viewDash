package com.viewdash.service.repository;

import com.viewdash.document.Process;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProcessRepository extends MongoRepository<Process, String> {

    @Query("{'status': ?0}")
    List<Process> findAllByStatus(String status);
}
