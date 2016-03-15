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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.cambiolabs.citewrite.data.*;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.CodesWatcher;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class CalendarController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	public void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String orderBy = null;
		String sort = request.getParameter("sort");
		if(sort != null && sort.length() > 0)
		{
			orderBy = sort;
		}
		else
		{
			orderBy = "ROOM_NO";
		}
		
		String dir = request.getParameter("dir");
		if(dir != null && dir.length() > 0)
		{
			orderBy += " "+dir;
		}
		else
		{
			orderBy += " ASC";
		}
		
		DBFilterList filter = new DBFilterList();
				
		
		String filterValue = request.getParameter("filter");
		if(filterValue != null && filterValue.length() > 0){
			filter.add(new DBFilter("ROOM_NO", DBFilter.LIKE, filterValue));
		} else {

			String room_no = request.getParameter("ROOM_NO");
			if(room_no != null && room_no.length() > 0){
				filter.add(new DBFilter("ROOM_NO", DBFilter.LIKE, room_no));
			}
			
			String room_type = request.getParameter("ROOM_TYPE");
			if(room_type != null && room_type.length() > 0){
				filter.add(new DBFilter("ROOM_TYPE", DBFilter.EQ, room_type));
			}
			
			String status = request.getParameter("STATUS");
			if(status != null && status.length() > 0){
				filter.add(new DBFilter("STATUS", DBFilter.EQ, status));
			}
			
			
			String lacat_x = request.getParameter("LOCATION_X");
			if(lacat_x != null && lacat_x.length() > 0){
				filter.add(new DBFilter("LOCATION_X", DBFilter.EQ, status));
			}
			
			String lacat_y = request.getParameter("LOCATION_Y");
			if(lacat_y != null && lacat_y.length() > 0){
				filter.add(new DBFilter("LOCATION_Y", DBFilter.EQ, status));
			}
			
			String is_delete = request.getParameter("IS_DELETE");
			if(is_delete != null && is_delete.length() > 0){
				filter.add(new DBFilter("IS_DELETE", DBFilter.EQ, is_delete));
			}
		}
		
			
		int start = 0;
		int max = 50;
		
		String strStart = request.getParameter("start");
		if(strStart != null && strStart.length() > 0)
		{
			try
			{
				start = Integer.parseInt(strStart);
			}
			catch(NumberFormatException nfe){}
		}
		
		String limit = request.getParameter("limit");
		if(limit != null && limit.length() > 0)
		{
			try
			{
				max = Integer.parseInt(limit);
			}
			catch(NumberFormatException nfe){}
		}
		
		//Code query = new Code();
		Rooms query = new Rooms();
		
		@SuppressWarnings("unchecked")
		ArrayList<Rooms> rooms = (ArrayList<Rooms>)query.get(start, max, orderBy, filter);
		
		int count = query.count(filter);
		
		response.setContentType("text/json");
		Gson gson = new Gson();
		String json = gson.toJson(rooms);
		
		response.getOutputStream().print("{count: "+count+", rooms: " + json + "}");	
	}
	
	public void listCalendar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
 		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;	
		try {
			user = User.getCurrentUser();

			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("reservations_rooms");
			qb.field("DISTINCT reservations_rooms.rr_room_id")
					.field("reservations_rooms.rr_reservation_id")
					.field("reservations_rooms.rr_reservation_in")
					.field("reservations_rooms.rr_reservation_out")
					
					.field("rooms.room_no")
					
					.field("reservations.reservation_agency_id")
					.field("reservations.reservation_guest_id")
					
					.field("guests.name guest_name")
					.field("agencies.agency_name");

			qb.join("rooms rooms", "rooms.room_id=reservations_rooms.rr_room_id");
			qb.join("reservations reservations", "reservations_rooms.rr_reservation_id=reservations.reservation_id");
			qb.join("guests guests","reservations.reservation_guest_id=guests.guest_id");
			qb.join("agencies agencies","reservations.reservation_agency_id=agencies.agency_id");

			DBFilterList filter = new DBFilterList();

			String value = request.getParameter("filter_citation_number");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("license", "LIKE", value));
			}

			value = request.getParameter("filter_vehicle_id");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("vehicle_id", "=", value));
			}

			value = request.getParameter("filter_citation_status");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("status.name", "=", value));
			}

			value = request.getParameter("filter_citation_start_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss")
						.parse(value);
				Timestamp start = dp.firstHour().getTimestamp();

				filter.add(new DBFilter("citation_date", ">=", start));

			}

			value = request.getParameter("filter_citation_end_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss")
						.parse(value);
				Timestamp end = dp.lastHour().getTimestamp();

				filter.add(new DBFilter("citation_date", "<=", end));

			}

			int start = 0;
			int limit = 0;


			ArrayList<Hashtable<String, String>> list = 
											qb.orderBy(sort)
											.orderDir(dir)
											.where(filter)
											.start(start)
											.max(limit)
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
	
	public void availableList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, UnknownObjectException 
	{
				
		//Code query = new Code();
		ReservationRoom rooms = new ReservationRoom();
		
		String checkin = request.getParameter("reservation_check_in");//
		checkin = checkin.replace("T", " ");
		String checkout = request.getParameter("reservation_check_out");//
		checkout = checkout.replace("T", " ");
		
		@SuppressWarnings("unchecked")
		ArrayList<Rooms> roomsList = rooms.noReceivedRooms(DateParser.toTimestamp(checkin,"yyyy-M-dd HH:mm:ss"), DateParser.toTimestamp(checkout,"yyyy-M-dd HH:mm:ss"));
		
		
		response.setContentType("text/json");
		Gson gson = new Gson();
		String json = gson.toJson(roomsList);
		
		response.getOutputStream().print("{rooms: " + json + "}");	
	}
	public ModelAndView panel(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
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
			
			
			ModelAndView mv =  new ModelAndView("calendar");
			/*if (reservation != null){
				mv.addObject("reservation", reservation);
				mv.addObject("guestCharges", guestCharges);
				mv.addObject("agencyCharges", agencyCharges);
			}*/
			
			/*mv.addObject("reservationrooms", reservationrooms);
			mv.addObject("user", user);
			mv.addObject("room", room);*/
			return mv;
				
			
		}
		catch(Exception e)
		{
			
		}
		response.setContentType("text/json");
		response.getOutputStream().print("{success: false, msg: 'Charges not found.'}");
		return null;
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		try
		{
			int room_id = 0;
			if(request.getParameter("ROOM_ID") != null && request.getParameter("ROOM_ID").length() > 0){
				room_id = Integer.parseInt(request.getParameter("ROOM_ID"));
			}
			
			Rooms room = new Rooms(room_id);
			room.IS_DELETE = 1;
			
			if(!room.commit())
			{
				json.addProperty("msg", "Error deleting room, try again.");
			}
			
			CodesWatcher.update();
		}
		catch(UnknownObjectException uoe)
		{
			json.addProperty("msg", "Room not found.");
		}
		
		response.getOutputStream().print(gson.toJson(json));
	}
	
	public void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		int room_id = 0;
		if(request.getParameter("ROOM_ID") != null && request.getParameter("ROOM_ID").length() > 0){
			room_id = Integer.parseInt(request.getParameter("ROOM_ID"));
		}
		
		try
			{
				Rooms room = null;
				room = new Rooms(room_id);			
					
				
				if(request.getParameter("ROOM_NO") != null && request.getParameter("ROOM_NO").length() > 0){
					room.ROOM_NO = Integer.parseInt(request.getParameter("ROOM_NO"));
				}
				
				if(request.getParameter("ROOM_TYPE") != null && request.getParameter("ROOM_TYPE").length() > 0){
					room.ROOM_TYPE =  Integer.parseInt(request.getParameter("ROOM_TYPE"));
				}
				
				if(request.getParameter("STATUS") != null && request.getParameter("STATUS").length() > 0){
					room.STATUS = Integer.parseInt(request.getParameter("STATUS"));
				}
				
				if(request.getParameter("LOCATION_Y") != null && request.getParameter("LOCATION_Y").length() > 0){
					room.LOCATION_Y = Float.parseFloat(request.getParameter("LOCATION_Y"));
				}
				
				if(request.getParameter("LOCATION_X") != null && request.getParameter("LOCATION_X").length() > 0){
					room.LOCATION_X = Float.parseFloat(request.getParameter("LOCATION_X"));
				}
				
				if(room.commit()){
					json.addProperty("success", true);
				}else {
					
					json.addProperty("msg", "Error saving Rooms, please try again.");
				}
			}
			catch(UnknownObjectException uoe)
			{
				
				json.addProperty("msg", "Error saving Rooms, please try again.");
				
			}		
		
		response.getOutputStream().print(gson.toJson(json));
		
	}
	
}
