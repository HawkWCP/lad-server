package com.lad.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lad.util.CommonUtil;

import net.sf.json.JSONException;

@SuppressWarnings("all")
@RestController
@RequestMapping("sensitive")
public class SensitiveController {

	private Logger logger = LogManager.getLogger();

	@PostMapping("sensitive-check")
	public String sensitiveChecky(String fieldJson) {
		String url = "http://localhost:8090/v1/query";

		Map<String, Object> map = new HashMap<>();
		map.put("sensitiveWord", false);

		try {
			JSONObject jsonObject = JSON.parseObject(fieldJson);
			List<Map<String, Object>> result = new ArrayList<>();
			for (Entry<String, Object> entry : jsonObject.entrySet()) {
				if (entry.getValue() instanceof java.lang.String) {

					String value = (String) entry.getValue();

					Map<String, String> params = new HashMap<>();
					params.put("q", value);
					String sendPost = sendPost(url, params);

					JSONObject object = JSON.parseObject(sendPost);
					JSONObject keywords = object.getJSONObject("data").getJSONObject("keywords");

					if (!keywords.isEmpty()) {
						Map<String, Object> wordWrapper = new HashMap<>();
						wordWrapper.put("field", entry.getKey());
						wordWrapper.put("value", value);
						List<Map<String, Object>> keywordList = new ArrayList<>();
						for (Entry<String, Object> keyEntry : keywords.entrySet()) {
							Map<String, Object> keyWordAnalysistor = new HashMap<>();
							keyWordAnalysistor.put("keyword", keyEntry.getKey());
							keyWordAnalysistor.put("number", keyEntry.getValue());
							keyWordAnalysistor.put("position", CommonUtil.getIndex(value, keyEntry.getKey()));
							keywordList.add(keyWordAnalysistor);
						}
						wordWrapper.put("sensitiveWord", keywordList);
						map.put("sensitiveWord", true);
						result.add(wordWrapper);
					}
				}
			}
			map.put("result", result);
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"sensitive-check\")=====error:{}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (JSONException e) {
			logger.error("@PostMapping(\"sensitive-check\")=====error:{}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (Exception e) {
			logger.error("@PostMapping(\"sensitive-check\")=====error:{}",e);
			Map<String,Object> result = new HashMap<>();
			result.put("ret", -1);
			result.put("message", "出现错误:"+e);
			return JSON.toJSONString(result);
		}

		return JSON.toJSONString(map);
	}

	@PostMapping("sensitive-query")
	public String sensitiveQuery(String str) {
		String url = "http://localhost:8090/v1/query";
		Map<String, String> params = new HashMap<>();
		params.put("q", str);
		String sendPost = sendPost(url, params);
		JSONObject object = JSON.parseObject(sendPost);
		JSONObject keywords = object.getJSONObject("data").getJSONObject("keywords");

		Map<String, Object> map = new HashMap<>();
		if (!keywords.isEmpty()) {
			List<JSONObject> keyWords = new ArrayList<>();
			Set<Entry<String, Object>> entrySet = keywords.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("keyWord", entry.getKey());
				jsonObject.put("nums", entry.getValue());
				jsonObject.put("positions", CommonUtil.getIndex(str, entry.getKey()));
				keyWords.add(jsonObject);
			}
			map.put("ret", -1);
			map.put("keyWords", keyWords);
			map.put("text", object.getJSONObject("data").get("text"));
			map.put("source", str);
		} else {
			map.put("ret", 0);
			map.put("keyWords", new ArrayList<>());
			map.put("text", str);
			map.put("source", str);
		}

		return JSON.toJSONString(map);
	}

	@PostMapping("sensitive-search")
	public String getSensitives() {
//		String url = "http://wf.ttlaoyou.com/v1/black_words";
		String url = "http://localhost:8090/v1/black_words";
		String sendGet = sendGet(url);
		JSONObject object = JSON.parseObject(sendGet);
		return sendGet(url);
	}

	private String sendPost(String url, Map<String, String> params) {
		try {
			List<BasicNameValuePair> formparams = new ArrayList<>();
			for (Entry<String, String> entry : params.entrySet()) {
				formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
					.setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
					.setConnectionRequestTimeout(5000).build();

			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.setEntity(reqEntity);
			post.setConfig(requestConfig);
			HttpResponse response = client.execute(post);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				String message = EntityUtils.toString(resEntity, "utf-8");
				return message;
			} else {
				return "请求失败";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String sendGet(String url) {
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		try {
			// 创建httpget.
			HttpGet httpget = new HttpGet(url);
			// 执行get请求.
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				// 打印响应状态
				if (entity != null) {
					if (response.getStatusLine().getStatusCode() / 100 == 2) {
						return EntityUtils.toString(entity);
					} else {
						return JSON.toJSONString(response.getStatusLine());
					}
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
