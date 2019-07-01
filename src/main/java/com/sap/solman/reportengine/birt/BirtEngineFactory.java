package com.sap.solman.reportengine.birt;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.sap.solman.reportengine.util.ConfigurationReader;


/**
Factory bean for the instance of the {@link IReportEngine report engine}.
 */
public class BirtEngineFactory implements FactoryBean, ApplicationContextAware, DisposableBean {  

	public boolean isSingleton(){ return true ; } 

	private WebApplicationContext context ; 
	private IReportEngine birtEngine ;	
	private Resource logDirectory ;
	private File _resolvedDirectory ;
	private java.util.logging.Level logLevel ; 
	
	public void setApplicationContext(ApplicationContext ctx){
		this.context = (WebApplicationContext)ctx;
		ServletContext sc = this.context.getServletContext();
	}

	public void destroy() throws Exception {
		birtEngine.destroy();
		birtEngine = null;
		Platform.shutdown() ;
	}

	public void setLogLevel(  java.util.logging.Level  ll){
		this.logLevel = ll ;
	}

	public void setLogDirectory( org.springframework.core.io.Resource resource ){
		File f=null;
		try {
			f = resource.getFile();
			validateLogDirectory(f);
			this._resolvedDirectory = f ;
		} catch (IOException e) {
			throw new RuntimeException("couldn't set log directory.");
		}

	}

	private void validateLogDirectory (File f) {
		Assert.notNull ( f ,  " the directory must not be null");
		Assert.isTrue(f.isDirectory() , " the path given must be a directory");
		Assert.isTrue(f.exists() , "the path specified must exist!");	
	} 

	public void setLogDirectory ( java.io.File f ){ 
		validateLogDirectory(f) ;
		this._resolvedDirectory = f; 
	}
	
	public IReportEngine getObject(){
		
		if(this.birtEngine == null){
			System.setProperty("RUN_UNDER_ECLIPSE", "false");
			
			EngineConfig config = new EngineConfig( );
			config.setBIRTHome("");//this.context.getServletContext().getRealPath("/birtreport"));
			//config.setEngineHome("");
			
			//This line injects the Spring Context into the BIRT Context
			config.getAppContext().put("spring", this.context );
			
			//config.setLogConfig( null != this._resolvedDirectory ? this._resolvedDirectory.getAbsolutePath() : null  , this.logLevel);
			config.setLogConfig( ConfigurationReader.getValue("birt_dir_base")+"/logs", Level.SEVERE );
			config.setLogFile( "BIRTApp.log" );
			
			IPlatformContext context = new PlatformServletContext( this.context.getServletContext() );
		    config.setPlatformContext( context );
		         
			try {
				Platform.startup( config );

				IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
				IReportEngine be = factory.createReportEngine( config );	
				this.birtEngine = be ; 
			}catch ( BirtException e ) {
				throw new RuntimeException ( "Could not start the Birt engine!", e) ;
			}
						
		}
		return this.birtEngine;
	}

	@Override
	public Class getObjectType() {
		return IReportEngine.class;
	}
	
	public void runReport(IReportEngine engine,IReportRunnable runnable, String designName, OutputStream outStream){

		IRunAndRenderTask task = engine.createRunAndRenderTask( runnable );
		String str = null;
		
		// prepare report parameters
		IGetParameterDefinitionTask taskPara = engine.createGetParameterDefinitionTask(runnable );
		IScalarParameterDefn param = (IScalarParameterDefn)taskPara.getParameterDefn( "project" );
		String projectId = ((String) taskPara.getDefaultValue( param ));
		taskPara.setParameterValue( "project", "calmptm" );
		HashMap parameterValues = taskPara.getParameterValues( );
		task.setParameterValues( parameterValues );

		// Validate parameter values.
		boolean parametersAreGood = task.validateParameters( );
		if(!parametersAreGood){
			System.out.print("Seems parameters are wrong.");
		}
		
		// Set the name of an output file.
		HTMLRenderOption options = new HTMLRenderOption( );
		//String output = designName.replaceFirst( ".rptdesign", ".html" );
		//options.setOutputFileName( output );
		options.setOutputStream(outStream);
		options.setImageHandler( new HTMLServerImageHandler( ));
		str = ConfigurationReader.getValue("birt_url_base") + "/" + ConfigurationReader.getValue("birt_image_url");
		options.setBaseImageURL(str);
		str = ConfigurationReader.getValue("birt_dir_base") + "/" + ConfigurationReader.getValue("birt_image_dir");
		options.setImageDirectory(str);
		//the generated html content doesn't contain <html> and <body>
		options.setEmbeddable(true); 
		task.setRenderOption( options );

		try {
			task.run( );
			//System.out.println( "Created Report " + output + "." );
		}catch ( EngineException e1 ) {
			System.err.println( "Report " + designName + " run failed." );
			System.err.println( e1.toString( ) );
		}
	}
	
}