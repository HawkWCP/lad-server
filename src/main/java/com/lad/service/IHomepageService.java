package com.lad.service;

import java.util.HashSet;

import com.lad.bo.HomepageBo;
import com.mongodb.WriteResult;

public interface IHomepageService extends IBaseService {
	public HomepageBo insert(HomepageBo homepageBo);

	public HomepageBo update_new_visitors_count(HomepageBo homepageBo);

	public HomepageBo update_total_visitors_count(HomepageBo homepageBo);

	public HomepageBo update_visitor_ids(HomepageBo homepageBo);

	public HomepageBo selectByUserId(String userId);

	WriteResult updateNewCount(String id, int num);

	public WriteResult update_not_push_set(String hid, HashSet<String> not_push_set);

	public WriteResult update_hide_record_set(String hid, HashSet<String> hide_record_set);
}
