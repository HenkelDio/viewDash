package com.viewdash.service;

import com.viewdash.document.Department;
import com.viewdash.service.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    @Autowired
    DepartmentRepository departmentRepository;

    public ResponseEntity<?> createDepartment(Department department) {
        logger.info("Creating department {}", department);

        try {
            departmentRepository.insert(department);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<List<Department>> findAllDepartments() {
        logger.info("Finding all departments");
        List<Department> departments = departmentRepository.findAll();
        return ResponseEntity.ok().body(departments);
    }
}
