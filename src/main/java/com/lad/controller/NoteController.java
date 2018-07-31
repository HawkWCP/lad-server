package com.lad.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
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
import com.lad.bo.ThumbsupBo;
import com.lad.bo.UserBo;
import com.lad.redis.RedisServer;
import com.lad.scrapybo.BroadcastBo;
import com.lad.scrapybo.InforBo;
import com.lad.scrapybo.SecurityBo;
import com.lad.scrapybo.VideoBo;
import com.lad.service.ICircleService;
import com.lad.service.ICollectService;
import com.lad.service.ICommentService;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.IInforService;
import com.lad.service.ILocationService;
import com.lad.service.IMessageService;
import com.lad.service.INoteService;
import com.lad.service.IReadHistoryService;
import com.lad.service.IReasonService;
import com.lad.service.IThumbsupService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.JPushUtil;
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
import net.sf.json.JSONObject;

@Api(value = "NoteController", description = "帖子相关接口")
@RestController
@RequestMapping("note")
public class NoteController extends BaseContorller {

	private final Logger logger = LogManager.getLogger(NoteController.class);

	@Autowired
	private INoteService noteService;
	@Autowired
	private ICircleService circleService;
	@Autowired
	private IUserService userService;

	@Autowired
	private ICommentService commentService;

	@Autowired
	private IThumbsupService thumbsupService;
	@Autowired
	private RedisServer redisServer;

	@Autowired
	private ILocationService locationService;

	@Autowired
	private IDynamicService dynamicService;

	@Autowired
	private IReasonService reasonService;

	@Autowired
	private IFriendsService friendsService;

	@Autowired
	private ICollectService collectService;

	@Autowired
	private IInforService inforService;

	@Autowired
	private IMessageService messageService;

	@Autowired
	private AsyncController asyncController;

	@Autowired
	private IReadHistoryService readHistoryService;

	private String pushTitle = "互动通知";

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
		String userid = userBo.getId();
		List<CircleBo> circleBos = circleService.selectByuserid(userid);
		List<String> circleids = new LinkedList<>();
		// 获取圈子id 并保存到circleids
		circleBos.forEach(circleBo -> circleids.add(circleBo.getId()));
		List<NoteBo> noteBos = noteService.dayNewNotes(circleids, page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			//获取创建者消息
			String createuid = noteBo.getCreateuid()==null?"":noteBo.getCreateuid();
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
		NoteBo noteBo = null;
		try {
			JSONObject jsonObject = JSONObject.fromObject(noteJson);
			noteBo = (NoteBo) JSONObject.toBean(jsonObject, NoteBo.class);
		} catch (Exception e) {
			return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(), ERRORCODE.FORMAT_ERROR.getReason());
		}

		String circleid = noteBo.getCircleId();
		updateHistory(userBo.getId(), circleid, locationService, circleService);
		noteBo.setVisitcount(1);
		noteBo.setCreateuid(userBo.getId());
		noteBo.setVisitcount(1);
		noteBo.setTemp(1);
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		LinkedList<String> photos = new LinkedList<>();
		String userId = userBo.getId();
		if (pictures != null) {
			for (MultipartFile file : pictures) {
				Long time = Calendar.getInstance().getTimeInMillis();
				String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
				logger.info(fileName);
				if ("video".equals(noteBo.getType())) {
					String[] paths = CommonUtil.uploadVedio(file, Constant.NOTE_PICTURE_PATH, fileName, 0);
					photos.add(paths[0]);
					noteBo.setVideoPic(paths[1]);
				} else {
					String path = CommonUtil.upload(file, Constant.NOTE_PICTURE_PATH, fileName, 0);
					logger.info("note add note pic path: {},  size: {} ", path, file.getSize());
					photos.add(path);
				}
			}
		}
		noteBo.setPhotos(photos);
		String[] useridArr = null;
		if (!StringUtils.isEmpty(atUserids)) {
			useridArr = CommonUtil.getIds(atUserids);
			LinkedList<String> atUsers = new LinkedList<>();
			Collections.addAll(atUsers, useridArr);
			noteBo.setAtUsers(atUsers);
		}
		noteService.insert(noteBo);
		if (useridArr != null) {
			String path = String.format("/note/note-info.do?noteid=%s&type=%s", noteBo.getId(), noteBo.getType());
			String content = "有人刚刚在帖子提到了您，快去看看吧!";
			JPushUtil.push(pushTitle, content, path, useridArr);
			addMessage(messageService, path, content, pushTitle, noteBo.getId(), useridArr);
		}
		addCircleShow(noteBo);
		asyncController.updateCircieNoteSize(circleid, 1);
		if (noteBo.isAsync()) {
			DynamicBo dynamicBo = new DynamicBo();
			dynamicBo.setTitle(noteBo.getSubject());
			dynamicBo.setView("我发表了帖子");
			dynamicBo.setCreateuid(userId);
			dynamicBo.setMsgid(noteBo.getId());
			dynamicBo.setOwner(noteBo.getCreateuid());
			dynamicBo.setLandmark(noteBo.getLandmark());
			dynamicBo.setType(Constant.NOTE_TYPE);
			CircleBo circleBo = circleService.selectById(circleid);
			if (circleBo != null) {
				dynamicBo.setSourceName(circleBo.getName());
				dynamicBo.setSourceid(circleBo.getId());
			}
			dynamicBo.setPicType(noteBo.getType());
			if (noteBo.getType().equals("video")) {
				dynamicBo.setVideoPic(noteBo.getVideoPic());
				dynamicBo.setVideo(noteBo.getPhotos().getFirst());
			} else {
				dynamicBo.setPhotos(new LinkedHashSet<>(noteBo.getPhotos()));
			}
			dynamicBo.setCreateuid(userBo.getId());
			dynamicService.addDynamic(dynamicBo);
		}
		updateDynamicNums(userId, 1, dynamicService, redisServer);
		userService.addUserLevel(userBo.getId(), 1, Constant.LEVEL_NOTE, 0);
		updateCircleHot(circleService, redisServer, circleid, 1, Constant.CIRCLE_NOTE);
		updateCircleHot(circleService, redisServer, circleid, 1, Constant.CIRCLE_NOTE_VISIT);
		asyncController.updateCircieNoteUnReadNum(userId, circleid);
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
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query")
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
		ThumbsupBo thumbsupBo = thumbsupService.findHaveOwenidAndVisitorid(noteid, userBo.getId());
		boolean isThumsup = false;
		if (null == thumbsupBo) {
			thumbsupBo = new ThumbsupBo();
			thumbsupBo.setType(Constant.NOTE_TYPE);
			thumbsupBo.setOwner_id(noteid);
			thumbsupBo.setImage(userBo.getHeadPictureName());
			thumbsupBo.setVisitor_id(userBo.getId());
			thumbsupBo.setCreateuid(userBo.getId());
			thumbsupService.insert(thumbsupBo);
			isThumsup = true;
		} else {
			if (thumbsupBo.getDeleted() == Constant.DELETED) {
				thumbsupService.udateDeleteById(thumbsupBo.getId());
				isThumsup = true;
			}
		}
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_THUMP);
		if (isThumsup) {
			updateCount(noteid, Constant.THUMPSUB_NUM, 1);
		}
		String content = "有人刚刚赞了你的帖子，快去看看吧!";
		asyncController.updateCircieUnReadNum(noteBo.getCreateuid(), noteBo.getCircleId());
		String path = "/note/note-info.do?noteid=" + noteid;
		JPushUtil.pushMessage(pushTitle, content, path, noteBo.getCreateuid());
		addMessage(messageService, path, content, pushTitle, noteid, 2, thumbsupBo.getId(), noteBo.getCircleId(),
				userBo.getId(), noteBo.getCreateuid());
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
		List<ThumbsupBo> thumbsupBos = thumbsupService.selectByOwnerIdPaged(page, limit, noteid, Constant.NOTE_TYPE);
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
			updateCircleHot(circleService, redisServer, noteBo.getCircleId(), -1, Constant.CIRCLE_THUMP);
			updateCount(noteid, Constant.THUMPSUB_NUM, -1);
			messageService.deleteMessageBySource(thumbsupBo.getId(), 2);
		}
		return Constant.COM_RESP;
	}

	@ApiOperation("获取帖子详情")
	@ApiImplicitParam(name = "noteid", value = "帖子id", required = true, dataType = "string", paramType = "query")
	@PostMapping("/note-info")
	public String noteInfo(String noteid, HttpServletRequest request, HttpServletResponse response) {
		NoteBo noteBo = noteService.selectById(noteid);
		if (null == noteBo) {
			return CommonUtil.toErrorResult(ERRORCODE.NOTE_IS_NULL.getIndex(), ERRORCODE.NOTE_IS_NULL.getReason());
		}
		UserBo userBo = getUserLogin(request);
		NoteVo noteVo = new NoteVo();
		String userid = "";
		if (userBo != null) {
			userid = userBo.getId();
			updateHistory(userBo.getId(), noteBo.getCircleId(), locationService, circleService);
			ReadHistoryBo historyByUseridAndNoteId = readHistoryService.getHistoryByUseridAndNoteId(noteid, noteid);
			if (historyByUseridAndNoteId == null) {
				// 添加帖子阅读历史
				ReadHistoryBo historyBo = new ReadHistoryBo();
				historyBo.setReaderId(userid);
				historyBo.setBeReaderId(noteid);
				historyBo.setType(0);
				historyBo.setReadNum(1);
				readHistoryService.addReadHistory(historyBo);
			} else {
				int readNum = historyByUseridAndNoteId.getReadNum() + 1;
				readHistoryService.updateReadNum(historyByUseridAndNoteId.getId(), readNum);
			}

			ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(noteid, userid);
			// 这个帖子自己是否点赞
			noteVo.setMyThumbsup(null != thumbsupBo);
			CollectBo collectBo = collectService.findByUseridAndTargetid(userid, noteid);
			noteVo.setCollect(collectBo != null);
		}
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_VISIT);
		updateCount(noteid, Constant.VISIT_NUM, 1);
		boToVo(noteBo, noteVo, userService.getUser(noteBo.getCreateuid()), userid);
		CircleBo circleBo = circleService.selectByIdIgnoreDel(noteBo.getCircleId());
		if (circleBo != null) {
			noteVo.setCirName(circleBo.getName());
			noteVo.setCirHeadPic(circleBo.getHeadPicture());
			noteVo.setCirNoteNum(circleBo.getNoteNum());
			noteVo.setCirUserNum(circleBo.getUsers().size());
		}
		noteVo.setVisitCount(noteBo.getVisitcount() + 1);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("noteVo", noteVo);
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

		updateCount(noteid, Constant.COMMENT_NUM, 1);
		userService.addUserLevel(userBo.getId(), 1, Constant.LEVEL_COMMENT, 0);
		updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_COMMENT);
		asyncController.updateRedStar(userBo, noteBo, noteBo.getCircleId(), currentDate);
		asyncController.updateCircieUnReadNum(noteBo.getCreateuid(), noteBo.getCircleId());
		String path = "/note/note-info.do?noteid=" + noteid;
		String content = "有人刚刚评论了你的帖子，快去看看吧!";
		JPushUtil.pushMessage(pushTitle, content, path, noteBo.getCreateuid());
		addMessage(messageService, path, content, pushTitle, noteid, 1, commentBo.getId(), noteBo.getCircleId(),
				userBo.getId(), noteBo.getCreateuid());
		if (!StringUtils.isEmpty(parentid)) {
			CommentBo comment = commentService.findById(parentid);
			if (comment != null) {
				asyncController.updateCircieUnReadNum(comment.getCreateuid(), noteBo.getCircleId());
				content = "有人刚刚回复了你的评论，快去看看吧!";
				JPushUtil.pushMessage(pushTitle, content, path, comment.getCreateuid());
				addMessage(messageService, path, content, pushTitle, noteid, 1, comment.getId(), noteBo.getCircleId(),
						userBo.getId(), noteBo.getCreateuid());
			}
		}
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
				updateCount(commentBo.getNoteid(), Constant.COMMENT_NUM, -1);
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
			commentVo.setMyThumbsup(thumbsupBo != null);
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
		List<BasicDBObject> objects = commentService.selectMyNoteReply(userBo.getId(), page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (BasicDBObject object : objects) {
			String id = object.get("noteid").toString();
			NoteBo noteBo = noteService.selectById(id);

			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
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
		ThumbsupBo thumbsupBo = thumbsupService.findHaveOwenidAndVisitorid(commentid, userBo.getId());
		int num = 0;
		if (isThumnbsup) {
			if (null == thumbsupBo) {
				thumbsupBo = new ThumbsupBo();
				thumbsupBo.setType(Constant.NOTE_COM_TYPE);
				thumbsupBo.setOwner_id(commentid);
				thumbsupBo.setImage(userBo.getHeadPictureName());
				thumbsupBo.setVisitor_id(userBo.getId());
				thumbsupBo.setCreateuid(userBo.getId());
				thumbsupService.insert(thumbsupBo);
				num++;
			} else {
				if (thumbsupBo.getDeleted() == Constant.DELETED) {
					thumbsupService.udateDeleteById(thumbsupBo.getId());
					num++;
				}
			}
		} else {
			if (null != thumbsupBo && thumbsupBo.getDeleted() == Constant.ACTIVITY) {
				thumbsupService.deleteById(thumbsupBo.getId());
				num--;
			}
		}
		asyncController.updateCommentThumbsup(commentid, num);
		return Constant.COM_RESP;
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
		List<ThumbsupBo> thumbsupBos = thumbsupService.selectByOwnerIdPaged(page, limit, noteid, Constant.NOTE_TYPE);
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
		UserBo userBo = getUserLogin(request);
		String loginUserid = userBo != null ? userBo.getId() : "";
		List<NoteBo> noteBos = noteService.selectMyNotes(userBo.getId(), page, limit);
		if (noteBos != null && !noteBos.isEmpty()) {
			updateHistory(userBo.getId(), noteBos.get(0).getCircleId(), locationService, circleService);
		}
		List<NoteVo> noteVoList = new LinkedList<>();
		NoteVo noteVo = null;
		for (NoteBo noteBo : noteBos) {
			noteVo = new NoteVo();
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			noteVo.setCirName(circleBo.getName());
			noteVo.setCirNoteNum(circleBo.getNoteSize());
			noteVo.setCirHeadPic(circleBo.getHeadPicture());
			noteVo.setCirVisitNum(circleBo.getVisitNum());
			boToVo(noteBo, noteVo, userBo, loginUserid);
			noteVo.setMyThumbsup(hasThumbsup(loginUserid, noteBo.getId()));
			noteVoList.add(noteVo);
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
		dynamicBo.setMsgid(noteid);
		dynamicBo.setCreateuid(userBo.getId());
		dynamicBo.setOwner(noteBo.getCreateuid());
		dynamicBo.setLandmark(landmark);
		dynamicBo.setType(Constant.NOTE_TYPE);
		dynamicBo.setPicType(noteBo.getType());
		if (noteBo.getType().equals("video")) {
			dynamicBo.setVideoPic(noteBo.getVideoPic());
			dynamicBo.setVideo(noteBo.getPhotos().getFirst());
		} else {
			dynamicBo.setPhotos(new LinkedHashSet<>(noteBo.getPhotos()));
		}
		CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
		if (circleBo != null) {
			dynamicBo.setSourceName(circleBo.getName());
			dynamicBo.setSourceid(circleBo.getId());
		}
		dynamicBo.setCreateuid(userBo.getId());
		dynamicService.addDynamic(dynamicBo);
		updateCount(noteid, Constant.SHARE_NUM, 1);
		updateDynamicNums(userBo.getId(), 1, dynamicService, redisServer);
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
		updateCount(noteid, Constant.COLLECT_NUM, 1);
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
		noteBo.setContent(old.getContent());
		noteBo.setCreateuid(userBo.getId());
		noteBo.setLandmark(landmark);
		noteBo.setSourceid(noteid);
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		noteBo.setForward(1);
		noteService.insert(noteBo);
		addCircleShow(noteBo);
		updateCount(noteid, Constant.SHARE_NUM, 1);
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
		noteBo.setContent(old.getContent());
		noteBo.setCreateuid(userBo.getId());
		noteBo.setLandmark(landmark);
		noteBo.setSourceid(old.getId());
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		noteBo.setForward(1);
		noteService.insert(noteBo);
		addCircleShow(noteBo);
		updateCount(noteid, Constant.SHARE_NUM, 1);
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

	@ApiOperation("附近的帖子列表,默认5千米范围")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "px", value = "当前人位置经度", required = true, paramType = "query", dataType = "double"),
			@ApiImplicitParam(name = "py", value = "当前人位置纬度", required = true, paramType = "query", dataType = "double"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/near-notes")
	public String nearPeopel(double px, double py, int limit, int page, HttpServletRequest request,
			HttpServletResponse response) {
		
		double[] position = new double[] { px, py };
		org.slf4j.Logger logger = LoggerFactory.getLogger(NoteController.class);
		logger.error(Arrays.toString(position));
		// 未登录情况
		UserBo userBo = getUserLogin(request);
		String userid = userBo != null ? userBo.getId() : "";
		CommandResult commanResult = noteService.findNearCircleByCommond(position, 5000, limit, page);
		BasicDBList dbList = (BasicDBList) commanResult.get("results");
		List<Object> subList = new BasicDBList();
		int size = dbList.size();
		int start = (page - 1) * limit;
		int end = page * limit;

		if (start <= size && end <= size) {
			subList = dbList.subList(start, end);
		} else if (start <= size && end >= size) {
			subList = dbList.subList(start, size);
		}
		
		List<NoteVo> noteVoList = new LinkedList<>();
        for (Object object : subList) {
        	BasicDBObject basicDBObject = (BasicDBObject)object;
        	BasicDBObject obj = (BasicDBObject) basicDBObject.get("obj");
        	Map<String,Object> map = obj.toMap();
        	map.put("id", map.get("_id").toString());
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
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();
		
		
		
		
		/*GeoResults<NoteBo> noteBos = noteService.findNearNote(position, 5000, limit, page);
		DecimalFormat df = new DecimalFormat("###.00");
		List<NoteVo> noteVoList = new LinkedList<>();
		for (GeoResult<NoteBo> result : noteBos) {
			NoteBo noteBo = result.getContent();
			NoteVo noteVo = new NoteVo();
			if (noteBo.getCreateuid().equals(userid)) {
				boToVo(noteBo, noteVo, userBo, userid);
			} else {
				UserBo user = userService.getUser(noteBo.getCreateuid());
				boToVo(noteBo, noteVo, user, userid);
			}
			double dis = Double.parseDouble(df.format(result.getDistance().getValue()));
			noteVo.setDistance(dis);
			noteVo.setMyThumbsup(hasThumbsup(userid, noteBo.getId()));
			CircleBo circleBo = circleService.selectById(noteBo.getCircleId());
			if (circleBo != null && circleBo.getName() != null) {
				noteVo.setCirName(circleBo.getName());
			} else {
				noteVo.setCirName("未知的圈子");
			}
			noteVoList.add(noteVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteVoList", noteVoList);
		return JSONObject.fromObject(map).toString();*/

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

		List<CircleBo> circleBoList = circleService.findHotCircles(city, 1, 10240);
		Set<String> circleSet = new HashSet<>();
		for (CircleBo circleBo : circleBoList) {
			circleSet.add(circleBo.getId());
		}
		List<NoteBo> noteBos = noteService.dayHotNotes(circleSet,page, limit);
		List<NoteVo> noteVoList = new LinkedList<>();
		for (NoteBo noteBo : noteBos) {
			NoteVo noteVo = new NoteVo();
			//获取创建者消息
			String createuid = noteBo.getCreateuid()==null?"":noteBo.getCreateuid();
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
			noteVoList.add(noteVo);
		}
	}

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
			if (noteBo.getNoteType() == 1) {
				int inforType = noteBo.getInforType();
				noteVo.setInforType(inforType);
				noteVo.setForwardType(1);
				switch (inforType) {
				case Constant.INFOR_HEALTH:
					InforBo inforBo = inforService.findById(noteBo.getSourceid());
					if (inforBo != null) {
						noteVo.setPhotos(inforBo.getImageUrls());
						noteVo.setSubject(inforBo.getTitle());
						noteVo.setVisitCount((long) inforBo.getVisitNum());
						noteVo.setInforTypeName(inforBo.getModule());
					}
					break;
				case Constant.INFOR_SECRITY:
					SecurityBo securityBo = inforService.findSecurityById(noteBo.getSourceid());
					if (securityBo == null) {
						noteVo.setSubject(securityBo.getTitle());
						noteVo.setVisitCount((long) securityBo.getVisitNum());
						noteVo.setInforTypeName(securityBo.getNewsType());
					}
					break;
				case Constant.INFOR_RADIO:
					BroadcastBo broadcastBo = inforService.findBroadById(noteBo.getSourceid());
					if (broadcastBo == null) {
						noteVo.setSubject(broadcastBo.getTitle());
						noteVo.setInforUrl(broadcastBo.getBroadcast_url());
						noteVo.setVisitCount((long) broadcastBo.getVisitNum());
						noteVo.setInforTypeName(broadcastBo.getModule());
					}
					break;
				case Constant.INFOR_VIDEO:
					VideoBo videoBo = inforService.findVideoById(noteBo.getSourceid());
					if (videoBo == null) {
						noteVo.setSubject(videoBo.getTitle());
						noteVo.setInforUrl(videoBo.getUrl());
						noteVo.setVideoPic(videoBo.getPoster());
						noteVo.setVisitCount((long) videoBo.getVisitNum());
						noteVo.setInforTypeName(videoBo.getModule());
					}
					break;
				default:
					break;
				}
			} else {
				NoteBo sourceNote = noteService.selectById(noteBo.getSourceid());
				if (sourceNote != null) {
					noteVo.setSubject(sourceNote.getSubject());
					noteVo.setContent(sourceNote.getContent());
					noteVo.setPhotos(sourceNote.getPhotos());
					noteVo.setVideoPic(sourceNote.getVideoPic());
					addNoteAtUsers(sourceNote, noteVo, userid);
					creatBo = userService.getUser(sourceNote.getCreateuid());
					UserBo from = userService.getUser(noteBo.getCreateuid());
					if (from != null) {
						noteVo.setFromUserid(from.getId());
						noteVo.setFromUserName(from.getUserName());
						if (!StringUtils.isEmpty(userid)) {
							FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userid, from.getId());
							if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
								noteVo.setFromUserName(friendsBo.getBackname());
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
		noteVo.setVisitCount(noteBo.getVisitcount());
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

	private CommentVo comentBo2Vo(CommentBo commentBo) {
		CommentVo commentVo = new CommentVo();
		BeanUtils.copyProperties(commentBo, commentVo);
		commentVo.setCommentId(commentBo.getId());
		commentVo.setUserid(commentBo.getCreateuid());
		return commentVo;
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

	private void updateCount(String noteid, int type, int num) {
		RLock lock = redisServer.getRLock(noteid.concat(String.valueOf(type)));
		try {
			lock.lock(2, TimeUnit.SECONDS);
			switch (type) {
			case Constant.VISIT_NUM:
				noteService.updateVisitCount(noteid);
				break;
			case Constant.COMMENT_NUM:
				noteService.updateCommentCount(noteid, num);
				break;
			case Constant.THUMPSUB_NUM:
				noteService.updateThumpsubCount(noteid, num);
				break;
			case Constant.SHARE_NUM:
				noteService.updateTransCount(noteid, num);
				break;
			case Constant.COLLECT_NUM:
				noteService.updateCollectCount(noteid, num);
				break;
			default:
				break;
			}
		} finally {
			lock.unlock();
		}
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
