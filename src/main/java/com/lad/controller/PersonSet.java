package com.lad.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lad.bo.CircleBo;
import com.lad.bo.LocationBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserTasteBo;
import com.lad.service.IFriendsService;
import com.lad.service.ILocationService;
import com.lad.service.IPictureService;
import com.lad.service.IRegistService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.vo.CircleBaseVo;
import com.lad.vo.UserBaseVo;
import com.lad.vo.UserInfoVo;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("person-set")
public class PersonSet extends BaseContorller {
	
	private static final Logger logger = LogManager.getLogger(PersonSet.class);

	@Autowired
	private IUserService userService;
	@Autowired
	private IRegistService registService;
	@Autowired
	private ILocationService locationService;

	@Autowired
	private IFriendsService friendsService;

	
	@Autowired
	private IPictureService pictureService;

	@ApiOperation("修改个人用户名称")
	@PostMapping("/username")
	public String username(String username, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(username)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_USERNAME.getIndex(), ERRORCODE.USER_USERNAME.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo.setUserName(username);
		userService.updateUserName(userBo);
		friendsService.updateUsernameByFriend(userBo.getId(), username, "");
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}
	
	@ApiOperation("修改个人用户地址")
	@PostMapping("/address")
	public String address(String address, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(address)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_USERNAME.getIndex(), ERRORCODE.USER_USERNAME.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo.setAddress(address);
		userService.updateAddress(userBo);
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("修改个人出生日期")
	@PostMapping("/birthday")
	public String birth_day(String birthday, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(birthday)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_BIRTHDAY.getIndex(), ERRORCODE.USER_BIRTHDAY.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo.setBirthDay(birthday);
		userService.updateBirthDay(userBo);
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("修改个人性别")
	@PostMapping("/sex")
	public String sex(String sex, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(sex)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_SEX.getIndex(), ERRORCODE.USER_SEX.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo.setSex(sex);
		userService.updateSex(userBo);
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("修改个性签名")
	@PostMapping("/personalized-signature")
	public String personalized_signature(String personalized_signature, HttpServletRequest request,
			HttpServletResponse response) {
		/*if (StringUtils.isEmpty(personalized_signature)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_SIGNATURE.getIndex(), ERRORCODE.USER_SIGNATURE.getReason());
		}*/
		personalized_signature = personalized_signature==null?"":personalized_signature;
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo.setPersonalizedSignature(personalized_signature);
		userService.updatePersonalizedSignature(userBo);
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("获取用户个人信息")
	@GetMapping("/user-info")
	public String user_info(HttpServletRequest request, HttpServletResponse response) throws
			Exception {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@GetMapping(\"/user-info\")=====user:{}({})",userBo.getUserName(),userBo.getId());
		UserInfoVo infoVo = new UserInfoVo();
		bo2vo(userBo, infoVo);
		LocationBo locationBo = locationService.getLocationBoByUserid(userBo.getId());
		if (locationBo != null) {
			infoVo.setPostion(locationBo.getPosition());
		}
		LinkedList<String> wall = CommonUtil.getTop4(pictureService,userBo.getId());
		infoVo.setPicTop4(wall==null?new LinkedList<String>():wall);
		long picSzie = pictureService.getPicSizeByUid(userBo.getId());
		infoVo.setPicSize(picSzie);
		List<CircleBo> circleBos = circleService.findMyCircles(userBo.getId(), 1, 4);
		List<CircleBaseVo> circles = new LinkedList<>();
		for (CircleBo circleBo : circleBos) {
			CircleBaseVo circleBaseVo = new CircleBaseVo();
			BeanUtils.copyProperties(circleBo,circleBaseVo);
			circleBaseVo.setCircleid(circleBo.getId());
			circleBaseVo.setNotesSize(circleBo.getNoteSize());
			circleBaseVo.setUsersSize(circleBo.getTotal());
			circles.add(circleBaseVo);
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("ret", 0);
		map.put("user", infoVo);
		map.put("userCricles", circles);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("根据名称搜索用户")
	@PostMapping("/search-by-name")
	public String searchByName(String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (StringUtils.isEmpty(name)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_USERNAME.getIndex(), ERRORCODE.USER_USERNAME.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<UserBo> list = userService.getUserByName(name);
		List<UserBaseVo> userVoList = new LinkedList<>();
		for (UserBo item : list) {
			UserBaseVo vo = new UserBaseVo();
			BeanUtils.copyProperties(item, vo);
			LinkedList<String> wall = CommonUtil.getTop4(pictureService, item.getId());
			vo.setPicTop4(wall);
			userVoList.add(vo);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("userList", userVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("根据电话号搜索用户")
	@PostMapping("/search-by-phone")
	public String searchByPhone(String phone, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (StringUtils.isEmpty(phone)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_PHONE.getIndex(), ERRORCODE.USER_PHONE.getReason());
		}
		if (!registService.is_phone_repeat(phone)) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_PHONE_NULL.getIndex(),
					ERRORCODE.ACCOUNT_PHONE_NULL.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo temp = userService.getUserByPhone(phone);
		UserBaseVo vo = new UserBaseVo();
		BeanUtils.copyProperties(temp, vo);
		LinkedList<String> wall = CommonUtil.getTop4(pictureService, temp.getId());
		vo.setPicTop4(wall);	
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("user", vo);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("根据用户id查找用户")
	@PostMapping("/search-by-userid")
	public String searchByUserid(String userid, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (StringUtils.isEmpty(userid)) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_ID.getIndex(), ERRORCODE.USER_ID.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo temp = userService.getUser(userid);
		UserBaseVo vo = new UserBaseVo();
		BeanUtils.copyProperties(temp, vo);
		LinkedList<String> wall = CommonUtil.getTop4(pictureService, userid);
		vo.setPicTop4(wall);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("user", vo);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("更新个人经纬度位置")
	@PostMapping("/location")
	public String location(double px, double py, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		LocationBo locationBo = locationService.getLocationBoByUserid(userid);
		if(null != locationBo){
			locationBo.setPosition(new double[]{px,py});
			locationBo = locationService.updateUserPoint(locationBo);
		}else{
			locationBo = new LocationBo();
			locationBo.setPosition(new double[]{px,py});
			locationBo.setUserid(userid);
			locationBo = locationService.insertUserPoint(locationBo);
		}
		userService.updateLocation(userBo.getPhone(), locationBo.getId());
		updateUserSession(request, userService);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}


	@ApiOperation("更新个人基本信息")
	@PostMapping("/update-info")
	public String updateUserInfo(String userName, String sex, String birthDay, HttpServletRequest request,
								 HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		if (StringUtils.isNotEmpty(userName)) {
			userBo.setUserName(userName);
		}
		if (StringUtils.isNotEmpty(sex)) {
			userBo.setSex(sex);
		}
		if (StringUtils.isNotEmpty(birthDay)) {
			userBo.setBirthDay(birthDay);
		}
		userService.updateUserInfo(userBo);
		updateUserSession(request, userService);
		return Constant.COM_RESP;
	}
	
	private void bo2vo(UserBo userBo, UserInfoVo infoVo){
		BeanUtils.copyProperties(userBo, infoVo);
		UserTasteBo tasteBo = userService.findByUserId(userBo.getId());
		if (tasteBo == null) {
			tasteBo = new UserTasteBo();
			tasteBo.setUserid(userBo.getId());
			userService.addUserTaste(tasteBo);
		}
		infoVo.setSports(tasteBo.getSports());
		infoVo.setMusics(tasteBo.getMusics());
		infoVo.setLifes(tasteBo.getLifes());
		infoVo.setTrips(tasteBo.getTrips());
		infoVo.setRegistTime(userBo.getCreateTime());
	}

}
