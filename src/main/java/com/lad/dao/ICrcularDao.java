package com.lad.dao;

import java.util.HashSet;
import java.util.List;

import com.lad.bo.CrcularBo;

public interface ICrcularDao{

	public HashSet<CrcularBo> insert(HashSet<CrcularBo> crcular);

	public List<CrcularBo> findCrcularById(String uid);

    void updateStatus(HashSet<String> ids);
}
