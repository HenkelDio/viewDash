package com.viewdash.service.repository;

import com.viewdash.document.Form;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FormRepository extends MongoRepository<Form, String> {
}
