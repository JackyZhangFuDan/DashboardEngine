package com.sap.solman.reportengine;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.sap.solman.reportengine.util.ConfigurationReader;

@Component
public class AppListener implements ApplicationContextAware{
	
	private WebApplicationContext ac = null;
	
	@EventListener(ContextRefreshedEvent.class)
	public void afterAppStarted(){
		new ConfigurationReader(this.ac);
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		this.ac = (WebApplicationContext) arg0;
	}
}
