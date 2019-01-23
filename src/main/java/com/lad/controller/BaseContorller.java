package com.lad.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.ModelMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lad.bo.ChatroomUserBo;
import com.lad.bo.CircleBo;
import com.lad.bo.CircleHistoryBo;
import com.lad.bo.CommentBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.DynamicNumBo;
import com.lad.bo.InforHistoryBo;
import com.lad.bo.InforRecomBo;
import com.lad.bo.LocationBo;
import com.lad.bo.MessageBo;
import com.lad.bo.NoteBo;
import com.lad.bo.PushTokenBo;
import com.lad.bo.ReadHistoryBo;
import com.lad.bo.ReasonBo;
import com.lad.bo.RedstarBo;
import com.lad.bo.ThumbsupBo;
import com.lad.bo.UserBo;
import com.lad.redis.RedisServer;
import com.lad.service.ICareService;
import com.lad.service.IChatroomService;
import com.lad.service.ICircleService;
import com.lad.service.ICommentService;
import com.lad.service.IDynamicService;
import com.lad.service.IExposeService;
import com.lad.service.IFriendsService;
import com.lad.service.IHomepageService;
import com.lad.service.IInforRecomService;
import com.lad.service.IInforService;
import com.lad.service.ILocationService;
import com.lad.service.IMessageService;
import com.lad.service.INoteService;
import com.lad.service.IReadHistoryService;
import com.lad.service.IReasonService;
import com.lad.service.IRestHomeService;
import com.lad.service.IThumbsupService;
import com.lad.service.ITokenService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.HuaWeiPushNcMsg;
import com.lad.util.MeizuPushUtil;
import com.lad.util.MiPushUtil;
import com.lad.util.MyException;
import com.lad.util.VivoPushUtil;

import lad.scrapybo.BroadcastBo;
import lad.scrapybo.DailynewsBo;
import lad.scrapybo.InforBo;
import lad.scrapybo.SecurityBo;
import lad.scrapybo.VideoBo;
import lad.scrapybo.YanglaoBo;

public abstract class BaseContorller {

	@Autowired
	protected RedisServer redisServer;

	@Autowired
	protected ITokenService tokenService;
	@Autowired
	protected IThumbsupService thumbsupService;
	@Autowired
	protected IReasonService reasonService;
	@Autowired
	protected ICircleService circleService;
	@Autowired
	protected INoteService noteService;
	@Autowired
	protected IReadHistoryService readHistoryService;

	@Autowired
	protected IMessageService messageService;
	@Autowired
	protected IInforService inforService;

	@Autowired
	protected IInforRecomService inforRecomService;
	@Autowired
	protected ICommentService commentService;
	@Autowired
	protected IDynamicService dynamicService;

	@Autowired
	protected IExposeService exposeService;
	@Autowired
	protected IUserService userService;

	@Autowired
	protected ILocationService locationService;

	@Autowired
	protected IFriendsService friendsService;

	@Autowired
	protected IChatroomService chatroomService;

	@Autowired
	protected IHomepageService homepageService;
	@Autowired
	protected IRestHomeService restHomeService;
	
	@Autowired
	protected ICareService careService;
	
	protected int dayTimeMins = 24 * 60 * 60 * 1000;
	private Logger logger = LogManager.getLogger();

	private static final ExecutorService THREADPOOL = Executors.newFixedThreadPool(5);
	
	/**
	 * 将转发相关功能整合于此
	 * @param user	行为的发起者
	 * @param obj	被转发对象
	 * @param destination	目的地
	 */
	protected void forwardAsNote(UserBo user ,Object obj,CircleBo destination) {
		
	}

	/**
	 * 
	 * @param user
	 * @param obj
	 * @return
	 */
	protected CommentBo comment(UserBo user, Object obj, String content, LinkedHashSet<String> photos) {
		CommentBo comment = new CommentBo();

		try {
			if (user == null || obj == null) {
				throw new Exception();
			}
			JSONObject object = JSON.parseObject(JSON.toJSONString(obj));
			String clazz = obj.getClass().getName();
			saveComment(user, content, photos, comment, object, clazz);

			String objId = object.getString("id");

			if ("com.lad.bo.NoteBo".equals(clazz)) {
				NoteBo noteBo = JSON.parseObject(JSON.toJSONString(object), NoteBo.class);

				updateHistory(user.getId(), noteBo.getCircleId(), locationService, circleService);
				String pushTitle = "互动通知";

				updateNoteCount(objId, Constant.COMMENT_NUM, 1);
				userService.addUserLevel(user.getId(), 1, Constant.LEVEL_COMMENT, 0);
				updateCircleHot(circleService, redisServer, noteBo.getCircleId(), 1, Constant.CIRCLE_NOTE_COMMENT);
				updateRedStar(user, noteBo, noteBo.getCircleId(), new Date());
				updateCircieUnReadNum(noteBo.getCreateuid(), noteBo.getCircleId());
				String path = "/note/note-info.do?noteid=" + objId;
				String pushContent = "有人刚刚评论了你的帖子，快去看看吧!";

				usePush(noteBo.getCreateuid(), pushTitle, content, path);

				addMessage(messageService, path, pushContent, pushTitle, objId, 1, comment.getId(),
						noteBo.getCircleId(), user.getId(), noteBo.getCreateuid());

			}
			if ("com.lad.bo.CommentBo".equals(clazz)) {
				String pushTitle = "互动通知";
				CommentBo parent = commentService.findById(comment.getTargetid());
				if (parent != null) {
					String path = "/note/note-info.do?noteid=" + parent.getId();
					String pushContent = "有人刚刚回复了你的评论，快去看看吧!";
					usePush(parent.getCreateuid(), pushTitle, pushContent, path);
					addMessage(messageService, path, pushContent, pushTitle, objId, 1, parent.getId(),
							comment.getTargetid(), user.getId(), comment.getCreateuid());
				}
			}
			if ("com.lad.bo.DynamicBo".equals(clazz)) {
				String pushTitle = "互动通知";
				DynamicBo parent = dynamicService.findDynamicById(comment.getTargetid());
				String path = "/dynamic/dynamic-infor?dynamicId=" + parent.getId();
				String pushContent = "有人刚刚回复了你的评论，快去看看吧!";
				usePush(parent.getCreateuid(), pushTitle, pushContent, path);
				addMessage(messageService, path, pushContent, pushTitle, objId, 1, parent.getId(),
						comment.getTargetid(), user.getId(), comment.getCreateuid());
			}
		} catch (Exception e) {
			logger.error("add comment=====e:{}", e);
		}

		return comment;
	}

	/**
	 * 帖子阅读 更新红人信息
	 * 
	 * @param userBo
	 * @param noteBo
	 * @param circleid
	 * @param currentDate
	 */
	@Async
	protected void updateRedStar(UserBo userBo, NoteBo noteBo, String circleid, Date currentDate) {
		RedstarBo redstarBo = commentService.findRedstarBo(userBo.getId(), circleid);
		int curretWeekNo = CommonUtil.getWeekOfYear(currentDate);
		int year = CommonUtil.getYear(currentDate);
		if (redstarBo == null) {
			redstarBo = setRedstarBo(userBo.getId(), circleid, curretWeekNo, year);
			commentService.insertRedstar(redstarBo);
		}
		// 判断贴的作者是不是自己
		boolean isNotSelf = !userBo.getId().equals(noteBo.getCreateuid());
		boolean isNoteUserCurrWeek = true;
		// 如果帖子作者不是自己
		if (isNotSelf) {
			// 帖子作者没有红人数据信息，则添加
			RedstarBo noteRedstarBo = commentService.findRedstarBo(noteBo.getCreateuid(), circleid);
			if (noteRedstarBo == null) {
				noteRedstarBo = setRedstarBo(noteBo.getCreateuid(), circleid, curretWeekNo, year);
				commentService.insertRedstar(noteRedstarBo);
			} else {
				// 判断帖子作者周榜是不是当前周，是则添加数据，不是则更新周榜数据
				isNoteUserCurrWeek = (year == noteRedstarBo.getYear() && curretWeekNo == noteRedstarBo.getWeekNo());
			}
		}
		// 判断自己周榜是不是同一周，是则添加数据，不是则更新周榜数据
		boolean isCurrentWeek = (year == redstarBo.getYear() && curretWeekNo == redstarBo.getWeekNo());
		// 更新自己或他人红人评论数量，需要加锁，保证数据准确
		RLock lock = redisServer.getRLock(Constant.COMOMENT_LOCK);
		try {
			lock.lock(5, TimeUnit.SECONDS);
			// 更新自己的红人信息
			if (isCurrentWeek) {
				commentService.addRadstarCount(userBo.getId(), circleid);
			} else {
				commentService.updateRedWeekByUser(userBo.getId(), curretWeekNo, year);
			}
			if (isNotSelf) {
				// 更新帖子作者的红人信息
				if (isNoteUserCurrWeek) {
					commentService.addRadstarCount(noteBo.getCreateuid(), circleid);
				} else {
					commentService.updateRedWeekByUser(noteBo.getCreateuid(), curretWeekNo, year);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private RedstarBo setRedstarBo(String userid, String circleid, int weekNo, int year) {
		RedstarBo redstarBo = new RedstarBo();
		redstarBo.setUserid(userid);
		redstarBo.setCommentTotal((long) 1);
		redstarBo.setCommentWeek((long) 1);
		redstarBo.setWeekNo(weekNo);
		redstarBo.setCircleid(circleid);
		redstarBo.setYear(year);
		return redstarBo;
	}

	private void saveComment(UserBo user, String content, LinkedHashSet<String> photos, CommentBo comment,
			JSONObject object, String clazz) {
		comment.setVisitor_id(user.getId());
		comment.setUserName(user.getUserName());
		comment.setOwnerid(object.getString("createuid"));
		comment.setTargetid(object.getString("id"));
		comment.setContent(content);
		comment.setHeadPicture(user.getHeadPictureName());
		comment.setPhotos(photos);
		comment.setCreateTime(new Date());
		comment.setCreateuid(user.getId());
		comment.setSourceId(object.getString("id"));
		switch (clazz) {
		case "com.lad.bo.NoteBo":
			comment.setTargetType(1);
			break;
		case "lad.scrapybo.InforBo":
			comment.setTargetType(2);
			break;
		case "lad.scrapybo.SecurityBo":
			comment.setTargetType(7);
			break;
		case "lad.scrapybo.BroadcastBo":
			comment.setTargetType(8);
			break;
		case "lad.scrapybo.VideoBo":
			comment.setTargetType(9);
			break;
		case "lad.scrapybo.DailynewsBo":
			comment.setTargetType(10);
			break;
		case "lad.scrapybo.YanglaoBo":
			comment.setTargetType(11);
			break;
		case "com.lad.bo.CommentBo":
			comment.setTargetType(3);
			CommentBo commentBo = commentService.findById(object.getString("id"));
			comment.setSourceId(commentBo.getSourceId());
			break;
		case "com.lad.bo.DynamicBo":
			comment.setTargetType(4);
			break;
		case "com.lad.bo.ExposeBo":
			comment.setTargetType(5);
			break;
		case "com.lad.bo.PartyBo":
			comment.setTargetType(6);
			break;
		}
		commentService.insert(comment);
	}

	/**
	 * 点赞或取消点赞
	 * 
	 * @param userBo     行为的发起者
	 * @param obj        被点赞或取消点赞的实体
	 * @param isThumbsup 点赞/取消点赞
	 * @return
	 */
	protected int thumbsup(UserBo userBo, Object obj, boolean isThumbsup) {
		if (isThumbsup) {
			return thumbsup(userBo, obj);
		} else {
			return deletedThumbsup(userBo, obj);
		}

	}

	private int deletedThumbsup(UserBo userBo, Object obj) {

		if (obj == null) {
			return -1;
		}
		JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
		String owner_id = jsonObject.getString("id");
		System.out.println(String.format("这里是取消点赞,访问者是:%s(%s),访问对象为%s",userBo.getUserName(),userBo.getId(),owner_id));
		String visitor_id = userBo.getId();
		ThumbsupBo thumbsupBo = thumbsupService.findHaveOwenidAndVisitorid(owner_id,visitor_id);
		System.out.println("thumbsupBo:"+JSON.toJSONString(thumbsupBo));
		if (thumbsupBo != null && thumbsupBo.getDeleted() == Constant.ACTIVITY) {
			System.out.println("取消点赞方法在这里进入下一层");
			thumbsupService.deleteById(thumbsupBo.getId());
		}
		// 还需要根据每个实体的类型减少实体中的点赞数
//		updateInforNum(targetid, inforType, -1, Constant.THUMPSUB_NUM);
		return 0;

	}

	/**
	 * 
	 * 统一点赞接口
	 * 
	 * @param uid 点赞人
	 * @param obj 点赞对象
	 * @return
	 */
	private int thumbsup(UserBo userBo, Object obj) {
		if (obj == null) {
			return -1;
		}
		Integer type = null;
		JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
		if (obj instanceof com.lad.bo.NoteBo) {
			type = 1;
			ThumbsupBo thumbsupBo = saveThumbsup(userBo, jsonObject, type);
			if (thumbsupBo != null) {
				String noteid = jsonObject.getString("id");
				String circleId = jsonObject.getString("circleId");
				// 标记为已读
				userReasonHander(userBo.getId(), circleId, noteid);
				// 圈子热度更新
				updateCircleHot(circleService, redisServer, circleId, 1, Constant.CIRCLE_NOTE_THUMP);
				updateNoteCount(noteid, Constant.THUMPSUB_NUM, 1);
				// 发送push
				String content = "有人刚刚赞了你的帖子，快去看看吧!";
				updateCircieUnReadNum(jsonObject.getString("createuid"), circleId);
				String path = "/note/note-info.do?noteid=" + noteid;

				usePush(jsonObject.getString("createuid"), "互动通知", content, path);

				addMessage(messageService, path, content, "互动通知", noteid, 2, thumbsupBo.getId(), circleId,
						userBo.getId(), jsonObject.getString("createuid"));
			}
		}
		if (obj instanceof lad.scrapybo.BaseInforBo) {
			type = 2;
			ThumbsupBo thumbsupBo = saveThumbsup(userBo, jsonObject, type);
			if (thumbsupBo != null) {
				String inforid = jsonObject.getString("id");
				String clazz = obj.getClass().getName();
				Integer inforType = null;
				if ("lad.scrapybo.InforBo".equals(clazz)) {
					inforType = 1;
				}
				if ("lad.scrapybo.SecurityBo".equals(clazz)) {
					inforType = 2;
				}
				if ("lad.scrapybo.BroadcastBo".equals(clazz)) {
					inforType = 3;
				}
				if ("lad.scrapybo.VideoBo".equals(clazz)) {
					inforType = 4;
				}
				if ("lad.scrapybo.DailynewsBo".equals(clazz)) {
					inforType = 5;
				}
				if ("lad.scrapybo.YanglaoBo".equals(clazz)) {
					inforType = 6;
				}
				updateInforNum(inforid, inforType, 1, Constant.THUMPSUB_NUM);
				infotHostAsync(inforid, inforType);
			}
		}
		if (obj instanceof com.lad.bo.CommentBo) {
			type = 3;
			ThumbsupBo thumbsupBo = saveThumbsup(userBo, jsonObject, type);
			if (thumbsupBo != null) {
				String commentid = jsonObject.getString("id");
				updateCommentThumbsup(commentid, 1);
			}
		}
		if (obj instanceof com.lad.bo.DynamicBo) {
			type = 4;
			ThumbsupBo thumbsupBo = saveThumbsup(userBo, jsonObject, type);
			if (thumbsupBo != null) {

			}
		}
		if (obj instanceof com.lad.bo.ExposeBo) {
			type = 6;
			ThumbsupBo thumbsupBo = saveThumbsup(userBo, jsonObject, type);
			if (thumbsupBo != null) {
				String exposeid = jsonObject.getString("id");
				updateExposeCounts(exposeService, exposeid, Constant.THUMPSUB_NUM, 1);
			}
		}

		if (type == null) {
			return -1;
		}

		return 0;
	}

	/**
	 * 异步更新阅读点赞等数据
	 * 
	 * @param id
	 * @param numType
	 * @param num
	 * @return
	 */
	@Async
	protected void updateExposeCounts(IExposeService service, String id, int numType, int num) {
		RLock lock = redisServer.getRLock(id.concat(String.valueOf(numType)));
		try {
			lock.lock(2, TimeUnit.SECONDS);
			service.updateCounts(id, numType, num);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 帖子点赞
	 * 
	 * @param commentid
	 * @param num
	 */
	@Async
	private void updateDynamicThumbsup(String dynamicId, int num) {
		if (num != 0) {
			RLock lock = redisServer.getRLock(dynamicId);
			try {
				lock.lock(1, TimeUnit.SECONDS);
				dynamicService.updateThumpsubNum(dynamicId, num);
			} finally {
				lock.unlock();
			}
		}
		// 还需要讲评论大模块本身的阅读状态改为已读,但这可以在上面的处理
		/*
		 * if (num > 0) { CommentBo commentBo = commentService.findById(commentid); if
		 * (commentBo != null && commentBo.getType() == Constant.NOTE_TYPE) { NoteBo
		 * noteBo = noteService.selectById(commentBo.getNoteid()); if (noteBo != null) {
		 * updateCircieUnReadNum(commentBo.getCreateuid(), noteBo.getCircleId()); } } }
		 */
	}

	/**
	 * 帖子点赞
	 * 
	 * @param commentid
	 * @param num
	 */
	@Async
	private void updateCommentThumbsup(String commentid, int num) {
		if (num != 0) {
			RLock lock = redisServer.getRLock(commentid);
			try {
				lock.lock(1, TimeUnit.SECONDS);
				commentService.updateThumpsubNum(commentid, num);
			} finally {
				lock.unlock();
			}
		}
		// 还需要讲评论大模块本身的阅读状态改为已读,但这可以在上面的处理
		/*
		 * if (num > 0) { CommentBo commentBo = commentService.findById(commentid); if
		 * (commentBo != null && commentBo.getType() == Constant.NOTE_TYPE) { NoteBo
		 * noteBo = noteService.selectById(commentBo.getNoteid()); if (noteBo != null) {
		 * updateCircieUnReadNum(commentBo.getCreateuid(), noteBo.getCircleId()); } } }
		 */
	}

	/**
	 * 帖子 未读信息
	 * 
	 * @param userid
	 * @param cirlceid
	 */
	@Async
	private void updateCircieUnReadNum(String userid, String cirlceid) {
		RLock lock = redisServer.getRLock(userid + "UnReadNumLock");
		try {
			lock.lock(2, TimeUnit.SECONDS);
			ReasonBo reasonBo = reasonService.findByUserAndCircle(userid, cirlceid, Constant.ADD_AGREE);
			if (reasonBo == null) {
				reasonBo = new ReasonBo();
				reasonBo.setCircleid(cirlceid);
				reasonBo.setCreateuid(userid);
				reasonBo.setStatus(Constant.ADD_AGREE);
				reasonBo.setUnReadNum(1);
				reasonService.insert(reasonBo);
			} else {
				reasonService.updateUnReadNum(userid, cirlceid, 1);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 资讯 更新单条咨询访问信息记录
	 * 
	 * @param inforid
	 * @param module
	 * @param type
	 */
	@Async
	private void updateInforHistroy(String inforid, String module, int type) {
		Date currenDate = CommonUtil.getZeroDate(new Date());
		String dateStr = CommonUtil.getCurrentDate(new Date());

		Date halfTime = CommonUtil.getHalfYearTime(currenDate);
		String halgStr = CommonUtil.getCurrentDate(halfTime);

		RLock lock = redisServer.getRLock(inforid);
		List<InforHistoryBo> historyBos = null;
		List<String> ids = new ArrayList<>();
		try {
			lock.lock(5, TimeUnit.SECONDS);
			InforHistoryBo historyBo = inforRecomService.findTodayHis(inforid, dateStr);
			if (historyBo == null) {
				historyBo = new InforHistoryBo();
				historyBo.setDayNum(1);
				historyBo.setInforid(inforid);
				historyBo.setModule(module);
				historyBo.setType(type);
				historyBo.setReadDate(dateStr);
				inforRecomService.addInfoHis(historyBo);
			} else {
				inforRecomService.updateHisDayNum(historyBo.getId(), 1);
			}
			InforRecomBo recomBo = inforRecomService.findRecomByInforid(inforid);
			if (recomBo == null) {
				recomBo = new InforRecomBo();
				recomBo.setInforid(inforid);
				recomBo.setModule(module);
				recomBo.setType(type);
				recomBo.setHalfyearNum(1);
				recomBo.setTotalNum(1);
				inforRecomService.addInforRecom(recomBo);
			} else {
				inforRecomService.updateRecomByInforid(recomBo.getId(), 1, 1);
				historyBos = inforRecomService.findHalfYearHis(inforid, halgStr);
				if (historyBos == null || historyBos.isEmpty()) {
					return;
				} else {
					int disNum = 0;
					for (InforHistoryBo history : historyBos) {
						disNum += history.getDayNum();
						ids.add(history.getId());
					}
					inforRecomService.updateRecomByInforid(recomBo.getId(), -disNum, 1);
				}
			}
		} finally {
			lock.unlock();
		}
		if (!ids.isEmpty()) {
			inforRecomService.updateZeroHis(ids);
		}
	}

	/**
	 * 阅读点赞评论等数据更新
	 * 
	 * @param inforid
	 * @param inforType 资讯类型
	 * @param num
	 * @param numType   更新数据类型， 阅读、点赞等
	 */
	protected void updateInforNum(String inforid, int inforType, int num, int numType) {
		RLock lock = redisServer.getRLock(inforid.concat(String.valueOf(numType)));
		try {
			lock.lock(2, TimeUnit.SECONDS);
			switch (inforType) {
			case Constant.INFOR_HEALTH:
				inforService.updateInforNum(inforid, numType, num);
				break;
			case Constant.INFOR_SECRITY:
				inforService.updateSecurityNum(inforid, numType, num);
				break;
			case Constant.INFOR_RADIO:
				inforService.updateRadioNum(inforid, numType, num);
				break;
			case Constant.INFOR_VIDEO:
				inforService.updateVideoNum(inforid, numType, num);
				break;
			case Constant.INFOR_DAILY:
				inforService.updateDailynewsByType(inforid, numType, num);
				break;
			case Constant.INFOR_YANGLAO:
				inforService.updateYanglaoByType(inforid, numType, num);
				break;
			default:
				break;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 热度信息更新
	 * 
	 * @param inforid
	 * @param inforType
	 */
	protected void infotHostAsync(String inforid, int inforType) {
		// 根据每条资讯id加锁
		String module = "";
		switch (inforType) {
		case Constant.INFOR_HEALTH:
			InforBo inforBo = inforService.findById(inforid);
			module = inforBo != null ? inforBo.getClassName() : "";
			break;
		case Constant.INFOR_SECRITY:
			SecurityBo securityBo = inforService.findSecurityById(inforid);
			module = securityBo != null ? securityBo.getNewsType() : "";
			break;
		case Constant.INFOR_RADIO:
			BroadcastBo broadcastBo = inforService.findBroadById(inforid);
			module = broadcastBo != null ? broadcastBo.getModule() : "";
			break;
		case Constant.INFOR_VIDEO:
			VideoBo videoBo = inforService.findVideoById(inforid);
			module = videoBo != null ? videoBo.getModule() : "";
			break;
		case Constant.INFOR_DAILY:
			DailynewsBo dailynewsBo = inforService.findByDailynewsId(inforid);
			module = dailynewsBo != null ? dailynewsBo.getClassName() : "";
			break;
		case Constant.INFOR_YANGLAO:
			YanglaoBo yanglaoBo = inforService.findByYanglaoId(inforid);
			module = yanglaoBo != null ? yanglaoBo.getClassName() : "";
			break;
		default:
			break;
		}
		if (!"".equals(module)) {
			updateInforHistroy(inforid, module, inforType);
		}
	}

	protected void updateNoteCount(String noteid, int type, int num) {
		RLock lock = redisServer.getRLock(noteid.concat(String.valueOf(type)));
		try {
			lock.lock(2, TimeUnit.SECONDS);
			switch (type) {

			case Constant.VISIT_NUM:// 访问
				noteService.updateVisitCount(noteid);
				break;
			case Constant.COMMENT_NUM:// 评论
				noteService.updateCommentCount(noteid, num);
				break;
			case Constant.THUMPSUB_NUM:// 点赞
				noteService.updateThumpsubCount(noteid, num);
				break;
			case Constant.SHARE_NUM:// 分享
				noteService.updateTransCount(noteid, num);
				break;
			case Constant.COLLECT_NUM:// 收藏
				noteService.updateCollectCount(noteid, num);
				break;
			default:
				break;
			}
		} finally {
			lock.unlock();
		}
	}

	protected void userReasonHander(String userid, String circleId, String noteId) {
		// 处理是否已读
		ReasonBo reasonBo = reasonService.findByUserAndCircle(userid, circleId, 1);
		if (reasonBo != null) {
			HashSet<String> unReadSet = reasonBo.getUnReadSet() == null ? new HashSet<String>()
					: reasonBo.getUnReadSet();
			unReadSet.remove(noteId);
			reasonService.updateUnReadSet(userid, circleId, unReadSet);
		}
		updateNoteReadHistory(userid, noteId);
	}

	protected void updateNoteReadHistory(String userid, String noteid) {
		ReadHistoryBo historyByUseridAndNoteId = readHistoryService.getHistoryByUseridAndNoteId(noteid, noteid);
		if (historyByUseridAndNoteId == null) {
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
	}

	private ThumbsupBo saveThumbsup(UserBo userBo, JSONObject jsonObject, Integer type) {
		ThumbsupBo thumbsupBo = thumbsupService.findHaveOwenidAndVisitorid(userBo.getId(), jsonObject.getString("id"));
		if (thumbsupBo == null) {
			thumbsupBo = new ThumbsupBo();
			thumbsupBo.setVisitor_id(userBo.getId());
			thumbsupBo.setOwner_id(jsonObject.getString("id"));
			thumbsupBo.setType(type);
			thumbsupBo.setImage(userBo.getHeadPictureName());
			thumbsupBo.setCreateuid(userBo.getId());
			thumbsupService.insert(thumbsupBo);
		} else {
			if (thumbsupBo.getDeleted() == Constant.DELETED) {
				thumbsupService.udateDeleteById(thumbsupBo.getId());
			}
		}

		return thumbsupBo;
	}

	/**
	 * push
	 * 
	 * @param redisServer
	 * @param title
	 * @param message     地址的json格式
	 * @param description 发送neirong,又名content
	 * @param path
	 * @param aliasList
	 * @param alias
	 */
	@Async
	private void push(RedisServer redisServer, String title, String message, String description, String path,
			Set<String> userTokens, Set<String> userRegIds, List<String> aliasList, String... alias) {

		RLock lock = redisServer.getRLock(Constant.CHAT_LOCK);

		try {
			// 3s自动解锁
			lock.lock(3, TimeUnit.SECONDS);

			if (userTokens.size() > 0) {
				THREADPOOL.execute(new Runnable() {
					@Override
					public void run() {
						try {
							HuaWeiPushNcMsg.push(title,  description, path, userTokens);
						} catch (IOException e) {
							logger.error("BaseContorller====={}", e);
						}
					}
				});
			}

			if (userRegIds.size() > 0) {
				THREADPOOL.execute(new Runnable() {

					@Override
					public void run() {
						try {
							MiPushUtil.sendMessageToRegIds(title, message, description, path, userRegIds);
						} catch (Exception e) {
							logger.error("BaseContorller====={}", e);
						}

					}
				});
			}

			THREADPOOL.execute(new Runnable() {

				@Override
				public void run() {
					try {
						MeizuPushUtil.pushMessageByAlias(title, description, message, aliasList);
					} catch (Exception e) {
						logger.error("BaseContorller====={}", e);
					}
				}
			});
			THREADPOOL.execute(new Runnable() {

				@Override
				public void run() {
					try {
						VivoPushUtil.sendToMany(title, description, aliasList, message);
					} catch (Exception e) {
						logger.error("BaseContorller====={}", e);
					}
				}
			});
//			JPushUtil.push(title, "极光推送:" + description, path, alias);
		} catch (Exception e) {
			logger.error("BaseContorller====={}", e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 定向到错误页面
	 * 
	 * @return view
	 */
	public String toErrorPage() {
		return "/error";
	}

	/**
	 * 定向到错误页面
	 * 
	 * @param msg   错误消息
	 * @param model ModelMap
	 * @return view
	 */
	public String toErrorPage(String msg, ModelMap model) {
		model.addAttribute("ERROR_MSG", msg);
		return this.toErrorPage();
	}

	/**
	 * 统一校验session是否存在，不存在以异常跑出
	 * 
	 * @param request
	 * @throws MyException
	 */
	public UserBo checkSession(HttpServletRequest request, IUserService userService) throws MyException {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			throw new MyException(CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason()));
		}
		if (session.getAttribute("isLogin") == null) {
			throw new MyException(CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason()));
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			throw new MyException(CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason()));
		}
		userBo = userService.getUser(userBo.getId());
		if (null == userBo) {
			throw new MyException(
					CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(), ERRORCODE.USER_NULL.getReason()));
		}
		session.setAttribute("userBo", userBo);
		return userBo;
	}

	/**
	 * 获取session
	 * 
	 * @param request
	 */
	public UserBo getUserLogin(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return null;
		}
		if (session.getAttribute("isLogin") == null) {
			return null;
		}
		return (UserBo) session.getAttribute("userBo");
	}

	/**
	 * 同步session ,用户数据又修改之后,保证session与数据库同步
	 * 
	 * @param request
	 */
	@Async
	public void updateUserSession(HttpServletRequest request, IUserService userService) {
		HttpSession session = request.getSession();
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (null != userBo) {
			userBo = userService.getUser(userBo.getId());
			if (userBo == null) {
				// 用户不存在，注销用户session
				session.invalidate();
			} else {
				// 更新用户session
				session.setAttribute("userBo", userBo);
			}
		}
	}

	/**
	 * 更新圈子访问记录信息
	 * 
	 * @param userid
	 * @param circleid
	 * @param locationService
	 * @param circleService
	 */
	@Async
	public void updateHistory(String userid, String circleid, ILocationService locationService,
			ICircleService circleService) {
		try {
			// 获取个人的圈子操作历史
			CircleHistoryBo circleHistoryBo = circleService.findByUserIdAndCircleId(userid, circleid);
			// 获取个人的地址信息
			LocationBo locationBo = locationService.getLocationBoByUserid(userid);

			if (circleHistoryBo == null) {
				circleHistoryBo = new CircleHistoryBo();
				circleHistoryBo.setCircleid(circleid);
				circleHistoryBo.setUserid(userid);
				circleHistoryBo.setType(0);
				if (null != locationBo) {
					circleHistoryBo.setPosition(locationBo.getPosition());
				} else {
					circleHistoryBo.setPosition(new double[] { 0, 0 });
				}
				circleService.insertHistory(circleHistoryBo);
			} else {
				circleService.updateHistory(circleHistoryBo.getId(), locationBo.getPosition());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新圈子中各种访问信息或者人气等等
	 * 
	 * @param circleService
	 * @param redisServer
	 * @param circleid
	 * @param num
	 * @param type
	 */
	@Async
	public void updateCircleHot(ICircleService circleService, RedisServer redisServer, String circleid, int num,
			int type) {
		RLock lock = redisServer.getRLock(Constant.CHAT_LOCK);
		try {
			// 3s自动解锁
			lock.lock(3, TimeUnit.SECONDS);
			circleService.updateCircleHot(circleid, num, type);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 更新动态信息数量表
	 * 
	 * @param userid
	 */
	@Async
	public void updateDynamicNums(String userid, int num, IDynamicService dynamicService, RedisServer server) {
		DynamicNumBo numBo = dynamicService.findNumByUserid(userid);
		if (numBo == null) {
			numBo = new DynamicNumBo();
			numBo.setUserid(userid);
			numBo.setNumber(1);
			numBo.setTotal(1);
			dynamicService.addNum(numBo);
		} else {
			RLock lock = server.getRLock(userid + "dynamicSize");
			try {
				lock.lock(2, TimeUnit.SECONDS);
				dynamicService.updateNumbers(numBo.getId(), num);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * 添加聊天室用户的昵称
	 * 
	 * @param chatroomid
	 * @param nickname
	 */
	@Async
	public void addChatroomUser(IChatroomService service, UserBo userBo, String chatroomid, String nickname) {
		ChatroomUserBo chatroomUserBo = service.findChatUserByUserAndRoomid(userBo.getId(), chatroomid);
		if (chatroomUserBo == null) {
			chatroomUserBo = new ChatroomUserBo();
			chatroomUserBo.setChatroomid(chatroomid);
			chatroomUserBo.setUserid(userBo.getId());
			chatroomUserBo.setNickname(nickname);
			chatroomUserBo.setUsername(userBo.getUserName());
			chatroomUserBo.setShowNick(false);
			chatroomUserBo.setDisturb(false);
			service.insertUser(chatroomUserBo);
		} else if (chatroomUserBo.getDeleted() == Constant.DELETED) {
			service.updateUserNickname(chatroomUserBo.getId(), nickname);
		}
	}

	/**
	 * 获取聚会状态
	 * 
	 * @param startTimes
	 * @param appointment
	 * @return 1 进行中， 2报名结束， 3活动结束
	 */
	public int getPartyStatus(LinkedHashSet<String> startTimes, int appointment) {
		if (!CommonUtil.isEmpty(startTimes)) {
			Iterator<String> iterator = startTimes.iterator();
			String lastTime = "";
			while (iterator.hasNext()) {
				lastTime = iterator.next();
			}
			if (lastTime.equals("0")) {
				return 1;
			}
			Date lastDate = CommonUtil.getDate(lastTime, "yyyy-MM-dd HH:mm");
			if (lastDate != null) {
				Date currentLastTime = CommonUtil.getLastDate(lastDate);
				// 当前时间大于聚会的结束时间 聚会结束
				if (System.currentTimeMillis() >= currentLastTime.getTime()) {
					return 3;
				}
				long last = lastDate.getTime();
				// 减去提前预约天数
				if (appointment > 0) {
					last = last - (appointment * dayTimeMins);
				}
				// 报名时间已经结束
				if (System.currentTimeMillis() >= last) {
					return 2;
				}
			}
		}
		return 1;
	}

	@Async
	public void addMessage(IMessageService service, String path, String content, String title, String createuid,
			String... userids) {
		for (String userid : userids) {
			MessageBo messageBo = new MessageBo();
			messageBo.setContent(content);
			messageBo.setPath(path);
			messageBo.setUserid(userid);
			messageBo.setCreateuid(createuid);
			messageBo.setTitle(title);
			service.insert(messageBo);
		}
	}

	/**
	 * 消息添加到列表
	 * 
	 * @param service
	 * @param path
	 * @param content
	 * @param title
	 * @param noteid
	 * @param type
	 * @param sourceid
	 * @param userid
	 */
	@Async
	public void addMessage(IMessageService service, String path, String content, String title, String noteid, int type,
			String sourceid, String circleid, String createuid, String userid) {
		MessageBo messageBo = new MessageBo();
		messageBo.setContent(content);
		messageBo.setPath(path);
		messageBo.setUserid(userid);
		messageBo.setTitle(title);
		messageBo.setTargetid(noteid);
		messageBo.setType(type);
		messageBo.setSourceid(sourceid);
		messageBo.setCircleid(circleid);
		messageBo.setCreateuid(createuid);
		service.insert(messageBo);
	}

	/**
	 * 收信方为单个id
	 * 
	 * @param alias
	 * @param content
	 * @param path
	 */
	protected void usePush(String alias, String title, String content, String path) {
		List<String> aliasList = new ArrayList<>();
		aliasList.add(alias);
		Map<String, String> msgMap = new HashMap<>();
		msgMap.put("path", path);
		String message = JSON.toJSONString(msgMap);

		// 获取华为token
		PushTokenBo tokenBo = tokenService.findTokenEnableByUserId(alias, 1);
		Set<String> tokenSet = new HashSet<>();
		if (tokenBo != null) {
			tokenSet.add(tokenBo.getToken());
		}

		// 获取小米regId
		PushTokenBo regIdBo = tokenService.findTokenEnableByUserId(alias, 2);
		Set<String> regIdSet = new HashSet<>();
		if (regIdBo != null) {
			regIdSet.add(regIdBo.getToken());
		}

		push(redisServer, title, message, content, path, tokenSet, regIdSet, aliasList, alias);
	}

	/**
	 * 收信方为一个id的Collection集合
	 * 
	 * @param useridSet
	 * @param content
	 * @param path
	 */
	protected void usePush(Collection<String> useridSet, String title, String content, String path) {
		List<String> aliasList = new ArrayList<>(useridSet);

		Map<String, String> msgMap = new HashMap<>();
		msgMap.put("path", path);
		String message = JSON.toJSONString(msgMap);

		String[] pushUser = new String[useridSet.size()];
		useridSet.toArray(pushUser);
		// 获取华为token
		List<PushTokenBo> tokens = tokenService.findTokenByUserIds(useridSet, 1);
		Set<String> tokenSet = new HashSet<>();
		if (tokens != null) {
			for (PushTokenBo pushTokenBo : tokens) {
				tokenSet.add(pushTokenBo.getToken());
			}
		}

		// 获取小米regId
		List<PushTokenBo> regIds = tokenService.findTokenByUserIds(useridSet, 2);
		Set<String> regIdSet = new HashSet<>();
		if (regIds != null) {
			for (PushTokenBo pushTokenBo : regIds) {
				regIdSet.add(pushTokenBo.getToken());
			}
		}

		push(redisServer, title, message, content, path, tokenSet, regIdSet, aliasList, pushUser);
	}

	/**
	 * 收信方为一个id数组
	 * 
	 * @param useridArr
	 * @param content
	 * @param path
	 */
	protected void usePush(String[] useridArr, String title, String content, String path) {
		List<String> aliasList = Arrays.asList(useridArr);
		Map<String, String> msgMap = new HashMap<>();
		msgMap.put("path", path);
		String message = JSON.toJSONString(msgMap);

		// 获取华为token
		List<PushTokenBo> tokens = tokenService.findTokenByUserIds(aliasList, 1);
		Set<String> tokenSet = new HashSet<>();
		if (tokens != null) {
			for (PushTokenBo pushTokenBo : tokens) {
				tokenSet.add(pushTokenBo.getToken());
			}
		}

		// 获取小米regId
		List<PushTokenBo> regIds = tokenService.findTokenByUserIds(aliasList, 2);
		Set<String> regIdSet = new HashSet<>();
		if (regIds != null) {
			for (PushTokenBo pushTokenBo : regIds) {
				regIdSet.add(pushTokenBo.getToken());
			}
		}

		push(redisServer, title, message, content, path, tokenSet, regIdSet, aliasList, useridArr);
	}
}
