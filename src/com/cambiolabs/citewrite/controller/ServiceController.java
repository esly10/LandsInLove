package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.cambiolabs.citewrite.data.Country;
import com.cambiolabs.citewrite.data.Services;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ServiceController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_SERVICE_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("filter_name");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("service_name", "LIKE", value));
			}
			
			value = request.getParameter("filter_type");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("service_type", "=", value));
			}
			
			value = request.getParameter("filter_bill_assigned");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("service_bill_assigned", "=", value));
			}
			
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
			
			Services services = new Services();
			ArrayList<Services> list = (ArrayList<Services>)services.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = services.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("services", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getWriter().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int serviceID = 0;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_SERVICE_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
				serviceID = Integer.parseInt(request.getParameter("service_id"));
				Services service = new Services(serviceID);
				service.setDescription(request.getParameter("service_description"));
				String Bill =request.getParameter("service_bill_assigned");		
				service.setName(request.getParameter("service_name"));	
				String Type = request.getParameter("service_type");		
				String Status =request.getParameter("service_status");		
				service.setRate(request.getParameter("service_rate_base"));		

				try{
					if(Type!= "" && Type.length()>0){
						service.setType(Integer.parseInt(Type));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Status!= "" && Status.length()>0){
						service.setStatus(Integer.parseInt(Status));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Bill!= "" && Bill.length()>0){
						service.setBilling(Integer.parseInt(Bill));
					}
				}catch(NumberFormatException nfe){}	
				
				
				/*try{
					if(Rate!= "" && Rate.length()>0){
						service.setRate(Double.parseDouble(Rate));
					}
				}catch(NumberFormatException nfe){}	*/
			if(!service.commit())
			{
				response.getWriter().print("{success: false, msg: 'Error saving service information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid service id: "+serviceID+"'}");
			return;
		}
		
		response.getWriter().print("{success: true}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int serviceID = 0;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_SERVICE_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			serviceID = Integer.parseInt(request.getParameter("service_id"));
			Services services = new Services(serviceID);
			if(!services.delete())
			{
				response.getWriter().print("{success: false, msg: 'Error removing service.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid service id'}");
			return;
		}
			
		response.getWriter().print("{success: true}");
	}
}