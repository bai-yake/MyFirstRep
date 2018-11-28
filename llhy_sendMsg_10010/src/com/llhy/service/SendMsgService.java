package com.llhy.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.asiainfo.openplatform.AIESBClient;
import com.llhy.utils.MoniOauth;

public class SendMsgService {
	
	public static String sendMsgBy10010(String serialNumber, String sms_content) throws Exception {
		MoniOauth mo = new MoniOauth();
		String http_response = "";
		String url0 = "http://133.160.93.56:20200/aopoauth/oauth/authorize?app_id=501214&response_type=code&redirect_uri=http://www.baidu.com";
		String url1 = "http://133.160.93.56:20200/aopoauth/oauth/token?app_id=501214&code=";
		String url11 = "&app_key=99eb933f5f0b66983442172a3ea16b8c&grant_type=authorization_code&redirect_uri=http://www.baidu.com";

//		String token = mo.accessToken("authorization_code", url0, url1, url11);
		String token = "7da05b7c-63fa-4d85-8ea3-a5c0959f5acb";


		/*Map m = convertJsonStrToMap(token);
//			System.out.println("tokenJSON:" + m.get("access_token"));
			token = (String) m.get("access_token");*/

		HashMap<String, String> sysParam = new HashMap<String, String>();
		sysParam.put("method", "ABILITY_10001027");
		sysParam.put("format", "json");
		sysParam.put("appId", "501214");

		// sysParam.put("appkey", "017d60703f74e7906848498ac3e451b7");//111
		sysParam.put("sign",
				"12A09430713331065B73FCA72BD561795655A3691793AB01B6037FAFD9B32A94");// 111

		sysParam.put("busiSerial", "12");
		sysParam.put("operId", "111");
		sysParam.put("version", "2.0");
		sysParam.put("accessToken", token);
		sysParam.put("timestamp", getTimeStamp());
		sysParam.put("openId", "ca06ac24-4fb8-456f-9d56-add7352299f9");
		sysParam.put("RegionId", "100000000442");

		// 18595420912 R000009
		String busiParam = "{\"msg\":{\"serial_number\":\"" + serialNumber
				+ "\",\"sms_content\":\"" + sms_content + "\"}}";
//		try {
			String e = "99eb933f5f0b66983442172a3ea16b8c";
			http_response = AIESBClient.execute(sysParam, busiParam,"http", e);
			System.out.println("http request response:" + http_response);
//		} catch (Exception var5) {
//			var5.printStackTrace();
//		}
		return http_response;
	}
	
	public static Map<String, Object> convertJsonStrToMap(String jsonStr) {
		Map<String, Object> map = JSON.parseObject(jsonStr);

		return map;
	}

	public static String getTimeStamp(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String currentDate = sdf.format(new Date());
		return currentDate;
	}
	
	public static void main(String[] args) throws Exception {
//		SendMsgService sms = new SendMsgService();
		String string = SendMsgService.sendMsgBy10010("18539935830", "家武，10010已OK");
		System.out.println(string);
	}
}
