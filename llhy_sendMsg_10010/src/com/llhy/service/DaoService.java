package com.llhy.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.llhy.pojo.BusinessBean;
import com.llhy.utils.DateUtil;
import com.llhy.utils.JdbcUtils;

public class DaoService {
	//备份表
	private String bak_tab = "";
	//短信发送业务扫描表
	private String business_tab = "";
	
	public DaoService() {
		Properties prop = new Properties();
		try {
			ClassLoader cl = this.getClass().getClassLoader();
			InputStream is = cl.getResourceAsStream("tableConfig.properties");
			prop.load(is);
			bak_tab = prop.getProperty("bak_tab");
			business_tab = prop.getProperty("business_tab");
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public List<BusinessBean> queryWantSendMsgData() throws Exception {
		List<BusinessBean> beanList = new ArrayList<BusinessBean>();
		String sql = "select log_id,serial_number,sms_text,is_send from "+business_tab+"  where is_send is null and rownum <= 1000 ";
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst = conn.prepareStatement(sql);
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
			BusinessBean bean = new BusinessBean();
			bean.setLog_id(rs.getString("log_id"));
			bean.setSerial_number(rs.getString("serial_number"));
			bean.setIs_send(rs.getString("is_send"));
			bean.setSms_text(rs.getString("sms_text"));
			beanList.add(bean);
		}
		JdbcUtils.free(rs, pst, conn);
		return beanList;
	}

	/**等待被处理的数据*/
	public int queryIsSendIsThreeCount() throws Exception{
		BigDecimal bd = null;
		String sql = "select count(0) flagCount from "+business_tab+" t where t.is_send = 3";
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst = conn.prepareStatement(sql);
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
			bd = rs.getBigDecimal("flagCount");
		}
		JdbcUtils.free(rs, pst, conn);
		return bd.intValue();
	}

	public void updataDealFlagInThree(List<BusinessBean> beanList) throws Exception {
		String sql = "update "+business_tab+" t set t.is_send = '3' where t.log_id = ?";
//		System.out.println("["+DateUtil.getDateTimeInfo()+"]==>jdbcSQL："+ sql);
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst = null;
		pst = conn.prepareStatement(sql);
		for (BusinessBean bean : beanList) {
			pst.setString(1, bean.getLog_id());
			pst.execute();
			conn.commit();
		}
		JdbcUtils.free(null, pst, conn);
//		System.out.println("["+DateUtil.getDateTimeInfo()+"]:==>关闭数据库连接");
	}

	public void deleteDataAndInsertBak(BusinessBean bean) throws Exception {
		insertBak(bean);
		deleteData(bean);
	}
	
	public void deleteData(BusinessBean bean) throws Exception{
		String sql = "delete from "+business_tab+" where log_id = ? ";
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：删除数据sql==>"+sql);
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：Parameters==>:"+bean.getLog_id());
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst  = conn.prepareStatement(sql);
		pst.setString(1, bean.getLog_id());
		pst.executeUpdate();
		conn.commit();
		JdbcUtils.free(null, pst, conn);
//		System.out.println("["+DateUtil.getDateTimeInfo()+"]:==>关闭数据库连接");
	}
	public void insertBak(BusinessBean bean) throws Exception{
		String sql = "insert into "+bak_tab+" (LOG_ID,SERIAL_NUMBER,SMS_TEXT,IS_SEND) values(?,?,?,'1')";
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：备份数据sql==>"+sql);
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst  = conn.prepareStatement(sql);
		pst.setString(1, bean.getLog_id());
		pst.setString(2, bean.getSerial_number());
		pst.setString(3, bean.getSms_text());
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：Parameters==>"+bean.getLog_id()+";"+bean.getSerial_number()+";"+bean.getSms_text());
		pst.executeUpdate();
		conn.commit();
		JdbcUtils.free(null, pst, conn);
//		System.out.println("["+DateUtil.getDateTimeInfo()+"]:==>关闭数据库连接");
	}
	
	/**重新设置为null，等待第二次发送
	 * @throws Exception */
	public void resetIsSendIsNull(BusinessBean bean) {
		String sql = "update "+business_tab+" t set t.is_send = null where t.log_id = ?";
		Connection conn = null ;
		PreparedStatement pst = null;
		try{
			conn= JdbcUtils.getConnection();
			pst = conn.prepareStatement(sql);
			pst.setString(1, bean.getLog_id());
			System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>重新配置is_send为null---"+sql);
			pst.execute();
			conn.commit();
		}catch (Exception e) {
			System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>设置is_send为null失败："+e.toString());
		}finally{
			JdbcUtils.free(null, pst, conn);
		}
	}

	/**压力测试
	 * 	模拟批量发短信，是否有重复的，数据是否一致
	 * @throws SQLException 
	 * */
	public void pressureTestInsert(BusinessBean bean) throws SQLException {
		String sql = "insert into  r_w_user_sms_t_byk_test (LOG_ID,SERIAL_NUMBER,SMS_TEXT,IS_SEND) values(?,?,?,'1')";
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：模拟发送数据sql==>"+sql);
		Connection conn = JdbcUtils.getConnection();
		PreparedStatement pst  = conn.prepareStatement(sql);
		pst.setString(1, bean.getLog_id());
		pst.setString(2, bean.getSerial_number());
		pst.setString(3, bean.getSms_text());
		System.out.println("["+DateUtil.getDateTimeInfo()+"]：Parameters==>"+bean.getLog_id()+";"+bean.getSerial_number()+";"+bean.getSms_text());
		pst.executeUpdate();
		conn.commit();
		JdbcUtils.free(null, pst, conn);
	}


	public void resetAllIsSendIsNull() {
		String sql = "update "+business_tab+" t set t.is_send = null";
		Connection conn = null ;
		PreparedStatement pst = null;
		try{
			conn= JdbcUtils.getConnection();
			pst = conn.prepareStatement(sql);
			System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>亚信接口出现异常，重新配置is_send为null---"+sql);
			pst.execute();
			conn.commit();
		}catch (Exception e) {
			System.out.println("["+DateUtil.getDateTimeInfo()+"]"+"==>亚信接口出现异常，设置is_send为null失败："+e.toString());
		}finally{
			JdbcUtils.free(null, pst, conn);
		}
	}
}
