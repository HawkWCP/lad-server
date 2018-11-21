package com.lad.service;

import java.util.List;

import org.springframework.data.geo.GeoResults;

import com.lad.bo.CircleBo;

public interface IBaseService {
	public <T> List<T> changeImgHost(List<T> list);

	public <T> T changeImgHost(T t);
	
}
