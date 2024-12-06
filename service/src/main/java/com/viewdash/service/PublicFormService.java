package com.viewdash.service;

import com.viewdash.document.Form;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PublicFormService extends AbstractService {

    public ResponseEntity<Form> getForm() {
        logger.info("Getting form");
        return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class));
    }

}
