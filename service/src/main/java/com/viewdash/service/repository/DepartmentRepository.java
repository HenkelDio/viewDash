package com.viewdash.service.repository;

import com.viewdash.document.Department;
import com.viewdash.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DepartmentRepository extends MongoRepository<Department, String> {

    @Query("{'status': ?0}")
    List<Department> findAllByStatus(String status);
}
