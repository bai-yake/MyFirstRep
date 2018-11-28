/*
 * 
 * 这是一个工具类，一般工具类不能被继承，所以定义成final
 * 
 * 不需要构造实例，用私有的构造方法
 *  单例模式或者用static静态代码块，
 *  本例用static，没有实例
 * */
package com.llhy.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public  class JdbcUtils {
	
    private static String url = "jdbc:oracle:thin:@133.160.95.110:1521:cjdb";
    private static String user = "wapapp";
    private static String password = "wapapp";

    private JdbcUtils() {
    }
    

    // 注册驱动
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    //建立连接
    public static Connection getConnection(String url,String user,String password) throws SQLException {
        
    	return DriverManager.getConnection(url, user, password);
    }
    
    public static Connection getConnection() throws SQLException {
        
    	return DriverManager.getConnection(url, user, password);
    }

    //释放资源
    public static void free(ResultSet rs, Statement st, Connection conn) {
    	SimpleDateFormat sdflog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null)
                    st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}