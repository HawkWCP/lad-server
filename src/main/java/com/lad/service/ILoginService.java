package com.lad.service;

import com.lad.bo.UserBo;

public interface ILoginService  {
	public UserBo getUser(String username, String password);
}
