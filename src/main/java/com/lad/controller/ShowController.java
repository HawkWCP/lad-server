package com.lad.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lad.bo.CircleBo;
import com.lad.bo.CircleTypeBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.FriendsBo;
import com.lad.bo.NoteBo;
import com.lad.bo.ShowBo;
import com.lad.bo.UserBo;
import com.lad.constants.DiscoveryConstants;
import com.lad.constants.GeneralContants;
import com.lad.constants.UserCenterConstants;
import com.lad.service.ICircleService;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.INoteService;
import com.lad.service.IShowService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.ShowVo;
import com.lad.vo.UserBaseVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

/**
 * 功能描述： Copyright: Copyright (c) 2018 Version: 1.0 Time:2018/4/28
 */
@Slf4j
@Api(value = "ShowController", description = "招接演出接口")
@RestController
@RequestMapping("show")
public class ShowController extends BaseContorller {

	private Logger logger = LogManager.getLogger(ShowController.class);
	@Autowired
	private IUserService userService;

	@Autowired
	private IFriendsService friendsService;

	@Autowired
	private IShowService showService;

	@Autowired
	private AsyncController asyncController;

	@Autowired
	private ICircleService circleService;

	@Autowired
	private IDynamicService dynamicService;

	@Autowired
	private INoteService noteService;

	@ApiOperation("showVo对象说明")
	@PostMapping("/showVotest")
	@Deprecated
	public ShowVo showVoTest(
			@RequestBody @ApiParam(name = "showVo", value = "演出信息实体类", required = true) ShowVo showVo) {
		return new ShowVo();
	}

	@ApiOperation("转发招/接演到我的动态")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "showid", value = "被转发的招/接演id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "view", value = "转发说明信息", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "showType", value = "表演类型:1.招演出;2.接演出", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "landmark", value = "转发时的地标", dataType = "string", paramType = "query") })
	@PostMapping("/forward-dynamic")
	public String forwardDynamic(String showid, String view, int showType, String landmark, HttpServletRequest request,
			HttpServletResponse response) {

		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info("@PostMapping(\"/forward-dynamic\")=====showid:{},view:{},landmark:{},user:{}({})", showid, view,
				landmark, userBo.getUserName(), userBo.getId());

		ShowBo showBo = showService.findById(showid);
		if (null == showBo) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_IS_NULL.getIndex(), ERRORCODE.SHOW_IS_NULL.getReason());
		}
		DynamicBo dynamicBo = new DynamicBo();
		dynamicBo.setTitle(showBo.getCompany());
		dynamicBo.setView(view);
		dynamicBo.setSourceId(showBo.getId());
		dynamicBo.setCreateuid(userBo.getId());
		dynamicBo.setOwner(showBo.getCreateuid());
		dynamicBo.setLandmark(landmark);
		dynamicBo.setForward(GeneralContants.YES);
		dynamicBo.setType(UserCenterConstants.FORWARD_FROM_DISCOVERY_SHOW);
		dynamicBo.setShowType(showType);
		if (showBo.getImages() != null && showBo.getImages().size() > 0) {
//			dynamicBo.setPicType("pic");
			dynamicBo.setPhotos(showBo.getImages());
		}
		dynamicBo.setSourceName(showBo.getCompany());

		List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
		dynamicBo.setUnReadFrend(new LinkedHashSet<>(friends));
		dynamicService.addDynamic(dynamicBo);
		updateDynamicNums(userBo.getId(), 1, dynamicService, redisServer);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicid", dynamicBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("转发到指定的圈子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "转发圈子id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "showid", value = "招/接演id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "showType", value = "表演类型:1.招演出;2.接演出", required = true, dataType = "int", paramType = "query") })
	@RequestMapping(value = "/forward-circle", method = { RequestMethod.GET, RequestMethod.POST })
	public String forwardCircle(String circleid, String showid, int showType,HttpServletRequest request,
			HttpServletResponse response) {
		return forwardCircle(circleid, showid, showType,null, request, response);
	}

	/**
	 * 作为一个可扩展的私有handler
	 * 
	 * @param circleid
	 * @param homeId
	 * @param landmark
	 * @param request
	 * @param response
	 * @return
	 */
	private String forwardCircle(String circleid, String showid, int showType,String landmark, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/forward-dynamic\")=====user:{}({}),circleid:{},showid:{}",
				userBo.getUserName(), userBo.getId(), circleid, showid);
		ShowBo showBo = showService.findById(showid);
		if (null == showBo) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_IS_NULL.getIndex(), ERRORCODE.SHOW_IS_NULL.getReason());
		}
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(), ERRORCODE.CIRCLE_IS_NULL.getReason());
		}

		NoteBo noteBo = new NoteBo();
		noteBo.setSourceid(showid);
		noteBo.setNoteType(NoteBo.SHOW_FORWARD);
		noteBo.setForward(1);
		noteBo.setCreateuid(userBo.getId());
		noteBo.setCircleId(circleid);
		noteBo.setShowType(showType);
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		if (landmark != null) {
			noteBo.setLandmark(landmark);
		}
//		String[] atUser = atUserids.split(",");
//		noteBo.setAtUsers(new LinkedList<>(Arrays.asList(atUser)));

		NoteBo insert = noteService.insert(noteBo);
		// 更新圈子成员未读帖子列表 重写了updateCircieNoteUnReadNum,添加了noteId字段
		asyncController.updateCircieNoteUnReadNum(userBo.getId(), circleid, insert.getId());
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteId", insert.getId());
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("发表招接演出信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "showVoJson", value = "演出实体类信息", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "images", value = "图片信息,如果是招演类型，则为营业执照图片", paramType = "query", dataType = "file"),
			@ApiImplicitParam(name = "video", value = "视频信息，与图片二选一", paramType = "query", dataType = "file") })
	@PostMapping("/insert")
	public String insert(String showVoJson, MultipartFile[] images, MultipartFile video, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		ShowBo showBo = null;
		try {
			JSONObject jsonObject = JSONObject.fromObject(showVoJson);
			showBo = (ShowBo) JSONObject.toBean(jsonObject, ShowBo.class);
		} catch (Exception e) {
			return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(), ERRORCODE.FORMAT_ERROR.getReason());
		}
		showBo.setCreateuid(userid);
		if (images != null && images.length > 0) {
			if (showBo.getType() == DiscoveryConstants.NEED) {
				MultipartFile file = images[0];
				Long time = Calendar.getInstance().getTimeInMillis();
				String fileName = String.format("%s-%d-%s", userid, time, file.getOriginalFilename());
				String path = CommonUtil.upload(file, Constant.RELEASE_PICTURE_PATH, fileName, 0);
				showBo.setComPic(path);
			} else if (showBo.getType() == DiscoveryConstants.PROVIDE) {
				LinkedHashSet<String> photos = new LinkedHashSet<>();
				for (MultipartFile file : images) {
					Long time = Calendar.getInstance().getTimeInMillis();
					String fileName = String.format("%s-%d-%s", userid, time, file.getOriginalFilename());
					String path = CommonUtil.upload(file, Constant.RELEASE_PICTURE_PATH, fileName, 0);
					photos.add(path);
				}
				showBo.setImages(photos);
			}
			showBo.setPicType("pic");
			log.info("shows {} add pic   size: {} ", userid, images.length);
		}
		if (video != null) {
			Long time = Calendar.getInstance().getTimeInMillis();
			String fileName = String.format("%s-%d-%s", userid, time, video.getOriginalFilename());
			String[] paths = CommonUtil.uploadVedio(video, Constant.RELEASE_PICTURE_PATH, fileName, 0);
			showBo.setVideo(paths[0]);
			showBo.setVideoPic(paths[1]);
			showBo.setPicType("video");
			log.info("user {} shows add video path: {},  videoPic: {} ", userid, paths[0], paths[1]);
		}
		showService.insert(showBo);
		int recomType = showBo.getType() == DiscoveryConstants.NEED ? DiscoveryConstants.PROVIDE
				: DiscoveryConstants.NEED;
		long num = showService.findByKeyword(showBo.getShowType(), userid, recomType);
		asyncController.addShowTypes(showBo.getShowType(), userid);
		if (showBo.getType() == DiscoveryConstants.NEED) {
			asyncController.pushShowToCreate(showService, showBo);
		} else {
			asyncController.pushShowToCompany(showService, showBo.getShowType(), showBo.getId(), userBo.getUserName(),
					userid);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showid", showBo.getId());
		map.put("recomShowNum", num);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("获取演出详情")
	@ApiImplicitParam(name = "showid", value = "演出id", required = true, paramType = "query", dataType = "string")
	@GetMapping("/show-info")
	public String detail(String showid, HttpServletRequest request, HttpServletResponse response) {
		ShowBo showBo = showService.findById(showid);
		if (showBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_NULL.getIndex(), ERRORCODE.SHOW_NULL.getReason());
		}
		ShowVo vo = new ShowVo();
		BeanUtils.copyProperties(showBo, vo);
		vo.setShowid(showBo.getId());
		UserBaseVo baseVo = new UserBaseVo();
		UserBo userBo = getUserLogin(request);
		UserBo createUser = null;
		String friendName = "";
		String userid = "";
		if (userBo != null) {
			userid = userBo.getId();
			if (userid.equals(showBo.getCreateuid())) {
				vo.setCreate(true);
				BeanUtils.copyProperties(userBo, baseVo);
			} else {
				createUser = userService.getUser(showBo.getCreateuid());
				FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, showBo.getCreateuid());
				if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
					friendName = friendsBo.getBackname();
				}
			}
		} else {
			createUser = userService.getUser(showBo.getCreateuid());
		}
		if (DiscoveryConstants.PROVIDE == showBo.getType()) {
			CircleBo circleBo = circleService.selectById(showBo.getCircleid());
			vo.setCirName(circleBo == null ? "" : circleBo.getName());
		}
		if (createUser != null) {
			BeanUtils.copyProperties(createUser, baseVo);
			baseVo.setUserName(!"".equals(friendName) ? friendName : createUser.getUserName());
		}
		vo.setCreatUser(baseVo);
		// 推荐信息获取
		List<ShowVo> showVos = new LinkedList<>();
		List<ShowBo> showBos;
		// 招演商家推荐接演团队信息
		if (showBo.getType() == DiscoveryConstants.NEED) {
			showBos = showService.findByKeyword(showBo.getShowType(), userid, DiscoveryConstants.PROVIDE, 1, 3);
		} else {
			// 接演团队推荐商家信息
			showBos = showService.findByKeyword(showBo.getShowType(), userid, DiscoveryConstants.NEED, 1, 3);
		}
		long num = showService.findByKeyword(showBo.getShowType(), userid, showBo.getType());
		bo2vos(showBos, showVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showVo", vo);
		map.put("recomShowVos", showVos);
		map.put("recomShowNum", num);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("修改招接演出信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "showid", value = "演出信息id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "showVoJson", value = "修改的参数和信息，json字符串，字段根据showVo来定义，不需要修改内容则不传入，可为空，文件url不需要传入", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "picType", value = "删除的是图片还是视频，video 视频，pic 图片", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "delImages", value = "删除的图片信息，多个以逗号给开，不删除则为空", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "images", value = "新增图片信息, 如果是招演类型，则默认为营业执照图片", paramType = "query", dataType = "file"),
			@ApiImplicitParam(name = "video", value = "需要修改视频信息，与图片二选一", paramType = "query", dataType = "file") })
	@PostMapping("/update")
	public String update(String showid, String showVoJson, String picType, String delImages, MultipartFile[] images,
			MultipartFile video, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		ShowBo showBo = showService.findById(showid);
		if (showBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_NULL.getIndex(), ERRORCODE.SHOW_NULL.getReason());
		}
		// 创建者或者管理员才能修改
		if (!userBo.getId().equals(showBo.getCreateuid()) && !CommonUtil.getAdminUserids().contains(userBo.getId())) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_NOT_MASTER.getIndex(),
					ERRORCODE.NOTE_NOT_MASTER.getReason());
		}
		Map<String, Object> params = new LinkedHashMap<>();
		if (!StringUtils.isEmpty(showVoJson)) {
			JSONObject jsonObject = JSONObject.fromObject(showVoJson);
			Iterator<?> iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				params.put(key, jsonObject.get(key));
			}
		}
		LinkedHashSet<String> photos = showBo.getImages();
		if (photos == null) {
			photos = new LinkedHashSet<>();
		}
		if (!StringUtils.isEmpty(delImages)) {
			if ("video".equals(picType)) {
				if (delImages.equals(showBo.getVideo())) {
					params.put("video", "");
					params.put("videoPic", "");
					params.put("picType", "");
				}
			} else if ("pic".equals(picType)) {
				// 删除公司图片
				if (showBo.getType() == DiscoveryConstants.NEED && delImages.equals(showBo.getComPic())) {
					params.put("comPic", "");
				} else {
					// 需要删除的图片
					String[] delArray = delImages.trim().split(",");
					for (String url : delArray) {
						photos.remove(url);
					}
					params.put("images", photos);
				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("ret", "-1");
				map.put("error", "need picType");
				return JSONObject.fromObject(map).toString();
			}
		}
		// 新增的图片
		if (images != null) {
			if (showBo.getType() == DiscoveryConstants.NEED) {
				log.info("show id {} showVoJson {} -- images {} ", showid, showVoJson, images.length);
				// 如果包含修改了演出时间
				if (params.containsKey("showTime")) {
					String showTime = (String) params.get("showTime");
					// 如果修改招演时间，需要将状态改成未结束状态
					if (!isTimeout(showTime)) {
						params.put("status", 0);
					}
				}
				for (MultipartFile file : images) {
					Long time = Calendar.getInstance().getTimeInMillis();
					String fileName = String.format("%s-%d-%s", userid, time, file.getOriginalFilename());
					String path = CommonUtil.upload(file, Constant.RELEASE_PICTURE_PATH, fileName, 0);
					log.info("show NEED {} pic  user {}  path  {}", showid, userid, path);
					params.put("comPic", path);
					break;
				}
			} else if (showBo.getType() == DiscoveryConstants.PROVIDE) {
				// 如果包含修改了结束时间
				if (params.containsKey("endTime")) {
					String endTime = (String) params.get("endTime");
					// 需要将状态改成未结束状态
					if (!isEndTimeout(endTime)) {
						params.put("status", 0);
					}
				}
				for (MultipartFile file : images) {
					Long time = Calendar.getInstance().getTimeInMillis();
					String fileName = String.format("%s-%d-%s", userid, time, file.getOriginalFilename());
					String path = CommonUtil.upload(file, Constant.RELEASE_PICTURE_PATH, fileName, 0);
					log.info("show PROVIDE {} pic  user {}  path  {}", showid, userid, path);
					photos.add(path);
				}
			}
			params.put("picType", "pic");
		} else {
			log.info("show id {}  showVoJson {} --------------- null ", showid, showVoJson);
		}
		log.info("show id {} 5555  showVoJson {} --------------- null ", showid, showVoJson);
		params.put("images", photos);
		if (video != null) {
			Long time = Calendar.getInstance().getTimeInMillis();
			String fileName = String.format("%s-%d-%s", userid, time, video.getOriginalFilename());
			String[] paths = CommonUtil.uploadVedio(video, Constant.RELEASE_PICTURE_PATH, fileName, 0);
			log.info("show {} pic  user {}  path  ", showid, userid, paths[0]);
			showBo.setVideo(paths[0]);
			showBo.setVideoPic(paths[1]);
			params.put("video", paths[0]);
			params.put("videoPic", paths[1]);
			params.put("picType", "video");
		}
		if (!params.isEmpty()) {
			params.put("updateTime", new Date());
			showService.update(showid, params);
		}
		int recomType = showBo.getType() == DiscoveryConstants.NEED ? DiscoveryConstants.PROVIDE
				: DiscoveryConstants.NEED;
		Object showType = params.get("showType");
		String showTypeStr = StringUtils.isEmpty(showType) ? showBo.getShowType() : showType.toString();
		long num = showService.findByKeyword(showTypeStr, userid, recomType);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showid", showBo.getId());
		map.put("recomShowNum", num);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("获取演出列表信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "type", value = "1招演出，2接演出", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "page", value = "页码", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "条数", paramType = "query", dataType = "int") })
	@GetMapping("/show-list")
	public String showList(int type, int page, int limit, HttpServletRequest request, HttpServletResponse response) {

		UserBo userBo = getUserLogin(request);
		List<ShowBo> showBos = showService.findByShowType(type, page, limit);
		List<ShowVo> showVos = new LinkedList<>();
		bo2vos(showBos, showVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("我的招接演出列表信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "type", value = "1招演出，2接演出, -1所有", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "page", value = "页码", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "条数", paramType = "query", dataType = "int") })
	@GetMapping("/my-shows")
	public String myShows(int type, int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<ShowBo> showBos = showService.findByCreateuid(userBo.getId(), type, page, limit);
		List<ShowVo> showVos = new LinkedList<>();
		List<String> showids = new LinkedList<>();
		UserBaseVo baseVo = new UserBaseVo();
		BeanUtils.copyProperties(userBo, baseVo);
		for (ShowBo showBo : showBos) {
			ShowVo showVo = new ShowVo();
			BeanUtils.copyProperties(showBo, showVo);
			// 在当前失效的招演信息
			if (showBo.getType() == DiscoveryConstants.NEED && showBo.getStatus() == 0
					&& isTimeout(showBo.getShowTime())) {
				showVo.setStatus(1);
				showids.add(showBo.getId());
			} else if (showBo.getType() == DiscoveryConstants.PROVIDE && isEndTimeout(showBo.getEndTime())) {
				showVo.setStatus(1);
				showids.add(showBo.getId());
			}
			if (DiscoveryConstants.PROVIDE == showBo.getType()) {
				CircleBo circleBo = circleService.selectById(showBo.getCircleid());
				showVo.setCirName(circleBo == null ? "" : circleBo.getName());
			}
			showVo.setShowid(showBo.getId());
			showVo.setCreate(true);
			showVo.setCreatUser(baseVo);
			showVos.add(showVo);
		}
		// 过期招演信息更新
		if (!showids.isEmpty()) {
			showService.updateShowStatus(showids, 1);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("推荐演出列表信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "showid", value = "演出详情的id信息", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "page", value = "页码", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "条数", paramType = "query", dataType = "int") })
	@GetMapping("/recom-shows")
	public String detail(String showid, int page, int limit, HttpServletRequest request, HttpServletResponse response) {

		UserBo userBo = getUserLogin(request);
		ShowBo showBo = showService.findById(showid);
		if (showBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_NULL.getIndex(), ERRORCODE.SHOW_NULL.getReason());
		}
		int type = -1;
		if (showBo.getType() == 1) {
			type = 2;
		}
		if (showBo.getType() == 2) {
			type = 1;
		}
		String userid = userBo == null ? "" : userBo.getId();
		List<ShowVo> showVos = new LinkedList<>();
		List<ShowBo> showBos = showService.findByKeyword(showBo.getShowType(), userid, type, page, limit);
		bo2vos(showBos, showVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("recomShowVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("删除演出")
	@ApiImplicitParam(name = "showid", value = "演出id", required = true, paramType = "query", dataType = "string")
	@GetMapping("/delete")
	public String delete(String showid, HttpServletRequest request, HttpServletResponse response) {
		ShowBo showBo = showService.findById(showid);
		if (showBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.SHOW_NULL.getIndex(), ERRORCODE.SHOW_NULL.getReason());
		}
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		// 创建者或者管理员才能删除
		if (userBo.getId().equals(showBo.getCreateuid()) || CommonUtil.getAdminUserids().contains(userBo.getId())) {
			showService.delete(showid);
			return Constant.COM_RESP;
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_NOT_MASTER.getIndex(),
					ERRORCODE.NOTE_NOT_MASTER.getReason());
		}
	}

	@ApiOperation("全部推荐接口")
	@ApiImplicitParam(name = "type", value = "1招演出，2接演出, -1所有", required = true, paramType = "query", dataType = "int")
	@GetMapping("/my-show-recoms")
	public String myShowRecoms(int type, HttpServletRequest request) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		int recomType = -1;
		if (type != -1) {
			recomType = type == DiscoveryConstants.NEED ? DiscoveryConstants.PROVIDE : DiscoveryConstants.NEED;
		}
		List<ShowBo> showBos = showService.findByMyShows(userBo.getId(), recomType);
		LinkedHashSet<String> showTypes = new LinkedHashSet<>();
		if (showBos != null) {
			showBos.forEach(showBo -> showTypes.add(showBo.getShowType()));
		}
		List<ShowVo> showVos = new LinkedList<>();
		if (!showTypes.isEmpty()) {
			List<ShowBo> recomShows = showService.findRecomShows(userBo.getId(), showTypes, type);
			bo2vos(recomShows, showVos, userBo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("recomShowVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("圈子演出关联后推荐接口")
	@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, paramType = "query", dataType = "string")
	@GetMapping("/circle-recoms")
	public String circleRecoms(String circleid, HttpServletRequest request) {
		UserBo userBo = getUserLogin(request);
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(), ERRORCODE.CIRCLE_IS_NULL.getReason());
		}
		if (!circleBo.isTakeShow()) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_SHOW_CLOSE.getIndex(),
					ERRORCODE.CIRCLE_SHOW_CLOSE.getReason());
		}
		// 圈子是否发表过接演出信息
		List<ShowBo> showBos = showService.findByCircleid(circleid, 0, DiscoveryConstants.PROVIDE);
		LinkedHashSet<String> showTypes = new LinkedHashSet<>();
		// 获取发布过的所有演出类型
		if (!CommonUtil.isEmpty(showBos)) {
			for (ShowBo showbo : showBos) {
				showTypes.add(showbo.getShowType());
			}
		} else {
			// 因为圈子标题比类型长，所以数据库匹配不行，需要圈子名称去匹配所有类型
			String cirName = circleBo.getName();
			// 获取演出分类列表
			List<CircleTypeBo> circleTypeBos = circleService.selectByLevel(1, CircleTypeBo.SHOW_TYPE);
			if (circleTypeBos != null) {
				circleTypeBos.forEach(typeBo -> {
					String regex = String.format("^.*%s.*$", typeBo.getCategory());
					// 如果分类名不等于"其他"并且圈子名包含表演分类名,则将演出类型封装到list
					if (!"其他".equals(typeBo.getCategory()) && cirName.matches(regex)) {
						showTypes.add(typeBo.getCategory());
					}
				});
			}
		}
		List<ShowVo> showVos = new LinkedList<>();
		List<ShowBo> recomShows = showService.findCircleRecoms(showTypes);
		bo2vos(recomShows, showVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("recomShowVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("招接演出搜索")
	@ApiImplicitParams({ @ApiImplicitParam(name = "keyword", value = "搜索关键字", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "type", value = "1 招演出， 2 接演出， -1 所有", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "page", value = "页码", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "条数", paramType = "query", dataType = "int") })
	@GetMapping("/search")
	public String showSearch(String keyword, int type, int page, int limit, HttpServletRequest request) {
		UserBo userBo = getUserLogin(request);
		List<ShowBo> showBos = showService.findByKeword(keyword, type, page, limit);
		List<ShowVo> showVos = new LinkedList<>();
		bo2vos(showBos, showVos, userBo);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showVos", showVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("查找所有演出类型")
	@GetMapping("/show-types")
	public String showTypes() {
		List<CircleTypeBo> circleTypeBos = circleService.selectByLevel(1, CircleTypeBo.SHOW_TYPE);
		LinkedHashSet<String> showTypes = new LinkedHashSet<>();
		if (circleTypeBos != null) {
			circleTypeBos.forEach(circleTypeBo -> showTypes.add(circleTypeBo.getCategory()));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("showTypes", showTypes);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 *
	 * @param showBos
	 * @param showVos
	 * @param loginUser
	 */
	private void bo2vos(List<ShowBo> showBos, List<ShowVo> showVos, UserBo loginUser) {
		String userid = loginUser != null ? loginUser.getId() : "";
		// 过期的商家发布信息
		List<String> showids = new LinkedList<>();
		for (ShowBo showBo : showBos) {
			// 已过期的不再添加
			if (showBo.getType() == DiscoveryConstants.NEED && isTimeout(showBo.getShowTime())) {
				showids.add(showBo.getId());
				continue;
			} else if (showBo.getType() == DiscoveryConstants.PROVIDE && isEndTimeout(showBo.getEndTime())) {
				showids.add(showBo.getId());
				continue;
			}
			ShowVo showVo = new ShowVo();
			BeanUtils.copyProperties(showBo, showVo);
			showVo.setShowid(showBo.getId());
			if (DiscoveryConstants.PROVIDE == showBo.getType()) {
				CircleBo circleBo = circleService.selectById(showBo.getCircleid());
				showVo.setCirName(circleBo == null ? "" : circleBo.getName());
			}
			UserBaseVo baseVo = new UserBaseVo();
			UserBo createUser = null;
			String friendName = "";
			if (loginUser == null) {
				createUser = userService.getUser(showBo.getCreateuid());
			} else {
				if (userid.equals(showBo.getCreateuid())) {
					BeanUtils.copyProperties(loginUser, baseVo);
					showVo.setCreate(true);
				} else {
					createUser = userService.getUser(showBo.getCreateuid());
					// 查询是否是好友关系
					FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, showBo.getCreateuid());
					if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
						friendName = friendsBo.getBackname();
					}
				}
			}
			if (createUser != null) {
				BeanUtils.copyProperties(createUser, baseVo);
				baseVo.setUserName(!"".equals(friendName) ? friendName : createUser.getUserName());
			}
			showVo.setCreatUser(baseVo);
			showVos.add(showVo);
		}
		// 过期招演信息更新
		if (!showids.isEmpty()) {
			showService.updateShowStatus(showids, 1);
		}
	}

	/**
	 * 判断是否超时
	 * 
	 * @param timeStr
	 * @return
	 */
	private boolean isTimeout(String timeStr) {
		// 已超时的不在推送
		Date time = CommonUtil.getDate(timeStr, "yyyy-MM-dd HH:mm");
		if (time != null) {
			return System.currentTimeMillis() > time.getTime();
		}
		return false;
	}

	/**
	 * 判断是否超时
	 * 
	 * @param timeStr
	 * @return
	 */
	private boolean isEndTimeout(String timeStr) {
		if (StringUtils.isEmpty(timeStr)) {
			return false;
		}
		// 已超时的不在推送
		Date time = CommonUtil.getDate(timeStr, "yyyy-MM-dd");
		if (time != null) {
			return System.currentTimeMillis() >= CommonUtil.getLastDate(time).getTime();
		}
		return false;
	}
}
