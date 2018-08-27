package com.lad.vo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserTasteVo {
	private LinkedHashSet<String> sports = new LinkedHashSet<>();

	private LinkedHashSet<String> musics = new LinkedHashSet<>();

	private LinkedHashSet<String> lifes = new LinkedHashSet<>();

	private LinkedHashSet<String> trips = new LinkedHashSet<>();
}
