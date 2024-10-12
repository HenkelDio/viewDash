package com.viewdash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Qualifier("mongoTemplate")
    @Autowired
    protected  MongoTemplate mongoTemplate;


}
