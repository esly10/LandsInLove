package com.cambiolabs.citewrite.tomcat;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import java.io.*;
import java.util.Timer;

public class Startup implements ServletContextListener {

	  private ServletContext context = null;
	  private Timer timer  = null;

	  //This method is invoked when the Web Application
	  //has been removed and is no longer able to accept
	  //requests

	  public void contextDestroyed(ServletContextEvent event)
	  {

		  //Output a simple message to the server's console
		  this.context = null;
	    
		  if(this.timer != null)
		  {
			  this.timer.cancel();
		  }
	  }


	  //This method is invoked when the Web Application
	  //is ready to service requests

	  public void contextInitialized(ServletContextEvent event)
	  {
		  	//validate license stuff
		  	

		    // you then validate the license by calling isValid().
		    // this checks that the license file itself is not tampered and it is not expired.
		  	
		   
		    
		  this.context = event.getServletContext();
		  this.timer = new Timer(true); //set it up as a timer
		  
		  Thread thread = new Thread(){
			  public void run()
			  {
				  File mobileDB = new File(context.getRealPath("")+"/WEB-INF/db");
				
				  //Config.load(mobileDB, timer);
			  }
		  };
		  thread.start();

	  }
	}