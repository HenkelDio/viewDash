package com.viewdash.service;

import com.viewdash.document.DTO.PerspectiveDTO;
import com.viewdash.document.Perspective;
import com.viewdash.document.Process;
import com.viewdash.document.User;
import com.viewdash.service.repository.PerspectiveRepository;
import com.viewdash.service.repository.ProcessRepository;
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
    public static final String PROCESS = "process";

    @Autowired
    PerspectiveRepository perspectiveRepository;

    @Autowired
    ProcessRepository processRepository;

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

        if(PROCESS.equals(type)) {
            Optional<Process> process = processRepository.findById(id);
            if(process.isPresent()) {
                Query query = new Query(Criteria.where("id").is(id));
                Update update = new Update().set("status",status);
                mongoTemplate.updateFirst(query, update, Process.class);
                return ResponseEntity.ok().build();
            }
        }


        return ResponseEntity.notFound().build();

    }

    public ResponseEntity<Process> createProcess(String name, User principal) {
        logger.info("Creating new process, by user: " + principal.getDocument());

        Process process = new Process();
        process.setName(name);
        process.setCreatedOn(System.currentTimeMillis());
        process.setStatus(Process.STATUS.ACTIVE);

        Process.CreatedBy createdBy = new Process.CreatedBy();
        createdBy.setDocument(principal.getDocument());
        createdBy.setName(principal.getName());
        process.setCreatedBy(createdBy);

        try {
            processRepository.save(process);
            return ResponseEntity.ok(process);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<List<Process>> listProcesses(String status) {
        logger.info("Finding all processes by status: " + status);

        try {
            return ResponseEntity.ok(processRepository.findAllByStatus(status));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Process> findProcessById(String id) {
        logger.info("Finding process by id");

        try {
            Process process = processRepository.findById(id).orElse(null);

            if (process == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(process);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Process> updateProcess(String name, String id, User principal) {
        logger.info("Updating process, user " + principal.getDocument());

        try {
            Query query = new Query(Criteria.where("_id").is(id));

            Process process = mongoTemplate.findOne(query, Process.class);
            if (process == null) {
                return ResponseEntity.notFound().build();
            }

            Update update = new Update().set("name", name);
            mongoTemplate.updateFirst(query, update, Process.class);
            return ResponseEntity.ok(process);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
