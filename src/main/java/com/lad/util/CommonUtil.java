package com.lad.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.lad.bo.ChatroomBo;
import com.lad.bo.DynamicBackBo;
import com.lad.bo.FriendsBo;
import com.lad.bo.PictureBo;
import com.lad.bo.PictureWallBo;
import com.lad.bo.UserBo;
import com.lad.service.IChatroomService;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.IPictureService;
import com.lad.service.IUserService;

import net.sf.json.JSONObject;

public class CommonUtil {

	private static Logger logger = LogManager.getLogger(CommonUtil.class);
	/**
	 * 每天多少毫秒时间
	 */
	private static long dayMinlls = 1000L * 60 * 60 * 24 * 180;

	public static boolean isRightPhone(String phone) {
		String regExp = "^1(3|4|5|7|8)\\d{9}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(phone);
		return m.find();
	}

	/**
	 *  获取字符串中的数字信息
	 * @param str
	 * @return
	 */
	public static int[] numInStr(String str) {
		String regex = "\\D+";
		String[] split = str.split(regex);
		int[] result = new int[split.length];

		for (int i = 0; i < split.length; i++) {
			result[i] = Integer.valueOf(split[i]);
		}

		return result;
	}

	public static String getSHA256(String content) {
		MessageDigest digest;
		String output = "";
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(content.getBytes("UTF-8"));
			output = Hex.encodeHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			logger.error("密码加密错误：{}", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("密码加密错误：{}", e);
		}
		return output;
	}

	/**
	 * 上传video是，获取缩略图
	 * 
	 * @param file
	 * @param path
	 * @param filename
	 * @param due
	 * @return 0 是video 的路径， 1 是缩略图路径
	 */
	public static String[] uploadVedio(MultipartFile file, String path, String filename, int due) {
		File targetFile = new File(path, filename);
		String vedio = "";
		String pic = "";
		try {
			if (!targetFile.exists()) {
				targetFile.mkdirs();
				file.transferTo(targetFile);
			}
			String outName = FFmpegUtil.transfer(targetFile, path);
			if (StringUtils.isEmpty(outName)) {
				outName = FFmpegUtil.transfer(targetFile, path);
			}
			if (StringUtils.isNotEmpty(outName)) {
				if (due == 0) {
					pic = QiNiu.uploadToQiNiu(path, outName);
				} else {
					pic = QiNiu.uploadToQiNiuDue(path, outName, due);
				}
				File picfile = new File(path, outName);
				picfile.delete();
			}
			if (due == 0) {
				vedio = QiNiu.uploadToQiNiu(path, filename);
			} else {
				vedio = QiNiu.uploadToQiNiuDue(path, filename, due);
			}
			targetFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] res = new String[] { Constant.QINIU_URL + vedio + "?v=" + CommonUtil.getRandom1(),
				Constant.QINIU_URL + pic + "?v=" + CommonUtil.getRandom1() };
		return res;
	}

	/**
	 * 上传图片
	 * 
	 * @param file
	 * @param path
	 * @param filename
	 * @param due
	 * @return
	 */
	public static String upload(MultipartFile file, String path, String filename, int due) {
		File targetFile = new File(path, filename);
		String result = "";
		if (!targetFile.exists()) {
			targetFile.mkdirs();
		}
		try {
			file.transferTo(targetFile);
			if (due == 0) {
				result = QiNiu.uploadToQiNiu(path, filename);
			} else {
				result = QiNiu.uploadToQiNiuDue(path, filename, due);
			}
			targetFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Constant.QINIU_URL + result + "?v=" + CommonUtil.getRandom1();
	}

	public static String toErrorResult(int ret, String error) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ret", ret);
		map.put("error", error);
		return JSONObject.fromObject(map).toString();
	}

	public static int sendSMS1(String mobile, String message) {
		try {
			message = URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
		}
		String url = "http://hprpt2.eucp.b2m.cn:8080/sdkproxy/sendsms.action?cdkey=0SDK-EBB-6699-RHSLQ&password=797391&phone="
				+ mobile + "&message=" + message;
		String responseString = HttpClientUtil.getInstance().doGetRequest(url);
		if (responseString.trim().equals(Constant.RESPONSE)) {
			return 0;
		}
		return -1;
	}

	public static int sendSMS2(String mobile, String message) {
		try {
			message = URLEncoder.encode(message, "GBK");
		} catch (UnsupportedEncodingException ex) {
			logger.error("com.lad.util.CommonUtil.sendSMS2-----:" + ex);
		}
		String url = "http://sms-gw.bjedu.cloud:9888/smsservice/SendSMS?UserId=100535&Password=ttlyyl_2017&Mobiles="
				+ mobile + "&Content=" + message + "&ExtNo=35";
		String responseString = HttpClientUtil.getInstance().doGetRequest(url);
		logger.info("com.lad.util.CommonUtil.sendSMS2-----:{} : =====message send result : {}", mobile, responseString);
		if (responseString.trim().equals(Constant.RESPONSE)) {
			return 0;
		}
		return -1;
	}

	/**
	 * 获取短息发送list
	 * 
	 * @return
	 */
	public static String getSMSReport() {
		String url = "http://sms-gw.bjedu.cloud:9888/smsservice/ReceiveReport?UserId=100535&Password=ttlyyl_2017&ExtNo=35";
		return HttpClientUtil.getInstance().doGetRequest(url);
	}

	/**
	 * 获取短息发送list
	 * 
	 * @return
	 */
	public static String getSMSReport2() {
		String url = "http://sms-gw.bjedu.cloud:9888/smsservice/ReceiveReport?UserId=100535&Password=ttlyyl_2017";
		return HttpClientUtil.getInstance().doGetRequest(url);
	}

	public static int getRandom1() {
		return (int) (1 + Math.random() * (10));
	}

	public static String getRandom() {
		return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
	}

	public static String buildCodeMsg(String number) {
		String msg = "";
		try {
			String new1 = new String("您的验证码为：".getBytes(), "UTF-8");
			String new2 = new String("，该验证码5分钟内有效，如非本人操作，请忽略。".getBytes(), "UTF-8");
			StringBuilder builder = new StringBuilder();
			builder.append(new1).append(number).append(new2);
			msg = builder.toString();
		} catch (Exception e) {
			logger.error("send Msg exception : {}", e.getMessage());
		}
		return msg;
	}

	public static String buildPassMsg(String number) {
		String msg = "";
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("您正在修改密码，验证码为：").append(number).append("，该验证码5分钟内有效，如非本人操作，请忽略。");
			msg = new String(builder.toString().getBytes(), "UTF-8");
		} catch (Exception e) {
			logger.error("send Msg exception : {}", e.getMessage());
		}
		return msg;
	}

	/**
	 * 将时间转成字符串
	 * 
	 * @param date 时间
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String time2str(Date date) {
		return date2Str("yyyy-MM-dd HH:mm:ss", date);
	}

	/**
	 * 将日期转换成制定格式
	 * 
	 * @param format 日期格式如 yyyy-MM-dd HH:mm:ss
	 * @param date   传入时间
	 * @return 时间字符串
	 */
	public static String date2Str(String format, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 判断可变形参是否为空
	 * 
	 * @param ids 参数
	 * @return true if empty
	 */
	public static boolean isEmpty(String... ids) {
		return ids == null || ids.length == 0;
	}

	/**
	 * 目标时间距离当前时间是否在time之内
	 * 
	 * @param beforeDate 目标时间
	 * @param time       单位毫秒
	 * @return ture
	 */
	public static boolean isTimeOut(Date beforeDate, long time) {
		long stap = System.currentTimeMillis() - beforeDate.getTime();
		return time >= stap;
	}

	/**
	 * 目标时间距离当前时间是否在10分钟内
	 * 
	 * @param beforeDate 目标时间
	 * @return ture
	 */
	public static boolean isTimeInTen(Date beforeDate) {
		return isTimeOut(beforeDate, 10 * 60 * 1000);
	}

	/**
	 * @param time 目标时间
	 * @return ture
	 */
	public static boolean isTimeIn(long time) {
		return (System.currentTimeMillis() - time) <= 300000;
	}

	/**
	 * 获取当前时间一周以前时期
	 * 
	 * @return
	 */
	public static Date getBeforeWeekDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		return calendar.getTime();
	}

	/**
	 * 获取当前时间每年的第几周
	 * 
	 * @return week no
	 */
	public static int getWeekOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 获取当前时间的年份
	 * 
	 * @return year
	 */
	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 根据逗号将数据列表打散
	 * 
	 * @param ids
	 * @return
	 */
	public static String[] getIds(String ids) {
		String[] idsArr;
		if (ids.indexOf(',') > -1) {
			idsArr = ids.split(",");
		} else {
			idsArr = new String[] { ids };
		}
		return idsArr;
	}

	/**
	 * 分页查询
	 * 
	 * @param query
	 * @param startId 开始主键
	 * @param gt
	 * @param limit
	 */
	public static void queryByIdPage(Query query, String startId, boolean gt, int limit) {
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
		if (!StringUtils.isEmpty(startId)) {
			if (gt) {
				query.addCriteria(new Criteria("_id").gt(startId));
			} else {
				query.addCriteria(new Criteria("_id").lt(startId));
			}
		}
		if (limit < 0 || limit > 500) {
			limit = 10;
		}
		query.limit(limit);
	}

	/**
	 * 判断时间是否在180天之内
	 * 
	 * @param currenDate 当前时间
	 * @return 180天前的日期
	 */
	public static Date getHalfYearTime(Date currenDate) {
		long stap = currenDate.getTime() - dayMinlls;
		return new Date(stap);
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @param currenDate 当前时间
	 * @return yyyy-MM-dd
	 */
	public static String getCurrentDate(Date currenDate) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		return sf.format(currenDate);
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @param dateStr yyyy-MM-dd HH:mm:ss
	 * @return date
	 */
	public static Date getDate(String dateStr) throws ParseException {
		if ("0".equals(dateStr) || StringUtils.isEmpty(dateStr)) {
			return null;
		}
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.parse(dateStr);
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @param dateStr yyyy-MM-dd HH:mm:ss
	 * @return date
	 */
	public static Date getDate(String dateStr, String format) {
		SimpleDateFormat sf = new SimpleDateFormat(format);
		try {
			return sf.parse(dateStr);
		} catch (ParseException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @param date 当前时间
	 * @return yyyy-MM-dd
	 */
	public static String getDateStr(Date date, String format) {
		SimpleDateFormat sf = new SimpleDateFormat(format);
		return sf.format(date);
	}

	/**
	 * 获取当前时间零点时间戳
	 * 
	 * @return
	 */
	public static Date getZeroDate(Date currenDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currenDate);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取当前天23：59:59时间戳
	 * 
	 * @return
	 */
	public static Date getLastDate(Date currenDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currenDate);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}

	/**
	 * 判断当前对象是否为空
	 * 
	 * @param collection 当前对象
	 * @return
	 */
	public static boolean isEmpty(Collection collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * 目前每次启动时加载
	 */
	private static HashSet<String> adminUserids;
	static {
		adminUserids = new LinkedHashSet<>();
		// to add
		adminUserids.add("");
	}

	/**
	 * 获取管理员id信息
	 * 
	 * @return
	 */
	public static HashSet<String> getAdminUserids() {
		return adminUserids;
	}

	public static String ff(String inFile, String outFile) {

		String command = "ffmpeg -i " + inFile + " -y -f image2 -ss 00:00:10 -t 00:00:01 -s 176x144 " + outFile;

		return "";

	}

	/**
	 * 根据生日计算年龄
	 * 
	 * @param birth
	 * @return
	 */
	public static int getAge(Date birth) {
		Calendar birthTime = Calendar.getInstance();
		birthTime.setTime(birth);
		int birthYear = birthTime.get(Calendar.YEAR);
		int birthMonth = birthTime.get(Calendar.MONTH) + 1;
		int birthDay = birthTime.get(Calendar.DAY_OF_MONTH);

		Calendar now = Calendar.getInstance();
		int nowYear = now.get(Calendar.YEAR);
		int nowMonth = now.get(Calendar.MONTH) + 1;
		int nowDay = now.get(Calendar.DAY_OF_MONTH);

		int age = 0;
		// System.out.println(nowYear+" "+birthYear);
		if (nowMonth - birthMonth >= 0 && nowDay - birthDay >= 0) {
			age = nowYear - birthYear;
		} else {
			age = nowYear - birthYear - 1;
		}
		return age;
	}

	/**
	 * 根据生日计算年龄
	 * 
	 * @param birth
	 * @return
	 */
	public static int getAge(String birth) {
		String regex = "\\D+";
		String[] split = birth.split(regex);
		if (split.length < 3) {
			return 0;
		}
		int birthYear = Integer.valueOf(split[0]);
		int birthMonth = Integer.valueOf(split[1]);
		int birthDay = Integer.valueOf(split[2]);

		Calendar now = Calendar.getInstance();
		int nowYear = now.get(Calendar.YEAR);
		int nowMonth = now.get(Calendar.MONTH) + 1;
		int nowDay = now.get(Calendar.DAY_OF_MONTH);

		int age = 0;
		// System.out.println(nowYear+" "+birthYear);
		if (nowMonth - birthMonth >= 0 && nowDay - birthDay >= 0) {
			age = nowYear - birthYear;
		} else {
			age = nowYear - birthYear - 1;
		}
		return age;
	}

	public static <T> List<T> randomQuery(MongoTemplate mongoTemplate, Query query, Class<T> clazz) {
		// 统计数据库长度
		int count = (int) mongoTemplate.count(query, clazz);
		List<T> find = new ArrayList<>();
		if (count < 100) {
			query.with(new Sort(Sort.Direction.DESC, "_id"));
			find = mongoTemplate.find(query, clazz);
		} else {
			// 不允许开始数据定位到最后25条,所以逻辑上skip的范围是(长度-25)
			int length = (count - 25) < 0 ? 1 : (count - 25);

			Random r = new Random();
			for (int i = 0; i < 4; i++) {
				int skip = r.nextInt(length);
				query.skip(skip);
				query.limit(25);
				List<T> find2 = mongoTemplate.find(query, clazz);
				find.addAll(find2);
			}
		}

		return find;
	}

	public static String fl_format(String json) {
		String regex = "\\D+";
		com.alibaba.fastjson.JSONObject object = JSON.parseObject(json);
		Set<String> keySet = object.keySet();

		for (String key : keySet) {
			String value = object.getString(key);
			if ("salary".equals(key)) {
				if (value.equals("不限")) {
					value = "0元-30000元";
				} else if (value.equals("25000元以上")) {
					value = "25001元-30000元";
				} else if (value.equals("3000元以下")) {
					value = "0元-3000元";
				}
				object.put(key, value);
				continue;
			}
			if ("age".equals(key)) {
				if (value.equals("不限")) {
					value = "17岁-100岁";
				} else if (value.contains("及以上")) {
					value = value.replaceAll(regex, "岁") + "-100岁";
				} else if (value.contains("及以下")) {
					value = "17岁-" + value.replaceAll(regex, "岁");
				}
				object.put(key, value);
				continue;
			}
			if ("hight".equals(key)) {
				if (value.equals("不限")) {
					value = "100厘米-250厘米";
				} else if (value.contains("及以上")) {
					value = value.replaceAll(regex, "厘米") + "-250厘米";
				} else if (value.contains("及以下")) {
					value = "100厘米-" + value.replaceAll(regex, "厘米");
				}
				object.put(key, value);
			}
		}
		return object.toJSONString();
	}

	public static Object vo_format(Object json, Class clazz) {
		com.alibaba.fastjson.JSONObject object = JSON.parseObject(JSON.toJSONString(json));
		Set<String> keySet = object.keySet();

		for (String key : keySet) {
			String value = object.getString(key);
			if ("salary".equals(key)) {
				if (value.equals("0元-30000元")) {
					value = "不限";
				} else if (value.equals("25001元-30000元")) {
					value = "25000元以上";
				} else if (value.equals("0元-3000元")) {
					value = "3000元以下";
				}
				object.put(key, value);
				continue;
			}
			if ("age".equals(key)) {
				if (value.equals("17岁-100岁")) {
					value = "不限";
				} else if (value.contains("100岁")) {
					value = value.replaceAll("-100岁", "及以上");
				} else if (value.contains("17岁")) {
					value = value.replaceAll("17岁-", "") + "及以下";
				}
				object.put(key, value);
				continue;
			}
			if ("hight".equals(key)) {
				if (value.equals("100厘米-250厘米")) {
					value = "不限";
				} else if (value.contains("250厘米")) {
					value = value.replaceAll("-250厘米", "及以上");
				} else if (value.contains("100厘米")) {
					value = value.replaceAll("100厘米-", "") + "及以下";
				}
				object.put(key, value);
			}
		}

		return JSON.parseObject(JSON.toJSONString(object), clazz);
	}

	// @Autowired
	// private IFriendsService friendsService;
	// @Autowired
	// private IChatroomService chatroomService;
	// @Autowired
	// private IUserService userService;
	/**
	 * 获取聊天室id
	 * 
	 * @param uid
	 * @param fid
	 * @param friendsService
	 * @param chatroomService
	 * @param userService
	 * @return
	 */
	public static String getChannelId(String uid, String fid, IFriendsService friendsService,
			IChatroomService chatroomService, IUserService userService) {
		ChatroomBo chatroomBo = chatroomService.selectByUserIdAndFriendid(uid, fid);
		UserBo userBo = userService.getUser(uid);
		if (userBo == null) {
			return "idWrong";
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(uid, fid);
		if (friendsBo == null) {
			return null;
		}
		UserBo friend = userService.getUser(friendsBo.getFriendid());
		if (chatroomBo == null) {
			chatroomBo = chatroomService.selectByUserIdAndFriendid(fid, uid);
			if (chatroomBo == null) {

				chatroomBo = new ChatroomBo();
				chatroomBo.setType(1);
				chatroomBo.setName(friend.getUserName());
				chatroomBo.setUserid(userBo.getId());
				chatroomBo.setFriendid(friend.getId());
				chatroomService.insert(chatroomBo);

				HashSet<String> userChatrooms = userBo.getChatrooms();
				HashSet<String> friendChatrooms = friend.getChatrooms();
				userChatrooms.add(chatroomBo.getId());
				friendChatrooms.add(chatroomBo.getId());
				userBo.setChatrooms(userChatrooms);
				friend.setChatrooms(friendChatrooms);
				userService.updateChatrooms(userBo);
				userService.updateChatrooms(friend);

				// 首次创建聊天室，需要输入名称
				String res = IMUtil.subscribe(0, chatroomBo.getId(), uid, friend.getId());
				if (!res.equals(IMUtil.FINISH)) {
					return res;
				}
			}
		}
		// 返回结果null,表示非好友
		// 返回结果finish,依照好友列表处理
		// 返回结果idWrong,传入id错误
		// 返回结果id字符串,正确
		return chatroomBo.getId();
	}

	// 获取照片墙
	public static LinkedList<String> getTop4(IPictureService pictureService, String uid) {
		LinkedList<String> result = new LinkedList<>();
		PictureWallBo wallBo = pictureService.getWallByUid(uid);
		if (wallBo != null && wallBo.getPictures().size() > 0) {
			result = wallBo.getPictures();
		} else {
			List<PictureBo> top4 = pictureService.getTop4ByUid(uid);
			for (PictureBo pictureBo : top4) {
				result.addLast(pictureBo.getUrl());
			}
		}
		return result;
	}

	// 获取照片墙
	public static LinkedList<String> getWall(IPictureService pictureService, String uid) {
		LinkedList<String> result = new LinkedList<>();
		PictureWallBo wallBo = pictureService.getWallByUid(uid);
		result = wallBo == null ? new LinkedList<>() : wallBo.getPictures();
		return result;
	}

	/**
	 * 获取敏感词坐标
	 * 
	 * @param source
	 * @param key
	 * @return
	 */
	public static List<List<Integer>> getIndex(String source, String key) {
		int formIndex = 0;
		List<List<Integer>> position = new ArrayList<>();

		while (true) {
			int indexOf = source.indexOf(key, formIndex);
			if (indexOf == -1) {
				break;
			}
			int endIndex = indexOf + key.length();
			List<Integer> temp = new ArrayList<>(2);
			temp.add(indexOf);
			temp.add(endIndex);
			position.add(temp);
			formIndex = endIndex;
			if (formIndex >= source.length()) {
				break;
			}
		}
		return position;
	}

	public static List<List<Integer>> getIndexAsList(String source, String key) {
		int formIndex = 0;
		List<List<Integer>> position = new ArrayList<>();
		int keyIndex = 1;

		while (true) {
			int indexOf = source.indexOf(key, formIndex);
			if (indexOf == -1) {
				break;
			}
			int endIndex = indexOf + key.length();
			List<Integer> temp = new ArrayList<>(2);
			temp.add(indexOf);
			temp.add(endIndex);
			position.add(temp);
			keyIndex++;
			formIndex = endIndex;
			if (formIndex >= source.length()) {
				break;
			}
		}
		return position;
	}
	
	public static List<String> deleteBack(IDynamicService dynamicService, IFriendsService friendsService,UserBo userBo) {
		List<FriendsBo> friendsBos = friendsService.getFriendByUserid(userBo.getId());
		List<String> friends = new LinkedList<>();
		friendsBos.forEach(bo->friends.add(bo.getFriendid()));
		// 去除我拉黑的对象
		DynamicBackBo backBo = dynamicService.findBackByUserid(userBo.getId());
		if (null != backBo) {
			HashSet<String> noSees = backBo.getNotSeeBacks();
			friends.removeAll(noSees);
		}
		// 去除拉黑我的对象
		List<DynamicBackBo> backBos = dynamicService.findWhoBackMe(userBo.getId());
		if (backBos != null && !backBos.isEmpty()) {
			backBos.stream().filter(bo->friends.contains(bo.getId())).forEach(bo->friends.remove(bo.getId()));
		}
		if (friends.contains(userBo.getId())) {
			friends.remove(userBo.getId());
		}
		return friends;
	}

	public static final String BEIJING = "北京市";
	public static final String CHONGQING = "重庆市";
	public static final String TIANJIN = "天津市";
	public static final String SHANGHAI = "上海市";
	// 获取格式为 省-市-区格式的字符串中的市
	public static String getCity(String area) {
		String[] split = area.split("-");
		String first = split[0];
		if(area.contains(BEIJING)||area.contains(CHONGQING)||area.contains(TIANJIN)||area.contains(SHANGHAI)) {
			return first;
		}
		if (split.length >= 2) {
			String result = first + "-" + split[1];
			return result;
		}else {
			return first;
		}
	}
}
