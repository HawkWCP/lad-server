package com.lad.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository("crcularDao")
public class CrcularDaoImpl {
    @Autowired
    private MongoTemplate mongoTemplate;
}
