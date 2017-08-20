package com.lad.dao.impl;

import com.lad.bo.InforSubscriptionBo;
import com.lad.dao.IInforSubDao;
import com.lad.util.Constant;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/5
 */
@Repository("inforSubDao")
public class InforSubDaoImpl implements IInforSubDao {
    
    @Autowired
    MongoTemplate mongoTemplate;


    public InforSubscriptionBo insert(InforSubscriptionBo inforSubscriptionBo){
        mongoTemplate.insert(inforSubscriptionBo);
        return inforSubscriptionBo;
    }

    public WriteResult updateSub(String userid, LinkedList<String> subscriptions){
        Query query = new Query();
        query.addCriteria(new Criteria("userid").is(userid));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        Update update = new Update();
        update.set("subscriptions", subscriptions);
        return mongoTemplate.updateFirst(query, update, InforSubscriptionBo.class);
    }

    public WriteResult updateCollect(String userid, LinkedHashSet<String> collects){
        Query query = new Query();
        query.addCriteria(new Criteria("userid").is(userid));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        Update update = new Update();
        update.set("collects", collects);
        return mongoTemplate.updateFirst(query, update, InforSubscriptionBo.class);
    }

    public InforSubscriptionBo findByUserid(String userid){
        Query query = new Query();
        query.addCriteria(new Criteria("userid").is(userid));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        return mongoTemplate.findOne(query, InforSubscriptionBo.class);
    }

    

}