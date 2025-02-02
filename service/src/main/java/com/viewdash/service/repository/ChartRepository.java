package com.viewdash.service.repository;

import com.viewdash.document.DepartmentChart;
import com.viewdash.document.Process;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChartRepository extends MongoRepository<DepartmentChart, String> {

    @Query("{'department': ?0}")
    List<Process> findAllByDepartment(String department);
}
