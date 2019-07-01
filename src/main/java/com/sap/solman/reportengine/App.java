package com.sap.solman.reportengine;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.sap.solman.reportengine.config.WebAppContextConfig;
import com.sap.solman.reportengine.util.ConfigurationReader;

public class App  implements WebApplicationInitializer{
	private static final int LOAD_ON_STARTUP = 1; // initialize when tomcat starts, not when first request comes in
    
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		WebApplicationContext applicationContext  = getApplicationContext();
		//new ConfigurationReader(applicationContext);
		
        //register Spring Web servlet
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet",new DispatcherServlet(applicationContext));
        dispatcher.setLoadOnStartup(LOAD_ON_STARTUP);
        dispatcher.addMapping("/api/v1/*");
        
	}
	
	private AnnotationConfigWebApplicationContext getApplicationContext() {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(WebAppContextConfig.class);
        //applicationContext.getEnvironment().setActiveProfiles("cloud"); //remove this line, let environment to set profile
        return applicationContext;
    }
}
