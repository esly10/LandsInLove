package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

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
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.CodesWatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class RoomsController extends MultiActionController
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
	
	public ModelAndView details(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
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
			int id = Integer.parseInt(request.getParameter("room_id"));
			Rooms room = new Rooms(id);
			long now = System.currentTimeMillis();
			Timestamp dateSend = new Timestamp(now);
			String date = request.getParameter("date");
		/*	if(!date.equals(""))
			{*/
				try
				{
					dateSend = Timestamp.valueOf(date);
				}
				catch(NumberFormatException nfe){
					json.addProperty("msg", "Invalid date, please select valid date");
					response.getOutputStream().print(gson.toJson(json));
					return null;
				}
			//}*/
			
			int reservationId = ReservationRoom.ReservationsId(id, dateSend);		
			ArrayList<ReservationRoom> reservationrooms = new ArrayList<ReservationRoom>();
			reservationrooms = ReservationRoom.Reservations(reservationId,dateSend);
			ArrayList<Rooms> roomsrelated = new ArrayList<Rooms>();
			Rooms roomrelated = null;
			for (int i =0; i<reservationrooms.size(); i++){
				roomrelated = new Rooms(reservationrooms.get(i).rr_room_id);
				roomsrelated.add(roomrelated);
			}
			ArrayList<Charges> guestCharges = null;
			ArrayList<Charges> agencyCharges = null;			
			Reservations reservation = null;
			Guests guests = null;
			Agencies agency = null;
			if (reservationrooms.size()>0){
				int reservationID = reservationrooms.get(0).rr_reservation_id;
				reservation = new Reservations(reservationID);
				//reservation.reservationnumber = "F000001";
				agency = new Agencies(reservation.reservation_agency_id);
				guests = new Guests(reservation.reservation_guest_id);
				guestCharges = new ArrayList<Charges>();
				guestCharges = Charges.GuestCharges(reservationID);	
				agencyCharges = new ArrayList<Charges>();
				agencyCharges = Charges.AgencyCharges(reservationID);	
			}
			if(request.getParameter("xaction") != null)
			{
			
				json.addProperty("success", true);
				
				json.add("room", gson.toJsonTree(room));
				response.setContentType("text/json");
				response.getOutputStream().print(gson.toJson(json));
				return null;
			}
			else
			{
				if (reservationrooms.size()==0){
					ModelAndView msg =  new ModelAndView("charge_no_found","message",
							"No reservations related to this room on the date indicated");
					msg.addObject("room", room);
					return msg;
				}else{
					ModelAndView mv =  new ModelAndView("charges");
					if (reservation != null){
						mv.addObject("reservation", reservation);
						mv.addObject("guestCharges", guestCharges);
						mv.addObject("agencyCharges", agencyCharges);
					}
					if (guests != null){
						mv.addObject("guest", guests);
					}
					if (roomsrelated.size() != 0){
						mv.addObject("roomsrelated", roomsrelated);
					}
					if (agency != null){
						mv.addObject("agency", agency);
					}
					mv.addObject("reservationrooms", reservationrooms);
					mv.addObject("user", user);
					mv.addObject("room", room);
					return mv;
				}
			}
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
