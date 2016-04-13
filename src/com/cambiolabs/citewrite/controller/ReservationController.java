package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.cambiolabs.citewrite.data.Agencies;
import com.cambiolabs.citewrite.data.CcType;
import com.cambiolabs.citewrite.data.Charges;
import com.cambiolabs.citewrite.data.DateFormater;
import com.cambiolabs.citewrite.data.MealPlan;
import com.cambiolabs.citewrite.data.PaymentMethod;
import com.cambiolabs.citewrite.data.ReservationRoom;
import com.cambiolabs.citewrite.data.Reservations;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ReservationController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public void reservationList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("reservations");
			qb.field("DISTINCT reservations.reservation_id")
					.field("reservations.reservation_number")
					.field("reservations.reservation_type")
					.field("reservations.reservation_rooms_qty")					
					.field("reservations.reservation_rooms_occupancy")
					.field("reservations.reservation_nights")
					.field("reservations.reservation_status")
					.field("reservations.reservation_agency_id")
					.field("reservations.reservation_guest_id")
					.field("reservations.reservation_user_id")
					.field("reservations.reservation_check_in")
					.field("reservations.reservation_check_out")
					.field("reservations.reservation_rooms")
					.field("reservations.reservation_adults")
					.field("reservations.reservation_children")
					.field("reservations.reservation_guides")
					.field("reservations.reservation_meal_plan")
					.field("reservations.reservation_rate_type")
					.field("reservations.reservation_payment_terms")
					.field("reservations.reservation_payment_value")
					.field("reservations.reservation_agency_tax")
					.field("reservations.reservation_agency_amount")
					.field("reservations.reservation_guest_tax")
					.field("reservations.reservation_guest_amount")
					.field("reservations.reservation_service_notes")
					.field("reservations.reservation_transport_notes")
					.field("reservations.reservation_internal_notes")
					.field("reservations.reservation_update_date")
					.field("reservations.reservation_creation_date")
					.field("reservations.card_name")
					.field("reservations.card_no")
					.field("reservations.card_exp")
					.field("reservations.card_type")
					.field("reservations.reservation_tax")					
					.field("reservations.reservation_event_date")
					.field("reservations.reservation_event_participants")					
					.field("reservations.reservation_ignore_service")
					.field("reservations.reservation_ignore_tax");

			qb.join("agencies agencies",
					"reservations.reservation_agency_id=agencies.agency_id")
					.field("agencies.agency_name")
					.field("agencies.agency_phone")
					.field("agencies.agency_email");
			qb.join("users users",
					"reservations.reservation_user_id=users.user_id")
					.field("users.username");
			qb.join("guests guests",
					"reservations.reservation_agency_id=guests.guest_id")
					.field("guests.name")
					.field("guests.title")
					.field("guests.phone")
					.field("guests.email")
					.field("guests.market")
					.field("guests.type");

			DBFilterList filter = new DBFilterList();

			String value = request.getParameter("filter_number");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("reservations.reservation_number", "LIKE", value));
			}

			value = request.getParameter("filter_guest");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("guests.name", "LIKE", value));
			}

			value = request.getParameter("filter_agency");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("agencies.agency_name", "LIKE", value));
			}

			value = request.getParameter("filter_checkIn");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss").parse(value);
				Timestamp start = dp.firstHour().getTimestamp();
				filter.add(new DBFilter("reservations.reservation_check_in", ">=", start));
			}
			
			value = request.getParameter("filter_checkOut");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss").parse(value);
				Timestamp end = dp.lastHour().getTimestamp();
				filter.add(new DBFilter("reservations.reservation_check_out", "<=", end));
			}

			value = request.getParameter("filter_status");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("reservations.reservation_status", "=", value));
			}
		
			/*value = request.getParameter("filter_type");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("reservations.reservation_type", "=", value));
			}*/

			value = request.getParameter("filter_reservation_id");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("reservations.reservation_id", "=", value));
			}
			
			value = request.getParameter("selected_grupbox_reservation");
			
			//Timestamp dateEnd = new Timestamp();
			if(value != null && value.length() > 0)
			{
				long now = System.currentTimeMillis();
				Timestamp dateStart = new Timestamp(now);
				Timestamp dateEnd = new Timestamp(now);
				DateFormater startFormater = new DateFormater(dateStart);
				DateFormater endFormater = new DateFormater(dateEnd);
				GregorianCalendar startCal =startFormater.getCal() ; //05 is june as month start from 0 -11
				GregorianCalendar endCal =endFormater.getCal() ;
				int intValue = Integer.parseInt(value);
				if(intValue!=0){
					switch (intValue){
					case 1:{
						qb.where("'"+dateStart + "' BETWEEN reservations.reservation_check_in and  reservations.reservation_check_out");
					}		
					break;
					case 2:{
						while( startCal.get( Calendar.DAY_OF_WEEK ) != Calendar.MONDAY ){
							startCal.add( Calendar.DATE, 1 );  
						}
						startCal.set(Calendar.HOUR_OF_DAY, 0);
						startCal.set(Calendar.MINUTE, 0);
						startCal.set(Calendar.SECOND, 0);
						startCal.set(Calendar.MILLISECOND, 0);
						dateStart = startFormater.getTimestampDate(startCal);	
						endCal = startCal;
						endCal.add(Calendar.DATE, 7);
						endCal.set(Calendar.HOUR_OF_DAY, 0);
						endCal.set(Calendar.MINUTE, 0);
						endCal.set(Calendar.SECOND, 0);
						endCal.set(Calendar.MILLISECOND, 0);
						dateEnd = startFormater.getTimestampDate(endCal);	
						filter.add(new DBFilter("reservations.reservation_check_in", ">=", dateStart));
						filter.add(new DBFilter("reservations.reservation_check_in", "<=", dateEnd));
					}		
					break;
					case 3:{
												
						int startMonth =  startFormater.getDatemonth()+1;
						if (startMonth == 13){
							startMonth =1;
						}else if(startMonth == 14){
							startMonth =2;
						}
						//startCal.add( Calendar.MONTH, 1 );
						startCal.set(startFormater.getDateyear(),startMonth-1, 1, 0, 0);
						endCal.set(startFormater.getDateyear(),startMonth, 1, 0, 0);
						dateStart = startFormater.getTimestampDate(startCal);
						dateEnd = startFormater.getTimestampDate(endCal);
						filter.add(new DBFilter("reservations.reservation_check_in", ">=", dateStart));
						filter.add(new DBFilter("reservations.reservation_check_in", "<=", dateEnd));
						
					}		
					break;
					case 4:{
						
						//startCal.add( Calendar.MONTH, 1 );
						startCal.set(startFormater.getDateyear(),0, 1, 0, 0);
						endCal.set(startFormater.getDateyear()+1,0, 1, 0, 0);
						dateStart = startFormater.getTimestampDate(startCal);
						dateEnd = startFormater.getTimestampDate(endCal);
						filter.add(new DBFilter("reservations.reservation_check_in", ">=", dateStart));
						filter.add(new DBFilter("reservations.reservation_check_in", "<=", dateEnd));
						
					}		
					break;
					case 5:{
						
						while( startCal.get( Calendar.DAY_OF_WEEK ) != Calendar.MONDAY ){
							startCal.add( Calendar.DATE, -1 );  
						}
						startCal.set(Calendar.HOUR_OF_DAY, 0);
						startCal.set(Calendar.MINUTE, 0);
						startCal.set(Calendar.SECOND, 0);
						startCal.set(Calendar.MILLISECOND, 0);
						dateStart = startFormater.getTimestampDate(startCal);	
						endCal = startCal;
						endCal.add(Calendar.DATE, 7);
						endCal.set(Calendar.HOUR_OF_DAY, 0);
						endCal.set(Calendar.MINUTE, 0);
						endCal.set(Calendar.SECOND, 0);
						endCal.set(Calendar.MILLISECOND, 0);
						dateEnd = startFormater.getTimestampDate(endCal);	
						filter.add(new DBFilter("reservations.reservation_check_in", ">=", dateStart));
						filter.add(new DBFilter("reservations.reservation_check_in", "<=", dateEnd));
					}		
					break;
					case 6:{
						
						int startMonth =  startFormater.getDatemonth()+1;
						if (startMonth == 13){
							startMonth =1;
						}else if(startMonth == 14){
							startMonth =2;
						}
						//startCal.add( Calendar.MONTH, 1 );
						startCal.set(startFormater.getDateyear(),startMonth-2, 1, 0, 0);
						endCal.set(startFormater.getDateyear(),startMonth-1, 1, 0, 0);
						dateStart = startFormater.getTimestampDate(startCal);
						dateEnd = startFormater.getTimestampDate(endCal);
						filter.add(new DBFilter("reservations.reservation_check_in", ">=", dateStart));
						filter.add(new DBFilter("reservations.reservation_check_in", "<=", dateEnd));
						
					}		
					break;
					}
				}
			}
				
			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir).where(filter).start(start).max(limit)
					.select();

			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("reservations", gson.toJsonTree(list));
			json.addProperty("count", count);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		response.getWriter().print(gson.toJson(json));
	}
	
	
	public ModelAndView print(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
			
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_CHARGES_VIEW)))
		{

			response.getOutputStream().print("You don't have permission to perform this action.");
			return null;
		}
		
		try
		{	
			Reservations reservation = new Reservations(Integer.parseInt(request.getParameter("reservation_id")));
			ModelAndView mv =  new ModelAndView("print_reservation");
			if (reservation != null){
				mv.addObject("reservation", reservation);
				mv.addObject("guest", reservation.getGuest());
				mv.addObject("agency", reservation.getAgency());
				mv.addObject("lastPeyment", reservation.getLastPayment());
				
				float subTotal = 0;
				float taxSum = 0;
				float servSum = 0;
				for(Charges charge : reservation.getCharges()){
					subTotal += charge.charge_total;
					if(charge.charge_item_name.equalsIgnoreCase("Room") || charge.charge_item_name.equalsIgnoreCase("Room NS")){
						taxSum+=charge.charge_total;
					}
					
					if(charge.charge_item_name.equalsIgnoreCase("Restaurant") || charge.charge_item_name.equalsIgnoreCase("Rest/Bar")){
						servSum+=charge.charge_total;
					}
					
				}
				
				if(taxSum > 1){
        			taxSum = (13*taxSum)/100;
        		}
        		if(servSum > 1){
        			servSum = (10*servSum)/100;
        		}	
        		
				mv.addObject("subtotal",String.format("$%.02f", subTotal));
				mv.addObject("tax", String.format("$%.02f", (taxSum+servSum)));
				mv.addObject("total", String.format("$%.02f", (subTotal+taxSum+servSum)));
				
				mv.addObject("user", user);
				mv.addObject("mealPlan", reservation.getMealPlan());
				mv.addObject("user", user);
				return mv;
			}			
			
			response.getOutputStream().print("Reservation not found.");
			return null;
			
		}
		catch(Exception e)
		{
			
		}
		response.setContentType("text/json");
		response.getOutputStream().print("{success: false, msg: 'Charges not found.'}");
		return null;
	}
	
	public void roomList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			QueryBuilder qb = new QueryBuilder("reservations_rooms");
			qb.field("DISTINCT reservations_rooms.rr_reservation_id")
					.field("reservations_rooms.rr_room_id")
					.field("reservations_rooms.rr_reservation_date")
					.field("reservations_rooms.rr_id");

			qb.join("rooms rooms",
					"reservations_rooms.rr_id=rooms.ROOM_ID")
					.field("reservations_rooms.ROOM_NO").field("reservations_rooms.ROOM_TYPE");
			
			DBFilterList filter = new DBFilterList();
			String value = request.getParameter("filter_reservation_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("rr_reservation_id", "=", value));
			}
			value = request.getParameter("filter_room_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("rooms.rr_room_id", "=", value));
			}
			value = request.getParameter("filter__id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("reservations_rooms.rr_id", "=", value));
			}
			value = request.getParameter("filter__room_number");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("rooms.ROOM_NO", "=", value));
			}
			value = request.getParameter("filter_reservation_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss").parse(value);
				Timestamp start = dp.firstHour().getTimestamp();
				filter.add(new DBFilter("reservations_rooms.rr_reservation_date", ">=", start));
			}
			
			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir).where(filter).start(start).max(limit)
					.select();

			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("rooms", gson.toJsonTree(list));
			json.addProperty("count", count);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void mealsPlanList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			if(sort == null){
				sort = "meal_plan_id";
			}
			
			if(dir == null){
				dir = " ";
			}
			
			DBFilterList filter = new DBFilterList();
			
			int start = 0;
			int limit = 1000;
			
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
			
			MealPlan mealPlan = new MealPlan();
			ArrayList<MealPlan> list = (ArrayList<MealPlan>)mealPlan.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = mealPlan.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("meals", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getWriter().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void paymentMethodList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			if(sort == null){
				sort = "payment_method_id";
			}
			
			if(dir == null){
				dir = " ";
			}
			
			DBFilterList filter = new DBFilterList();
			
			int start = 0;
			int limit = 1000;
			
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
			
			PaymentMethod payment_method = new PaymentMethod();
			ArrayList<PaymentMethod> list = (ArrayList<PaymentMethod>)payment_method.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = payment_method.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("paymentMethod", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getWriter().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void ccTypeList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			if(sort == null){
				sort = "cc_type_id";
			}
			
			if(dir == null){
				dir = " ";
			}
			
			DBFilterList filter = new DBFilterList();
			
			int start = 0;
			int limit = 1000;
			
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
			
			CcType ccType = new CcType();
			ArrayList<CcType> list = (ArrayList<CcType>)ccType.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = ccType.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("cc_type", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getWriter().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void chargeList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			if(sort == null){
				sort = "charge_id";
			}
			
			if(dir == null){
				dir = " ";
			}
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("charge_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("charge_id", "=", value));
			}
			
			value = request.getParameter("reservation_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("charge_reservation_id", "=", value));
			}

             value = request.getParameter("filter_name");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_name", "LIKE", value));
			}
			
			value = request.getParameter("filter_dni");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("agency_identification", "LIKE", value));
			}
			
			value = request.getParameter("filter_type");
			if(value != null && value.length() > 0)
			{
				int intValue = Integer.parseInt(value);
				if(intValue>2){
					filter.add(new DBFilter("agency_type", "<", "3"));
				}else{
					filter.add(new DBFilter("agency_type", "=", value));
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
			
			Charges charges = new Charges();
			ArrayList<Charges> list = (ArrayList<Charges>)charges.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = charges.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("charges", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getWriter().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveCharges(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int ID = 0;
		String uniqueId = request.getParameter("unique_id");
		if(uniqueId == null){
			uniqueId = "";
		}
		
		Charges charges = null;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
				if(request.getParameter("charge_id") != null && request.getParameter("charge_id").length() > 0){
					ID = Integer.parseInt(request.getParameter("charge_id"));
				}
				
				if(ID > 0){
					charges = new Charges(ID);	
				}else {
					charges = new Charges(uniqueId);
				}				
	
				String res_id = request.getParameter("charge_reservation_id"); //
				charges.setReservationId(Integer.parseInt(res_id));
						
				charges.setCharge_item_name(request.getParameter("charge_item_name"));//
				charges.setCharge_item_desc(request.getParameter("charge_item_desc"));
				//charges.setUnique_id(request.getParameter("unique_id"));
				
				String charge_rate = request.getParameter("charge_rate");//
				String charge_total = request.getParameter("charge_total");//
				charges.setCharge_folio(request.getParameter("charge_folio"));//
				String charge_qty = request.getParameter("charge_qty");//
				String charge_nights = request.getParameter("charge_nights");//
				String charge_date = request.getParameter("charge_date");//	
				
				try{if(charge_qty!= null && charge_qty!= "" && charge_qty.length()>0){charges.setCharge_qty(Integer.parseInt(charge_qty));}}catch(NumberFormatException nfe){}
				try{if(charge_nights!= null && charge_nights!= "" && charge_nights.length()>0){charges.setcharge_nights(Integer.parseInt(charge_nights));}}catch(NumberFormatException nfe){}
				try{if(charge_rate!= null && charge_rate!= "" && charge_rate.length()>0){charges.setCharge_rate(Float.parseFloat(charge_rate));}}catch(NumberFormatException nfe){}
				try{if(charge_total!= null && charge_total!= "" && charge_total.length()>0){charges.setCharge_total(Float.parseFloat(charge_total));}}catch(NumberFormatException nfe){}
							
				long l_charge_date =  0;
				if(charge_date!= null && charge_date!= "" && charge_date.length()>0){
					charges.setDate(DateParser.toTimestamp(charge_date,"MM/dd/yyyy h:mm a"));
				}else {
					l_charge_date =  System.currentTimeMillis();
					Timestamp now = new Timestamp(l_charge_date);
					
					if(ID == 0){ charges.setDate(now); }
				}
				
			if(!charges.commit())
			{
				response.getWriter().print("{success: false, msg: 'Error saving reservation information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid reservation id: "+ID+"'}");
			return;
		}
		
		response.getWriter().print("{success: true}");
	}
	
	public void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ParseException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int reservationID = 0;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
				if(request.getParameter("reservation_id") != null){
				reservationID = Integer.parseInt(request.getParameter("reservation_id"));
				}
				
				Reservations reservation = new Reservations(reservationID);
				reservation.setReservation_number(request.getParameter("reservation_number"));
				
				String Type = request.getParameter("reservation_type"); //
				String Status = request.getParameter("reservation_status");//		
				String Agency = request.getParameter("reservation_agency_id");//
				String Guest = request.getParameter("reservation_guest_id");//
				//String User =request.getParameter("reservation_user_id");		//
				String Checkin = request.getParameter("reservation_check_in");//
				if(Checkin != null){
					Checkin = Checkin.substring(0,10)+" 12:00:00";
				}
				
				String Checkout = request.getParameter("reservation_check_out");//
				if(Checkout != null){
					Checkout = Checkout.substring(0,10)+" 12:00:00";
				}
				
				String eventDate = request.getParameter("reservation_event_date");//				
				if(eventDate != null){
					eventDate = eventDate.substring(0,10)+" 12:00:00";
				}
				
				String reservation_event_participants = request.getParameter("reservation_event_participants");//				
				String Rooms = request.getParameter("reservation_rooms");//
				String Rooms_qty = request.getParameter("reservation_rooms_qty");//
				String Rooms_occupancy = request.getParameter("reservation_rooms_occupancy");//
				String Nights = request.getParameter("reservation_nights");//
				String Adults = request.getParameter("reservation_adults");//
				String Children = request.getParameter("reservation_children");
				String Guides = request.getParameter("reservation_guides");		
				String MealPlan = request.getParameter("reservation_meal_plan");//
				String Rate = request.getParameter("reservation_rate_type");		
				String PaymentTerms = request.getParameter("reservation_payment_terms");
				String PaymentValue = request.getParameter("reservation_payment_value");
				String AgencyTax = request.getParameter("reservation_agency_tax");		
				String AgencyAmount = request.getParameter("reservation_agency_amount");
				String GuestTax = request.getParameter("reservation_guest_tax");		
				String GuestAmount = request.getParameter("reservation_guest_amount");		
				reservation.setReservation_service_notes(request.getParameter("reservation_service_notes"));
				reservation.setReservation_transport_notes(request.getParameter("reservation_transport_notes"));
				reservation.setReservation_internal_notes(request.getParameter("reservation_internal_notes"));
				reservation.setReservation_bar_notes(request.getParameter("reservation_bar_notes"));
				
				String Update = request.getParameter("reservation_update_date");		
				String Creation = request.getParameter("reservation_creation_date");
				String reservation_tax = request.getParameter("reservation_tax");
	
				/*String reservation_ignore_service = request.getParameter("reservation_ignore_service");
				if(reservation_ignore_service.equals("true")){
					reservation.setReservation_ignore_service(1);
				}else {
					reservation.setReservation_ignore_service(0);
				}*/
				
				String reservation_ignore_tax = request.getParameter("reservation_ignore_tax");
				if(reservation_ignore_tax.equals("true")){
					reservation.setReservation_ignore_tax(1);
				}else {
					reservation.setReservation_ignore_tax(0);
				}
				
					
				reservation.setCard_name(request.getParameter("card_name"));
				reservation.setCard_no(request.getParameter("card_no"));
				reservation.setCard_exp(request.getParameter("card_exp"));
				String card_type = request.getParameter("card_type");				
				reservation.setReservation_status_by_string(Status);
	
				try{if(card_type!= null && card_type!= "" && card_type.length()>0){reservation.setCard_type(Integer.parseInt(card_type));}}catch(NumberFormatException nfe){}
				try{if(reservation_event_participants!= null && reservation_event_participants!= "" && reservation_event_participants.length()>0){reservation.setReservation_event_participants(Integer.parseInt(reservation_event_participants));}}catch(NumberFormatException nfe){}		
				try{if(Type!= null && Type!= "" && Type.length()>0){reservation.setReservation_type(Integer.parseInt(Type));}}catch(NumberFormatException nfe){}	
				try{if(Agency!= null && Agency!= "" && Agency.length()>0){reservation.setReservation_agency_id(Integer.parseInt(Agency));}}catch(NumberFormatException nfe){}	
				try{if(Guest!= null && Guest!= "" && Guest.length()>0){reservation.setReservation_guest_id(Integer.parseInt(Guest));}}catch(NumberFormatException nfe){}
				reservation.setReservation_user_id(user.user_id);	
				try{if(Checkin!= null && Checkin!= "" && Checkin.length()>0){reservation.setReservation_check_in(DateParser.toTimestamp(Checkin,"yyyy-M-dd HH:mm:ss"));}}catch(NumberFormatException nfe){}
				try{if(Checkout!= null && Checkout!= "" && Checkout.length()>0){reservation.setReservation_check_out(DateParser.toTimestamp(Checkout,"yyyy-M-dd HH:mm:ss"));}}catch(NumberFormatException nfe){}	
				
				try{if(eventDate!= null && eventDate!= "" && eventDate.length()>0){reservation.setReservation_event_date(DateParser.toTimestamp(eventDate,"yyyy-M-dd HH:mm:ss"));}}catch(NumberFormatException nfe){}
				
				try{if(Rooms!= null && Rooms!= "" && Rooms.length()>0){reservation.setReservation_rooms(Rooms);}}catch(NumberFormatException nfe){}
				try{if(Nights!= null && Nights!= "" && Nights.length()>0){reservation.setReservation_nights(Integer.parseInt(Nights));}}catch(NumberFormatException nfe){}	
				try{if(Adults!= null && Adults!= "" && Adults.length()>0){reservation.setReservation_adults(Integer.parseInt(Adults));}}catch(NumberFormatException nfe){}
				
				try{if(Rooms_qty!= null && Rooms_qty!= "" && Rooms_qty.length()>0){reservation.setReservation_rooms_qty(Integer.parseInt(Rooms_qty));}}catch(NumberFormatException nfe){}
				try{if(Rooms_occupancy!= null && Rooms_occupancy!= "" && Rooms_occupancy.length()>0){reservation.setReservation_rooms_occupancy(Integer.parseInt(Rooms_occupancy));}}catch(NumberFormatException nfe){}
				
				try{if(Children!= null && Children!= "" && Children.length()>0){reservation.setReservation_children(Integer.parseInt(Children));}}catch(NumberFormatException nfe){}	
				try{if(Guides!= null && Guides!= "" && Guides.length()>0){reservation.setReservation_guides(Integer.parseInt(Guides));}}catch(NumberFormatException nfe){}
				try{if(MealPlan!= null && MealPlan!= "" && MealPlan.length()>0){reservation.setReservation_meal_plan(Integer.parseInt(MealPlan));}}catch(NumberFormatException nfe){}	
				try{if(Rate!= null && Rate!= "" && Rate.length()>0){reservation.setReservation_rate_type(Integer.parseInt(Rate));}}catch(NumberFormatException nfe){}
				try{if(PaymentTerms!= null && PaymentTerms!= "" && PaymentTerms.length()>0){reservation.setReservation_payment_terms(Integer.parseInt(PaymentTerms));}}catch(NumberFormatException nfe){}	
				try{if(PaymentValue!= null && PaymentValue!= "" && PaymentValue.length()>0){reservation.setReservation_payment_value(Integer.parseInt(PaymentValue));}}catch(NumberFormatException nfe){}
				try{if(AgencyTax!= null && AgencyTax!= "" && AgencyTax.length()>0){reservation.setReservation_agency_tax(Float.parseFloat(AgencyTax));}}catch(NumberFormatException nfe){}	
				try{if(AgencyAmount!= null && AgencyAmount!= "" && AgencyAmount.length()>0){reservation.setReservation_agency_amount(Float.parseFloat(AgencyAmount));}}catch(NumberFormatException nfe){}
				try{if(GuestTax!= null && GuestTax!= "" && GuestTax.length()>0){reservation.setReservation_guest_tax(Float.parseFloat(GuestTax));}}catch(NumberFormatException nfe){}	
				try{if(GuestAmount!= null && GuestAmount!= "" && GuestAmount.length()>0){reservation.setReservation_guest_amount(Float.parseFloat(GuestAmount));}}catch(NumberFormatException nfe){}
				try{if(reservation_tax!= null && reservation_tax!= "" && reservation_tax.length()>0){reservation.setReservation_tax(Integer.parseInt(reservation_tax));}}catch(NumberFormatException nfe){}
				
				long update =  System.currentTimeMillis();
				Timestamp now = new Timestamp(update);
				reservation.setReservation_update_date(now.toString()); //reservation.setUpdateDate(now);
				
				if(Creation!= null && Creation!= "" && Creation.length()>0){
					reservation.setReservation_creation_date(Creation); //Timestamp.valueOf(Creation)
				}else {
					long createdNow =  System.currentTimeMillis();
					Timestamp created = new Timestamp(createdNow);
					if(reservationID == 0){ reservation.setReservation_creation_date(created.toString()); }
				}
				
			if(!reservation.commit())
			{
				json.addProperty("success", false);
				return;
			}else {
				if(Rooms != null){
				if(Rooms.length() > 0){
				String[] stringArray = Rooms.split(";");
					reservation.deleteRooms();
				for (int i = 0; i < stringArray.length; i++) {
			         String roomId = stringArray[i];
			         ReservationRoom reservationRoom = new ReservationRoom();
			         reservationRoom.rr_room_id = Integer.parseInt(roomId);
			         reservationRoom.rr_reservation_id = reservation.reservation_id;
			         reservationRoom.rr_reservation_in = reservation.reservation_check_in;
			         reservationRoom.rr_reservation_out = reservation.reservation_check_out;
			         reservationRoom.commit();
			         
			    }
				}				
				}								
				
				json.addProperty("success", true);
				json.addProperty("reservation_id", reservation.reservation_id);
			}
		}
		catch(UnknownObjectException uoe)
		{
			json.addProperty("msg", "Invalid reservation id: "+reservationID);
			return;
		}
		
		response.getWriter().print(gson.toJson(json));
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int reservationID = 0;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			reservationID = Integer.parseInt(request.getParameter("reservation_id"));
			Reservations reservation = new Reservations(reservationID);
			if(!reservation.delete())
			{
				response.getWriter().print("{success: false, msg: 'Error removing reservation.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid reservation id'}");
			return;
		}
			
		response.getWriter().print("{success: true}");
	}
	
	public void deleteCharges(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		int chargeID = 0;
		String uniqueId = request.getParameter("unique_id");
		if(uniqueId == null){
			uniqueId = "";
		}
		
		Charges charge = null;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
			
			chargeID = Integer.parseInt(request.getParameter("charge_id"));
			
			if(chargeID > 0){
				charge = new Charges(chargeID);
			}else {
				charge = new Charges(uniqueId);
			}
			
			if(charge.charge_id > 0){
			if(!charge.delete())
			{
				response.getWriter().print("{success: false, msg: 'Error removing Charge.'}");
				return;
			}
			}else {
				response.getWriter().print("{success: true'}");
				return;
			}
			
		}
		catch(UnknownObjectException uoe)
		{
			response.getWriter().print("{success: false, msg: 'Invalid charge id'}");
			return;
		}
			
		response.getWriter().print("{success: true, msg: 'Charge removed.'}");
	}
}