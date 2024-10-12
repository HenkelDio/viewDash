package com.viewdash.service;

import com.viewdash.document.DTO.PerspectiveDTO;
import com.viewdash.document.Department;
import com.viewdash.document.Perspective;
import com.viewdash.document.User;
import com.viewdash.service.repository.PerspectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService extends AbstractService {

    public static final String PERSPECTIVE = "perspective";
    @Autowired
    PerspectiveRepository perspectiveRepository;

    public ResponseEntity<List<Perspective>> listPerspectives(String status) {
        logger.info("Finding all perspectives by status: " + status);

        try {
            return ResponseEntity.ok(perspectiveRepository.findAllByStatus(status));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Perspective> createPerspective(PerspectiveDTO perspective, User principal) {
        logger.info("Creating new perspective: " + perspective + " by user: " + principal.getDocument());

        Perspective newPerspective = new Perspective();
        newPerspective.setName(perspective.getName());
        newPerspective.setDescriptions(perspective.getDescriptions());
        newPerspective.setCreatedOn(System.currentTimeMillis());
        newPerspective.setStatus(Perspective.STATUS.ACTIVE);

        Perspective.CreatedBy createdBy = new Perspective.CreatedBy();
        createdBy.setDocument(principal.getDocument());
        createdBy.setName(principal.getName());
        newPerspective.setCreatedBy(createdBy);

        try {
            perspectiveRepository.save(newPerspective);
            return ResponseEntity.ok(newPerspective);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Perspective> findPerspectiveById(String id) {
        logger.info("Finding by id");

        try {
            Perspective perspective = perspectiveRepository.findById(id).orElse(null);

            if (perspective == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(perspective);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<?> changeStatus(String status, String id, String type) {
        logger.info("Update status type " + type + " to " + status);


        if(PERSPECTIVE.equals(type)) {
            Optional<Perspective> perspective = perspectiveRepository.findById(id);
            if(perspective.isPresent()) {
                Query query = new Query(Criteria.where("id").is(id));
                Update update = new Update().set("status",status);
                mongoTemplate.updateFirst(query, update, Perspective.class);
                return ResponseEntity.ok().build();
            }
        }


        return ResponseEntity.notFound().build();

    }
}
