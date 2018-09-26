package com.lad.dao;

import com.lad.bo.DynamicNumBo;
import com.mongodb.WriteResult;

import java.util.List;
import java.util.Set;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/24
 */
public interface IDynamicNumDao {


    DynamicNumBo addNum(DynamicNumBo numBo);


    DynamicNumBo findByUserid(String userid);
    

    WriteResult updateNumbers(String id, int addNum);


    DynamicNumBo findByUserids(List<String> userids);


    WriteResult updateNumbersZero(String userid);



}
