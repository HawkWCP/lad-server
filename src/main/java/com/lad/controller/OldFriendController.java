package com.lad.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanComparator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.OldFriendRequireBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserTasteBo;
import com.lad.service.IChatroomService;
import com.lad.service.IFriendsService;
import com.lad.service.IOldFriendService;
import com.lad.service.IPictureService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.ERRORCODE;
import com.lad.util.IMUtil;
import com.lad.vo.OldFriendRequireVo;
import com.lad.vo.ShowResultVo;
import com.lad.vo.UserTasteVo;
import com.mongodb.WriteResult;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("oldFriend")
@SuppressWarnings("all")
public class OldFriendController extends BaseContorller {
	@Autowired
	private IOldFriendService oldFriendService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IFriendsService friendsService;
	@Autowired
	private IChatroomService chatroomService;
	@Autowired
	private IPictureService pictureService;

	/**
	 * 匹配推荐
	 * 
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/recommend")
	public String recommend(String requireId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		Map map = new HashMap<>();
		// 通过基础id获取需求
		OldFriendRequireBo require = oldFriendService.getByRequireId(userBo.getId(), requireId);
		if (require == null) {
			return CommonUtil.toErrorResult(ERRORCODE.REQUIREID_NOMATCH.getIndex(),
					ERRORCODE.REQUIREID_NOMATCH.getReason());
		}

		List<Map> recommend = oldFriendService.getRecommend(require);

		if (recommend.size() >= 1) {
			List result = new ArrayList<>();
			for (Map recMap : recommend) {
				Map resultOne = new HashMap<>();
				OldFriendRequireBo resultBo = (OldFriendRequireBo) recMap.get("requireBo");
				ShowResultVo showResultVo = getShowResultVo(resultBo);
				String channelId = CommonUtil.getChannelId(userBo.getId(), resultBo.getCreateuid(), friendsService,
						chatroomService, userService);
				if (channelId == null || IMUtil.FINISH.equals(channelId) || "idWrong".equals(channelId)) {
					showResultVo.setFriend(false);
					showResultVo.setChannelId("");
				} else {
					showResultVo.setFriend(true);
					showResultVo.setChannelId(channelId);
				}
				resultOne.put("match", recMap.get("match"));
				resultOne.put("baseData", showResultVo);
				OldFriendRequireVo resultVo = new OldFriendRequireVo();
				BeanUtils.copyProperties(resultBo, resultVo);
				resultVo = (OldFriendRequireVo) CommonUtil.vo_format(resultVo, OldFriendRequireVo.class);
				resultVo.setImages(CommonUtil.getWall(pictureService, resultBo.getCreateuid()));
				resultOne.put("require", resultVo);
				result.add(resultOne);
			}
			map.put("ret", 0);

			Comparator<? super Map> c = new BeanComparator("match").reversed();
			result.sort(c);
			map.put("recommend", result);
		} else {
			map.put("ret", -1);
			map.put("message", "未找到匹配者");
		}
		return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
	}

	/**
	 * 最新推荐
	 * 
	 * @param page
	 * @param limit
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/getNew")
	public String getNew(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}

		List<OldFriendRequireBo> list = oldFriendService.findNewPublish(page, limit, userBo.getId());

		Map map = new HashMap<>();
		if (list.size() >= 1) {
			List resultList = new ArrayList();
			for (OldFriendRequireBo requireBo : list) {
				UserBo user = userService.getUser(requireBo.getCreateuid());
				ShowResultVo showResult = new ShowResultVo();

				showResult.setMyself(user.getId().equals(userBo.getId()));

				showResult.setId(requireBo.getId());

				// 设置昵称
				String userName = user.getUserName();
				showResult.setNickName(StringUtils.isEmpty(userName) ? "用户" + user.getId() : userName);

				// 设置头像
				String headPictureName = user.getHeadPictureName();
				showResult.setHeadPicture(StringUtils.isEmpty(headPictureName) ? "" : headPictureName);

				// 设置兴趣
				UserTasteBo findByUserId = userService.findByUserId(user.getId());
				UserTasteBo hobbysBo = StringUtils.isEmpty(findByUserId) ? new UserTasteBo() : findByUserId;
				UserTasteVo hobbysVo = new UserTasteVo();
				BeanUtils.copyProperties(hobbysBo, hobbysVo);
				showResult.setHobbys(hobbysVo);
				// 设置生日
				if (user.getBirthDay() != null) {
					String birthDay = user.getBirthDay();
					DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
					try {
						Date parse = format.parse(birthDay);
						showResult.setAge(CommonUtil.getAge(parse));
					} catch (Exception e) {

						showResult.setAge(0);
					}
				} else {
					showResult.setAge(0);
				}

				if (user.getSex() != null) {
					showResult.setSex(user.getSex());
				} else {
					showResult.setSex("未填写");
				}
				if (user.getCity() != null) {
					showResult.setAddress(user.getCity());
				} else {
					showResult.setAddress("未填写");
				}
				String channelId = CommonUtil.getChannelId(userBo.getId(), user.getId(), friendsService,
						chatroomService, userService);
				if (channelId == null || IMUtil.FINISH.equals(channelId) || "idWrong".equals(channelId)) {
					showResult.setFriend(false);
					showResult.setChannelId("");
				} else {
					showResult.setFriend(true);
					showResult.setChannelId(channelId);
				}

				showResult.setUid(user.getId());
				resultList.add(showResult);
			}
			map.put("ret", 0);
			map.put("result", resultList);
		} else {
			map.put("ret", -1);
			map.put("message", "无符合条件的数据");
		}

		return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
	}

	/**
	 * 关键字搜索
	 * 
	 * @param keyWord
	 * @param page
	 * @param limit
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/search")
	public String search(String keyWord, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}

		// System.out.println(keyWord);
		// keyWord = "相师";
		List<UserBo> list = oldFriendService.findListByKeyword(keyWord, page, limit, userBo.getId());

		Map map = new HashMap<>();

		List resultList = new ArrayList();
		for (UserBo user : list) {
			// UserBo user = userService.getUser(requireBo.getCreateuid());
			OldFriendRequireBo requireBo = oldFriendService.getRequireByCreateUid(user.getId());
			ShowResultVo showResult = new ShowResultVo();
			if (requireBo == null) {
				continue;
			}
			showResult.setId(requireBo.getId());

			// 设置昵称
			if (user.getUserName() != null) {
				showResult.setNickName(user.getUserName());
			} else {
				showResult.setNickName("");
			}

			// 设置头像
			if (user.getHeadPictureName() != null) {
				showResult.setHeadPicture(user.getHeadPictureName());
			} else {
				showResult.setHeadPicture("");
			}

			// 设置兴趣
			UserTasteBo findByUserId = userService.findByUserId(user.getId());
			UserTasteBo hobbysBo = StringUtils.isEmpty(findByUserId) ? new UserTasteBo() : findByUserId;
			UserTasteVo hobbysVo = new UserTasteVo();
			BeanUtils.copyProperties(hobbysBo, hobbysVo);
			showResult.setHobbys(hobbysVo);

			// 设置生日
			if (userBo.getBirthDay() != null) {
				String birthDay = userBo.getBirthDay();
				DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
				try {
					Date parse = format.parse(birthDay);
					showResult.setAge(CommonUtil.getAge(parse));
				} catch (Exception e) {
					showResult.setAge(0);
				}
			}

			if (user.getSex() != null) {
				showResult.setSex(user.getSex());
			} else {
				showResult.setSex("");
			}
			if (user.getCity() != null) {
				showResult.setAddress(user.getCity());
			} else {
				showResult.setAddress("");
			}
			showResult.setMyself(user.getId().equals(userBo.getId()));
			showResult.setImages(CommonUtil.getWall(pictureService, requireBo.getCreateuid()));
			resultList.add(showResult);
		}
		if (resultList.size() >= 1) {
			map.put("ret", 0);
			map.put("result", resultList);
		} else {
			map.put("ret", -1);
			map.put("message", "无符合条件的数据");
		}

		return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
	}

	/**
	 * 获取详情
	 * 
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/getDesc")
	public String getDesc(String requireId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		OldFriendRequireBo requireBo = oldFriendService.getByRequireId(requireId);
		if (requireBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.No_SUCH_PUBLISH.getIndex(),
					ERRORCODE.No_SUCH_PUBLISH.getReason());
		}

		UserBo user = userService.getUser(requireBo.getCreateuid());

		Map map = new HashMap<>();
		if (user != null) {
			ShowResultVo result = new ShowResultVo();
			if (user.getUserName() != null) {
				result.setNickName(user.getUserName());
			} else {
				result.setNickName("");
			}
			if (user.getHeadPictureName() != null) {
				result.setHeadPicture(user.getHeadPictureName());
			} else {
				result.setHeadPicture("");
			}
			if (user.getSex() != null) {
				result.setSex(user.getSex());
			} else {
				result.setSex("");
			}

			if (user.getBirthDay() != null) {
				String birthDay = user.getBirthDay();
				DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
				try {
					Date parse = format.parse(birthDay);
					result.setAge(CommonUtil.getAge(parse));
				} catch (Exception e) {
					result.setAge(0);
				}
			}
			if (user.getAddress() != null) {
				result.setAddress(user.getAddress());
			} else {
				result.setAddress("");
			}
			result.setUid(user.getId());
			result.setAddress(user.getAddress());
			String channelId = CommonUtil.getChannelId(userBo.getId(), user.getId(), friendsService, chatroomService,
					userService);
			if (channelId == null || IMUtil.FINISH.equals(channelId) || "idWrong".equals(channelId)) {
				result.setFriend(false);
				result.setChannelId("");
			} else {
				result.setFriend(true);
				result.setChannelId(channelId);
			}
			UserTasteBo findByUserId = userService.findByUserId(user.getId());
			UserTasteBo hobbysBo = StringUtils.isEmpty(findByUserId) ? new UserTasteBo() : findByUserId;
			UserTasteVo hobbysVo = new UserTasteVo();
			BeanUtils.copyProperties(hobbysBo, hobbysVo);
			result.setHobbys(hobbysVo);
			result.setMyself(requireBo.getCreateuid().equals(userBo.getId()));
			result.setImages(CommonUtil.getWall(pictureService, user.getId()));
			map.put("ret", 0);
			map.put("baseData", result);
		} else {
			map.put("ret", -1);
			map.put("baseData", "未查找到用户的相关数据");
		}

		OldFriendRequireVo requireVo = new OldFriendRequireVo();
		BeanUtils.copyProperties(requireBo, requireVo);
		requireVo = (OldFriendRequireVo) CommonUtil.vo_format(requireVo, OldFriendRequireVo.class);
		map.put("requireData", requireVo);
		return JSON.toJSONString(map);
	}

	/**
	 * 修改需求
	 * 
	 * @param requireData
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/updateRequire")
	public String updateRequire(@RequestParam String requireData, String requireId, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		requireData = CommonUtil.fl_format(requireData);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		// 校验require与uid是否匹配
		OldFriendRequireBo oldRequireBo = oldFriendService.getByRequireId(userBo.getId(), requireId);
		if (oldRequireBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.REQUIREID_NOMATCH.getIndex(),
					ERRORCODE.REQUIREID_NOMATCH.getReason());
		}

		Iterator<Map.Entry<String, Object>> entitySet = JSONObject.fromObject(requireData).entrySet().iterator();

		Map<String, Object> params = new HashMap<>();
		while (entitySet.hasNext()) {
			Map.Entry<String, Object> entity = entitySet.next();

			if ("sex".equals(entity.getKey()) && !(entity.getValue().equals(oldRequireBo.getSex()))) {
				params.put(entity.getKey(), entity.getValue());
				continue;
			}

			if ("age".equals(entity.getKey()) && !(entity.getValue().equals(oldRequireBo.getAge()))) {
				params.put(entity.getKey(), entity.getValue());
				continue;
			}
			if ("address".equals(entity.getKey()) && !(entity.getValue().equals(oldRequireBo.getAddress()))) {
				params.put(entity.getKey(), entity.getValue());
				continue;
			}
			if ("hobbys".equals(entity.getKey())) {
				params.put(entity.getKey(), entity.getValue());
				continue;
			}
		}

		if (params.size() <= 0) {
			return CommonUtil.toErrorResult(ERRORCODE.UPDATE_NO_CHANGE.getIndex(),
					ERRORCODE.UPDATE_NO_CHANGE.getReason());
		}
		WriteResult result = oldFriendService.updateByParams(params, requireId);

		Map map = new HashMap<>();
		map.put("ret", 0);
		map.put("uid", userBo.getId());
		map.put("requireId", requireId);
		return JSON.toJSONString(map);
	}

	/**
	 * 获取需求详情
	 * 
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/getRequire")
	public String getRequire(String requireId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}

		OldFriendRequireBo requireBo = oldFriendService.getByRequireId(userBo.getId(), requireId);

		Map map = new HashMap<>();
		if (requireBo != null) {
			OldFriendRequireVo requireVo = new OldFriendRequireVo();
			try {
				BeanUtils.copyProperties(requireBo, requireVo);
				requireVo = (OldFriendRequireVo) CommonUtil.vo_format(requireVo, OldFriendRequireVo.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put("ret", 0);
			map.put("requireData", requireVo);

		} else {
			map.put("ret", -1);
			map.put("message", "数据请求失败,请检查你的参数是否正确");
		}
		return JSON.toJSONString(map);
	}

	/**
	 * 删除一条数据
	 * 
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/delete")
	public String delete(String requireId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		Map map = new HashMap<>();
		try {
			WriteResult result = oldFriendService.deleteByRequireId(userBo.getId(), requireId);

			map.put("ret", 0);
			map.put("message", "删除成功");
		} catch (Exception e) {

			map.put("ret", -1);
			map.put("message", e.toString());
		}

		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 添加一条数据
	 * 
	 * @param requireData
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/add")
	public String insert(@RequestParam String requireData, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		requireData = CommonUtil.fl_format(requireData);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		JSONObject fromObject = JSONObject.fromObject(requireData);
		OldFriendRequireBo requireBo = (OldFriendRequireBo) JSONObject.toBean(fromObject, OldFriendRequireBo.class);
		if (requireBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.PARAMS_ERROR.getIndex(), ERRORCODE.PARAMS_ERROR.getReason());
		}

		long count = oldFriendService.getRequireCount(userBo.getId());
		if (count >= 1) {
			return CommonUtil.toErrorResult(ERRORCODE.PUBLISHNUM_BEYOND.getIndex(),
					ERRORCODE.PUBLISHNUM_BEYOND.getReason());
		}
		if (requireBo.isAgree() == false) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_AGREEMENT_FALSE.getIndex(),
					ERRORCODE.USER_AGREEMENT_FALSE.getReason());
		}		
		requireBo.setCreateuid(userBo.getId());
		requireBo.setUpdateTime(new Date());
		requireBo.setUpdateuid(userBo.getId());
		requireBo.setUid(new ObjectId(userBo.getId()));
		String requireId = oldFriendService.insert(requireBo);
		Map map = new HashMap<>();
		if (requireId != null) {
			map.put("ret", 0);
			map.put("uid", userBo.getId());
			map.put("requireId", requireId);
		} else {
			map.put("ret", -1);
			map.put("message", ERRORCODE.INSERT_FAILD.getReason());
		}
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/init")
	public String init(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}

		String requireId = oldFriendService.getInitData(userBo.getId());

		Map map = new HashMap<>();
		if (requireId != null) {
			map.put("ret", 0);
			map.put("uid", userBo.getId());
			map.put("requireId", requireId);
		} else {
			map.put("ret", -1);
			map.put("message", ERRORCODE.No_SUCH_PUBLISH.getReason());
		}

		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 获取基础资料
	 * 
	 * @param requireId
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/getBase")
	public String getBase(String requireId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		OldFriendRequireBo requireBo = oldFriendService.getByRequireId(userBo.getId(), requireId);
		if (requireBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.No_SUCH_PUBLISH.getIndex(),
					ERRORCODE.No_SUCH_PUBLISH.getReason());
		}

		ShowResultVo result = new ShowResultVo();
		if (userBo.getUserName() != null) {
			result.setNickName(userBo.getUserName());
		} else {
			result.setNickName("");
		}
		if (userBo.getHeadPictureName() != null) {
			result.setHeadPicture(userBo.getHeadPictureName());
		} else {
			result.setHeadPicture("");
		}
		if (userBo.getSex() != null) {
			result.setSex(userBo.getSex());
		} else {
			result.setSex("");
		}

		if (userBo.getBirthDay() != null) {
			String birthDay = userBo.getBirthDay();
			DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
			try {
				Date parse = format.parse(birthDay);
				result.setAge(CommonUtil.getAge(parse));
			} catch (Exception e) {
				result.setAge(0);
			}
		}
		if (userBo.getCity() != null) {
			result.setAddress(userBo.getCity());
		} else {
			result.setAddress("");
		}

		UserTasteBo findByUserId = userService.findByUserId(userBo.getId());
		UserTasteBo hobbysBo = StringUtils.isEmpty(findByUserId) ? new UserTasteBo() : findByUserId;
		UserTasteVo hobbysVo = new UserTasteVo();
		BeanUtils.copyProperties(hobbysBo, hobbysVo);
		result.setHobbys(hobbysVo);
		result.setId(requireBo.getId());
		result.setImages(CommonUtil.getWall(pictureService, userBo.getId()));

		Map map = new HashMap<>();
		map.put("ret", 0);
		map.put("baseData", result);

		return JSON.toJSONString(map);
	}

	/**
	 * 当视图要求不符合返回实体是,将数据封装到不规则实体中
	 * 
	 * @param requireBo
	 * @return
	 */
	private ShowResultVo getShowResultVo(OldFriendRequireBo requireBo) {
		// 根据requireId
		UserBo user = userService.getUser(requireBo.getCreateuid());
		ShowResultVo showResult = new ShowResultVo();

		// 设置头像
		if (user.getHeadPictureName() != null) {
			showResult.setHeadPicture(user.getHeadPictureName());
		} else {
			showResult.setHeadPicture(null);
		}
		// 设置昵称
		if (user.getUserName() != null) {
			showResult.setNickName(user.getUserName());
		} else {
			showResult.setNickName(null);
		}

		// 设置性别
		if (user.getSex() != null) {
			showResult.setSex(user.getSex());
		} else {
			showResult.setSex(null);
		}

		// 设置年龄
		if (user.getBirthDay() != null) {
			String birthDay = user.getBirthDay();
			DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
			try {
				Date parse = format.parse(birthDay);
				showResult.setAge(CommonUtil.getAge(parse));
			} catch (Exception e) {
				showResult.setAge(0);
			}
		} else {
			showResult.setAge(0);
		}

		// 设置居住地
		if (user.getCity() != null) {
			showResult.setAddress(user.getCity());
		} else {
			showResult.setAddress(null);
		}

		// 设置兴趣
		UserTasteBo findByUserId = userService.findByUserId(user.getId());
		UserTasteBo hobbysBo = StringUtils.isEmpty(findByUserId) ? new UserTasteBo() : findByUserId;
		UserTasteVo hobbysVo = new UserTasteVo();
		BeanUtils.copyProperties(hobbysBo, hobbysVo);
		showResult.setHobbys(hobbysVo);

		return showResult;
	}

	@RequestMapping("epicQuery")
	public String epicQuery(String rId, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		Map map = new HashMap<>();
		// 通过基础id获取需求
		OldFriendRequireBo require = oldFriendService.getByRequireId(userBo.getId(), rId);
		if (require == null) {
			return CommonUtil.toErrorResult(ERRORCODE.REQUIREID_NOMATCH.getIndex(),
					ERRORCODE.REQUIREID_NOMATCH.getReason());
		}

		AggregationResults<Document> recommend = oldFriendService.epicQuery(require);

		return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
	}
}
