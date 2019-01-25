package com.lad.service.impl;

import com.lad.dao.ICrcularDao;
import com.lad.service.ICrcularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("crcularService")
public class CrcularServiceImpl extends BaseServiceImpl implements ICrcularService {
    @Autowired
    private ICrcularDao crcularDao;
}
