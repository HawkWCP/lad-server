package com.lad.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.ExposeBo;
import com.lad.dao.ExposeDao;
import com.lad.service.IExposeService;
import com.mongodb.WriteResult;

import lad.scrapybo.InforBo;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/22
 */
@Service("exposeService")
public class ExposeServiceImpl extends BaseServiceImpl implements IExposeService {

    @Autowired
    private ExposeDao exposeDao;

    @Override
    public ExposeBo insert(ExposeBo exposeBo) {
        return changeImgHost(exposeDao.insert(exposeBo));
    }

    @Override
    public WriteResult updateExpose(String id, Map<String, Object> params) {
        return exposeDao.updateByParam(id, params);
    }

    @Override
    public List<ExposeBo> findByRegex(String title, List<String> exposeTypes, int page, int limit) {
        return changeImgHost(exposeDao.findRegexByPage(title, exposeTypes, page, limit));
    }

    @Override
    public ExposeBo findById(String id) {
        return changeImgHost(exposeDao.findById(id));
    }

    @Override
    public WriteResult deleteById(String id) {
        return exposeDao.deleteById(id);
    }

    @Override
    public List<ExposeBo> findByParams(Map<String, Object> params, int page, int limit) {
        return changeImgHost(exposeDao.findParamsByPage(params, page, limit));
    }

    @Override
    public WriteResult updateCounts(String id, int numType, int num) {
        return exposeDao.updateCounts(id, numType, num);
    }

	@Override
	public void updateVisitNum(String exposeid, int i) {
		exposeDao.updateVisitNum(exposeid,i);
	}

	@Override
	public List<InforBo> findAllInfores() {
        return changeImgHost(exposeDao.findAllInfores());
	}

	@Override
	public WriteResult updateSource(String title, String source) {
		return exposeDao.updateSource(title,source);
	}
}
