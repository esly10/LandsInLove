package com.cambiolabs.citewrite.email.preparator;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.internet.MimeMessage;
import javax.swing.text.html.HTML;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.cambiolabs.citewrite.data.Owner;
import com.cambiolabs.citewrite.email.MailerTask;

public class PasswordReset extends AbstractMessage 
{

	private Owner owner = null;
	private String password = "";
	public PasswordReset(Owner owner, String password)
	{
		this.owner = owner;
		this.password = password;
	}
	
	public void prepare(MimeMessage mimeMessage) throws Exception 
	{
		MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true);
		message.setTo(owner.email);
		
		String meta = "";
		String html = "";
		String text = "";
		String file = "";
		
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("owner", owner);
		model.put("password", password);
		try
		{
			try 
			{
				file =  VelocityEngineUtils.mergeTemplateIntoString(engine,"custom/emails/password-recover.vm", model);
			} 
			catch (Exception e) 
			{
				file =  VelocityEngineUtils.mergeTemplateIntoString(engine,"config/email/template/password-recover.vm", model);
			}
			
			int metaStart = file.indexOf("**EMAIL-META**");
			int metaEnd = file.indexOf("**EMAIL-HTML**");
			meta = file.substring(metaStart+14,metaEnd);
			
			int htmlStart = file.indexOf("**EMAIL-HTML**");
			int htmlEnd = file.indexOf("**EMAIL-TEXT**");
			html = file.substring(htmlStart+14,htmlEnd);
			
			int textStart = file.indexOf("**EMAIL-TEXT**");
			int textEnd = file.indexOf("**EMAIL-END**");
			text = file.substring(textStart+14,textEnd);
			
			String from = "";
			String subject = "";
			int i = meta.indexOf("subject:");
			
			if(meta.length() > 0)
			   {
			     from = meta.substring(7,i);
			   }
			   if(i != 0)
			   {
				   subject = meta.substring(i+8);
			     
			   }
						
			message.setFrom(from);
			message.setSubject(subject);
			
			if(html.length() > 0 && text.length() > 0)
			{
				message.setText(text, html);
			}
			else
			{
				if(html.length()>0)
				{
					message.setText(html, true);
				}
				else if(text.length()>0)
				{
					message.setText(text, true);
				}
			}

		} 
		catch (Exception e)
		{
			
		}
		
	}

}
