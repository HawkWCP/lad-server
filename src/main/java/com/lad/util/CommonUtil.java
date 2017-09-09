package com.lad.util;

import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
	public static boolean isRightPhone(String phone) {
		String regExp = "^1(3|4|5|7|8)\\d{9}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(phone);
		return m.find();
	}

	public static String getSHA256(String content) {
		MessageDigest digest;
		String output = "";
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(content.getBytes("UTF-8"));
			output = Hex.encodeHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 上传video是，获取缩略图
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
		String[] res = new String[]{
				Constant.QINIU_URL + vedio + "?v=" + CommonUtil.getRandom1(),
				Constant.QINIU_URL + pic + "?v=" + CommonUtil.getRandom1()
		};
		return res;
	}

	/**
	 * 上传图片
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

	@Async
	public static int sendSMS2(String mobile, String message) {
		try {
			message = URLEncoder.encode(message, "GBK");
		} catch (UnsupportedEncodingException ex) {
		}
		String url = "http://sms-gw.bjedu.cloud:9888/smsservice/SendSMS?UserId=100535&Password=ttlyyl_2017&Mobiles="+mobile+"&Content="+message+"&ExtNo=35";
		String responseString = HttpClientUtil.getInstance().doGetRequest(url);
		System.out.println(mobile + " : =====message send result : " + responseString);
		if (responseString.trim().equals(Constant.RESPONSE)) {
			return 0;
		}
		return -1;
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
			String new1 =  new String("您的验证码为：".getBytes(), "GBK");
			String new2 =  new String("，该验证码5分钟内有效，如非本人操作，请忽略。".getBytes(), "GBK");
			StringBuilder builder = new StringBuilder();
			builder.append(new1).append(number).append(new2);
			msg = builder.toString();
		} catch (Exception e) {
			System.out.println("send Msg exception " + e.getMessage());
		}
		return msg;
	}

	public static String buildPassMsg(String number) {
		String msg = "";
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("您正在修改密码，验证码为：").append(number).append("，该验证码5分钟内有效，如非本人操作，请忽略。");
			msg = new String(builder.toString().getBytes(), "GBK");
		} catch (Exception e) {
			System.out.println("send Msg exception " + e.getMessage());
		}
		return msg;
	}

	
	/**
	 * 将时间转成字符串
	 * @param date 时间
	 * @return  yyyy-MM-dd HH:mm:ss
	 */
	public static String time2str(Date date){
		return date2Str("yyyy-MM-dd HH:mm:ss", date);
	}
    /**
     * 将日期转换成制定格式
     * @param format  日期格式如 yyyy-MM-dd HH:mm:ss
     * @param date  传入时间
     * @return  时间字符串
     */
    public static String date2Str(String format, Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

	/**
	 * 判断可变形参是否为空
	 * @param ids 参数
	 * @return  true if empty
	 */
	public static boolean isEmpty(String... ids){
    	return ids == null || ids.length == 0;
	}


	/**
	 * 目标时间距离当前时间是否在time之内
	 * @param beforeDate 目标时间
	 * @param time 单位毫秒
	 * @return ture
	 */
	public static boolean isTimeOut(Date beforeDate, long time){
		long stap = System.currentTimeMillis() - beforeDate.getTime() ;
		return time >= stap;
	}

	/**
	 * 目标时间距离当前时间是否在10分钟内
	 * @param beforeDate 目标时间
	 * @return ture
	 */
	public static boolean isTimeInTen(Date beforeDate){
		return isTimeOut(beforeDate, 10*60*1000);
	}

	/**
	 * @param time 目标时间
	 * @return ture
	 */
	public static boolean isTimeIn(long time){
		return (System.currentTimeMillis() - time) <= 300000;
	}


	/**
	 * 获取当前时间一周以前时期
	 * @return
	 */
	public static Date getBeforeWeekDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		return calendar.getTime();
	}

	/**
	 * 获取当前时间每年的第几周
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
	 * @return year
	 */
	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 根据逗号将数据列表打散
	 * @param ids
	 * @return
	 */
	public static String[] getIds(String ids){
		String[] idsArr;
		if (ids.indexOf(',') > -1) {
			idsArr = ids.split(",");
		} else {
			idsArr = new String[]{ids};
		}
		return idsArr;
	}


	/**
	 * 分页查询
	 * @param query
	 * @param startId  开始主键
	 * @param gt
	 * @param limit
	 */
	public static void queryByIdPage(Query query, String startId, boolean gt, int limit){
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

	public static String ff(String inFile, String outFile){


		String command = "ffmpeg -i " + inFile + " -y -f image2 -ss 00:00:10 -t 00:00:01 -s 176x144 " + outFile;


		return "";

	}


}
