package com.lad.util;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class HttpClientUtil {

    private static Logger logger = LogManager.getLogger(HttpClientUtil.class);
    
    private static HttpClient client = null;

    // 构造单例
    private HttpClientUtil() {

        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        // 默认连接超时时间
        params.setConnectionTimeout(60000);
        // 默认读取超时时间
        params.setSoTimeout(60000);
        // 默认单个host最大连接数
        params.setDefaultMaxConnectionsPerHost(200);// very important!!
        // 最大总连接数
        params.setMaxTotalConnections(500);// very important!!
        httpConnectionManager.setParams(params);

        client = new HttpClient(httpConnectionManager);

        client.getParams().setConnectionManagerTimeout(3000);
        // client.getParams().setIntParameter("http.socket.timeout", 10000);
        // client.getParams().setIntParameter("http.connection.timeout", 5000);
    }

    private static class ClientUtilInstance {
        private static final HttpClientUtil ClientUtil = new HttpClientUtil();
    }

    public static HttpClientUtil getInstance() {
        return ClientUtilInstance.ClientUtil;
    }

    /**
     * 发送http GET请求，并返回http响应字符串
     * 
     * @param urlstr  完整的请求url字符串
     * @return
     */
    public String doGetRequest(String urlstr) {
        
        String response = "";

        HttpMethod httpmethod = new GetMethod(urlstr);
        try {
            int statusCode = client.executeMethod(httpmethod);
            InputStream _InputStream = null;
            if (statusCode == HttpStatus.SC_OK) {
                _InputStream = httpmethod.getResponseBodyAsStream();
            }
            if (_InputStream != null) {
                response = GetResponseString(_InputStream, "UTF-8");
            }
        } catch (HttpException e) {
            logger.error("获取响应错误，原因：{}",e);
        } catch (IOException e) {
            logger.error("获取响应错误，原因 {}",e);
        } finally {
            httpmethod.releaseConnection();
        }
        return response;
    }

    public String doPostRequest(String postUrl) {
        String response = "";
        PostMethod postMethod = new PostMethod(postUrl);
        try {
            int statusCode = client.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                InputStream _InputStream = null;
                if (statusCode == HttpStatus.SC_OK) {
                    _InputStream = postMethod.getResponseBodyAsStream();
                }
                if (_InputStream != null) {
                    response = GetResponseString(_InputStream, "UTF-8");
                }
            }
        } catch (HttpException e) {
            logger.error("获取响应错误，原因：{}",e.getMessage());
        } catch (IOException e) {
            logger.error("获取响应错误，原因：{}",e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
        return response;
    }

    /**
     * 
     * @param _InputStream
     * @param Charset
     * @return
     */
    public String GetResponseString(InputStream _InputStream, String Charset) {
        String response = "";
        try {
            if (_InputStream != null) {
                StringBuffer buffer = new StringBuffer();
                InputStreamReader isr = new InputStreamReader(_InputStream, Charset);
                Reader in = new BufferedReader(isr);
                int ch;
                while ((ch = in.read()) > -1) {
                    buffer.append((char) ch);
                }
                response = buffer.toString();
                buffer = null;
            }
        } catch (IOException e) {
            logger.error("获取响应错误，原因：{}",e);
        }
        return response;
    }
    
    public String sendPost(String url,Map<String,String> params) {
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
			return e.toString();
		}

	}
}

