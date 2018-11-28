package com.llhy.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	
	public static String getDateTimeInfo() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		return sdf.format(d);
	}
}
