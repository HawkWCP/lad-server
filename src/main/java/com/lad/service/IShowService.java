package com.lad.service;

import com.lad.bo.ShowBo;
import com.mongodb.WriteResult;

import java.util.List;
import java.util.Map;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/26
 */
public interface IShowService {


    ShowBo insert(ShowBo showBo);


    WriteResult update(String id, Map<String, Object> params);


    WriteResult delete(String id);
    

    WriteResult batchDelete(String... ids);


    List<ShowBo> findByCreateuid(String userid, int type, int page, int limit);


    List<ShowBo> findByKeyword(String keyword, String userid, int type, int page, int limit);


    List<ShowBo> findByCircleid(String circleid, int status, int type);


    List<ShowBo> findByShowType(String keyword, int type);


    WriteResult updateShowStatus(List<String> showids, int status);

    
    WriteResult updateShowStatus(String showid, int status);

    /**
     * 根据id查找
     * @param id
     * @return
     */
    ShowBo findById(String id);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    WriteResult deleteById(String id);


    List<ShowBo> findByShowType(int type, int page, int limit);
}