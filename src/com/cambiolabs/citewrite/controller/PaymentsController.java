package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;

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
import com.cambiolabs.citewrite.db.QueryBuilder;
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
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			/*User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
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
			
			QueryBuilder qb = new QueryBuilder("payments");
			qb.field("DISTINCT payments.payment_id")
					.field("payments.reservation_id")
					.field("payments.payment_date")
					.field("payments.payment_method")					
					.field("payments.receive_date")
					.field("payments.transaction_no")
					.field("payments.back_account")
					.field("payments.amount")
					.field("payments.bill_to")
					.field("payments.payment_notes");	

			qb.join("payment_method payment_method",
					"payments.payment_method=payment_method.payment_method_id")
					.field("payment_method.payment_method_description");
			
			
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
			
			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir)
					.where(filter)
					.start(start)
					.max(limit)
					.select();
		
			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}
			
			json.addProperty("count", count);
			json.add("payments", gson.toJsonTree(list));
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
		
		int ID = 0;
		Payments payment = null;
		try
		{
			
			/*User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_AGENCY_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
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
				payment.setTransaction_no(Integer.parseInt(request.getParameter("transaction_no")));
				String method =request.getParameter("payment_method");
		
				try{
					if(method!= "" && method.length()>0){
						 payment.setPayment_method(Integer.parseInt(method));
					}
				}catch(NumberFormatException nfe){}	
				
				if(method.equals("5")){
					payment.setAmount(-Float.parseFloat(request.getParameter("amount")));
				}
				else {
					payment.setAmount(Float.parseFloat(request.getParameter("amount")));
				}
		
			if(!payment.commit())
			{
				response.getWriter().print("{success: false, msg: 'Error saving Payment information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid payment id: "+ID+"'}");
			return;
		}
		
		response.getWriter().print("{success: true, payment_id: "+payment.payment_id+"}");
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
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
				response.getWriter().print(gson.toJson(json));
				return;
			}*/
			
			ID = Integer.parseInt(request.getParameter("payment_id"));
			Payments payment = new Payments(ID);
			if(!payment.delete())
			{
				response.getWriter().print("{success: false, msg: 'Error removing payment.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid payment id'}");
			return;
		}
			
		response.getWriter().print("{success: true}");
	}
}