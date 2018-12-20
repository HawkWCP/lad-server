package com.lad.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.lad.bo.DynamicBackBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.FriendsBo;
import com.lad.bo.HomepageBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserVisitBo;
import com.lad.constants.UserCenterConstants;
import com.lad.redis.RedisServer;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.IHomepageService;
import com.lad.service.IThumbsupService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.DynamicVo;
import com.lad.vo.UserBaseVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/23
 */
@Api("动态信息相关接口")
@RestController
@RequestMapping("/dynamic")
public class DynamicController extends BaseContorller {

	private static final Logger logger = LogManager.getLogger(DynamicController.class);

	@Autowired
	private RedisServer redisServer;

	@Autowired
	private IUserService userService;

	@Autowired
	private IDynamicService dynamicService;

	@Autowired
	private IThumbsupService thumbsupService;

	@Autowired
	private IFriendsService friendsService;

	@Autowired
	private IHomepageService homepageService;

	private String pushTitle = "动态通知";

	@ApiOperation("动态详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dynamicId", value = "动态id", required = true, paramType = "query", dataType = "string") })
	@PostMapping(value = "/dynamic-infor")
	public String dynamicInfo(String dynamicId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			logger.info("@PostMapping(value = \"/dynamic-infor\")=====user:{}({}),dynamicId:{}", userBo.getUserName(),
					userBo.getId(), dynamicId);

			DynamicBo dynamicBo = dynamicService.findDynamicById(dynamicId);
			DynamicVo dynamicVo = new DynamicVo();
			bo2vo(dynamicBo, dynamicVo, userBo);

			map.put("ret", 0);
			map.put("result", dynamicVo);
		} catch (Exception e) {
			logger.error("@PostMapping(value = \"/dynamic-infor\") throw exception:{}", e);
			map.put("ret", -1);
			map.put("message", e.getMessage());
		}

		return JSON.toJSONString(map);
	}

	/**
	 * 看过我动态的人数
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("看过我动态的人数")
	@RequestMapping(value = "/new-visitors-count", method = { RequestMethod.GET, RequestMethod.POST })
	public String visitMyDynamicsNum(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/new-visitors-count\")=====user:{}({})", userBo.getUserName(),
				userBo.getId());

		List<UserVisitBo> visitBos = userService.visitToMeList(userBo.getId(), 1, 1, Integer.MAX_VALUE);
		// 保存所有访问者的id
		Set<String> temp = new HashSet<>();
		int new_visit_num = 0;

		for (UserVisitBo visitBo : visitBos) {
			UserBo user = userService.getUser(visitBo.getVisitid());
			if (user != null) {
				// 过滤掉想要隐藏的人
				HomepageBo selectByUserId = homepageService.selectByUserId(user.getId());
				if (selectByUserId != null && selectByUserId.getHide_record_set().contains(userBo.getId())) {
					continue;
				}
				boolean new_read = !visitBo.isRead();
				if (!temp.contains(user.getId())) {
					temp.add(user.getId());
					if (new_read) {
						new_visit_num++;
					}
				}
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("all_visitors_count", temp.size());
		map.put("new_visitors_count", new_visit_num);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 设置隐身访问
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("设置访问不隐身")
	@RequestMapping(value = "/set-not-hide", method = { RequestMethod.GET, RequestMethod.POST })
	public String setNotHide(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/set-not-hide\")=====user:{}({}),uid:{}", userBo.getUserName(),
				userBo.getId(), uid);
		HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
		if (homepageBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
		}
		HashSet<String> hide_record_set = homepageBo.getHide_record_set() == null ? new HashSet<String>()
				: homepageBo.getHide_record_set();
		if (hide_record_set.contains(uid)) {
			hide_record_set.remove(uid);
		}
		homepageService.update_hide_record_set(homepageBo.getId(), hide_record_set);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 设置隐身访问
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("设置隐身访问")
	@RequestMapping(value = "/set-hide-me", method = { RequestMethod.GET, RequestMethod.POST })
	public String setHideMe(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/set-hide-me\")=====user:{}({}),uid:{}", userBo.getUserName(),
				userBo.getId(), uid);
		HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
		if (homepageBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
		}
		HashSet<String> hide_record_set = homepageBo.getHide_record_set() == null ? new HashSet<String>()
				: homepageBo.getHide_record_set();
		hide_record_set.add(uid);
		homepageService.update_hide_record_set(homepageBo.getId(), hide_record_set);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 设置用户访问为通知
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("设置用户访问为通知")
	@RequestMapping(value = "/set-allow-push", method = { RequestMethod.GET, RequestMethod.POST })
	public String setAllowPush(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/set-allow-push\")=====user:{}({}),uid:{}", userBo.getUserName(),
				userBo.getId(), uid);
		HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
		if (homepageBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
		}
		HashSet<String> not_push_set = homepageBo.getNot_push_set() == null ? new HashSet<String>()
				: homepageBo.getNot_push_set();
		if (not_push_set.contains(uid)) {
			not_push_set.remove(uid);
		}
		homepageService.update_not_push_set(homepageBo.getId(), not_push_set);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 设置用户访问不通知
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("设置用户访问不通知")
	@RequestMapping(value = "/set-not-push", method = { RequestMethod.GET, RequestMethod.POST })
	public String setNotPush(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/set-not-push\")=====user:{}({}),uid:{}", userBo.getUserName(),
				userBo.getId(), uid);
		HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
		if (homepageBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
		}
		HashSet<String> not_push_set = homepageBo.getNot_push_set() == null ? new HashSet<String>()
				: homepageBo.getNot_push_set();
		not_push_set.add(uid);
		homepageService.update_not_push_set(homepageBo.getId(), not_push_set);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 删除来访记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("删除来访记录(谁看过我)")
	@RequestMapping(value = "/delete-vzt2me-history", method = { RequestMethod.GET, RequestMethod.POST })
	public String deleteVisit2Me(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/delete-vzt2me-history\"),user:{},userid:{},uid:{}",
				userBo.getUserName(), userBo.getId(), uid);
		userService.deleteByVisitid(uid, userBo.getId());
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 删除来访记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("删除来访记录(我看过谁)")
	@RequestMapping(value = "/delete-i2vzt-history", method = { RequestMethod.GET, RequestMethod.POST })
	public String deleteMe2Visit(String uid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/delete-i2vzt-history\"),user:{},userid:{},uid:{}", userBo.getUserName(),
				userBo.getId(), uid);
		userService.deleteByVisitid(userBo.getId(), uid);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 添加动态
	 * 
	 * @param px
	 * @param py
	 * @param title
	 * @param content
	 * @param landmark
	 * @param pictures
	 * @param type
	 * @param request
	 * @param response
	 * @return
	 */
	private String insert(double px, double py, String title, String content, String landmark, MultipartFile[] pictures,
			String type, LinkedHashSet<String> atIds, boolean ishide, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info(
				"@RequestMapping(value = \"/insert\")=====user:{}({}),position:[{},{}],title:{},content:{},landmark:{},type:{}",
				userBo.getUserName(), userBo.getId(), px, py, title, content, landmark, type);
		String userId = userBo.getId();
		DynamicBo dynamicBo = new DynamicBo();

		dynamicBo.setTitle(title == null ? "动态未命名" : title);
		dynamicBo.setContent(content);
		dynamicBo.setCreateuid(userId);

		dynamicBo.setLandmark(landmark);
		dynamicBo.setPostion(new double[] { px, py });

		if (pictures != null) {
			LinkedHashSet<String> images = dynamicBo.getPhotos();
			for (MultipartFile file : pictures) {
				Long time = Calendar.getInstance().getTimeInMillis();
				String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
				if ("video".equals(type)) {
					// 0 是video 的路径， 1 是缩略图路径
					String[] paths = CommonUtil.uploadVedio(file, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
					// images.add(paths[0]);
					dynamicBo.setVideo(paths[0]);
					dynamicBo.setVideoPic(paths[1]);
				} else {
					String path = CommonUtil.upload(file, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
					images.add(path);
				}
			}
			dynamicBo.setPhotos(images);
		}

		if (ishide) {
			dynamicBo.setAccess_level(UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE);
		}
		dynamicService.addDynamic(dynamicBo);

		List<String> pushIds = atIds.stream()
				.filter(atid -> !homepageService.selectByUserId(atid).getNot_push_set().contains(userId))
				.collect(Collectors.toList());
		String path = new String("/dynamic/dynamic_info?dynamicId=" + dynamicBo.getId());
		String pushContent = String.format("“%s”在Ta的个人动态中@了你，快去看看吧！", userBo.getUserName());
		usePush(pushIds, pushTitle, pushContent, path);
		updateDynamicNums(userId, 1, dynamicService, redisServer);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicid", dynamicBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 添加动态
	 * 
	 * @param px
	 * @param py
	 * @param title
	 * @param content
	 * @param landmark
	 * @param pictures
	 * @param type
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("添加动态信息")
	@PostMapping(value = "/insert")
	public String insert(double px, double py, String content, String landmark, MultipartFile[] pictures, String type,
			LinkedHashSet<String> atIds, boolean ishide, HttpServletRequest request, HttpServletResponse response) {
		return insert(px, py, null, content, landmark, pictures, type, atIds, ishide, request, response);
	}

	/**
	 * 所有好友动态列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("所有好友动态列表")
	@RequestMapping(value = "/all-dynamics", method = { RequestMethod.GET, RequestMethod.POST })
	public String allDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {

		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info("@RequestMapping(value = \"/all-dynamics\")=====user:{}({}),page:{},limit:{}", userBo.getUserName(),
				userBo.getId(), page, limit);
		List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);

		List<DynamicBo> msgBos = dynamicService.findAllFriendsMsg(friends, page, limit);

		List<DynamicVo> dynamicVos = new ArrayList<>();
		bo2vo(msgBos, dynamicVos, userBo);

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicVos", dynamicVos);
		map.put("backPic", userBo.getDynamicPic());
		map.put("headPic", userBo.getHeadPictureName());
		map.put("signature", userBo.getPersonalizedSignature());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 获取所有好友动态数量
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("获取所有好友动态数量")
	@RequestMapping(value = "/all-dynamics-num", method = { RequestMethod.GET, RequestMethod.POST })
	public String allFriendsNum(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info("@RequestMapping(value = \"/all-dynamics-num\")=====user:{}({})", userBo.getUserName(),
				userBo.getId());
		List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
		List<DynamicBo> msgBos = dynamicService.findAllFriendsMsg(friends, -1, 0);

		long notReadNum = dynamicService.findDynamicNotReadNum(userBo.getId());
		int hideNum = 0;
		for (DynamicBo dynamicBo : msgBos) {
			if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
				hideNum++;
			}
			if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
				if (!dynamicBo.getAccess_allow_set().contains(userBo.getId())) {
					hideNum++;
				}
			}
		}

		int total = msgBos != null ? msgBos.size() : 0;
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicNum", total - hideNum);
		map.put("notReadNum", notReadNum - hideNum);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 好友动态列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("好友动态列表")
	@RequestMapping(value = "/friend-dynamics", method = { RequestMethod.GET, RequestMethod.POST })
	public String allDynamics(String friendid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info("@RequestMapping(value = \"/friend-dynamics\")=====user:{}({}),friendid:{},page:{},limit:{}",
				userBo.getUserName(), userBo.getId(), friendid, page, limit);
		UserBo friend = userService.getUser(friendid);
		if (friend == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(), ERRORCODE.FRIEND_NULL.getReason());
		}
		addVisitHis(userBo.getId(), friendid);
		List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(friendid, page, limit);
		List<DynamicVo> dynamicVos = new ArrayList<>();
		bo2vo(msgBos, dynamicVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicVos", dynamicVos);
		map.put("backPic", friend.getDynamicPic());
		map.put("headPic", friend.getHeadPictureName());
		map.put("signature", friend.getPersonalizedSignature());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 获取好友动态数量
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(" 获取好友动态数量")
	@RequestMapping(value = "/one-dynamics-num", method = { RequestMethod.GET, RequestMethod.POST })
	public String friendsNum(String friendid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/one-dynamics-num\")=====user:{}({}),friendid:{}", userBo.getUserName(),
				userBo.getId(), friendid);
		List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(friendid, -1, 0);

		int hideNum = 0;
		for (DynamicBo dynamicBo : msgBos) {

			if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
				hideNum++;
			}
			if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
				if (!dynamicBo.getAccess_allow_set().contains(userBo.getId())) {
					hideNum++;
				}
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicNum", msgBos == null ? 0 : (msgBos.size() - hideNum));
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 我的动态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("我的动态")
	@RequestMapping(value = "/my-dynamics", method = { RequestMethod.GET, RequestMethod.POST })
	public String myDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/my-dynamics\")=====user:{}({}),friendid:{},page:{},limit:{}",
				userBo.getUserName(), userBo.getId(), page, limit);
		List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(userBo.getId(), page, limit);
		List<DynamicVo> dynamicVos = new ArrayList<>();
		bo2vo(msgBos, dynamicVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicVos", dynamicVos);
		map.put("backPic", userBo.getDynamicPic() == null ? "" : userBo.getDynamicPic());
		map.put("headPic", userBo.getHeadPictureName());
		map.put("signature", userBo.getPersonalizedSignature());
		HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
		HashSet<String> not_push_set = homepageBo.getNot_push_set();

		List<UserVisitBo> userVisitBos = userService.findUserVisitFirst(userBo.getId(), not_push_set, 1);

		UserBaseVo show = new UserBaseVo();
		for (UserVisitBo userVisitBo : userVisitBos) {
			HomepageBo ownerHomePage = homepageService.selectByUserId(userVisitBo.getOwnerid());
			HashSet<String> hide_record_set = ownerHomePage.getHide_record_set();

			if (userVisitBo != null && !hide_record_set.contains(userVisitBo.getOwnerid())) {
				UserBo user = userService.getUser(userVisitBo.getVisitid());
				if (user != null) {
					BeanUtils.copyProperties(user, show);
				}
				break;
			}
		}

		map.put("showUser", JSONObject.fromObject(show).toString());
		return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
	}

	/**
	 * 我的动态数量
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("我的动态数量")
	@RequestMapping(value = "/my-dynamics-num", method = { RequestMethod.GET, RequestMethod.POST })
	public String myDynamicsNum(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/my-dynamics-num\")=====user:{}({})", userBo.getUserName(),
				userBo.getId());

		List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(userBo.getId(), -1, 0);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicNum", msgBos == null ? 0 : msgBos.size());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("不看他的动态")
	@RequestMapping(value = "/dynamic-not-see", method = { RequestMethod.GET, RequestMethod.POST })
	public String notSee(String friendid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/dynamic-not-see\")=====user:{}({}),friendid:{}", userBo.getUserName(),
				userBo.getId(), friendid);
		DynamicBackBo backBo = dynamicService.findBackByUserid(userBo.getId());
		if (backBo == null) {
			backBo = new DynamicBackBo();
			backBo.setUserid(userBo.getId());
			HashSet<String> notSees = backBo.getNotSeeBacks();
			notSees.add(friendid);
			backBo.setNotSeeBacks(notSees);
			dynamicService.addDynamicBack(backBo);
		} else {
			HashSet<String> notSees = backBo.getNotSeeBacks();
			notSees.add(friendid);
			dynamicService.updateBackNotSee(backBo.getId(), notSees);
		}
		return Constant.COM_RESP;
	}

	/**
	 * 谁看过我的动态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("谁看过我的动态")
	@RequestMapping(value = "/visit-my-dynamic", method = { RequestMethod.GET, RequestMethod.POST })
	public String visitMyDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/visit-my-dynamic\")=====user:{}({}),page:{},limit",
				userBo.getUserName(), userBo.getId(), page, limit);

		List<UserVisitBo> visitBos = userService.visitToMeList(userBo.getId(), 1, page, limit);
		List<Object> visitUsers = new LinkedList<>();
		// 保存所有访问者的id
		Set<String> temp = new HashSet<>();

		// 获取不通知列表
		HomepageBo myHomepage = homepageService.selectByUserId(userBo.getId());
		HashSet<String> not_push_set = myHomepage.getNot_push_set() == null ? new HashSet<>()
				: myHomepage.getNot_push_set();

		for (UserVisitBo visitBo : visitBos) {
			UserBo user = userService.getUser(visitBo.getVisitid());

			if (user != null) {
				// 过滤掉想要隐藏的人
				HomepageBo selectByUserId = homepageService.selectByUserId(user.getId());
				if (selectByUserId != null && selectByUserId.getHide_record_set().contains(userBo.getId())) {
					continue;
				}

				if (!temp.contains(user.getId())) {
					UserBaseVo baseVo = new UserBaseVo();
					BeanUtils.copyProperties(user, baseVo);
					List<UserVisitBo> visBos = userService.visitToMeList(userBo.getId(), user.getId(), 1);
					List<Date> visitTime = new ArrayList<>();
					for (UserVisitBo date : visBos) {
						if (date != null) {
							if (date.getVisitTime() != null) {
								visitTime.add(date.getVisitTime());
							}
						}
					}
					com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(JSON.toJSONString(baseVo));
					parseObject.put("visitTime", visitTime);
					parseObject.put("push", !not_push_set.contains(user.getId()));
					temp.add(user.getId());
					visitUsers.add(parseObject);
				}
			}
		}

		dynamicService.updateReadToTure(userBo.getId(), temp);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("visitUserVos", visitUsers);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 我看过谁的动态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("我看过谁的动态")
	@RequestMapping(value = "/my-visit-dynamic", method = { RequestMethod.GET, RequestMethod.POST })
	public String myVisitDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/my-visit-dynamic\")=====user:{}({}),page:{},limit",
				userBo.getUserName(), userBo.getId(), page, limit);
		List<UserVisitBo> visitBos = userService.visitFromMeList(userBo.getId(), 1, page, limit);
		List<Object> visitUsers = new LinkedList<>();
		Set<String> temp = new HashSet<>();
		HomepageBo myHomepage = homepageService.selectByUserId(userBo.getId());
		HashSet<String> hide_record_set = myHomepage.getHide_record_set() == null ? new HashSet<String>()
				: myHomepage.getHide_record_set();
		;
		for (UserVisitBo visitBo : visitBos) {
			UserBo user = userService.getUser(visitBo.getOwnerid());
			if (user != null) {
				if (!temp.contains(user.getId())) {
					UserBaseVo baseVo = new UserBaseVo();
					BeanUtils.copyProperties(user, baseVo);
					List<UserVisitBo> visBos = userService.visitToMeList(user.getId(), userBo.getId(), 1);
					List<Date> visitTime = new ArrayList<>();
					for (UserVisitBo date : visBos) {
						if (date != null) {
							if (date.getVisitTime() != null) {
								visitTime.add(date.getVisitTime());
							}
						}
					}
					com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(JSON.toJSONString(baseVo));
					parseObject.put("visitTime", visitTime);
					parseObject.put("hide", hide_record_set.contains(user.getId()));
					temp.add(user.getId());
					visitUsers.add(parseObject);
				}

			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("visitUserVos", visitUsers);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 我的动态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("更新动态背景图片")
	@RequestMapping(value = "/update-backpic", method = { RequestMethod.GET, RequestMethod.POST })
	public String updateDynamicsPic(MultipartFile backPic, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/update-backpic\")=====user:{}({})", userBo.getUserName(),
				userBo.getId());

		if (backPic != null) {
			Long time = Calendar.getInstance().getTimeInMillis();
			String fileName = String.format("%s-%d-%s", userBo.getId(), time, backPic.getOriginalFilename());
			String path = CommonUtil.upload(backPic, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
			userService.updateUserDynamicPic(userBo.getId(), path);
			userBo.setDynamicPic(path);
			request.getSession().setAttribute("userBo", userBo);
		}
		return Constant.COM_RESP;
	}

	/**
	 * 访问记录添加
	 * 
	 * @param userid
	 * @param friendid
	 */
	private void addVisitHis(String userid, String friendid) {
		UserVisitBo visitBo = new UserVisitBo();
		visitBo.setVisitTime(new Date());
		visitBo.setVisitid(userid);
		visitBo.setOwnerid(friendid);
		visitBo.setType(1);
		HomepageBo selectByUserId = homepageService.selectByUserId(userid);
		if (selectByUserId != null && selectByUserId.getHide_record_set().contains(friendid)) {
			visitBo.setRead(true);
		}
		userService.addUserVisit(visitBo);
	}

	// 新建重载方法,对单个实体进行移植
	private void bo2vo(DynamicBo msgBo, DynamicVo dynamicVo, UserBo userBo) {
		if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
			System.out.println("动态为私有");
			return;
		}
		if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
			System.out.println("只对部分人开放");
			LinkedHashSet<String> access_allow_set = msgBo.getAccess_allow_set();
			if (!access_allow_set.contains(userBo.getId())) {
				return;
			}
		}
		BeanUtils.copyProperties(msgBo, dynamicVo);

		if (!userBo.getId().equals(msgBo.getCreateuid())) {
			// 如果该条动态不是自己创建的
			UserBo user = userService.getUser(msgBo.getCreateuid());
			dynamicVo.setUserPic(user.getHeadPictureName());
			dynamicVo.setUserid(msgBo.getCreateuid());
			FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userBo.getId(), msgBo.getCreateuid());
			if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
				dynamicVo.setUserName(friendsBo.getBackname());
			} else {
				dynamicVo.setUserName(user.getUserName());
			}
		} else {
			dynamicVo.setUserPic(userBo.getHeadPictureName());
			dynamicVo.setUserid(userBo.getId());
			dynamicVo.setUserName(userBo.getUserName());
		}

		dynamicVo.setTime(msgBo.getCreateTime());
		dynamicVo.setIsMyThumbsup(
				thumbsupService.findHaveOwenidAndVisitorid(msgBo.getSourceId(), userBo.getId()) != null);

		LinkedHashSet<String> unReadFrend = msgBo.getUnReadFrend();
		if (unReadFrend != null && unReadFrend.contains(userBo.getId())) {
			unReadFrend.remove(userBo.getId());
			dynamicService.updateUnReadSet(msgBo.getId(), unReadFrend);
			dynamicVo.setNeww(true);
		}
	}

	private void bo2vo(List<DynamicBo> msgBos, List<DynamicVo> dynamicVos, UserBo userBo) {
		// userBo 动态的浏览者
		for (DynamicBo msgBo : msgBos) {
			if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
				continue;
			}
			if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
				LinkedHashSet<String> access_allow_set = msgBo.getAccess_allow_set();
				if (!access_allow_set.contains(userBo.getId())) {
					continue;
				}
			}
			DynamicVo dynamicVo = new DynamicVo();
			BeanUtils.copyProperties(msgBo, dynamicVo);

			if (!userBo.getId().equals(msgBo.getCreateuid())) {
				// 如果该条动态不是自己创建的
				UserBo user = userService.getUser(msgBo.getCreateuid());
				dynamicVo.setUserPic(user.getHeadPictureName());
				dynamicVo.setUserid(msgBo.getCreateuid());
				FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userBo.getId(),
						msgBo.getCreateuid());
				if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
					dynamicVo.setUserName(friendsBo.getBackname());
				} else {
					dynamicVo.setUserName(user.getUserName());
				}
			} else {
				dynamicVo.setUserPic(userBo.getHeadPictureName());
				dynamicVo.setUserid(userBo.getId());
				dynamicVo.setUserName(userBo.getUserName());
			}

			dynamicVo.setTime(msgBo.getCreateTime());
			dynamicVo.setIsMyThumbsup(
					thumbsupService.findHaveOwenidAndVisitorid(msgBo.getSourceId(), userBo.getId()) != null);

			LinkedHashSet<String> unReadFrend = msgBo.getUnReadFrend();
			if (unReadFrend != null && unReadFrend.contains(userBo.getId())) {
				unReadFrend.remove(userBo.getId());
				dynamicService.updateUnReadSet(msgBo.getId(), unReadFrend);
				dynamicVo.setNeww(true);
			}
			dynamicVos.add(dynamicVo);
		}
	}

}
