package com.viewdash.service;

import com.viewdash.document.Department;
import com.viewdash.service.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.util.List;

@Service
public class DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    @Autowired
    DepartmentRepository departmentRepository;

    @Qualifier("mongoTemplate")
    @Autowired
    private MongoTemplate mongoTemplate;

    public ResponseEntity<?> createDepartment(Department department) {
        logger.info("Creating department {}", department);
        department.setStatus("ACTIVE");

        try {
            departmentRepository.insert(department);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<List<Department>> findAllDepartments(String status) {
        logger.info("Finding all departments");
        List<Department> departments = departmentRepository.findAllByStatus(status);
        return ResponseEntity.ok().body(departments);
    }

    public ResponseEntity<?> changeStatusDepartment(String status, String name) {
        logger.info("Update status department");
        Query query = new Query(Criteria.where("name").is(name));

        Department department = mongoTemplate.find(query, Department.class).getFirst();
        if(department == null){
            return ResponseEntity.notFound().build();
        }

        Update update = new Update().set("status",status);
        mongoTemplate.updateFirst(query, update, Department.class);
        return ResponseEntity.ok().build();
    }
}
