package com.lad.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.lad.bo.CircleBo;
import com.lad.bo.CircleShowBo;
import com.lad.bo.CollectBo;
import com.lad.bo.CommentBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.FriendsBo;
import com.lad.bo.NoteBo;
import com.lad.bo.ReadHistoryBo;
import com.lad.bo.RestHomeBo;
import com.lad.bo.ShowBo;
import com.lad.bo.ThumbsupBo;
import com.lad.bo.UserBo;
import com.lad.constants.DiscoveryConstants;
import com.lad.constants.GeneralContants;
import com.lad.service.ICollectService;
import com.lad.service.ICommentService;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.ILocationService;
import com.lad.service.IRestHomeService;
import com.lad.service.IShowService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.CommentVo;
import com.lad.vo.NoteVo;
import com.lad.vo.UserBaseVo;
import com.lad.vo.UserNoteVo;
import com.lad.vo.UserThumbsupVo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lad.scrapybo.BroadcastBo;
import lad.scrapybo.DailynewsBo;
import lad.scrapybo.InforBo;
import lad.scrapybo.SecurityBo;
import lad.scrapybo.VideoBo;
import lad.scrapybo.YanglaoBo;
import net.sf.json.JSONObject;

@Api(value = "NoteController", description = "帖子相关接口")
@RestController
@RequestMapping("note")
public class NoteController extends ExtraController {

	private static final String NoteQualiName = "com.lad.controller.NoteController";
	private final Logger logger = LogManager.getLogger(NoteController.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private ICommentService commentService;

	@Autowired
	private ILocationService locationService;

	@Autowired
	private IDynamicService dynamicService;

	@Autowired
	private IFriendsService friendsService;

	@Autowired
	private ICollectService collectService;

	@Autowired
	private AsyncController asyncController;
	private String pushTitle = "互动通知";

	/**
	 * 获取我加入的所有圈子中所有帖子,并标注这些帖子是否已读
	 * 
	 * @param page
	 * @param limit
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("我圈子的新帖")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/note-not-read")
	public String criNotRead(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info(NoteQualiName + ".criNotRead-----{user:" + userBo.getUserName() + ",userId:" + userBo.getId()
				+ ",page:" + page + ",limit:" + limit + "}");
		String userid = userBo.getId();
		List<CircleBo> circleBos = circleService.selectByuserid(userid);
		List<String> circleids = new LinkedList<>();
		// 获取圈子id 并保存到circleids
		circleBos.forEach(circleBo -> circleids.add(circleBo.getId()));
		// List<NoteBo> noteBos = noteService.dayNewNotes(circleids, page,
		// limit);
		List<NoteBo> noteBos = noteService.joinCircleNotes(circleids, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			// 获取创建者消息
			String createuid = noteBo.getCreateuid() == null ? "" : noteBo.getCreateuid();
			UserBo createUser = userService.getUser(createuid);
			boToVo(noteBo, noteVo, createUser, userid);
			// 是否阅读需要重写
			ReadHistoryBo historyBo = readHistoryService.getHistoryByUseridAndNoteId(userid, noteBo.getId());
			noteVo.setRead(historyBo != null);
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			noteVo.setCirName(circleBo != null ? circleBo.getName() : "");
			noteVoList.add(noteVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 发表帖子 1.检查用户登录状态 2.更新圈子访问记录信息
	 * 
	 * @param noteJson
	 * @param atUserids
	 * @param pictures
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("发表帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteJson", value = "帖子信息json数据", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "atUserids", value = "帖子中@的用户id，多个以逗号隔开", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "pictures", value = "图片或视频文件流", dataType = "file") })
	@PostMapping("/insert")
	public String insert(String noteJson, String atUserids, MultipartFile[] pictures, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userId = userBo.getId();
		logger.info(NoteQualiName + ".insert-----{noteJson:" + noteJson + ",atUserids:" + atUserids + "}");
		NoteBo noteBo = null;
		try {
			JSONObject jsonObject = JSONObject.fromObject(noteJson);
			noteBo = (NoteBo) JSONObject.toBean(jsonObject, NoteBo.class);
		} catch (Exception e) {
			return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(), ERRORCODE.FORMAT_ERROR.getReason());
		}

		String circleid = noteBo.getCircleId();
		updateHistory(userId, circleid, locationService, circleService);
		// 设置访问人数为1
		noteBo.setVisitcount(1);
		// 设置创建者为当前登录者
		noteBo.setCreateuid(userId);
		// 设置热度为1
		noteBo.setTemp(1);
		// 设置创日期
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		// 检查并上传图片
		LinkedList<String> photos = new LinkedList<>();
		if (pictures != null) {
			for (MultipartFile file : pictures) {
				Long time = Calendar.getInstance().getTimeInMillis();
				String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
				if ("video".equals(noteBo.getType())) {
					String[] paths = CommonUtil.uploadVedio(file, Constant.NOTE_PICTURE_PATH, fileName, 0);
					photos.add(paths[0]);
					noteBo.setVideoPic(paths[1]);
				} else {
					String path = CommonUtil.upload(file, Constant.NOTE_PICTURE_PATH, fileName, 0);
					logger.info(NoteQualiName + ".insert-----note add note pic path: {},  size: {} ", path,
							file.getSize());
					photos.add(path);
				}
			}
		}
		noteBo.setPhotos(photos);
		// 设置@的用户列表
		String[] useridArr = null;
		if (!StringUtils.isEmpty(atUserids)) {
			useridArr = CommonUtil.getIds(atUserids);
			LinkedList<String> atUsers = new LinkedList<>();
			Collections.addAll(atUsers, useridArr);
			noteBo.setAtUsers(atUsers);
		}
		// 将设置好的帖子实体添加进入数据库
		NoteBo insert = noteService.insert(noteBo);
		// 向@的人发送push信息
		if (useridArr != null) {
			String path = String.format("/note/note-info.do?noteid=%s&type=%s", noteBo.getId(), noteBo.getType());
			String content = "有人刚刚在帖子提到了您，快去看看吧!";

			usePush(useridArr, pushTitle, content, path);

//			addCrcular(Arrays.asList(useridArr),pushTitle, content,path);
			addMessage(messageService, path, content, pushTitle, noteBo.getId(), useridArr);
		}
		// 设置当前用户访问该帖子的历史
		if (insert != null) {
			ReadHistoryBo historyBo = new ReadHistoryBo();
			historyBo.setReaderId(insert.getCreateuid());
			historyBo.setBeReaderId(insert.getId());
			historyBo.setType(0);
			historyBo.setReadNum(1);
			readHistoryService.addReadHistory(historyBo);
		}
		// 圈子表演?
		addCircleShow(noteBo);
		// 圈子帖子数量+1
		asyncController.updateCircieNoteSize(circleid, 1);
		// 如果设置了同步则同步状态
		if (noteBo.isAsync()) {
			DynamicBo dynamicBo = new DynamicBo();
			dynamicBo.setTitle(noteBo.getSubject());
			dynamicBo.setView("");
			dynamicBo.setCreateuid(userId);
			dynamicBo.setOwner(noteBo.getCreateuid());
			dynamicBo.setLandmark(noteBo.getLandmark());
			dynamicBo.setForward(GeneralContants.YES);
			dynamicBo.setType(Constant.NOTE_TYPE);
			CircleBo circleBo = circleService.selectById(circleid);
			if (circleBo != null) {
				dynamicBo.setSourceName(circleBo.getName());
				dynamicBo.setSourceId(noteBo.getId());
			}
//			dynamicBo.setPicType(noteBo.getType());
			if (noteBo.getType().equals("video")) {
				dynamicBo.setVideoPic(noteBo.getVideoPic());
				dynamicBo.setVideo(noteBo.getPhotos().getFirst());
			} else {
				dynamicBo.setPhotos(new LinkedHashSet<>(noteBo.getPhotos()));
			}
			dynamicBo.setCreateuid(userBo.getId());
			List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
			dynamicBo.setUnReadFrend(new LinkedHashSet<>(friends));
			dynamicService.addDynamic(dynamicBo);
		}
		// ?
		updateDynamicNums(userId, 1, dynamicService, redisServer);
		// 用户等级设置
		userService.addUserLevel(userBo.getId(), 1, Constant.LEVEL_NOTE, 0);
		// 更新圈子热度
		updateCircleHot(circleService, redisServer, circleid, 1, Constant.CIRCLE_NOTE);
		updateCircleHot(circleService, redisServer, circleid, 1, Constant.CIRCLE_NOTE_VISIT);

		// 更新圈子成员未读帖子列表 重写了updateCircieNoteUnReadNum,添加了noteId字段
		asyncController.updateCircieNoteUnReadNum(userId, circleid, insert.getId());
		// 返回结果
		NoteVo noteVo = new NoteVo();
		boToVo(noteBo, noteVo, userBo, userId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVo", noteVo);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("更新帖子图片")
	@ApiImplicitParams({ @ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string"),
			@ApiImplicitParam(name = "photos", value = "图片或视频文件流数组", required = true, dataType = "file") })
	@PostMapping("/photo")
	public String note_picture(@RequestParam("photos") MultipartFile[] files,
			@RequestParam(required = true) String noteid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userId = userBo.getId();

		NoteBo noteBo = noteService.selectById(noteid);
		if (null == noteBo) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		LinkedList<String> photos = noteBo.getPhotos();
		List<String> paths = new ArrayList<>();
		for (MultipartFile file : files) {
			Long time = Calendar.getInstance().getTimeInMillis();
			String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
			String path = CommonUtil.upload(file, Constant.NOTE_PICTURE_PATH, fileName, 0);
			photos.add(path);
			paths.add(path);
		}
		noteService.updatePhoto(noteid, photos);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("path", paths);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("帖子点赞")
	@PostMapping("/thumbsup")
	public String thumbsup(String noteid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		NoteBo noteBo = noteService.selectById(noteid);
		if (noteBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		// 点赞push一条龙服务
		thumbsup(userBo, noteBo,true);
		return Constant.COM_RESP;
	}

	@ApiOperation("帖子点赞列表")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/thumbsup-list")
	public String noteThumbsups(String noteid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		boolean isLogin = userBo != null;
		String userid = isLogin ? "" : userBo.getId();
		List<ThumbsupBo> thumbsupBos = thumbsupService.selectByOwnerIdPaged(page, limit, noteid, ThumbsupBo.THUMBSUP_NOTE);
		List<UserBaseVo> userVos = new LinkedList<>();
		for (ThumbsupBo thumbsupBo : thumbsupBos) {
			UserBaseVo baseVo = new UserBaseVo();
			if (userid.endsWith(thumbsupBo.getVisitor_id())) {
				BeanUtils.copyProperties(userBo, baseVo);
				userVos.add(baseVo);
			} else {
				UserBo user = userService.getUser(thumbsupBo.getVisitor_id());
				if (user == null) {
					continue;
				}
				BeanUtils.copyProperties(user, baseVo);
				if (isLogin) {
					FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, user.getId());
					if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
						baseVo.setUserName(friendsBo.getBackname());
					}
				}
				userVos.add(baseVo);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("userVos", userVos);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("取消帖子点赞")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/cancal-thumbsup")
	public String cancelThumbsup(String noteid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(noteid, userBo.getId());
		if (thumbsupBo != null) {
			thumbsupService.deleteById(thumbsupBo.getId());
			NoteBo noteBo = noteService.selectById(noteid);
			updateCircleHot(circleService, redisServer, noteBo.getCircleId(), -1, Constant.CIRCLE_NOTE_THUMP);
			updateNoteCount(noteid, Constant.THUMPSUB_NUM, -1);
			messageService.deleteMessageBySource(thumbsupBo.getId(), 2);
		}
		return Constant.COM_RESP;
	}

	@ApiOperation("获取帖子详情")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/note-info")
	public String noteInfo(String noteid, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			NoteBo noteBo = noteService.selectById(noteid);
			if (null == noteBo) {
				return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
			}
			logger.info("com.lad.controller.NoteController.noteInfo=====[noteid:{}]", noteid);
			UserBo userBo = getUserLogin(request);
			NoteVo noteVo = new NoteVo();
			String userid = "";
			if (userBo != null) {
				userid = userBo.getId();
				logger.info("com.lad.controller.NoteController.noteInfo=====[user:{},userid:{}]", userBo.getUserName(),
						userid);
				// 添加访问历史
				updateHistory(userBo.getId(), noteBo.getCircleId(), locationService, circleService);
				// 处理是否已读
				userReasonHander(userid, noteBo.getCircleId(), noteBo.getId());
				// 这个帖子自己是否点赞
				ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(noteid, userid);
				noteVo.setMyThumbsup(null != thumbsupBo);
				// 是否收藏
				CollectBo collectBo = collectService.findByUseridAndTargetid(userid, noteid);
				noteVo.setCollect(collectBo != null);
			}

			// 热度处理
			updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_VISIT);

			updateNoteCount(noteid, Constant.VISIT_NUM, 1);
			boToVo(noteBo, noteVo, userService.getUser(noteBo.getCreateuid()), userid);
			CircleBo circleBo = circleService.selectByIdIgnoreDel(noteBo.getCircleId());
			if (circleBo != null) {
				noteVo.setCirName(circleBo.getName());
				noteVo.setCirHeadPic(circleBo.getHeadPicture());
				noteVo.setCirNoteNum(circleBo.getNoteNum());
				noteVo.setCirUserNum(circleBo.getUsers().size());
			}
			noteVo.setVisitCount(noteBo.getVisitcount() + 1);
			map.put("ret", 0);
			map.put("noteVo", noteVo);
		} catch (Exception e) {
			logger.error(e);
		}
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 最新动态帖子
	 */
	@ApiOperation("获取圈子内最新动态帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/new-situation")
	public String newSituation(String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		List<NoteBo> noteBos = noteService.finyByCreateTime(circleid, page, limit);
		List<NoteVo> noteVos = new LinkedList<>();
		UserBo loginUser = getUserLogin(request);
		if (noteBos != null) {
			vosToList(noteBos, noteVos, loginUser);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 精华帖子，（字数100以上,按浏览量倒序，取消）,取前10
	 */
	@ApiOperation("获取圈子内精华帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "页码", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/essential-note")
	public String bestNote(String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		List<NoteBo> noteBos = noteService.findByTopEssence(circleid, Constant.NOTE_JIAJING, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		UserBo loginUser = getUserLogin(request);
		vosToList(noteBos, noteVoList, loginUser);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 获取置顶帖子，（置顶帖子条件，字数>=200, 图片>=3, 取消）时间倒序取前2
	 * 
	 * @return
	 */
	@ApiOperation("获取圈子内置顶的帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页时最后一条数据id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/top-notes")
	public String topNotes(String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		limit = limit < 1 ? 2 : limit;
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.findByTopEssence(circleid, Constant.NOTE_TOP, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 热门详情；1周内帖子的阅读数+赞数+转发数+评论数最多的列表，取前10
	 * 
	 * @return
	 */
	@ApiOperation("热门帖子")
	@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/hot-notes")
	public String hotNotes(String circleid, HttpServletRequest request, HttpServletResponse response) {
		List<NoteBo> noteBos = noteService.selectHotNotes(circleid);
		List<NoteVo> noteVoList = new LinkedList<>();
		UserBo loginUser = getUserLogin(request);
		vosToList(noteBos, noteVoList, loginUser);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 评论帖子或者回复评论
	 * 
	 * @return
	 */
	@ApiOperation("评论帖子或者回复评论")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "countent", value = "评论内容", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "parentid", value = "父评论id", dataType = "string", paramType = "query") })
	@PostMapping("/add-comment")
	public String addComment(@RequestParam(required = true) String noteid,
			@RequestParam(required = true) String countent, String parentid, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		NoteBo noteBo = noteService.selectById(noteid);
		if (noteBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		updateHistory(userBo.getId(), noteBo.getCircleId(), locationService, circleService);
		Date currentDate = new Date();
		CommentBo commentBo = new CommentBo();
		commentBo.setNoteid(noteBo.getId());
		commentBo.setParentid(parentid);
		commentBo.setUserName(userBo.getUserName());
		commentBo.setContent(countent);
		commentBo.setType(Constant.NOTE_TYPE);
		commentBo.setCreateuid(userBo.getId());
		commentBo.setOwnerid(noteBo.getCreateuid());
		commentBo.setCreateTime(currentDate);
		commentService.insert(commentBo);

		updateNoteCount(noteid, Constant.COMMENT_NUM, 1);
		userService.addUserLevel(userBo.getId(), 1, Constant.LEVEL_COMMENT, 0);
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_COMMENT);
		asyncController.updateRedStar(userBo, noteBo, noteBo.getCircleId(), currentDate);
		asyncController.updateCircieUnReadNum(noteBo.getCreateuid(), noteBo.getCircleId());
		String path = "/note/note-info.do?noteid=" + noteid;
		String content = "有人刚刚评论了你的帖子，快去看看吧!";

		usePush(noteBo.getCreateuid(), pushTitle, content, path);
		List<String> list = new ArrayList<>();
		list.add(noteBo.getCreateuid());
//		addCrcular(list,pushTitle, content,path);


		addMessage(messageService, path, content, pushTitle, noteid, 1, commentBo.getId(), noteBo.getCircleId(),
				userBo.getId(), noteBo.getCreateuid());
		if (!StringUtils.isEmpty(parentid)) {
			CommentBo comment = commentService.findById(parentid);
			if (comment != null) {
				asyncController.updateCircieUnReadNum(comment.getCreateuid(), noteBo.getCircleId());
				content = "有人刚刚回复了你的评论，快去看看吧!";

				usePush(comment.getCreateuid(), pushTitle, content, path);

				List<String> master = new ArrayList<>();
				master.add(comment.getCreateuid());
//				addCrcular(master,pushTitle, content,path);
				addMessage(messageService, path, content, pushTitle, noteid, 1, comment.getId(), noteBo.getCircleId(),
						userBo.getId(), noteBo.getCreateuid());
			}
		}
//		CommentBo commentBo = comment(userBo, noteBo, countent, new LinkedHashSet<>());
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("commentVo", comentBo2Vo(commentBo));
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 删除自己的帖子评论
	 * 
	 * @return
	 */
	@ApiOperation("删除自己的帖子评论")
	@ApiImplicitParam(name = "commentid", value = "评论id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/delete-self-comment")
	public String deleteComments(String commentid, HttpServletRequest request, HttpServletResponse response) {

		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		CommentBo commentBo = commentService.findById(commentid);
		if (commentBo != null) {
			if (userBo.getId().equals(commentBo.getCreateuid())) {
				commentService.delete(commentid);
				messageService.deleteMessageBySource(commentid, 1);
				updateNoteCount(commentBo.getNoteid(), Constant.COMMENT_NUM, -1);
			} else {
				return CommonUtil.toErrorResult(ERRORCODE.NOTE_NOT_MASTER.getIndex(),
						ERRORCODE.NOTE_NOT_MASTER.getReason());
			}
		}
		return Constant.COM_RESP;
	}

	/**
	 * 获取帖子评论
	 * 
	 * @return
	 */
	@ApiOperation("获取帖子的评论")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "gt", value = "true，获取之后的数据，false 之前数据", required = true, dataType = "boolean", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/get-comments")
	public String getComments(String noteid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		NoteBo noteBo = noteService.selectById(noteid);
		if (noteBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		String userid = "";
		boolean isLogin = userBo != null;
		if (isLogin) {
			updateHistory(userBo.getId(), noteBo.getCircleId(), locationService, circleService);
			userid = userBo.getId();
		}

		List<CommentBo> commentBos = commentService.selectByNoteid(noteid, page, limit);
		List<CommentVo> commentVos = new ArrayList<>();
		for (CommentBo commentBo : commentBos) {
			CommentVo commentVo = comentBo2Vo(commentBo);
			ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(commentBo.getId(), userid);
			commentVo.setIsMyThumbsup(thumbsupBo != null);
			commentVo.setThumpsubCount(commentBo.getThumpsubNum());
			if (!StringUtils.isEmpty(commentBo.getParentid())) {
				CommentBo parent = commentService.findById(commentBo.getParentid());
				if (isLogin && !userid.equals(commentBo.getParentid())) {
					FriendsBo bo = friendsService.getFriendByIdAndVisitorIdAgree(userid, commentBo.getParentid());
					if (bo == null || StringUtils.isEmpty(bo.getBackname())) {
						commentVo.setParentUserName(parent.getUserName());
					} else {
						commentVo.setParentUserName(bo.getBackname());
					}
				} else {
					commentVo.setParentUserName(parent.getUserName());
				}
				commentVo.setParentUserid(parent.getCreateuid());
			}
			UserBo comUser = userService.getUser(commentBo.getCreateuid());
			if (isLogin && !userid.equals(commentBo.getCreateuid())) {
				FriendsBo bo = friendsService.getFriendByIdAndVisitorIdAgree(userid, commentBo.getCreateuid());
				if (bo == null || StringUtils.isEmpty(bo.getBackname())) {
					commentVo.setUserName(comUser.getUserName());
				} else {
					commentVo.setUserName(bo.getBackname());
				}
			} else {
				commentVo.setUserName(commentBo.getUserName());
			}
			commentVo.setUserHeadPic(comUser.getHeadPictureName());
			commentVo.setUserid(commentBo.getCreateuid());
			commentVo.setUserBirth(comUser.getBirthDay());
			commentVo.setUserSex(comUser.getSex());
			commentVo.setUserLevel(comUser.getLevel());
			commentVos.add(commentVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("commentVoList", commentVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 获取自己的所有评论
	 * 
	 * @return
	 */
	@ApiOperation("获取自己的所有评论列表")
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页时最后一条数据id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/get-self-comments")
	public String getSelfComments(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		List<CommentBo> commentBos = commentService.selectByUser(userBo.getId(), page, limit);
		List<CommentVo> commentVos = new ArrayList<>();
		for (CommentBo commentBo : commentBos) {
			commentVos.add(comentBo2Vo(commentBo));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("commentVoList", commentVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 *
	 * @return
	 */
	@ApiOperation("获取自己评论过别人的帖子")
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/my-comment-notes")
	public String getMyCommentNotes(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userid = userBo.getId();
		List<BasicDBObject> objects = commentService.selectMyNoteReply(userid, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (BasicDBObject object : objects) {
			String id = object.getString("noteid");
			if("".equals(id)|| id == null){
				continue;
			}
			NoteBo noteBo = noteService.selectById(id);
			if (noteBo != null) {
				CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
				if (circleBo != null) {
					NoteVo noteVo = new NoteVo();
					noteVo.setCirName(circleBo.getName());
					noteVo.setCirHeadPic(circleBo.getHeadPicture());
					noteVo.setCirNoteNum(circleBo.getNoteSize());
					noteVo.setCirVisitNum(circleBo.getVisitNum());
					UserBo author = userService.getUser(noteBo.getCreateuid());
					boToVo(noteBo, noteVo, author, userid);
					noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
					noteVoList.add(noteVo);
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 评论点赞或取消点赞
	 * 
	 * @return
	 */
	@ApiOperation("对评论点赞或取消评论点赞")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "commentid", value = "评论id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "isThumnbsup", value = "true，点赞，false 取消点赞", required = true, dataType = "boolean", paramType = "query") })
	@PostMapping("/comment-thumbsup")
	public String commentThumbsup(String commentid, boolean isThumnbsup, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		
		CommentBo commentBo = commentService.findById(commentid);
		String result = null;
		if (commentBo!=null) {
			int thumbsup = thumbsup(userBo, commentBo, isThumnbsup);
			if(thumbsup == 0) {
				result = Constant.COM_RESP;
			}else {
				result = Constant.RESP_SUCCES;
			}
		}
		return result;
	}

	/**
	 * 获取帖子点赞列表
	 * 
	 * @return
	 */
	@ApiOperation("获取指定帖子的点赞用户列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/get-note-thumbsups")
	public String getNoteThumbsups(String noteid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		// TODO
		List<ThumbsupBo> thumbsupBos = thumbsupService.selectByOwnerIdPaged(page, limit, noteid, ThumbsupBo.THUMBSUP_NOTE);
		List<UserThumbsupVo> userVos = new ArrayList<>();
		UserBo userBo = getUserLogin(request);
		boolean isLogin = userBo != null;
		String userid = isLogin ? "" : userBo.getId();
		for (ThumbsupBo thumbsupBo : thumbsupBos) {
			UserThumbsupVo baseVo = new UserThumbsupVo();
			if (userid.equals(thumbsupBo.getVisitor_id())) {
				BeanUtils.copyProperties(userBo, baseVo);
			} else {
				UserBo user = userService.getUser(thumbsupBo.getVisitor_id());
				if (user == null) {
					continue;
				}
				BeanUtils.copyProperties(user, baseVo);
				if (isLogin) {
					FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, user.getId());
					if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
						baseVo.setUserName(friendsBo.getBackname());
					}
				}
			}
			baseVo.setThumbsupTime(thumbsupBo.getCreateTime());
			userVos.add(baseVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("userVoList", userVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 我的帖子
	 * 
	 * @return
	 */
	@ApiOperation("我的帖子")
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/my-notes")
	public String myNotes(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String loginUserid = userBo.getId();
		List<NoteBo> noteBos = noteService.selectMyNotes(userBo.getId(), page, limit);
		if (noteBos != null && !noteBos.isEmpty()) {
			updateHistory(userBo.getId(), noteBos.get(0).getCircleId(), locationService, circleService);
		}
		List<NoteVo> noteVoList = new LinkedList<>();
		NoteVo noteVo = null;
		for (NoteBo noteBo : noteBos) {
			noteVo = new NoteVo();
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			if (circleBo != null) {
				noteVo.setCirName(circleBo.getName());
				noteVo.setCirNoteNum(circleBo.getNoteSize());
				noteVo.setCirHeadPic(circleBo.getHeadPicture());
				noteVo.setCirVisitNum(circleBo.getVisitNum());
				boToVo(noteBo, noteVo, userBo, loginUserid);
				noteVo.setMyThumbsup(hasThumbsup(loginUserid, noteBo.getId()));
				noteVo.setRead(true);
				noteVoList.add(noteVo);
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 圈主删除帖子
	 * 
	 * @return
	 */
	@ApiOperation("圈主或管理员删除帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteids", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query") })
	@PostMapping("/delete-circle-notes")
	public String deleteNotes(@RequestParam String noteids, @RequestParam String circleid, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		CircleBo circleBo = circleService.selectById(circleid);
		if (null == circleBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(), ERRORCODE.CIRCLE_IS_NULL.getReason());
		}
		updateHistory(userBo.getId(), circleBo.getId(), locationService, circleService);
		String[] ids = CommonUtil.getIds(noteids);
		int notes = 0;
		if (circleBo.getCreateuid().equals(userBo.getId()) || circleBo.getMasters().contains(userBo.getId())) {
			for (String id : ids) {
				NoteBo noteBo = noteService.selectById(id);
				if (null != noteBo) {
					// 圈主删除帖子
					noteService.deleteNote(id, userBo.getId());
					deleteShouw(id);
					commentService.deleteByNote(id);
					messageService.deleteMessageByNoteid(id, -1);
					notes++;
				}
			}
			if (notes != 0) {
				asyncController.updateCircieNoteSize(circleid, -notes);
			}
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_NOT_MASTER.getIndex(),
					ERRORCODE.NOTE_NOT_MASTER.getReason());
		}
		return Constant.COM_RESP;
	}

	/**
	 * 个人删除自己的帖子
	 * 
	 * @return
	 */
	@ApiOperation("自己删除自己的帖子")
	@ApiImplicitParam(name = "noteids", value = "帖子id，多个以逗号隔开", required = true, dataType = "string", paramType = "query")
	@PostMapping("/delete-my-notes")
	public String deleteMyNotes(@RequestParam String noteids, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String[] ids = CommonUtil.getIds(noteids);
		for (String id : ids) {
			NoteBo noteBo = noteService.selectById(id);
			if (null != noteBo) {
				// 删除帖子
				if (noteBo.getCreateuid().equals(userBo.getId())) {
					noteService.deleteNote(id, userBo.getId());
					commentService.deleteByNote(id);
					messageService.deleteMessageByNoteid(id, -1);
					asyncController.updateCircieNoteSize(noteBo.getCircleId(), -1);
					deleteShouw(id);
				}
			}
		}
		return Constant.COM_RESP;
	}

	/**
	 * 圈子内帖子
	 */
	@ApiOperation("获取圈子帖子列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/circle-notes")
	public String ciecleNotes(@RequestParam String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(), ERRORCODE.CIRCLE_IS_NULL.getReason());
		}
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.selectCircleNotes(circleid, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 圈子管理员加精帖子
	 */

	@ApiOperation("加精帖子或取消加精")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "essence", value = "标识，0 取消，1 加精", dataType = "int", paramType = "query") })
	@PostMapping("/set-essence")
	public String setEssence(@RequestParam String circleid, @RequestParam String noteid, int essence,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo.getCreateuid().equals(userBo.getId()) || circleBo.getMasters().contains(userBo.getId())) {
			noteService.updateToporEssence(noteid, essence, Constant.NOTE_JIAJING);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_MASTER_NULL.getIndex(),
					ERRORCODE.CIRCLE_MASTER_NULL.getReason());
		}
		return Constant.COM_RESP;
	}

	@ApiOperation("置顶帖子或取消")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "top", value = "0 取消置顶，1 置顶", dataType = "int", paramType = "query") })
	@PostMapping("/set-top")
	public String setTopNotes(@RequestParam String circleid, @RequestParam String noteid, int top,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo.getCreateuid().equals(userBo.getId()) || circleBo.getMasters().contains(userBo.getId())) {
			noteService.updateToporEssence(noteid, top, Constant.NOTE_TOP);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_MASTER_NULL.getIndex(),
					ERRORCODE.CIRCLE_MASTER_NULL.getReason());
		}
		return Constant.COM_RESP;
	}

	/**
	 * 置顶和精华帖子
	 */
	@ApiOperation("获取圈子中置顶和精华的帖子列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/top-essence")
	public String topAndessence(String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.findByTopAndEssence(circleid, 1, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("转发帖子到我的动态")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "被转发的帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "view", value = "转发说明信息", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "landmark", value = "转发时的地标", dataType = "string", paramType = "query") })
	@PostMapping("/forward-dynamic")
	public String forwardDynamic(String noteid, String view, String landmark, HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("@PostMapping(\"/forward-dynamic\")=====noteid:{},view:{},landmark:{}",noteid,view,landmark);
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		NoteBo noteBo = noteService.selectById(noteid);
		if (null == noteBo) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		DynamicBo dynamicBo = new DynamicBo();
		dynamicBo.setTitle(noteBo.getSubject());
		dynamicBo.setView(view);
		// String sourceId = noteBo.getForward()==1?noteBo.getSourceid():noteid;
		dynamicBo.setSourceId(noteid);
		dynamicBo.setCreateuid(userBo.getId());
		dynamicBo.setOwner(noteBo.getCreateuid());
		dynamicBo.setLandmark(landmark);
		dynamicBo.setForward(GeneralContants.YES);
		dynamicBo.setType(Constant.NOTE_TYPE);
		if (noteBo.getType().equals("video")) {
			dynamicBo.setVideoPic(noteBo.getVideoPic());
			dynamicBo.setVideo(noteBo.getPhotos().getFirst());
		} else {
			dynamicBo.setPhotos(new LinkedHashSet<>(noteBo.getPhotos()));
		}
		//
		CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
		if (circleBo != null) {
			dynamicBo.setSourceName(circleBo.getName());
		}
		dynamicBo.setCreateuid(userBo.getId());

		List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
		dynamicBo.setUnReadFrend(new LinkedHashSet<>(friends));
		dynamicService.addDynamic(dynamicBo);
		updateNoteCount(noteid, Constant.SHARE_NUM, 1);
		updateDynamicNums(userBo.getId(), 1, dynamicService, redisServer);
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_SHARE);
		userReasonHander(userBo.getId(), noteBo.getCircleId(), noteid);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicid", dynamicBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 收藏帖子
	 * 
	 * @param noteid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("帖子收藏")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, paramType = "query", dataType = "string")
	@PostMapping("/col-note")
	public String colNotes(String noteid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		CollectBo chatBo = collectService.findByUseridAndTargetid(userBo.getId(), noteid);
		if (chatBo != null) {
			return CommonUtil.toErrorResult(ERRORCODE.COLLECT_EXIST.getIndex(), ERRORCODE.COLLECT_EXIST.getReason());
		}
		NoteBo noteBo = noteService.selectById(noteid);
		chatBo = new CollectBo();
		chatBo.setCreateuid(userBo.getId());
		chatBo.setUserid(userBo.getId());
		chatBo.setTargetid(noteid);
		chatBo.setType(Constant.COLLET_URL);
		chatBo.setSub_type(Constant.NOTE_TYPE);
		chatBo.setTitle(noteBo.getSubject());
		LinkedList<String> photos = noteBo.getPhotos();
		if ("video".equals(noteBo.getType())) {
			chatBo.setTargetPic(noteBo.getVideoPic());
			if (!CommonUtil.isEmpty(photos)) {
				chatBo.setVideo(noteBo.getPhotos().get(0));
			}
		} else {
			if (!CommonUtil.isEmpty(photos)) {
				chatBo.setTargetPic(noteBo.getPhotos().get(0));
			}
		}
		CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
		if (circleBo != null) {
			chatBo.setSource(circleBo.getName());
			chatBo.setSourceid(noteBo.getCircleId());
			chatBo.setSourceType(5);
		}
		collectService.insert(chatBo);
		userReasonHander(userBo.getId(), noteBo.getCircleId(), noteid);
		updateNoteCount(noteid, Constant.COLLECT_NUM, 1);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("col-time", CommonUtil.time2str(chatBo.getCreateTime()));
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("取消帖子收藏")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, paramType = "query", dataType = "string")
	@PostMapping("/cancel-collect")
	public String cancelCollect(String noteid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		CollectBo collectBo = collectService.findByUseridAndTargetid(userBo.getId(), noteid);
		if (collectBo != null) {
			collectService.delete(collectBo.getId());
		}
		return Constant.COM_RESP;
	}

	/**
	 * 转发其他圈子
	 */
	@ApiOperation("转发帖子到其他圈子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "被转发的帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "circleid", value = "转发到的圈子", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "landmark", value = "转发时的地标", dataType = "string", paramType = "query") })
	@PostMapping("/forward-circle")
	public String forwardCircle(String noteid, String circleid, String landmark, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		NoteBo old = noteService.selectById(noteid);
		if (null == old) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		if (!StringUtils.isEmpty(old.getSourceid())) {
			old = noteService.selectById(old.getSourceid());
		}
		NoteBo noteBo = new NoteBo();
		noteBo.setCircleId(circleid);
		noteBo.setVideoPic(old.getVideoPic());
		noteBo.setSubject(old.getSubject());
		noteBo.setPhotos(old.getPhotos());
		noteBo.setType(old.getType());
		noteBo.setNoteType(NoteBo.NOTE_FORWARD);
		noteBo.setContent(old.getContent());
		noteBo.setCreateuid(userBo.getId());
		noteBo.setLandmark(landmark);
		noteBo.setSourceid(noteid);
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		noteBo.setForward(1);
		NoteBo insert = noteService.insert(noteBo);
		// 更新圈子成员未读帖子列表 重写了updateCircieNoteUnReadNum,添加了noteId字段
		asyncController.updateCircieNoteUnReadNum(userid, circleid, insert.getId());

		addCircleShow(noteBo);
		updateNoteCount(noteid, Constant.SHARE_NUM, 1);
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_SHARE);
		userReasonHander(userBo.getId(), noteBo.getCircleId(), noteid);
		NoteVo noteVo = new NoteVo();
		boToVo(noteBo, noteVo, userBo, userid);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVo", noteVo);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 转发其他圈子
	 */
	@ApiOperation("评论帖子并转发到其他圈子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "noteid", value = "被转发的帖子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "circleid", value = "转发到的圈子", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "landmark", value = "转发时的地标", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "view", value = "转发时的评语", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "atUserids", value = "转发时@的用户id，多个以逗号隔开", dataType = "string", paramType = "query") })
	@PostMapping("/forward-circle-view")
	public String forwardCircleView(String noteid, String circleid, String landmark, String view, String atUserids,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		NoteBo old = noteService.selectById(noteid);
		if (null == old) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		if (!StringUtils.isEmpty(old.getSourceid())) {
			old = noteService.selectById(old.getSourceid());
			if (null == old) {
				return CommonUtil.toErrorResult(ERRORCODE.NOTE_FIRST_NULL.getIndex(),
						ERRORCODE.NOTE_FIRST_NULL.getReason());
			}
		}
		NoteBo noteBo = new NoteBo();
		noteBo.setCircleId(circleid);
		noteBo.setVideoPic(old.getVideoPic());
		noteBo.setPhotos(old.getPhotos());
		noteBo.setType(old.getType());
		noteBo.setNoteType(NoteBo.NOTE_FORWARD);
		noteBo.setContent(old.getContent());
		noteBo.setCreateuid(userBo.getId());
		noteBo.setLandmark(landmark);
		noteBo.setSourceid(old.getId());
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		noteBo.setForward(1);
		noteService.insert(noteBo);
		addCircleShow(noteBo);
		updateNoteCount(noteid, Constant.SHARE_NUM, 1);
		userReasonHander(userBo.getId(), noteBo.getCircleId(), noteid);

		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_SHARE);
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_COMMENT);

		NoteVo noteVo = new NoteVo();
		boToVo(noteBo, noteVo, userBo, userid);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVo", noteVo);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("查找圈子中既没有加精也没有置顶的帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/not-top-essence")
	public String notTopAndessence(String circleid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.findNotTopAndEssence(circleid, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("根据指定日期查找指定类型的帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "date", value = "指定日期字符串，格式yyyy-MM-dd", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "类型，0 普通帖子，1置顶帖子，2加精帖子，3置顶且加精", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/by-assign-date")
	public String findByDateAndType(String circleid, String date, int type, int page, int limit,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		Date dateTime;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dateTime = sf.parse(date);
		} catch (Exception e) {
			logger.error("Date Format Error {} ", e);
			return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(), ERRORCODE.FORMAT_ERROR.getReason());
		}
		List<NoteBo> noteBos = noteService.findByDate(circleid, dateTime, type, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("根据帖子标题关键字或帖子类型搜索")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "title", value = "标题关键字，空表示按照类型搜索", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "帖子类型，根据创建帖子的type确定是图片还是视频，空表示查找所所】有", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/search-title")
	public String findByNoteTitle(String circleid, String title, String type, int page, int limit,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.selectByTitle(circleid, title, type, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("搜索指定用户发表的帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "userid", value = "圈子中指定的用户", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/search-user")
	public String findByNoteCreatUser(String circleid, String userid, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		List<NoteBo> noteBos = noteService.selectByUserid(circleid, userid, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		if (noteBos != null) {
			vosToList(noteBos, noteVoList, loginUser);
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("搜索指定日期内的帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "圈子id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "startTime", value = "指定查询日期，格式yyyy-MM-dd", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "分页页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页数量", dataType = "int", paramType = "query") })
	@PostMapping("/search-time")
	public String findByNoteTime(String circleid, String startTime, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo loginUser = getUserLogin(request);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date startDate = sf.parse(startTime);
			Date start = CommonUtil.getZeroDate(startDate);
			Date end = CommonUtil.getLastDate(startDate);
			List<NoteBo> noteBos = noteService.selectByCreatTime(circleid, start, end, page, limit);
			List<NoteVo> noteVoList = new LinkedList<>();
			if (noteBos != null) {
				vosToList(noteBos, noteVoList, loginUser);
			}
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("ret", 0);
			map.put("noteVoList", noteVoList);
			return JSONObject.fromObject(map).toString();
		} catch (ParseException e) {
			logger.error("Date Format Error {} ", e);
		}
		return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(), ERRORCODE.FORMAT_ERROR.getReason());
	}

	@SuppressWarnings("unchecked")
	@ApiOperation("附近的帖子列表,默认5千米范围")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "px", value = "当前人位置经度", required = true, paramType = "query", dataType = "double"),
			@ApiImplicitParam(name = "py", value = "当前人位置纬度", required = true, paramType = "query", dataType = "double"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/near-notes")
	public String nearPeopel(double px, double py, int limit, int page, HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("com.lad.controller.NoteController.nearPeopel-----{px:" + px + ",py:" + py + ",page:" + page
				+ ",limit:" + limit + "}");
		double[] position = new double[] { px, py };
		// 未登录情况
		UserBo userBo = getUserLogin(request);
		String userid = userBo != null ? userBo.getId() : "";
		CommandResult commanResult = noteService.findNearCircleByCommond(position, 10000, limit, page);
		BasicDBList dbList = (BasicDBList) commanResult.get("results");

		List<NoteVo> noteVoList = new LinkedList<>();
		for (Object object : dbList) {
			BasicDBObject basicDBObject = (BasicDBObject) object;
			BasicDBObject obj = (BasicDBObject) basicDBObject.get("obj");
			Map<String, Object> map = obj.toMap();
			map.put("id", map.get("_id").toString());
			NoteBo noteBo = com.alibaba.fastjson.JSONObject.parseObject(JSON.toJSONString(map), NoteBo.class);
			NoteVo noteVo = new NoteVo();
			if (noteBo.getCreateuid().equals(userid)) {
				boToVo(noteBo, noteVo, userBo, userid);
			} else {
				UserBo user = userService.getUser(noteBo.getCreateuid());
				boToVo(noteBo, noteVo, user, userid);
			}
			noteVo.setDistance(Double.valueOf(basicDBObject.get("dis").toString()));
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			if (circleBo != null && circleBo.getName() != null) {
				noteVo.setCirName(circleBo.getName());
			} else {
				noteVo.setCirName("未知的圈子");
			}
			noteVoList.add(noteVo);
		}

		// 排序
		if (noteVoList.size() >= 2) {
			for (int i = 0; i < noteVoList.size() - 1; i++) {
				for (int j = i + 1; j < noteVoList.size(); j++) {
					NoteVo p1 = noteVoList.get(i);
					int p1t = Integer.valueOf(p1.getCreateDate().replaceAll("-", ""));
					NoteVo p2 = noteVoList.get(j);
					int p2t = Integer.valueOf(p2.getCreateDate().replaceAll("-", ""));
					if (p1t < p2t) {
						NoteVo temp = noteVoList.get(i);
						noteVoList.set(i, p2);
						noteVoList.set(j, temp);
					} else if (p1t == p2t && p1.getDistance() > p2.getDistance()) {
						NoteVo temp = noteVoList.get(i);
						noteVoList.set(i, p2);
						noteVoList.set(j, temp);
					} else if (p1t == p2t && p1.getDistance() == p2.getDistance()
							&& p1.getCreateTime().getTime() < p2.getCreateTime().getTime()) {
						NoteVo temp = noteVoList.get(i);
						noteVoList.set(i, p2);
						noteVoList.set(j, temp);
					}
				}
			}
		}
		// 分页
		List<NoteVo> subList = new LinkedList<>();
		// 顺序无误
		int size = noteVoList.size();
		int start = (page - 1) * limit;
		int end = page * limit;

		if (start <= size && end <= size) {
			subList = noteVoList.subList(start, end);
		} else if (start <= size && end >= size) {
			subList = noteVoList.subList(start, size);
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", subList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("我的新帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/my-new-notes")
	public String myNewNote(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		// 未登录情况
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String userid = userBo.getId();
		List<NoteBo> noteBos = noteService.selectByUserid("", userid, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			boToVo(noteBo, noteVo, userBo, userid);
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			noteVo.setCirName(circleBo != null ? circleBo.getName() : "");
			noteVoList.add(noteVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("每日新帖子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/day-new-notes")
	public String dayNewNotes(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		// 未登录情况
		UserBo userBo = getUserLogin(request);
		String userid = userBo == null ? "" : userBo.getId();
		List<CircleBo> circleBos = circleService.findHotCircles(1, 10);
		List<String> circleids = new LinkedList<>();
		circleBos.forEach(circleBo -> circleids.add(circleBo.getId()));
		List<NoteBo> noteBos = noteService.dayNewNotes(circleids, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			boToVo(noteBo, noteVo, userBo, userid);
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			noteVo.setCirName(circleBo != null ? circleBo.getName() : "");
			noteVoList.add(noteVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("每日热帖")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/day-hot-notes")
	public String dayHotNotes(String city, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		String userid = userBo == null ? "" : userBo.getId();
		// 根据城市筛选热门圈子,并将id封装到cricleSet
		List<CircleBo> circleBoList = circleService.findHotCircles(city, 1, 10240);
		Set<String> circleSet = new HashSet<>();
		for (CircleBo circleBo : circleBoList) {
			circleSet.add(circleBo.getId());
		}

		List<NoteBo> noteBos = noteService.dayHotNotes(circleSet, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			// 获取创建者消息
			String createuid = noteBo.getCreateuid() == null ? "" : noteBo.getCreateuid();
			UserBo createUser = userService.getUser(createuid);
			boToVo(noteBo, noteVo, createUser, userid);
			ReadHistoryBo historyBo = readHistoryService.getHistoryByUseridAndNoteId(userid, noteBo.getId());
			noteVo.setRead(historyBo != null);
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			noteVo.setCirName(circleBo != null ? circleBo.getName() : "");

			noteVoList.add(noteVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 
	 * @param noteBos
	 * @param noteVoList
	 * @param loginUser
	 */
	private void vosToList(List<NoteBo> noteBos, List<NoteVo> noteVoList, UserBo loginUser) {
		String loginUserid = loginUser == null ? "" : loginUser.getId();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			if (noteBo.getCreateuid().equals(loginUserid)) {
				boToVo(noteBo, noteVo, loginUser, loginUserid);
			} else {
				UserBo userBo = userService.getUser(noteBo.getCreateuid());
				boToVo(noteBo, noteVo, userBo, loginUserid);
			}
			noteVo.setMyThumbsup(hasThumbsup(loginUserid, noteBo.getId()));
			ReadHistoryBo historyBo = readHistoryService.getHistoryByUseridAndNoteId(loginUserid, noteBo.getId());
			noteVo.setRead(historyBo != null);
			noteVoList.add(noteVo);
		}
	}

	@Autowired
	private IRestHomeService restHomeService;
	@Autowired
	private IShowService showService;

	/**
	 * 
	 * @param noteBo
	 * @param noteVo
	 * @param creatBo
	 * @param userid
	 */
	private void boToVo(NoteBo noteBo, NoteVo noteVo, UserBo creatBo, String userid) {
		BeanUtils.copyProperties(noteBo, noteVo);
		// 表示转发
		if (noteBo.getForward() == 1) {
			noteVo.setSourceid(noteBo.getSourceid());
			noteVo.setForward(true);
			// 0 表示转发的帖子，1 表示转发的资讯
			if (noteBo.getNoteType() == NoteBo.INFOR_FORWARD) {
				int inforType = noteBo.getInforType();
				noteVo.setInforType(inforType);
				noteVo.setForwardType(NoteBo.INFOR_FORWARD);
				switch (inforType) {
				case Constant.INFOR_HEALTH:
					InforBo inforBo = inforService.findById(noteBo.getSourceid());
					if (inforBo != null) {
						noteVo.setPhotos(inforBo.getImageUrls());
						noteVo.setSubject(inforBo.getTitle());
						noteVo.setVisitCount((long) inforBo.getVisitNum());
						noteVo.setInforTypeName(inforBo.getModule());
						noteVo.setClassName(inforBo.getClassName() == null ? "" : inforBo.getClassName());
					}
					break;
				case Constant.INFOR_SECRITY:
					SecurityBo securityBo = inforService.findSecurityById(noteBo.getSourceid());
					if (securityBo != null) {
						noteVo.setSubject(securityBo.getTitle());
						noteVo.setVisitCount((long) securityBo.getVisitNum());
						noteVo.setInforTypeName(securityBo.getNewsType());
					}
					break;
				case Constant.INFOR_RADIO:
					BroadcastBo broadcastBo = inforService.findBroadById(noteBo.getSourceid());
					if (broadcastBo != null) {
						noteVo.setSubject(broadcastBo.getTitle());
						noteVo.setInforUrl(broadcastBo.getBroadcast_url());
						noteVo.setVisitCount((long) broadcastBo.getVisitNum());
						noteVo.setInforTypeName(broadcastBo.getModule());
						noteVo.setClassName(broadcastBo.getClassName() == null ? "" : broadcastBo.getClassName());
					}
					break;
				case Constant.INFOR_VIDEO:
					VideoBo videoBo = inforService.findVideoById(noteBo.getSourceid());
					if (videoBo != null) {
						noteVo.setSubject(videoBo.getTitle());
						noteVo.setInforUrl(videoBo.getUrl());
						noteVo.setVideoPic(videoBo.getPoster());
						noteVo.setVisitCount((long) videoBo.getVisitNum());
						noteVo.setInforTypeName(videoBo.getModule());
						noteVo.setClassName(videoBo.getClassName() == null ? "" : videoBo.getClassName());
					}
					break;
				case Constant.INFOR_DAILY:
					DailynewsBo dailyNewsBo = inforService.findDailynewsById(noteBo.getSourceid());
					if (dailyNewsBo != null) {
						noteVo.setSubject(dailyNewsBo.getTitle());
						noteVo.setVisitCount((long) dailyNewsBo.getVisitNum());
						noteVo.setContent(dailyNewsBo.getText());
						noteVo.setInforTypeName(dailyNewsBo.getClassName());
						noteVo.setClassName(dailyNewsBo.getClassName() == null ? "" : dailyNewsBo.getClassName());
					}
					break;
				case Constant.INFOR_YANGLAO:
					YanglaoBo yaolaoBo = inforService.findByYanglaoId(noteBo.getSourceid());
					if (yaolaoBo != null) {
						noteVo.setSubject(yaolaoBo.getTitle());
						noteVo.setVisitCount((long) yaolaoBo.getVisitNum());
						noteVo.setContent(yaolaoBo.getText());
						noteVo.setInforTypeName(yaolaoBo.getClassName() == null ? "" : yaolaoBo.getClassName());
						noteVo.setClassName(yaolaoBo.getClassName() == null ? "" : yaolaoBo.getClassName());
					}
					break;
				default:
					break;
				}
			} else if (noteBo.getNoteType() == NoteBo.REST_FORWARD) {
				// 转发自养老院
				RestHomeBo sourceNote = restHomeService.findHomeById(noteBo.getSourceid());
				if (sourceNote != null) {
					noteVo.setForwardType(NoteBo.REST_FORWARD);
					noteVo.setSubject(sourceNote.getName());
					noteVo.setContent(sourceNote.getIntroduction());
					noteVo.setPhotos(new LinkedList<>(sourceNote.getImages()));
					noteVo.setVisitCount(noteBo.getVisitcount());
//					noteVo.setLandmark(sourceNote.getArea()+":"+sourceNote.getAddress());
//					addNoteAtUsers(noteBo, noteVo, userid);
					// 获取创建者
					UserBo from = userService.getUser(sourceNote.getCreateuid());

					if (from != null) {
						noteVo.setFromUserid(from.getId());
						// 如果登陆者id不为空
						if (!org.springframework.util.StringUtils.isEmpty(userid)) {
							// 参数1:主体id; 参数2:朋友id 判断原贴作者是否为当前登录者好友
							FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, from.getId());
							if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
								noteVo.setFromUserName(friendsBo.getBackname());
							} else {
								noteVo.setFromUserName(from.getUserName());
							}
						} else {
							noteVo.setFromUserName(from.getUserName());
						}
						noteVo.setFromUserPic(from.getHeadPictureName());
						noteVo.setFromUserSex(from.getSex());
						noteVo.setFromUserSign(from.getPersonalizedSignature());
						noteVo.setFromUserBirth(from.getBirthDay());
						noteVo.setFromUserLevel(from.getLevel());
						noteVo.setClassName("发现:养老院");
					}
				}
			} else if (noteBo.getNoteType() == NoteBo.SHOW_FORWARD) {
				ShowBo showBo = showService.findById(noteBo.getSourceid());
				if (showBo != null) {
					noteVo.setForwardType(NoteBo.SHOW_FORWARD);
					if (showBo.getType() == DiscoveryConstants.NEED) {
						noteVo.setSubject(showBo.getCompany() + "发布的找演出");
					} else {
						noteVo.setSubject(showBo.getCompany() + "发布的接演出");
					}

					noteVo.setContent(showBo.getBrief());
					noteVo.setPhotos(new LinkedList<>(showBo.getImages()));
					noteVo.setVisitCount(noteBo.getVisitcount());
//					noteVo.setLandmark(sourceNote.getArea()+":"+sourceNote.getAddress());
//					addNoteAtUsers(noteBo, noteVo, userid);
					// 获取创建者
					UserBo from = userService.getUser(showBo.getCreateuid());

					if (from != null) {
						noteVo.setFromUserid(from.getId());
						// 如果登陆者id不为空
						if (!org.springframework.util.StringUtils.isEmpty(userid)) {
							// 参数1:主体id; 参数2:朋友id 判断原贴作者是否为当前登录者好友
							FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, from.getId());
							if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
								noteVo.setFromUserName(friendsBo.getBackname());
							} else {
								noteVo.setFromUserName(from.getUserName());
							}
						} else {
							noteVo.setFromUserName(from.getUserName());
						}
						noteVo.setFromUserPic(from.getHeadPictureName());
						noteVo.setFromUserSex(from.getSex());
						noteVo.setFromUserSign(from.getPersonalizedSignature());
						noteVo.setFromUserBirth(from.getBirthDay());
						noteVo.setFromUserLevel(from.getLevel());
						noteVo.setClassName("发现:演出");
					}
				}

			} else {
				NoteBo sourceNote = noteService.selectById(noteBo.getSourceid());
				if (sourceNote != null) {
					noteVo.setForwardType(NoteBo.NOTE_FORWARD);
					noteVo.setSubject(sourceNote.getSubject());
					noteVo.setContent(sourceNote.getContent());
					noteVo.setPhotos(sourceNote.getPhotos());
					noteVo.setVideoPic(sourceNote.getVideoPic());
					noteVo.setVisitCount(noteBo.getVisitcount());
					addNoteAtUsers(sourceNote, noteVo, userid);
					// creatBo = userService.getUser(sourceNote.getCreateuid());
					UserBo from = userService.getUser(sourceNote.getCreateuid());
					if (from != null) {
						noteVo.setFromUserid(from.getId());
						noteVo.setFromUserName(from.getUserName());
						if (!StringUtils.isEmpty(userid)) {
							FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, from.getId());
							if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
								noteVo.setFromUserName(friendsBo.getBackname());
							} else {
								noteVo.setFromUserName(from.getUserName());
							}
						}
						noteVo.setFromUserPic(from.getHeadPictureName());
						noteVo.setFromUserSex(from.getSex());
						noteVo.setFromUserSign(from.getPersonalizedSignature());
						noteVo.setFromUserBirth(from.getBirthDay());
						noteVo.setFromUserLevel(from.getLevel());
					}
				}
			}
		} else {
			noteVo.setVisitCount(noteBo.getVisitcount());
			addNoteAtUsers(noteBo, noteVo, userid);
		}
		if (creatBo != null) {
			noteVo.setUsername(creatBo.getUserName());
			if (!"".equals(userid) && !userid.equals(creatBo.getId())) {
				FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, creatBo.getId());
				if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
					noteVo.setUsername(friendsBo.getBackname());
				}
			}
			noteVo.setUserLevel(creatBo.getLevel());
			noteVo.setSex(creatBo.getSex());
			noteVo.setBirthDay(creatBo.getBirthDay());
			noteVo.setHeadPictureName(creatBo.getHeadPictureName());
		}
		noteVo.setPosition(noteBo.getPosition());
		noteVo.setCommontCount(noteBo.getCommentcount());
		noteVo.setNodeid(noteBo.getId());
		noteVo.setTransCount(noteBo.getTranscount());
		noteVo.setThumpsubCount(noteBo.getThumpsubcount());
	}

	private void addNoteAtUsers(NoteBo noteBo, NoteVo noteVo, String loginUserid) {
		LinkedList<String> atUsers = noteBo.getAtUsers();
		if (!CommonUtil.isEmpty(atUsers)) {
			List<UserNoteVo> atUserVos = new LinkedList<>();
			List<UserBo> userBos = userService.findUserByIds(atUsers);
			for (UserBo userBo : userBos) {
				UserNoteVo userNoteVo = new UserNoteVo();
				userNoteVo.setSex(userBo.getSex());
				userNoteVo.setUserid(userBo.getId());
				userNoteVo.setUserName(userBo.getUserName());
				if (!StringUtils.isEmpty(loginUserid)) {
					FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(loginUserid, userBo.getId());
					if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
						userNoteVo.setBackName(friendsBo.getBackname());
					}
				}
				atUserVos.add(userNoteVo);
			}
			noteVo.setAtUsers(atUserVos);
		}
	}

	/**
	 * 判断前用户是否点赞
	 * 
	 * @param loginUserid
	 * @param noteid
	 * @return
	 */
	private boolean hasThumbsup(String loginUserid, String noteid) {
		if (!"".equals(loginUserid)) {
			ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(noteid, loginUserid);
			return thumbsupBo != null;
		}
		return false;
	}



	/**
	 * 需要和聚会展示最新信息
	 * 
	 * @param noteBo
	 */
	private void addCircleShow(NoteBo noteBo) {
		CircleShowBo circleShowBo = new CircleShowBo();
		circleShowBo.setCircleid(noteBo.getCircleId());
		circleShowBo.setTargetid(noteBo.getId());
		circleShowBo.setType(0);
		circleShowBo.setCreateTime(noteBo.getCreateTime());
		circleService.addCircleShow(circleShowBo);
	}

	/**
	 * 删除展示信息
	 * 
	 * @param noteid
	 */
	private void deleteShouw(String noteid) {
		circleService.deleteShow(noteid);
	}


}
