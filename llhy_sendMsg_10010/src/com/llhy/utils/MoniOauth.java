package com.llhy.utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class MoniOauth {
    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {

        //authorization_code 类型授权码模式
        String url0 = "http://133.160.93.56:20200/aopoauth/oauth/authorize?app_id=501214&response_type=code&redirect_uri=http://www.baidu.com";
        String url1 = "http://133.160.93.56:20200/aopoauth/oauth/token?app_id=501214&code=";
        String url11 = "&app_key=99eb933f5f0b66983442172a3ea16b8c&grant_type=authorization_code&redirect_uri=http://www.baidu.com";
        
        
        String token = accessToken("authorization_code",url0,url1,url11);


        //implicit 类型的授权码
        String url2 = "http://133.160.93.56:20200/aopoauth/oauth/authorize?app_id=501161&response_type=token"+
                 "&app_key=daf7c51b8773e79ccfe96d220e97a68e&redirect_uri=http://www.baidu.com";
        //String token = accessToken("implicit",url2,null,null);


        // client_credentials@@客户端模式，访问一些公共资源的应用，实际就是不需要授权的
        String url3 = "http://133.160.93.56:20200/aopoauth/oauth/token?app_id=501123"+
                 "&app_key=2e231d9a0c3fa4e55307aed9929f1e1c&grant_type=client_credentials";
        //String token = accessToken("client_credentials",url3,null,null);

        System.out.println("token : "+token);
    }

    /**
     *
     * @param cesType 授权类型 authorization_code，implicit，client_credentials
     * @param url0 非authorization_code授权类型 为访问的URL
     *              authorization_code授权类型的URL格式：http://10.15.34.21:20200/aopoauth/oauth/authorize?app_id=501146&response_type=code&redirect_uri=http://www.baidu.com
     * @param url1 authorization_code授权类型的URL格式：http://10.15.34.21:20200/aopoauth/oauth/token?app_id=501146&code=
     * @param url2 authorization_code授权类型的URL格式：&app_key=cf332e304ed0db65bc6cf32bde6c5f8e&grant_type=authorization_code&redirect_uri=http://www.baidu.com
     * @return token
     */
    private static String http_connect_timeout = "";
	private static String http_read_timeout = "";
	
    public static String accessToken(String cesType,String url0,String url1,String url2){
    	
    	System.out.println("获取超时时间start*********");
		Properties prop = new Properties();
		try {
			ClassLoader cl = new MoniOauth().getClass().getClassLoader();
			InputStream is = cl.getResourceAsStream("aiesb.properties");
			prop.load(is);
			http_connect_timeout = prop.getProperty("http_connect_timeout");
			http_read_timeout = prop.getProperty("http_read_timeout");
			System.out.println("http_connect_timeout==>"+http_connect_timeout+";http_read_timeout==>"+http_read_timeout);
			System.out.println("获取超时时间end*********");
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        String token = "";
        HttpClient httpClient = new HttpClient();
        //配置HTTP请求连接超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(Integer.parseInt(http_connect_timeout));
        //配置HTTP请求响应超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(Integer.parseInt(http_read_timeout));
        //设置HTTP请求超时时不进行自动重发
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0,false));
        if (cesType.indexOf("client_credentials") > -1) {
            try{
                GetMethod getMethod = new GetMethod(url0);
                httpClient.executeMethod(getMethod);
                getMethod.setRequestHeader("Content-type","application/json; charset=UTF-8");
                String response = getMethod.getResponseBodyAsString();
                token = response;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if (cesType.indexOf("implicit") >-1 ) {

            try {
                GetMethod getMethod = new GetMethod(url0);
                getMethod.setFollowRedirects(false);
                httpClient.executeMethod(getMethod);
                //自动登录
                Header responseHeader0 = login(httpClient);

                GetMethod getMethod1 = new GetMethod(responseHeader0.getValue());
                getMethod1.setFollowRedirects(false);
                getMethod1.setRequestHeader("Referer", "http://133.160.93.56:20200/aopoauth/login.jsp");
                httpClient.executeMethod(getMethod1);
                getMethod.setRequestHeader("Content-type","application/json; charset=UTF-8");

                Header responseHeader1 = getMethod1.getResponseHeader("Location");
                token = getAccessToken(responseHeader1.getValue());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else  {
            try {
                GetMethod getMethod = new GetMethod(url0);
                getMethod.setFollowRedirects(false);
                httpClient.executeMethod(getMethod);

                //自动登录
                Header responseHeader0 = login(httpClient);

                GetMethod getMethod1 = new GetMethod(responseHeader0.getValue());
                getMethod1.setFollowRedirects(false);
                getMethod1.setRequestHeader("Referer", "http://133.160.93.56:20200/aopoauth/login.jsp");
                httpClient.executeMethod(getMethod1);
                String responseHTML = getMethod1.getResponseBodyAsString();
                //获取相应的网页内容，查看是否有需要授权的服务
                if(responseHTML.contains("checkScope")){
                    System.out.println("需要进行服务授权........");
                    PostMethod postMethod1 = new PostMethod("http://133.160.93.56:20200/aopoauth/oauth/authorize");
                    postMethod1.setRequestHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
                    postMethod1.setRequestHeader("Referer", url0);
                    List<String> list = match(responseHTML, "input", "name");
                    List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
                    for(String param:list){
                        System.out.println(param);
                        NameValuePair nvp = new NameValuePair(param,"true");
                        nvpList.add(nvp);
                    }
                    System.out.println(nvpList.size());
                    NameValuePair[] data1 = nvpList.toArray(new NameValuePair[nvpList.size()]);
                    postMethod1.setRequestBody(data1);
                    httpClient.executeMethod(postMethod1);
                    Header responseHeader1 = getMethod1.getResponseHeader("Location");

                    GetMethod getMethod2 = new GetMethod(url1+getCodeAndOpenId(responseHeader1.getValue()).get("code")+url2);
                    httpClient.executeMethod(getMethod2);
                    String response2 = getMethod2.getResponseBodyAsString();
                    token = response2;
                }else{
                    Header responseHeader1 = getMethod1.getResponseHeader("Location");
                    GetMethod getMethod2 = new GetMethod(url1+getCodeAndOpenId(responseHeader1.getValue()).get("code")+url2);
                    httpClient.executeMethod(getMethod2);
                    String response2 = getMethod2.getResponseBodyAsString();
                    token = response2;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return token;
    }

    /**
     * 自动登录
     * @param httpClient
     * @return
     */
    public  static Header login(HttpClient httpClient){
        Header responseHeader0=null;
        try {
            PostMethod postMethod = new PostMethod("http://133.160.93.56:20200/aopoauth/login.do?");
            postMethod.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            postMethod.setRequestHeader("Referer", "http://133.160.93.56:20200/aopoauth/login.jsp");
            NameValuePair[] data = {new NameValuePair("j_username", "hn-unicom"), new NameValuePair("j_password", "123456")};
            postMethod.setRequestBody(data);
            httpClient.executeMethod(postMethod);
            responseHeader0 = postMethod.getResponseHeader("Location");
        }catch (Exception e){
            e.printStackTrace();
        }
        return responseHeader0;
    }
    /**
     * 解析授权码code和openId
     * @param location
     * @return
     */
    public static Map<String,String> getCodeAndOpenId(String location){
        String[] s = location.split("\\?")[1].split("&");
        Map<String,String> map = new HashMap<String, String>();
        map.put("code", s[0].split("=")[1]);
        map.put("open_id", s[1].split("=")[1]);
        return map;
    }

    /**
     * 解析token
     * @param location
     * @return
     */
    public static String getAccessToken(String location){
        String w = location.split("\\#")[1];
        return "{"+w+"}";
    }
    /**
     * 获取指定HTML标签的指定属性的值
     * @param source 要匹配的源文本
     * @param element 标签名称
     * @param attr 标签的属性名称
     * @return 属性值列表
     */
    public static List<String> match(String source, String element, String attr) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + "[^<>]*?\\s" + attr + "=['\"]?(.*?)['\"]?(\\s.*?)?>";
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            result.add(r);
        }
        return result;
    }

}
