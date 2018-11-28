package com.llhy.pojo;

public class BusinessBean {
	private String log_id;			//主键ID
	private String serial_number;	//手机号
	private String sms_text;		//短信内容
	private String is_send;			//发送标记
	public String getLog_id() {
		return log_id;
	}
	public void setLog_id(String log_id) {
		this.log_id = log_id;
	}
	public String getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}
	public String getSms_text() {
		return sms_text;
	}
	public void setSms_text(String sms_text) {
		this.sms_text = sms_text;
	}
	public String getIs_send() {
		return is_send;
	}
	public void setIs_send(String is_send) {
		this.is_send = is_send;
	}
	public BusinessBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BusinessBean(String log_id, String serial_number, String sms_text,
			String is_send) {
		super();
		this.log_id = log_id;
		this.serial_number = serial_number;
		this.sms_text = sms_text;
		this.is_send = is_send;
	}
	@Override
	public String toString() {
		return "BusinessBean [log_id=" + log_id + ", serial_number="
				+ serial_number + ", sms_text=" + sms_text + ", is_send="
				+ is_send + "]";
	}

	public void myTest(){
		System.out.println("ceshi");
	}
	
}
