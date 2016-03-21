package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.cambiolabs.citewrite.data.*;
import com.cambiolabs.citewrite.data.PasswordConfig.AuthorizationType;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class UserController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());

	public void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		try
		{
			
			User currentUser = User.getCurrentUser();
			if(currentUser == null || (!currentUser.isAdmin()))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("filter_user_name");
			if(value != null && value.length() > 0)
			{
				DBFilter nameFilter = new DBFilter("first_name", "LIKE", value);
				filter.add(nameFilter);
				filter.addOr(nameFilter, new DBFilter("last_name", "LIKE", value));
			}
			
			value = request.getParameter("filter_user_username");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("username", "LIKE", value));
			}
			
			value = request.getParameter("filter_user_lastname");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("last_name", "LIKE", value));
			}
			
			/*value = request.getParameter("filter_officer_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("officer_id", "LIKE", value));
			}*/
			
			int start = 0;
			int limit = 0;
			
			if(request.getParameter("start") != null)
			{
				try
				{
					start = Integer.parseInt(request.getParameter("start"));
				}
				catch(NumberFormatException nfe){}
			}
			
			if(request.getParameter("limit") != null)
			{
				try
				{
					limit = Integer.parseInt(request.getParameter("limit"));
				}
				catch(NumberFormatException nfe){}
			}
			
			User user = new User();
			@SuppressWarnings("unchecked")
			ArrayList<User> list = (ArrayList<User>)user.get(start, limit, sort + " " + dir, filter);
			
			int count = list.size();
			if(limit > 0)
			{
				count = user.count(filter);
			}
			
			json.addProperty("success", true);
			json.addProperty("count", count);
			json.add("users", gson.toJsonTree(list));

			response.getOutputStream().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int userID = 0;
		try
		{
			
			User currentUser = User.getCurrentUser();
			if(currentUser == null || (!currentUser.isAdmin()))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			userID = Integer.parseInt(request.getParameter("user_id"));
			User user = new User(userID);
			user.setFirstName(request.getParameter("user_first_name"));
			user.setLastName(request.getParameter("user_last_name"));
			user.setUsername(request.getParameter("user_username"));
			String pwd = request.getParameter("user_password");
			if(pwd != null && pwd.length() > 0)
			{
				user.setPassword(pwd);
			}
			//user.setOfficerID(request.getParameter("user_officer_id"));
			
			User test = User.getByUsername(user.username);
			if(test != null && user.username.equalsIgnoreCase(test.username) && user.user_id != test.user_id)
			{
				response.getOutputStream().print("{success: false, msg: 'User name is already in use. Please select a different user name.'}");
				return;
			}
			
			
			Enumeration<String> names = request.getParameterNames();
			int permissions = 0;
			while (names.hasMoreElements()) 
			{
				  String name =  (String)names.nextElement();
				  if(name.startsWith("user-permission"))
				  {
					  String[] parts = name.split("-");
					  if(parts.length == 3)
					  {
						  try
						  {
							  int perm = Integer.parseInt(parts[2]);
							  permissions |= perm;
						  }
						  catch(NumberFormatException nfe){continue;}
					  }
				  }
				  
			}
			
			user.setPermissions(permissions);
			
			if(!user.commit())
			{
				response.getOutputStream().print("{success: false, msg: 'Error saving user information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid device id: "+userID+"'}");
			return;
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid value for next citation number'}");
			return;
		}
		
		response.getOutputStream().print("{success: true}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int userID = 0;
		try
		{
			
			User currentUser = User.getCurrentUser();
			if(currentUser == null || (!currentUser.isAdmin()))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			userID = Integer.parseInt(request.getParameter("user_id"));
			User user = new User(userID);
			if(!user.delete())
			{
				response.getOutputStream().print("{success: false, msg: 'Error removing user.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid user id'}");
			return;
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid user id'}");
			return;
		}
		
		response.getOutputStream().print("{success: true}");
	}
	
	public void authenticated(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		User authUser = User.getCurrentUser();
		if(authUser == null)
		{
			response.getOutputStream().print("{success: false}");
		}
		else
		{
			response.getOutputStream().print("{success: true}");
		}
	}
	
	/* Retrieves the current users information */
	public void account(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		response.setContentType("text/json");
		String json = gson.toJson(user);

		response.getOutputStream().print("{user: " + json + "}");
	}
	
	public void saveAccount(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		response.setContentType("text/json");
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		user.setFirstName(request.getParameter("account_first_name"));
		user.setLastName(request.getParameter("account_last_name"));
		user.setUsername(request.getParameter("account_username"));
		
		String pwd = request.getParameter("account_password");
		if(pwd != null && pwd.length() > 0)
		{
			PasswordConfig passwordConfig = PasswordConfig.get(AuthorizationType.USER);
			if(!passwordConfig.isValid(pwd)){
				json.addProperty("msg", passwordConfig.getMessage());
				response.getOutputStream().print(gson.toJson(json));
				return;	
			}
			user.setPassword(pwd);
		}
		
		User test = User.getByUsername(user.username);
		if(test != null && user.username.equalsIgnoreCase(test.username) && user.user_id != test.user_id)
		{
			response.getOutputStream().print("{success: false, msg: 'User name is already in use. Please select a different user name.'}");
			return;
		}

		if(!user.commit())
		{
			response.getOutputStream().print("{success: false, msg: 'Error saving account information.'}");
			return;
		}
		
		json.addProperty("success", true);
		json.add("user",gson.toJsonTree(user));
		response.getOutputStream().print(gson.toJson(json));
	}
	
	private AuthenticationManager authenticationManager = null;
	public void setAuthenticationManager(AuthenticationManager authenticationManager)
	{
		this.authenticationManager = authenticationManager;
	}
	
	public ModelAndView password(HttpServletRequest request, HttpServletResponse response) 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		response.setContentType("text/json");
		json.addProperty("success", false);
		
		
		ModelAndView mv = new ModelAndView("change_password");
		mv.addObject("title", "Please change your password");
		
		try 
		{
			PasswordConfig pasworConfig = PasswordConfig.get(PasswordConfig.AuthorizationType.USER);
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String newPassword = request.getParameter("new_password");
			
			if(username == null && password == null && newPassword == null)
			{
				return mv;
			}
			
			User user =  User.getByUsername(username);
									
				if(user.password.equals(Util.getPasswordHash(password)))
				{
					if(!user.password.equals(Util.getPasswordHash(newPassword)))
					{
						if(pasworConfig.isValid(newPassword))
						{
							user.setPassword(newPassword);
							if(user.commit())
							{
								UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.username, newPassword);
								
								Authentication auth = authenticationManager.authenticate(token);
								SecurityContext context = SecurityContextHolder.getContext();
								context.setAuthentication(auth);
								
								request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
								
								json.addProperty("success", true);
								json.addProperty("msg", "Password changed successfully.");
								json.addProperty("redirect", request.getContextPath() + "/admin/");
								response.getOutputStream().print(gson.toJson(json));
								return null;
							
							}
							else
							{
								json.addProperty("msg", "Unknown error change password. Please contact the administrator.");
								response.getOutputStream().print(gson.toJson(json));
								return null;
							}
						}
						else
						{
							json.addProperty("msg", pasworConfig.getMessage());
							response.getOutputStream().print(gson.toJson(json));
							return null;
						}
					}
					else
					{
						json.addProperty("msg", "New password cannot be the same as your current password.");
						response.getOutputStream().print(gson.toJson(json));
						return null;
					}
				}
				else
				{
					json.addProperty("msg", "Username or Password invalid");
					response.getOutputStream().print(gson.toJson(json));
					return null;
				}
			
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
			return mv;
		}
	}
}

	