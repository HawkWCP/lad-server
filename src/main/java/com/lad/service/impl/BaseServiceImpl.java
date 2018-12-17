package com.lad.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.lad.service.IBaseService;
import com.lad.util.Constant;

public class BaseServiceImpl implements IBaseService {

	@Override
	public <T> List<T> changeImgHost(List<T> list) {
		ArrayList<T> result = new ArrayList<>();

		if(list!=null&&list.size()>0) {
			for (T t : list) {
				String jsonString = JSON.toJSONString(t);
				result.add((T) JSON.parseObject(
						jsonString.replaceAll("http://oojih7o1f.bkt.clouddn.com/", Constant.QINIU_URL), t.getClass()));
			}
			return result;
		}else {
			return new ArrayList<T>();
		}

	}

	@Override
	public <T> T changeImgHost(T t) {
		if(t!=null) {
			String jsonString = JSON.toJSONString(t);
			return (T) JSON.parseObject(jsonString.replaceAll("http://oojih7o1f.bkt.clouddn.com/", Constant.QINIU_URL),
					t.getClass());
		}else {
			return t;
		}
	}
}
