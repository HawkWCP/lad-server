package com.lad.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lad.bo.CircleBo;
import com.lad.bo.CollectBo;
import com.lad.bo.NoteBo;
import com.lad.bo.PartyBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserTagBo;
import com.lad.service.ICollectService;
import com.lad.service.IPartyService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.CollectVo;

import lad.scrapybo.BroadcastBo;
import lad.scrapybo.InforBo;
import lad.scrapybo.SecurityBo;
import lad.scrapybo.VideoBo;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/collect")
public class CollectController extends BaseContorller {

	private static final Logger logger = LoggerFactory.getLogger(CollectController.class);

	@Autowired
	private ICollectService collectService;
	@Autowired
	private IUserService userService;


	@Autowired
	private IPartyService partyService;


	@RequestMapping("/chat")
	@ResponseBody
	public String chat(String title, String content, String userid, HttpServletRequest request,
			HttpServletResponse response) {

		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo user = userService.getUser(userid);
		if (user == null) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(), ERRORCODE.USER_NULL.getReason());
		}
		CollectBo chatBo = new CollectBo();
		chatBo.setCreateuid(userBo.getId());
		chatBo.setUserid(userBo.getId());
		chatBo.setContent(content);
		chatBo.setTitle(title);
		chatBo.setType(Constant.CHAT_TYPE);
		chatBo.setSourceid(userid);
		chatBo.setTargetPic(user.getHeadPictureName());
		chatBo.setSource(user.getUserName());
		chatBo = collectService.insert(chatBo);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("col-time", CommonUtil.time2str(chatBo.getCreateTime()));
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/my-chats")
	@ResponseBody
	public String myChats(@RequestParam(required = false) String start_id, @RequestParam int limit,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		List<CollectBo> collectBos = collectService.findChatByUserid(userBo.getId(), start_id, limit,
				Constant.CHAT_TYPE);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("col-chats", collectBos);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/add-tag")
	@ResponseBody
	public String addTag(String name, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserTagBo userTagBo = new UserTagBo();
		userTagBo.setUserid(userBo.getId());
		userTagBo.setTagName(name);
		userTagBo.setTagType(0);
		collectService.insertTag(userTagBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/my-tags")
	@ResponseBody
	public String myTag(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<UserTagBo> tagBos = collectService.findTagByUserid(userBo.getId(), 0);
		List<String> tagNames = new ArrayList<>();
		for (UserTagBo tagBo : tagBos) {
			tagNames.add(tagBo.getTagName());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("tags", tagNames);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 我的收藏
	 * 
	 * @param page
	 * @param limit
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/my-collects")
	@ResponseBody
	public String myCols(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		logger.info("@RequestMapping(\"/my-collects\")=====page:{},limit:{}",page,limit);
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<CollectBo> collectBos = collectService.findAllByUserid(userBo.getId(), page, limit);
		List<CollectVo> collectVos = new LinkedList<>();
		bo2vos(collectBos, collectVos);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("collectVos", collectVos);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/col-files")
	@ResponseBody
	public String colFile(String path, int fileType, String videoPic, String userid, HttpServletRequest request,
			HttpServletResponse response) {

		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.CollectController.colFile-----{userName:未登录用户,path:" + path + ",fileType:"
					+ fileType + ",videoPic:" + videoPic + ",userid:" + userid + "}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.CollectController.colFile-----{userName:" + userBo.getUserName() + ",userId:"
				+ userBo.getId() + ",path:" + path + ",fileType:" + fileType + ",videoPic:" + videoPic + ",userid:"
				+ userid + "}");
		CollectBo chatBo = new CollectBo();
		chatBo.setCreateuid(userBo.getId());
		chatBo.setUserid(userBo.getId());
		if (fileType == Constant.COLLET_VIDEO) {
			chatBo.setTargetPic(videoPic);
			chatBo.setVideo(path);
		}
		chatBo.setSourceid(userid);
		chatBo.setPath(path);
		chatBo.setType(fileType);
		if (fileType == Constant.COLLET_URL) {
			chatBo.setSub_type(Constant.FILE_TYPE);
		}
		collectService.insert(chatBo);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("col-time", CommonUtil.time2str(chatBo.getCreateTime()));
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/by-tagName")
	@ResponseBody
	public String findByTag(String tagName, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}

		List<CollectBo> collectBos = collectService.findByTag(tagName, userBo.getId(), page, limit);
		List<CollectVo> collectVos = new LinkedList<>();
		bo2vos(collectBos, collectVos);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("collectVos", collectVos);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/by-type")
	@ResponseBody
	public String findByTag(int type, int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<CollectBo> collectBos = collectService.findByUseridAndType(userBo.getId(), type, page, limit);
		List<CollectVo> collectVos = new LinkedList<>();
		bo2vos(collectBos, collectVos);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("collectVos", collectVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 给收藏添加分类
	 * 
	 * @param tags
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/add-col-tag")
	@ResponseBody
	public String addCollectTag(String tags, String collectid, HttpServletRequest request,
			HttpServletResponse response) {
		CollectBo collectBo = collectService.findById(collectid);
		if (collectBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.COLLECT_IS_NULL.getIndex(),
					ERRORCODE.COLLECT_IS_NULL.getReason());
		}
		String[] tagArr = tags.split(",");
		LinkedHashSet<String> userTags = collectBo.getUserTags();
		for (String tag : tagArr) {
			userTags.add(tag);
		}
		collectService.updateTags(collectid, userTags);
		return Constant.COM_RESP;
	}

	/**
	 * 给收藏添加分类
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/del-collect")
	@ResponseBody
	public String delCollect(String collectids, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		String[] idArr = CommonUtil.getIds(collectids);
		if (idArr.length == 1) {
			collectService.delete(idArr[0]);
		} else {
			List<String> ids = new ArrayList<>();
			for (String id : idArr) {
				ids.add(id);
			}
			collectService.delete(ids);
		}
		return Constant.COM_RESP;
	}

	private void bo2vos(List<CollectBo> collectBos, List<CollectVo> collectVos) {
		for (CollectBo collectBo : collectBos) {
			CollectVo vo = new CollectVo();
			BeanUtils.copyProperties(collectBo, vo);
			vo.setCollectid(collectBo.getId());
			vo.setCollectTime(collectBo.getCreateTime());
			vo.setCollectPic(collectBo.getTargetPic());
			int type = collectBo.getType();
			if (type == Constant.CHAT_TYPE || (Constant.COLLET_PIC <= type && type <= Constant.COLLET_VOICE)) {
				UserBo userBo = userService.getUser(collectBo.getSourceid());
				vo.setCollectUserid(collectBo.getSourceid());
				if (userBo != null) {
					vo.setCollectUserName(userBo.getUserName());
					vo.setCollectUserPic(userBo.getHeadPictureName());
				}
			} else if (collectBo.getType() == Constant.COLLET_URL) {
				String id = collectBo.getTargetid();
				if (collectBo.getSub_type() == Constant.CIRCLE_TYPE) {
					CircleBo circleBo = circleService.selectById(id);
					if (circleBo != null) {
						vo.setCollectPic(circleBo.getHeadPicture());
					}	
				} else if(collectBo.getSub_type() == Constant.NOTE_TYPE) {
					NoteBo noteBo = noteService.selectById(id);

					if(noteBo == null){
						continue;
					}

					if(noteBo.getForward() == 1) {
						vo.setNoteForward(1);
						int noteType = noteBo.getNoteType();

						if(noteType == NoteBo.NOTE_FORWARD) {
							vo.setNoteForwardType(NoteBo.NOTE_FORWARD);
							noteBo = noteService.selectById(noteBo.getSourceid());
							setNoteType(vo, noteBo);
						}else if(noteType == NoteBo.INFOR_FORWARD) {
							vo.setNoteForwardType(NoteBo.INFOR_FORWARD);
							String inforid = noteBo.getSourceid();
							int inforType = noteBo.getInforType();
							vo.setTargetid(inforid);
							switch (inforType) {
								case Constant.INFOR_HEALTH:
									InforBo inforBo = inforService.findById(inforid);

									if (inforBo != null) {
										vo.setModule(inforBo.getModule());
										vo.setClassName(inforBo.getClassName());
										vo.setTitle(inforBo.getTitle());
										//收藏来源类型，资讯类型来源分类，1 健康， 2安防， 3 广播， 4 视频， 5 圈子
										vo.setNoteForwardInforType(1);
										vo.setNoteForwardSourceId(inforBo.getId());
										vo.setNoteFileType(inforBo.getImageUrls().size()>0?2:1);
										vo.setCollectPic(inforBo.getImageUrls().get(0));
									}
									break;
								case Constant.INFOR_SECRITY:
									SecurityBo securityBo = inforService.findSecurityById(inforid);
									if (securityBo != null) {
										vo.setModule(securityBo.getNewsType());
										vo.setTitle(securityBo.getTitle());
										vo.setNoteForwardInforType(2);
										vo.setNoteForwardSourceId(securityBo.getId());
										vo.setNoteFileType(1);
									}
									break;
								case Constant.INFOR_RADIO:
									BroadcastBo broadcastBo = inforService.findBroadById(inforid);
									if (broadcastBo != null) {
										vo.setModule(broadcastBo.getModule());
										vo.setClassName(broadcastBo.getClassName());
										vo.setTitle(broadcastBo.getTitle());
										vo.setNoteForwardInforType(3);
										vo.setNoteForwardSourceId(broadcastBo.getId());
										vo.setNoteFileType(4);
									}
									break;
								case Constant.INFOR_VIDEO:
									VideoBo videoBo = inforService.findVideoById(inforid);
									if (videoBo != null) {
										vo.setModule(videoBo.getModule());
										vo.setClassName(videoBo.getClassName());
										vo.setTitle(videoBo.getTitle());
										vo.setNoteForwardInforType(4);
										vo.setNoteForwardSourceId(videoBo.getId());
										vo.setNoteFileType(2);
										vo.setVideo(videoBo.getUrl());
									}
									break;
								default:
									break;
							}
						}
					}else{
						vo.setNoteForward(0);
						setNoteType(vo, noteBo);
					}
					
				}else if (collectBo.getSub_type() == Constant.PARTY_TYPE) {
					PartyBo partyBo = partyService.findById(id);
					if (partyBo != null) {
						vo.setCollectPic(partyBo.getBackPic());
					}
				} else if (collectBo.getSub_type() == Constant.INFOR_TYPE) {
					String inforid = "";
					vo.setCollectPic(collectBo.getTargetPic());
					int inforType = collectBo.getSourceType();
					vo.setSourceType(inforType);
					// 表示收藏的为合集
					if (!StringUtils.isEmpty(collectBo.getFirstid())) {
						vo.setInforGroups(true);
						vo.setModule(collectBo.getModule());
						vo.setClassName(collectBo.getClassName());
						vo.setTargetid(collectBo.getFirstid());
					} else {
						inforid = collectBo.getTargetid();
						vo.setTargetid(inforid);
						switch (inforType) {
						case Constant.INFOR_HEALTH:
							InforBo inforBo = inforService.findById(inforid);
							if (inforBo != null) {
								vo.setModule(inforBo.getModule());
								vo.setClassName(inforBo.getClassName());
								vo.setTitle(inforBo.getTitle());
							}
							break;
						case Constant.INFOR_SECRITY:
							SecurityBo securityBo = inforService.findSecurityById(inforid);
							if (securityBo != null) {
								vo.setModule(securityBo.getNewsType());
								vo.setTitle(securityBo.getTitle());
							}
							break;
						case Constant.INFOR_RADIO:
							BroadcastBo broadcastBo = inforService.findBroadById(inforid);
							if (broadcastBo != null) {
								vo.setModule(broadcastBo.getModule());
								vo.setClassName(broadcastBo.getClassName());
								vo.setTitle(broadcastBo.getTitle());
							}
							break;
						case Constant.INFOR_VIDEO:
							VideoBo videoBo = inforService.findVideoById(inforid);
							if (videoBo != null) {
								vo.setModule(videoBo.getModule());
								vo.setClassName(videoBo.getClassName());
								vo.setTitle(videoBo.getTitle());
							}
							break;
						default:
							break;
						}
					}
				}
				vo.setVideo(collectBo.getVideo());
			}
			collectVos.add(vo);
		}
	}

	private void setNoteType(CollectVo vo, NoteBo noteBo) {
		if("pic".equals(noteBo.getType())) {
            vo.setNoteFileType(2);
            vo.setCollectPic(noteBo.getPhotos().get(0));
        }else if("video".equals(noteBo.getType())){
            vo.setNoteFileType(3);
            vo.setVideo(noteBo.getPhotos().get(0));
            vo.setVideoPic(noteBo.getVideoPic());
        }else if("notes".equals(noteBo.getType())) {
            vo.setNoteFileType(1);
        }
	}

}
