package com.cambiolabs.citewrite.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

import com.cambiolabs.citewrite.email.preparator.AbstractMessage;



public class MailerTask implements Runnable 
{
	protected final Log logger = LogFactory.getLog(getClass());

	private static VelocityEngine velocityEngine;
	private static JavaMailSender mailSender;
	
	private AbstractMessage preparator = null;
	
	public MailerTask()
	{
		super();
	}
	
	public void setMessagePreparator(AbstractMessage preparator)
	{
		this.preparator = preparator;
	}
	
	public void setVelocityEngine(VelocityEngine vEngine) 
	{
		velocityEngine = vEngine;
	}
	
	public void setMailSender(JavaMailSender mSender)
	{
		mailSender = mSender;
	}

	@Override
	public void run() 
	{
		try
		{
			this.preparator.setVelocityEngine(velocityEngine);
			mailSender.send(this.preparator);
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
		
	}
	
	
}
