package com.cambiolabs.citewrite.controller;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import java.sql.Timestamp;
import com.cambiolabs.citewrite.data.*;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class GuestsController extends MultiActionController
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
			if(user == null || (!user.hasPermission(User.PL_GUEST_MANAGE)))
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
				filter.add(new DBFilter("name", "LIKE", value));
			}
			
			value = request.getParameter("reservation_guest_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("guest_id", "=", value));
			}
			
			value = request.getParameter("query");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("name", "LIKE", "%"+value+"%"));
			}
			
			value = request.getParameter("filter_dni");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("dni", "LIKE", value));
			}
			
			value = request.getParameter("filter_market");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("market", "=", value));
			}
			
			value = request.getParameter("selected_grupbox_guest");
			if(value != null && value.length() > 0)
			{
				int intValue = Integer.parseInt(value);
				if(intValue!=0){				
					if(intValue>2){
						filter.add(new DBFilter("type", "<", "3"));
					}else{
						filter.add(new DBFilter("type", "=", value));
					}	
				}
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
			
			Guests guests = new Guests();
			if(sort == null){
				sort= "name";
			}
			
			if(dir == null){
				dir= "ASC";
			}
			
			ArrayList<Guests> list = (ArrayList<Guests>)guests.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = guests.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("guests", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getOutputStream().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void countryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("filter_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("name", "LIKE", value));
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
			
			Country country = new Country();
			ArrayList<Country> list = (ArrayList<Country>)country.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = country.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("country", gson.toJsonTree(list));
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
		
		Guests guest = null;
		int guestID = 0;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_GUEST_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
				String id = request.getParameter("guest_id");
				if(id != null && id.length() > 0){
					guestID = Integer.parseInt(id);
				}
			
				guest = new Guests(guestID);
				guest.setName(request.getParameter("guest_name"));		
				guest.setDni(request.getParameter("guest_dni"));
				String Tittle = request.getParameter("guest_title");		
				//guest.setZip(request.getParameter("guest_zip"));
				guest.setAddress(request.getParameter("guest_address"));
				String Country =request.getParameter("guest_country");		
				guest.setPhone(request.getParameter("guest_phone"));
				guest.setEmail(request.getParameter("guest_email"));		
				//guest.setMobile(request.getParameter("guest_mobile"));		
				//guest.setFax(request.getParameter("guest_fax"));
				guest.setNotes(request.getParameter("guest_notes"));
				String Market = request.getParameter("guest_market");
				String Type = request.getParameter("guest_type");
				long now = System.currentTimeMillis();
				Timestamp nowtime = new Timestamp(now);
				guest.setCreationDate(nowtime);

				try{
					if(Tittle != null && Tittle!= "" && Tittle.length()>0){
						guest.setTitle(Integer.parseInt(Tittle));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Country != null && Country!= "" && Country.length()>0){
						guest.setCountry(Integer.parseInt(Country));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Market != null && Market!= "" && Market.length()>0){
						guest.setMarket(Integer.parseInt(Market));
					}
				}catch(NumberFormatException nfe){}	
				
				try{
					if(Type != null && Type!= "" && Type.length()>0){
						guest.setType(Integer.parseInt(Type));
					}
				}catch(NumberFormatException nfe){}	
				
				
				
			if(!guest.commit())
			{
				response.getOutputStream().print("{success: false, msg: 'Error saving guest information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid guest id: "+guestID+"'}");
			return;
		}
		
		response.getOutputStream().print("{success: true, guest_id : "+guest.guest_id+"}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int guestID = 0;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_GUEST_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			guestID = Integer.parseInt(request.getParameter("guest_id"));
			Guests guest = new Guests(guestID);
			if(!guest.delete())
			{
				response.getOutputStream().print("{success: false, msg: 'Error removing guest.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid guest id'}");
			return;
		}
			
		response.getOutputStream().print("{success: true}");
	}
}