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
import com.cambiolabs.citewrite.data.Payments;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class PaymentsController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			/*User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}*/
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			if(sort == null){
				sort = "payment_id";
			}
			
			if(dir == null){
				dir = " ";
			}
			
			DBFilterList filter = new DBFilterList();			
			
			String value = request.getParameter("payment_method");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("payment_method", DBFilter.EQ, value));
			}
			
			value = request.getParameter("reservation_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("reservation_id", DBFilter.EQ, value));
			}
			
			value = request.getParameter("transaction_no");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("transaction_no", DBFilter.EQ, value));
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
			
			Payments payments = new Payments();
			ArrayList<Payments> list = (ArrayList<Payments>)payments.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = payments.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("payments", gson.toJsonTree(list));
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
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int ID = 0;
		Payments payment = null;
		try
		{
			
			/*User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}*/
			
				String id = request.getParameter("payment_id");
				if(id != null){
					ID = Integer.parseInt(id);
				}
				
				payment = new Payments(ID);
				payment.setReservation_id(Integer.parseInt(request.getParameter("reservation_id")));				
			
				String value = request.getParameter("payment_date");
				if(value!= null && value!= "" && value.length()>0){
					payment.setPayment_date(DateParser.toTimestamp(value,"MM/dd/yyyy")); //DateParser.toTimestamp(value,"MM/dd/yyyy h:mm a")
				}else {					
					Timestamp payment_date = new Timestamp(System.currentTimeMillis());					
					payment.setPayment_date(payment_date);
				}
				
				value = request.getParameter("receive_date");
				if(value!= null && value!= "" && value.length()>0){
					payment.setReceive_date(DateParser.toTimestamp(value,"MM/dd/yyyy")); //DateParser.toTimestamp(value,"MM/dd/yyyy h:mm a")
				}else {					
					Timestamp receive_date = new Timestamp(System.currentTimeMillis());					
					payment.setReceive_date(receive_date);
				}
				
				payment.setBack_account(request.getParameter("back_account"));
				payment.setBill_to(request.getParameter("bill_to"));
				payment.setPayment_notes(request.getParameter("payment_notes"));
				payment.setAmount(Float.parseFloat(request.getParameter("amount")));
				payment.setTransaction_no(Integer.parseInt(request.getParameter("transaction_no")));
				payment.setPayment_method(request.getParameter("payment_method"));
		
			if(!payment.commit())
			{
				response.getOutputStream().print("{success: false, msg: 'Error saving Payment information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid payment id: "+ID+"'}");
			return;
		}
		
		response.getOutputStream().print("{success: true, payment_id: "+payment.payment_id+"}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int ID = 0;
		try
		{
			/*User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}*/
			
			ID = Integer.parseInt(request.getParameter("payment_id"));
			Payments payment = new Payments(ID);
			if(!payment.delete())
			{
				response.getOutputStream().print("{success: false, msg: 'Error removing payment.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid payment id'}");
			return;
		}
			
		response.getOutputStream().print("{success: true}");
	}
}