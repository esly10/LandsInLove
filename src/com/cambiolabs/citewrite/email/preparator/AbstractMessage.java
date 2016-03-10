package com.cambiolabs.citewrite.email.preparator;

import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.MimeMessagePreparator;

public abstract class AbstractMessage implements MimeMessagePreparator {

	protected VelocityEngine engine;
	
	public void setVelocityEngine(VelocityEngine engine)
	{
		this.engine = engine;
	}
	
	@Override
	public void prepare(MimeMessage mimeMessage) throws Exception 
	{
		// TODO Auto-generated method stub
	}

}
