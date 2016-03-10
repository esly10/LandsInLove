package com.cambiolabs.citewrite.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

//import com.cambiolabs.citewrite.data.Guest;
import com.cambiolabs.citewrite.data.User;

public class LoginSuccesHandler implements AuthenticationSuccessHandler
{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException
	{
		Object principal = auth.getPrincipal();
		
	    if (principal instanceof User)
	    {
	    	response.sendRedirect(request.getContextPath() + "/admin/");
	    }
	    /*else if(principal instanceof Owner)
	    {
	    	response.sendRedirect(request.getContextPath() + "/owner/account");
	    }/*
	    else if(principal instanceof Guest)
	    {
	    	response.sendRedirect(request.getContextPath() + "/guest/citation/details");
	    }*/
	    else
	    {
	    	response.sendRedirect(request.getContextPath() + "/");
	    }
	}

}
