package com.lad.util;

public enum Education {
	NOLIMIT(0,"不限"),
	JUNIORHIGH(1,"初中"),
	SENIORHIGHT(2,"高中"),
	JUNIORCOLLEGE(3,"大专"),
	UNDERGRADUATE(4,"本科"),
	BEYONDUNIVERSITY(5,"研究生及以上");
	
	private int index;
	private String description;
	
	private Education(int index,String description){
		this.index = index;
		this.description = description;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public static int getIndex(String str){
		Education[] values = Education.values();
		for (Education education : values) {
			if(str.equals(education.getDescription())){
				return education.getIndex();
			}
		}
		return -1;
	}
	
	public static Education getEnumByDesc(String desc){
		Education result = null;
		for (Education education : Education.values()) {
			if(desc.equals(education.getDescription())){
				result = education;
			}
		}
		return result;
	}
	
	public int compare(String desc){
		int otherIndex = -1;
		Education[] values = Education.values();
		for (Education education : values) {
			if(desc.equals(education.getDescription())){
				otherIndex = education.getIndex();
			}
		}
		if(otherIndex == -1){
			try {
				throw new EnumException("no such enum named "+desc);
			} catch (EnumException e) {
				e.printStackTrace();
			}
		}
		return this.index-otherIndex;
	}
	
	class EnumException extends Exception{
		public EnumException(String message){
			super(message);
		}
	}
}
