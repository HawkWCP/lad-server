package com.lad.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class VivoPushUtil {
	private static final String APPKEY = "764b3dea-2bf9-4fbf-a5d0-a89374f9d61e";

	private static final String APPSECRET = "51856e2e-2d83-48d6-9e78-d29790e500c0";

	private static final String APPID = "10379";
	
	private static final String AUTH_FAILED = "权限认证失败";

	/**
	 * 鉴权地址
	 *  Curl e.g.：
	 * 		curl -X POST -H 'Content-Type:application/json' -d '${your_request_body}' https://host:port/message/auth
	 * 	Request body e.g.:
	 * 		{"appId":10004, "appKey":"25509283-3767-4b9e-83fe-b6e55ac6243e", "timestamp":1501484120000, "sign":"8424f52fd5eaedc16474e4f702d230d2" }
	 * 	Response body e.g.:
	 * 		http status 200:
	 * 		业务成功：{"result": 0, "desc": "请求成功", "authToken": "24ojds98fu3jqrioeu982134jieds9fq43u09uaf" }
	 * 		业务异常：{"result": xxx, "desc": "xxx 不合法"}
	 */
	private static final String AUTH_TOKEN_URL = "https://api-push.vivo.com.cn/message/auth";
	
	/**
	 * 单推地址
	 * 消息体示例
	 * 	{
			"regId": "12345678901234567890123",
			"notifyType": 1,
			"title": "标题 1",
			"content": "内容 1",
			"timeToLive": 86400,
			"skipType": 2,
			"skipContent": "http://www.vivo.com",
			"networkType": "1",
			"clientCustomMap": {
				"key1": "vlaue1",
				"key2": "vlaue2"
			},
			"extra": {
				"callback": "http://www.vivo.com",
				"callback.param": "vivo"
			},
			"requestId": "25509283-3767-4b9e-83fe-b6e55ac6b123"
		}
	 */
	private static final String SEND_URL = "https://api-push.vivo.com.cn/message/send";
	

	

	
	/**
	 * 设置群消息推送内容的地址
	 * 请求体示例
	 * 	{
			"title": "标题 1",
			"content": "内容 1",
			"notifyType": 1,
			"timeToLive": 86400,
			"skipType": 2,
			"skipContent": "http://www.vivo.com",
			"networkType": "1",
			"clientCustomMap": {
				"key1": "vlaue1",
				"key2": "vlaue2"
			},
			"requestId": "25509283-3767-4b9e-83fe-b6e55ac6b123"
		}
		
		返回结果示例:
		{
			"result": 0,
			"desc": "请求成功",
			"taskId": "342982232646905856"
		}
		taskId可以在群推中用到
	 */
	private static final String SAVE_PAYLOAD_URL = "https://api-push.vivo.com.cn/message/saveListPayload";
	
	/**
	 * 群消息推送的发送地址
	 * 请求体示例:
	 * 	{
			"regIds": ["12345678901234567890121", "12345678901234567890122"],
			"taskId": "342982232646905856",
			"requestId": "25509283-3767-4b9e-83fe-b6e55ac6b123"
		}
	 */
	private static final String PUSH_LIST_URL = "https://api-push.vivo.com.cn/message/pushToList";

	
	
	/**
	 * 工具方法1:获取authToken
	 * 根据vivo push的规定,每一次向推送消息时都需要在请求头里添加一个authToken的参数,而这个值需要向服务器请求
	 * 请求体为:{"appId":10004, "appKey":"25509283-3767-4b9e-83fe-b6e55ac6243e", "timestamp":1501484120000, "sign":"8424f52fd5eaedc16474e4f702d230d2" }
	 * @return
	 */
	
	/**
	 * 全量推送地址
	 * 请求体示例
	 	{
			"notifyType": 1,
			"title": "标题 1",
			"content": "内容 1",
			"timeToLive": 86400,
			"skipType": 2,
			"skipContent": "http://www.vivo.com",
			"networkType": "1",
			"clientCustomMap": {
				"key1": "vlaue1",
				"key2": "vlaue2"
			},
			"requestId": "25509283-3767-4b9e-83fe-b6e55ac6b123"
		}
	 */
//	private static final String PUSH_ALL_URL = "https://api-push.vivo.com.cn/message/all";
	
	private static Logger logger = LogManager.getLogger(VivoPushUtil.class);
	
	private static String authToken;
	/**
	 * vivo服务器规定每天最多只能申请10000次authToken
	 */
	private static Map<String,Integer> get_token_count;
	
	static {
		get_token_count = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		String thisDay = year+"-"+month+"-"+day;
		
		get_token_count.put(thisDay, 0);
		
		authToken = getAuthToken();
	}
	
	/**
	 *  群推
	 * @param title
	 * @param content
	 * @param aliasList
	 * @param path
	 */
	public static void sendToMany(String title,String content,List<String> aliasList,String path) {
		logger.info("vivo push 群发送:title:{},content:{},aliasList:{},path:{}", title, content, aliasList, path);
		try {
			if(authToken == null) {
				authToken = getAuthToken();
			}
			// 构造消息对象
			Map<String, String> headerMap = getHeaderMap();
			Msg msg = new Msg();
			msg.setTitle(title);
			msg.setContent(content);
			msg.setSkipContent(path);
			
			String payload = sendPost(SAVE_PAYLOAD_URL, headerMap, msg);
			String taskId = null;
			if(payload!=null) {
				JSONObject payloadJson = JSON.parseObject(payload);
				if(payloadJson.getInteger("result")==0) {
					taskId = payloadJson.getString("taskId");
				}
			}
			if(taskId == null) {
				throw new Exception("taskId构造失败");
			}
			
			// 构造目标对象
			Target target = new Target();
			if(aliasList.size()<2) {
				aliasList = new ArrayList<String>(aliasList);
				aliasList.add("缺省id"+new Date().getTime());
			}
			target.setAliases(aliasList);
			target.setTaskId(taskId);
			
			String sendPost = sendPost(PUSH_LIST_URL, headerMap, target);
			
			// 如果全线失败,更新一次authToken
			if(AUTH_FAILED.equals(JSON.parseObject(sendPost).getString("desc"))) {
				authToken = getAuthToken();
				// 再试一次
				sendPost = sendPost(PUSH_LIST_URL, headerMap, target);
			}
			
			if(JSON.parseObject(sendPost).getInteger("result")!=0) {
				throw new Exception("消息发送失败");
			}
			logger.info("vivo push 发送成功,发送结果为:{},消息体为:{},目标对象为:{}", sendPost,JSON.toJSONString(msg),JSON.toJSONString(target));
		}catch(Exception e) {
			logger.error("vivo push发送失败:{}", e);
		}

	}
	
	/**
	 * 单推
	 * @param title
	 * @param content
	 * @param alias
	 * @param path
	 */
	public static void sendToOne(String title,String content,String alias,String path) {
		logger.info("vivo push 单对单发送=====title:{},content:{},alias:{},path:{}", title, content, alias, path);
		try {
			if(authToken == null) {
				authToken = getAuthToken();
			}
			Map<String, String> headerMap = getHeaderMap();
			Msg msg = new Msg();
			msg.setTitle(title);
			msg.setContent(content);
			msg.setAlias(alias);
			msg.setSkipContent(path);
			msg.setRequestId("天天老友");
			
			
			String sendPost = sendPost(SEND_URL, headerMap, msg);
			
			// 如果全线失败,更新一次authToken
			if(AUTH_FAILED.equals(JSON.parseObject(sendPost).getString("desc"))) {
				authToken = getAuthToken();
				// 再试一次
				sendPost = sendPost(SEND_URL, headerMap, msg);
			}
			if(JSON.parseObject(sendPost).getInteger("result")!=0) {
				throw new Exception("消息发送失败");
			}
			logger.info("vivo push 发送成功,发送结果为:{},消息体:{}", sendPost,JSON.toJSONString(msg));
		}catch(Exception e) {
			logger.error("vivo push 发送失败:{}", e);
		}
	}
	
	/**
	 * 生成请求头
	 * @return
	 */
	private static Map<String,String> getHeaderMap(){
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/json");
		headerMap.put("authToken", authToken);
		return headerMap;
	}
	
	/**
	 * authToken失效或null的情况下访问获取唯一authToken
	 * @return
	 */
	private static String getAuthToken() {
		// 获取当前日期
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		String thisDay = year+"-"+month+"-"+day;
		
		// 检查当前日期是否被包含在get_token_count
		Set<String> keySet = get_token_count.keySet();
		if(!keySet.contains(thisDay)) {
			// 若是没包含在get_token_count中,重置get_token_count,防止get_token_count过大
			get_token_count = new HashMap<>();
			get_token_count.put(thisDay, 0);
		}
		
		int count = get_token_count.get(thisDay);
		if(count<10000) {
			synchronized (VivoPushUtil.class) {
				if(count<10000) {
					try {
						// 获取当前时间戳
						long time = new Date().getTime();
						String sign = getSign(time + "");
						if (sign == null) {
							throw new Exception("生成sign失败");
						}
						
						// 设置请求头
						Map<String, String> headerMap = new HashMap<>();
						headerMap.put("Content-Type", "application/json");

						// 设置请求体
						Map<String, String> bodyMap = new HashMap<>();
						bodyMap.put("appId", APPID);
						bodyMap.put("appKey", APPKEY);
						bodyMap.put("timestamp", time + "");
						bodyMap.put("sign", sign);

						// 获取结果
						String response = sendPost(AUTH_TOKEN_URL, headerMap, bodyMap);
						JSONObject parseObject = JSON.parseObject(response);
						if (parseObject.getInteger("result") == 0) {
							authToken = parseObject.getString("authToken");
							get_token_count.put(thisDay, count++);
						} else {
							throw new Exception("服务器返回错误信息:" + response);
						}
					} catch (Exception e) {
						logger.error("vivo push throw exception in getAuthToken():{}", e);
						e.printStackTrace();
					}

				}
			}
		}

		return authToken;
	}

	/**
	 * 生成sign
	 * 生成规则为: appId+appKey+timestamp+appSecret的MD5加密取小写
	 * @param timestamp
	 * @return
	 */
	private static String getSign(String timestamp) {
		String result = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			String passString = APPID + APPKEY + timestamp + APPSECRET;
			
			byte[] digest = md5.digest(passString.getBytes());
			
			
			result = byteArrayToHexString(digest);
		} catch (NoSuchAlgorithmException e) {
			logger.error("vivo push throw exception in getSign():{}", e);
			e.printStackTrace();
		}
		return result.toLowerCase();
	}

	/**
	 * 轮换字节数组为十六进制字符串
	 * 
	 * @param b 字节数组
	 * @return 十六进制字符串
	 */
	private static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}


	// 将一个字节转化成十六进制形式的字符串
	private static String byteToHexString(byte b) {
		String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D","E", "F" };
		
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;

		
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * 向服务器发送post请求
	 * @param url
	 * @param headerMap
	 * @param bodyMap
	 * @return
	 */
	private static String sendPost(String url, Map<String, String> headerMap, Map<String, String> bodyMap) {
		PrintWriter writer = null;
		BufferedReader reader = null;
		String result = "";
		try {
			// 创建一个http请求实体
			URL sendUrl = new URL(url);

			// 获取连接
			URLConnection connection = sendUrl.openConnection();
			
			// 设置请求头
			for (Entry<String, String> headerEntry : headerMap.entrySet()) {
				connection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
			}
			
			// 设置传入传出为true
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// 发送请求
			writer = new PrintWriter(connection.getOutputStream());
			writer.write(JSON.toJSONString(bodyMap));
			writer.flush();

			// 接收相应结果
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送 POST 请求出现异常:{}", e);
			e.printStackTrace();
		} finally {
			// 关闭流
			try {
				if (writer != null) {
					writer.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 向服务器发送post请求
	 * @param url
	 * @param headerMap
	 * @param bodyMap
	 * @return
	 */
	private static String sendPost(String url, Map<String, String> headerMap, Object msg) {
		PrintWriter writer = null;
		BufferedReader reader = null;
		String result = "";
		try {
			// 创建一个http请求实体
			URL sendUrl = new URL(url);

			// 获取连接
			URLConnection connection = sendUrl.openConnection();
			
			// 设置请求头
			for (Entry<String, String> headerEntry : headerMap.entrySet()) {
				connection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
			}
			
			// 设置传入传出为true
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// 发送请求
			writer = new PrintWriter(connection.getOutputStream());
			writer.write(JSON.toJSONString(msg));
			writer.flush();

			// 接收相应结果
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送 POST 请求出现异常:{}", e);
			e.printStackTrace();
		} finally {
			// 关闭流
			try {
				if (writer != null) {
					writer.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@Setter
	@Getter
	@ToString
	private static class Msg{
		// 应用订阅 PUSH服务器得到的id,长度 23个字字符(regId,alias,两者需一个不为空,当两个不为空时,取 regId)		
		private String regId;
		
		// 别名长度不超过40字符（regId，alias两者需一个不为空，当两个不为空时，取 regId）
		private String alias;

		
		// 通知类型 1:无，2:响铃，3:振动，4:响铃和振动
		private Integer notifyType = 4;
		
		// 通知标题（用于通知栏消息） 最大 20 个汉字（一个汉字等于两个英文字符，即最大不超过 40 个英文字符）
		private String title;
		
		// 通知内容（用于通知栏消息） 最大 50 个汉字（一个汉字等于两个英文字符，即最大不超过 100 个英文字符）
		private String content;
		
		// 消息保留时长 单位：秒，取值至少 60 秒，最长 7 天。当值为空时，默认一天
		private Integer timeToLive = 86400;
		
		// 点击跳转类型 1：打开 APP 首页 2：打开链接 3：自定义 4:打开 app 内指定页面
		private Integer skipType = 4;
		
		// 跳转内容 skipType为 2 时，跳转内容最大1000 个字符，skipType为 3 或 4 时，跳转内容最大 1024 个字符
		private String skipContent;
		
		// 网络方式 -1：不限，1：wifi 下发送，不填默认为-1
		private Integer networkType = -1;
		
		// 客户端自定义键值参数 自定义参数不能超过 10 个，总长度不能超过 1024 字符。
		private Map<String, String> clientCustomMap;
		
		// 高级特性（详见：高级特性 extra）requestId String Y 用户请求唯一标识 最大 64 字符
		private Map<String, String> extra;
		
		// 用户请求唯一标识 最大 64 字符
		private String requestId= "天天老友";
	}
	
	@Setter
	@Getter
	@ToString
	private static class Target{
		
		private List<String> regIds;
		
		private List<String> aliases;
		
		private String taskId;
		
		private String requestId = "天天老友";
	}
}
