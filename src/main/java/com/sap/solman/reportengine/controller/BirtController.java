package com.sap.solman.reportengine.controller;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.solman.reportengine.birt.BirtEngineFactory;
import com.sap.solman.reportengine.util.ConfigurationReader;

@RestController
@RequestMapping(produces={"application/json"})
public class BirtController {
	@Autowired
	private BirtEngineFactory birtFactory;
	
	@GetMapping(value="/testbirt")
	public String getBirt(HttpServletResponse response){
		
		IReportEngine engine = birtFactory.getObject();
		
		String designName = ConfigurationReader.getValue("birt_dir_base")+"/scrum.rptdesign";
		IReportRunnable runnable = null;
		try {
		   runnable = engine.openReportDesign( designName );
		}catch ( EngineException e ) {
		   System.err.println ( "Design " + designName + " not found!" );
		   try {
			   birtFactory.destroy( );
		   } catch (Exception e1) {
				e1.printStackTrace();
		   }
		}
		
		// Get the value of a simple property.
		if(runnable != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.birtFactory.runReport(engine, runnable, designName, baos);
			System.out.println("created html report: " + baos.toString());
			return null;
		}else{
			return null;
		}
	}

}
