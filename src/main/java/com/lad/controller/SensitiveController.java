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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lad.util.CommonUtil;

@SuppressWarnings("all")
@RestController
@RequestMapping("sensitive")
public class SensitiveController {
	
	@RequestMapping("sensitive-query")
	public String sensitiveQuery(String str){
//		String url = "http://wf.ttlaoyou.com/v1/query";
		String url = "http://localhost:8090/v1/query";
		Map<String,String> params = new HashMap<>();
		params.put("q", str);
		String sendPost = sendPost(url, params);
		System.out.println(sendPost);
		JSONObject object = JSON.parseObject(sendPost);
		JSONObject keywords = object.getJSONObject("data").getJSONObject("keywords");
		
		Map<String,Object> map = new HashMap<>();
		if(!keywords.isEmpty()){
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
		}else{
			map.put("ret", 0);
			map.put("keyWords", new ArrayList<>());
			map.put("text", str);
			map.put("source", str);
		}
		
		return JSON.toJSONString(map);
	}
	
	
	@RequestMapping("sensitive-search")
	public String getSensitives(){
//		String url = "http://wf.ttlaoyou.com/v1/black_words";
		String url = "http://localhost:8090/v1/black_words";
		String sendGet = sendGet(url);
		JSONObject object = JSON.parseObject(sendGet);
		return sendGet(url);
	}
	
	private String sendPost(String url,Map<String,String> params) {
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
					if(response.getStatusLine().getStatusCode()/100==2){
						return EntityUtils.toString(entity);
					}else{
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
