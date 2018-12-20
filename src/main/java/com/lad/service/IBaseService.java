package com.lad.service;

import java.util.List;

public interface IBaseService {
	public <T> List<T> changeImgHost(List<T> list);

	public <T> T changeImgHost(T t);
	
}
