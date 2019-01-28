package com.lad.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lad.bo.CrcularBo;

public interface ICrcularService {

	HashSet<CrcularBo> insert(HashSet<CrcularBo> crculars);

	List<CrcularBo> findCrcularById(String uid);

    void updateStatus(HashSet<String> ids);
}
