package com.allimu.mastercontroller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Hello world!
 *
 */
public class App
{
	
    public static void main( String[] args )
    
    {
    	//加载spirng配置文件
        @SuppressWarnings({ "resource", "unused" })
		ApplicationContext context= new ClassPathXmlApplicationContext("classpath:spring-netty.xml");
        
    }
}
