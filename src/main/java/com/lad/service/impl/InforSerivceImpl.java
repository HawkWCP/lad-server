package com.lad.service.impl;

import com.lad.bo.InforReadNumBo;
import com.lad.bo.InforSubscriptionBo;
import com.lad.dao.IInforDao;
import com.lad.dao.IInforReadNumDao;
import com.lad.dao.IInforSubDao;
import com.lad.scrapybo.InforBo;
import com.lad.service.IInforService;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/5
 */
@Service("inforSerivce")
public class InforSerivceImpl implements IInforService {

    @Autowired
    private IInforReadNumDao inforReadNumDao;

    @Autowired
    private IInforSubDao inforSubDao;

    @Autowired
    private IInforDao inforDao;

    @Override
    public List<InforBo> findAllGroups() {
        return inforDao.selectAllInfos();
    }

    @Override
    public List<InforBo> findGroupInfos(String module) {
        return null;
    }

    @Override
    public List<InforBo> findGroupClass(String className) {
        return null;
    }

    @Override
    public List<InforBo> findClassInfos(String className, String createTime, int limit) {
        return inforDao.findByList(className, createTime, limit);
    }

    @Override
    public InforBo findById(String id) {
        return inforDao.findById(id);
    }

    @Override
    public InforSubscriptionBo findMySubs(String userid) {
        return inforSubDao.findByUserid(userid);
    }

    @Override
    public void subscriptionGroup(String groupName, boolean isAdd) {

    }

    @Override
    public List<InforSubscriptionBo> findMyCollects(String userid) {
        return null;
    }

    @Override
    public void collectInfor(String id, boolean isAdd) {

    }

    @Override
    public Long findReadNum(String inforid) {
        InforReadNumBo readNumBo = inforReadNumDao.findByInforid(inforid);
        if (readNumBo != null) {
            return readNumBo.getVisitNum();
        }
        return 0L;
    }

    public InforReadNumBo findReadByid(String inforid){
       return inforReadNumDao.findByInforid(inforid);
    }

    @Override
    public InforSubscriptionBo insertSub(InforSubscriptionBo inforSubscriptionBo) {
        return inforSubDao.insert(inforSubscriptionBo);
    }

    @Override
    public WriteResult updateSub(String userid, LinkedList<String> subscriptions) {
        return inforSubDao.updateSub(userid, subscriptions);
    }

    @Override
    public WriteResult updateCollect(String userid, LinkedHashSet<String> collects) {
        return inforSubDao.updateCollect(userid, collects);
    }

    @Override
    public InforSubscriptionBo findByUserid(String userid) {
        return null;
    }

    @Override
    public InforReadNumBo addReadNum(InforReadNumBo readNumBo) {
        return inforReadNumDao.insert(readNumBo);
    }

    @Override
    public void updateReadNum(String inforid) {
        inforReadNumDao.update(inforid);
    }
}
