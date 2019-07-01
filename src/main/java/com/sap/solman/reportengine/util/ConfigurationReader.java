package com.sap.solman.reportengine.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;


public class ConfigurationReader {
	
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static Properties p = null;
	
	public static String getValue(String name){
		if(p == null){
			return null;
		}
		
		return p.getProperty(name);
	}
	
	public ConfigurationReader(WebApplicationContext ac){
		this.initialize(ac);
	}
	
	private void initialize(WebApplicationContext ac){
		String path = "";
		if(
	        	Arrays.stream(ac.getEnvironment().getActiveProfiles()).anyMatch(
	        		env -> (env.equalsIgnoreCase("cloud")) 
	        	)
        ){
        	path = "WEB-INF/classes/cloud.properties";
		}else{
			path = "WEB-INF/classes/localserver.properties";
		}
		
		try {
			Resource r = ac.getResource(path);
			p = new Properties( );
			p.load(r.getInputStream());
		} catch (MalformedURLException e) {
			this.logger.error("Read configurations fail." + e.getMessage());
		} catch (IOException e) {
			this.logger.error("Read configurations fail." + e.getMessage());
		}
	}
}
