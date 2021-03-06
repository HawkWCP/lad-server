package com.lad.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.lad.bo.ShowBo;
import com.lad.constants.DiscoveryConstants;
import com.lad.dao.impl.ShowDao;
import com.lad.service.IShowService;
import com.mongodb.WriteResult;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/28
 */
@Service("showService")
public class ShowServiceImpl implements IShowService {

    @Autowired
    private ShowDao showDao;

    @Override
    public ShowBo insert(ShowBo showBo) {
        showDao.insert(showBo);
        return showBo;
    }

    @Override
    public WriteResult update(String id, Map<String, Object> params) {
        return showDao.update(id, params);
    }

    @Override
    public WriteResult delete(String id) {
        return showDao.deleteById(id);
    }

    @Override
    public WriteResult batchDelete(String... ids) {
        return showDao.batchDeleteByIds(ids);
    }

    @Override
    public List<ShowBo> findByCreateuid(String userid, int type, int page, int limit) {
        return showDao.findByMyShows(userid, type, page, limit);
    }

    @Override
    public List<ShowBo> findByKeyword(String keyword, String userid, int type, int page, int limit) {
        return showDao.findByList(keyword, userid, type, page, limit);
    }

    @Override
    public List<ShowBo> findByCircleid(String circleid, int status, int type) {
        return showDao.findByCircleid(circleid, status, type);
    }

    @Override
    public List<ShowBo> findByShowType(String keyword, int type) {
        return showDao.findByShowType(keyword, type);
    }

    @Override
    @Async
    public WriteResult updateShowStatus(List<String> showids, int status) {
        return showDao.updateShowStatus(showids, status);
    }

    @Override
    public WriteResult updateShowStatus(String showid, int status) {
        return showDao.updateShowStatus(showid, status);
    }

    @Override
    public ShowBo findById(String id) {
        return showDao.findById(id);
    }

    @Override
    public WriteResult deleteById(String id) {
        return showDao.deleteById(id);
    }

    @Override
    public List<ShowBo> findByShowType(int type, int page, int limit) {
        return showDao.findByShowType(type, page, limit);
    }

    @Override
    public List<ShowBo> findRecomShows(String userid, LinkedHashSet<String> showTypes, int type) {
        return showDao.findRecomShows(userid, showTypes, type);
    }

    @Override
    public List<ShowBo> findByMyShows(String userid, int type) {
        return showDao.findByMyShows(userid, type);
    }

    @Override
    public List<ShowBo> findCircleRecoms(LinkedHashSet<String> showTypes) {
        return showDao.findCircleRecoms(showTypes);
    }

    @Override
    public List<ShowBo> findByKeword(String keyword, int type, int page, int limit) {
        return showDao.findByKeword(keyword, type, page, limit);
    }

    @Override
    public long findByKeyword(String keyword, String userid, int type) {
        return showDao.findByList(keyword, userid, type);
    }

	@Override
	public int findPublishZhaoNum(String id) {
		return showDao.findPublishZhaoNum(id);
	}

	@Override
	public int findPublishJieNum(String id) {
		return showDao.findPublishJieNum(id);
	}

	@Override
	public List<ShowBo> findByList(String[] matchField, String keyword, String userid, int type, int page, int limit) {
		return showDao.findByList(matchField,keyword,null,DiscoveryConstants.NEED,page,limit);
	}
}
