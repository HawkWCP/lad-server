package com.lad.service.impl;

import com.lad.bo.CrcularBo;
import com.lad.dao.ICrcularDao;
import com.lad.service.ICrcularService;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("crcularService")
public class CrcularServiceImpl extends BaseServiceImpl implements ICrcularService {
    @Autowired
    private ICrcularDao crcularDao;

	@Override
	public HashSet<CrcularBo> insert(HashSet<CrcularBo> crcular) {
		return crcularDao.insert(crcular);
	}

	@Override
	public List<CrcularBo> findCrcularById(String uid) {
		return crcularDao.findCrcularById( uid) ;
	}

	@Override
	public void updateStatus(HashSet<String> ids) {
		crcularDao.updateStatus(ids);
	}
}
