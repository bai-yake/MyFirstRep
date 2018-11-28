package com.llhy.handler;

import java.sql.SQLException;
import java.util.List;

import net.sf.json.JSONObject;

import com.llhy.pojo.BusinessBean;
import com.llhy.service.DaoService;
import com.llhy.service.SendMsgService;
import com.llhy.utils.DateUtil;
import com.llhy.utils.ListUtil;

/**
 * 10010批量发送短信
 * @author baiyk
 */
public class SendMsgHandler {
	//默认线程数量
	private static int threadNums = 10;
	static DaoService dao = null;
	
	static{
		dao = new DaoService();
	}
	
	public void sendMsgHandler(){
		
		boolean isQuery = true;
			while(true){
				try{
					int countFlag = dao.queryIsSendIsThreeCount();
					if(countFlag > 0){
						//线程数据有积压，停止查询要处理的数据
						isQuery = false;
					}else{
						//继续查询要处理的数据
						isQuery = true;
					}
				}catch(Exception e){
					System.out.println("["+DateUtil.getDateTimeInfo()+"]==>查询等待被处理（IsSend=3）的数据异常"+e.toString());
					e.printStackTrace();
				}
				
				if(isQuery){
					//查询是否有需要发短信的数据
					List<BusinessBean> listdatas = null; 
					try {
						listdatas = dao.queryWantSendMsgData();
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					if(listdatas.size() > 0){
						System.out.println("");
						System.out.println("["+DateUtil.getDateTimeInfo()+"]:==>------------------------------------查询到新数据待处理----停止查询------------------------------------");
						if(listdatas.size() <= 50){
							threadNums = 5;
						}else if(listdatas.size() <= 500){
							threadNums = 10;
						}else if(listdatas.size() > 500 && listdatas.size() <= 900){
							threadNums = 20;
						}else {
							threadNums = 50;
						}
						System.out.println("["+DateUtil.getDateTimeInfo()+"]==>查询出数据总数为：--"+listdatas.size()+";--初始化线程数为:--"+threadNums);
						System.out.println("["+DateUtil.getDateTimeInfo()+"]============正在设置修改中间标志位start");
						//更改状态位中间值
						try {
							dao.updataDealFlagInThree(listdatas);
						} catch (Exception e1) {
							System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>更改状态位中间值异常："+e1.toString());
							e1.printStackTrace();
						}
						System.out.println("["+DateUtil.getDateTimeInfo()+"]============正在设置修改中间标志位end");
						List<List> threadList = ListUtil.limitListByThreadNum(threadNums, listdatas);
						for (int i = 0; i < threadList.size(); i++) {
							System.out.println("["+DateUtil.getDateTimeInfo()+"]:线程==>"+(i+1)+"--处理数据个数为："+threadList.get(i).size());
							final List<BusinessBean> list = threadList.get(i);
							new Thread(new Runnable(){
								public void run(){
									for (BusinessBean bean : list) {
										/*压力测试（插入我们自己的表中）
										经测试测试2W条，用时9分11秒，平均每秒处理36条*/
										/*try {
											dao.deleteDataAndInsertBak(bean);
											dao.pressureTestInsert(bean);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}*/
										String jsonStr = "";
										try{
											//发送短信
											jsonStr = SendMsgService.sendMsgBy10010(bean.getSerial_number(), bean.getSms_text());
										}catch(Exception e){
											System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>亚信发送短信超时或其他异常："+e.toString());
											//将IsSend=3未重置为null
											dao.resetAllIsSendIsNull();
											e.printStackTrace();
										}
										
										JSONObject jsonObj = JSONObject.fromObject(jsonStr);
										String respCode = (String)jsonObj.get("respCode");
										if(respCode.equals("00000")){
											try{
												//接口调用成功
												JSONObject result = jsonObj.getJSONObject("result");
												JSONObject resultData = result.getJSONObject("resultData");
												if(result.get("resultCode").toString().equals("0000")){
													if(resultData.get("ret_code").toString().equals("0")){
														//发短信已成功
														//删除数据。并插入备份表
														try {
															dao.deleteDataAndInsertBak(bean);
														} catch (Exception e) {
															System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>删除或备份数据异常："+e.toString());
															e.printStackTrace();
														}
													}else{
														System.out.println("["+DateUtil.getDateTimeInfo()+"]==>调用‘亚信’发送短信接口出错：--ret_code："+resultData.get("ret_code").toString()+
																";ret_msg:"+resultData.get("ret_msg").toString());
														if(resultData.get("ret_msg").toString().equals("输入号码非手机号码，请重新输入")){
															//非手机号，删除对应数据
															try {
																dao.deleteData(bean);
															} catch (Exception e) {
																System.out.println("["+DateUtil.getDateTimeInfo()+"]==>删除错误非手机号数据异常：==>"+e.toString());
															}
														}else if(resultData.get("ret_msg").toString().contains("给异网号码发送短信")){
															try {
																dao.deleteData(bean);
															} catch (Exception e) {
																System.out.println("["+DateUtil.getDateTimeInfo()+"]==>删除错误非联通手机号数据异常：==>"+e.toString());
															}
														}else{
															System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>等待第二次发送：log_id："+bean.getLog_id());
															dao.resetIsSendIsNull(bean);
														}
													}
												}else{
													System.out.println("["+DateUtil.getDateTimeInfo()+"]==>调用‘亚信’发送短信接口异常：--result："+result+";resultMessage:"+result.get("resultMessage").toString());
													System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>等待第二次发送：log_id："+bean.getLog_id());
													dao.resetIsSendIsNull(bean);
												}
											}catch (NullPointerException e) {
												System.out.println("["+DateUtil.getDateTimeInfo()+"]==>空指针异常==调用接口无返回信息：--result：" + e.toString());
												dao.resetIsSendIsNull(bean);
											}
										}else{
											System.out.println("["+DateUtil.getDateTimeInfo()+"]==>调用‘亚信’发送短信接口失败：--respCode："+respCode);
											System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>等待第二次发送：log_id："+bean.getLog_id());
											dao.resetIsSendIsNull(bean);
										}
									}
								}
							}).start();
						}
					}
					
				}
			}
		
	}
	
	public static void main(String[] args) {
//		SendMsgService.sendMsgBy10010("15638141670", "这是一条测试短信");
		
	/*	String jsonStr = "{\"respCode\":\"00000\",\"respDesc\":\"调用成功！\",\"result\":{\"resultCode\":\"0000\",\"resultData\":{\"ret_code\":\"0\",\"ret_msg\":\"ok\"},\"resultMessage\":\"操作成功!\"}}";
		
		JSONObject jsonObj = JSONObject.fromObject(jsonStr);
		String respCode = (String)jsonObj.get("respCode");
		System.out.println("respCode:"+respCode);
		JSONObject result = jsonObj.getJSONObject("result");
		JSONObject resultData = result.getJSONObject("resultData");
		System.out.println("resultCode："+result.get("resultCode").toString());
		System.out.println("resultCode："+result.get("resultMessage").toString());
		System.out.println("ret_code："+resultData.get("ret_code").toString());*/
		
		SendMsgHandler smh = new SendMsgHandler();
		try {
			//启动发短信程序
			smh.sendMsgHandler();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"查询数据异常："+e.toString());
		}
		
		
		
	}

}
