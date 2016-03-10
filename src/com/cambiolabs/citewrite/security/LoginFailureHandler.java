package com.cambiolabs.citewrite.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class LoginFailureHandler implements AuthenticationFailureHandler
{
	private String type = "";
	public void setType(String type)
	{
		this.type = type;
	}
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException auth)
			throws IOException, ServletException
	{
		if (this.type.equals("admin"))
		{
			if(auth instanceof CredentialsExpiredException)
			{
				response.sendRedirect(request.getContextPath() + "/admin/user/password");				
			}
			else
			{
				response.sendRedirect(request.getContextPath() + "/admin/login?message="+auth.getMessage());
			}
		}
		else if(this.type.equals("owner"))
		{
			if(auth instanceof CredentialsExpiredException)
			{
				response.sendRedirect(request.getContextPath() + "/owner/password");				
			}
			else
			{
				response.sendRedirect(request.getContextPath() + "/?message="+auth.getMessage());
			}
		}
		else if(this.type.equals("guest"))
		{
			response.sendRedirect(request.getContextPath() + "/guest/login?message="+auth.getMessage());
		}
		else
		{
			response.sendRedirect(request.getContextPath() + "/");
		}
	}

}
