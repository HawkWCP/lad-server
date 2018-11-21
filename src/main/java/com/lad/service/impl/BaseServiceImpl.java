package com.lad.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.geo.GeoResults;

import com.alibaba.fastjson.JSON;
import com.lad.service.IBaseService;
import com.lad.util.Constant;

public class BaseServiceImpl implements IBaseService {

	@Override
	public <T> List<T> changeImgHost(List<T> list) {
		ArrayList<T> result = new ArrayList<>();
		for (T t : list) {
			String jsonString = JSON.toJSONString(t);
			result.add((T) JSON.parseObject(
					jsonString.replaceAll("http://oojih7o1f.bkt.clouddn.com/", Constant.QINIU_URL), t.getClass()));
		}
		return result;
	}

	@Override
	public <T> T changeImgHost(T t) {
		String jsonString = JSON.toJSONString(t);
		return (T) JSON.parseObject(jsonString.replaceAll("http://oojih7o1f.bkt.clouddn.com/", Constant.QINIU_URL),
				t.getClass());
	}
}
