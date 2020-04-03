package com.allimu.mastercontroller.netty.model;

//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class SchoolCode {
	public static Long schoolCode;
	static {
//		Properties pps=new Properties();
//		try {
////			pps.load(new FileInputStream("/usr/local/schoolCode.properties"));
//			schoolCode=Long.parseLong(pps.getProperty("schoolCode"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		schoolCode=4401030002L;
	}
}
