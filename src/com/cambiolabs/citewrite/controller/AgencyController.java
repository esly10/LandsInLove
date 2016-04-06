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

import com.cambiolabs.citewrite.data.Agencies;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class AgencyController extends MultiActionController
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
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			
			String value = request.getParameter("filter_name");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_name", "LIKE", value));
			}
			
			value = request.getParameter("filter_country");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_country", "=", value));
			}
			
			value = request.getParameter("reservation_agency_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_id", "=", value));
			}
			
			value = request.getParameter("query");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_name", "LIKE", "%"+value+"%"));
			}
			
			value = request.getParameter("selected_grupbox_agencies");
			if(value != null && value.length() > 0)
			{
				int intValue = Integer.parseInt(value);
				if(intValue!=0){
					if(intValue>3){
						filter.add(new DBFilter("agency_type", "<=", "3"));
					}else{
						filter.add(new DBFilter("agency_type", "=", value));
					}
				}
					
			}
			
			value = request.getParameter("filter_dni");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_identification", "LIKE", value));
			}
			
			/*value = request.getParameter("filter_type");
			if(value != null && value.length() > 0)
			{
				int intValue = Integer.parseInt(value);
				if(intValue>2){
					filter.add(new DBFilter("agency_type", "<", "3"));
				}else{
					filter.add(new DBFilter("agency_type", "=", value));
				}	
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
			
			Agencies agencies = new Agencies();
			ArrayList<Agencies> list = (ArrayList<Agencies>)agencies.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = agencies.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("agencies", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getOutputStream().print(gson.toJson(json));
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
		
		int agencyID = 0;
		Agencies agency = null;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
				agencyID = Integer.parseInt(request.getParameter("agency_id"));
				agency = new Agencies(agencyID);
				agency.setAgency_name(request.getParameter("agency_name"));		
				agency.setDni(request.getParameter("agency_identification"));
				String Type = request.getParameter("agency_type");		
				//agency.setZip(request.getParameter("agency_zip"));
				agency.setAddress(request.getParameter("agency_address"));
				String Country =request.getParameter("agency_country");		
				agency.setAgency_phone(request.getParameter("agency_phone"));
				agency.setAgency_email(request.getParameter("agency_email"));		
				agency.setWeb(request.getParameter("agency_web_site"));		
				//agency.setFax(request.getParameter("agency_fax"));
				agency.setNotes(request.getParameter("agency_notes"));

				try{
					if(Type!= "" && Type.length()>0){
						agency.setType(Integer.parseInt(Type));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Country!= "" && Country.length()>0){
						agency.setCountry(Integer.parseInt(Country));
					}
				}catch(NumberFormatException nfe){}	
				
			if(!agency.commit())
			{
				response.getOutputStream().print("{success: false, msg: 'Error saving agency information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid agency id: "+agencyID+"'}");
			return;
		}
		
		response.getOutputStream().print("{success: true, agency_id : "+agency.agency_id+"}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int agencyID = 0;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			agencyID = Integer.parseInt(request.getParameter("agency_id"));
			Agencies agency = new Agencies(agencyID);
			if(!agency.delete())
			{
				response.getOutputStream().print("{success: false, msg: 'Error removing agency.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid agency id'}");
			return;
		}
			
		response.getOutputStream().print("{success: true}");
	}
}