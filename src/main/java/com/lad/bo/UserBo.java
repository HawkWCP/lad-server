package com.lad.bo;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "user")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserBo extends BaseBo {
	// 老友之星
	private boolean star;

	private Date starTime;

	private int sort = Integer.MAX_VALUE;

	private String address;

	private String userName;

	private String phone;

	private String sex;

	private String password;

	private String headPictureName;

	private String birthDay;

	private String personalizedSignature;

	private HashSet<String> chatrooms = new HashSet<String>();

	private List<String> circleTops = new LinkedList<>();

	private int level = 1;
	// 个人动态页面背景图
	private String dynamicPic;

	// 登录类型 0 普通注册用户， 1 微信授权， 2 QQ授权， 3 微博授权
	private int loginType;
	// 授权用户唯一标识
	private String openid;
	// 微信个人用户授权userinfo
	private String unionid;
	// 授权的token
	private String accessToken;
	//
	private String scope;
	//
	private String refeshToken;
	// 获取授权时间
	private String tokenTime;
	// 授权token的有效持续时间 单位秒
	private long expiresTime;
	// 授权的用户特权信息，json数组，如微信沃卡用户为（chinaunicom）
	private String privilege;

	private String province;

	private String city;

	// vip等级，0表示非vip
	private int vipLevel;

	// 实名认证 类型 0 未实名认证， 1 身份证实名认证， 2 银行卡实名认证
	private int idCardType;
	// 实名认证卡号
	private String idCardNo;
	// 实名认证的图片
	private String idCardPic;
	// 实名名称
	private String realName;

	private Date lastLoginTime;

	/**
	 * 面对面群聊
	 */
	private HashSet<String> faceChatrooms = new HashSet<String>();

	private LinkedList<String> chatroomsTop = new LinkedList<String>();
	/**
	 * 个人在前端显示的聊天室窗口
	 */
	private HashSet<String> showChatrooms = new LinkedHashSet<>();

	private String locationid;
}
