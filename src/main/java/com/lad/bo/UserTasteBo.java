package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：用户兴趣 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/13
 */
@Document(collection = "userTaste")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserTasteBo extends BaseBo {

	private String userid;

	private LinkedHashSet<String> sports = new LinkedHashSet<>();

	private LinkedHashSet<String> musics = new LinkedHashSet<>();

	private LinkedHashSet<String> lifes = new LinkedHashSet<>();

	private LinkedHashSet<String> trips = new LinkedHashSet<>();
}
