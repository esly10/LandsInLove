package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.ResultSet;
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
import com.cambiolabs.citewrite.data.Charges;
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
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("reservations");
			qb.field("DISTINCT reservations.reservation_id")
					.field("reservations.reservation_number").field("reservations.reservation_type")
					.field("reservations.reservation_status").field("reservations.reservation_agency_id")
					.field("reservations.reservation_guest_id").field("reservations.reservation_user_id")
					.field("reservations.reservation_check_in")
					.field("reservations.reservation_check_out").field("reservations.reservation_rooms")
					.field("reservations.reservation_adults")
					.field("reservations.reservation_children").field("reservations.reservation_guides")
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
					.field("reservations.reservation_update_data")
					.field("reservations.reservation_creation_date")
					.field("DATE_FORMAT(reservations.reservation_check_in, '%a  %d-%b-%Y') checkin")
					.field("DATE_FORMAT(reservations.reservation_check_out, '%a  %d-%b-%Y') checkout");

			qb.join("agencies agencies",
					"reservations.reservation_agency_id=agencies.agency_id")
					.field("agencies.agency_name").field("agencies.agency_phone")
					.field("agencies.agency_email");
			qb.join("users users",
					"reservations.reservation_user_id=users.user_id")
					.field("users.username");
			qb.join("guests guests",
					"reservations.reservation_guest_id=guests.guest_id")
					.field("guests.name").field("guests.title")
					.field("guests.phone").field("guests.email")
					.field("guests.market").field("guests.type");

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

		response.getOutputStream().print(gson.toJson(json));
	}
	
	
	
	public void roomList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
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
	
	public void chargeList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
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
		
		int reservationID = 0;
		try
		{
			
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
				reservationID = Integer.parseInt(request.getParameter("reservation_id"));
				Reservations reservation = new Reservations(reservationID);
				reservation.setReservation_Number(request.getParameter("reservation_number"));		
				String Type = request.getParameter("reservation_type");
				String Status = request.getParameter("reservation_status");		
				String Agency = request.getParameter("reservation_agency_id");
				String Guest = request.getParameter("reservation_guest_id");
				String User =request.getParameter("reservation_user_id");		
				String Checkin = request.getParameter("reservation_check_in");
				String Checkout = request.getParameter("reservation_check_out");		
				String Rooms = request.getParameter("reservation_rooms");		
				String Nights = request.getParameter("reservation_nights");
				String Adults = request.getParameter("reservation_adults");
				String Children = request.getParameter("reservation_children");
				String Guides = request.getParameter("reservation_guides");		
				String MealPlan = request.getParameter("reservation_meal_plan");
				String Rate = request.getParameter("reservation_rate_type");		
				String PaymentTerms = request.getParameter("reservation_payment_terms");
				String PaymentValue = request.getParameter("reservation_payment_value");
				String AgencyTax = request.getParameter("reservation_agency_tax");		
				String AgencyAmount = request.getParameter("reservation_agency_amount");
				String GuestTax = request.getParameter("reservation_guest_tax");		
				String GuestAmount = request.getParameter("reservation_guest_amount");		
				reservation.setServiceNotes(request.getParameter("reservation_service_notes"));
				reservation.setTransportNotes(request.getParameter("reservation_transport_notes"));
				reservation.setInternalNotes(request.getParameter("reservation_internal_notes"));
				String Update = request.getParameter("reservation_update_data");		
				String Creation = request.getParameter("reservation_creation_date");
	
				try{if(Type!= "" && Type.length()>0){reservation.setType(Integer.parseInt(Type));}}catch(NumberFormatException nfe){}	
				try{if(Status!= "" && Status.length()>0){reservation.setStatus(Integer.parseInt(Status));}}catch(NumberFormatException nfe){}	
				try{if(Agency!= "" && Agency.length()>0){reservation.setAgencyAmount(Integer.parseInt(Agency));}}catch(NumberFormatException nfe){}	
				try{if(Guest!= "" && Guest.length()>0){reservation.setGuestId(Integer.parseInt(Guest));}}catch(NumberFormatException nfe){}
				try{if(User!= "" && User.length()>0){reservation.setType(Integer.parseInt(User));}}catch(NumberFormatException nfe){}	
				try{if(Checkin!= "" && Checkin.length()>0){reservation.setCheckIn( DateParser.toTimestamp(Checkin,"MM/dd/yyyy h:mm a"));;}}catch(NumberFormatException nfe){}
				try{if(Checkout!= "" && Checkout.length()>0){reservation.setCheckOut( DateParser.toTimestamp(Checkout,"MM/dd/yyyy h:mm a"));}}catch(NumberFormatException nfe){}	
				try{if(Rooms!= "" && Rooms.length()>0){reservation.setRooms(Integer.parseInt(Rooms));}}catch(NumberFormatException nfe){}
				try{if(Nights!= "" && Nights.length()>0){reservation.setNights(Integer.parseInt(Nights));}}catch(NumberFormatException nfe){}	
				try{if(Adults!= "" && Adults.length()>0){reservation.setAdults(Integer.parseInt(Adults));}}catch(NumberFormatException nfe){}
				try{if(Children!= "" && Children.length()>0){reservation.setChildren(Integer.parseInt(Children));}}catch(NumberFormatException nfe){}	
				try{if(Guides!= "" && Guides.length()>0){reservation.setGuides(Integer.parseInt(Guides));}}catch(NumberFormatException nfe){}
				try{if(MealPlan!= "" && MealPlan.length()>0){reservation.setMealPlan(Integer.parseInt(MealPlan));}}catch(NumberFormatException nfe){}	
				try{if(Rate!= "" && Rate.length()>0){reservation.setRateType(Integer.parseInt(Rate));}}catch(NumberFormatException nfe){}
				try{if(PaymentTerms!= "" && PaymentTerms.length()>0){reservation.setPaymentTerms(Integer.parseInt(PaymentTerms));}}catch(NumberFormatException nfe){}	
				try{if(PaymentValue!= "" && PaymentValue.length()>0){reservation.setPaymentValue(Integer.parseInt(PaymentValue));}}catch(NumberFormatException nfe){}
				try{if(AgencyTax!= "" && AgencyTax.length()>0){reservation.setAgencyTax(Float.parseFloat(AgencyTax));}}catch(NumberFormatException nfe){}	
				try{if(AgencyAmount!= "" && AgencyAmount.length()>0){reservation.setAgencyAmount(Float.parseFloat(AgencyAmount));}}catch(NumberFormatException nfe){}
				try{if(GuestTax!= "" && GuestTax.length()>0){reservation.setGuestTax(Float.parseFloat(GuestTax));}}catch(NumberFormatException nfe){}	
				try{if(GuestAmount!= "" && GuestAmount.length()>0){reservation.setGuestAmount(Float.parseFloat(GuestAmount));}}catch(NumberFormatException nfe){}
				try{if(Update!= "" && Update.length()>0){reservation.setUpdateDate(DateParser.toTimestamp(Update,"MM/dd/yyyy h:mm a"));}}catch(NumberFormatException nfe){}	
				try{if(Creation!= "" && Creation.length()>0){reservation.setCreationDate(DateParser.toTimestamp(Creation,"MM/dd/yyyy h:mm a"));}}catch(NumberFormatException nfe){}
				
			if(!reservation.commit())
			{
				response.getOutputStream().print("{success: false, msg: 'Error saving reservation information.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid reservation id: "+reservationID+"'}");
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
		
		int reservationID = 0;
		try
		{
			User user = User.getCurrentUser();
			if(user == null || (!user.hasPermission(User.PL_RESERVATION_MANAGE)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			reservationID = Integer.parseInt(request.getParameter("reservation_id"));
			Reservations reservation = new Reservations(reservationID);
			if(!reservation.delete())
			{
				response.getOutputStream().print("{success: false, msg: 'Error removing reservation.'}");
				return;
			}
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid reservation id'}");
			return;
		}
			
		response.getOutputStream().print("{success: true}");
	}
}