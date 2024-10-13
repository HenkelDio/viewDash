package com.viewdash.service;

import com.viewdash.document.Chart;
import com.viewdash.document.Department;
import com.viewdash.document.STATUS;
import com.viewdash.document.User;
import com.viewdash.service.repository.ChartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChartService extends AbstractService {

    @Autowired
    ChartRepository chartRepository;

    public ResponseEntity<?> createChart(User principal, Chart chart) {
        logger.info("Creating new chart, user: " + principal.getName());
        chart.setCreatedOn(System.currentTimeMillis());
        chart.setStatus(STATUS.ACTIVE);

        Chart.CreatedBy createdBy = new Chart.CreatedBy();
        createdBy.setDocument(principal.getDocument());
        createdBy.setName(principal.getName());
        chart.setCreatedBy(createdBy);

        try {
            chartRepository.insert(chart);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<List<Chart>> findAllByDepartment(User principal, String status) {
        logger.info("Finding all charts by departments, user: " + principal.getDocument());

        try {
            List<String> departments = principal.getDepartments()
                    .stream()
                    .map(item -> item.getLabel())
                    .collect(Collectors.toList());

            Query query = new Query(Criteria.where("department").in(departments)
                    .and("status").is(status));

            return ResponseEntity.ok(mongoTemplate.find(query, Chart.class));
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<?> changeStatus(String status, String id) {
        logger.info("Update status chart");
        Query query = new Query(Criteria.where("_id").is(id));

        Chart chart = mongoTemplate.findOne(query, Chart.class);
        if(chart == null){
            return ResponseEntity.notFound().build();
        }

        Update update = new Update().set("status",status);
        mongoTemplate.updateFirst(query, update, Chart.class);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Chart> findAllById(User principal, String id) {
        logger.info("Searching chart by id: " + id + " user " + principal.getDocument());

        try {
            Chart chart = mongoTemplate.findById(id, Chart.class);
            return ResponseEntity.ok(chart);
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<?> updateChart(Chart chart, User principal) {
        logger.info("Updating chart " + chart.getTitle() + " user " + principal.getDocument());

        try {
            Query query = new Query(Criteria.where("_id").is(chart.getId()));
            Chart foundChart = mongoTemplate.findOne(query, Chart.class);

            if(foundChart == null){
                return ResponseEntity.notFound().build();
            }

            chartRepository.save(chart);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
