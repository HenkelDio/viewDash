package com.viewdash.service;

import com.viewdash.document.Answer;
import com.viewdash.document.Classification;
import com.viewdash.document.Department;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassificationService extends AbstractService {

    public ResponseEntity create(Classification classification) {
        logger.info("Creating classification {}", classification.getDescription());

        try {
            classification.setStatus("ACTIVE");
            mongoTemplate.save(classification);
            return new ResponseEntity<>(classification, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    public ResponseEntity update(Classification classification) {
        logger.info("Updating classification {}", classification.getDescription());

        try {
            Query query = new Query(Criteria.where("_id").is(classification.getId()));

            Update update = new Update().set("description", classification.getDescription());

            if(classification.getDepartment().getId() != null) {
                update.set("department.id", classification.getDepartment().getId());
            }

            if(classification.getColor() != null) {
                update.set("color", classification.getColor());
            }

            mongoTemplate.updateFirst(query, update, Classification.class);
            return new ResponseEntity<>(classification, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<List<Classification>> list(String status) {
        logger.info("Listing classifications");

        try {
            Query query = new Query(Criteria.where("status").is(status));
            List<Classification> classifications = mongoTemplate.find(query, Classification.class);
            for (Classification classification : classifications) {
                if (classification.getDepartment() != null && classification.getDepartment().getId() != null) {
                    Department department = mongoTemplate.findById(
                            classification.getDepartment().getId(), Department.class
                    );
                    classification.setDepartment(department);
                }
            }

            return new ResponseEntity<>(classifications, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private Department getDepartmentById(Department department) {
        Query query = new Query(Criteria.where("_id").is(department.getId()));
        return mongoTemplate.findOne(query, Department.class);
    }

    public ResponseEntity changeStatus(String id, String status) {
        try {
            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update().set("status", status);
            mongoTemplate.updateFirst(query, update, Classification.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity saveAnswerClassification(Map<String, String> body) {
        try {
            Query query = new Query(Criteria.where("_id").is(body.get("answerId")));
            Update update = new Update().set("classification._id", body.get("classificationId"));
            mongoTemplate.updateFirst(query, update, Answer.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
