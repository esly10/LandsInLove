package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.ui.velocity.VelocityEngineUtils;
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

public class RoomsController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	private static VelocityEngine velocityEngine;
	public void setVelocityEngine(VelocityEngine vEngine) {
		velocityEngine = vEngine;
	}
	
	public static Properties properties;
	
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
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		String json = gson.toJson(rooms);
		
		response.getOutputStream().print("{count: "+count+", rooms: " + json + "}");	
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
			if(user == null || (!user.hasPermission(User.PL_CHARGES_VIEW)))
			{
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getWriter().print(gson.toJson(json));
				return;
			}
				String orderBy = null;
				String sort = request.getParameter("sort");
				
				long now = System.currentTimeMillis();
				Timestamp dateSend = new Timestamp(now);
				String value = request.getParameter("date");
				if (value != null && value.length() > 0) {
					DateParser dp = new DateParser("yyyy-MM-dd HH:mm:ss").parse(value);
					dateSend = dp.firstHour().getTimestamp();
					//qb.where("'"+start + "' BETWEEN rr_reservation_in and  rr_reservation_out");
				}
				
				
				QueryBuilder qb = new QueryBuilder("rooms");
				qb.field("DISTINCT rooms.Room_no")
						.field("rooms.Room_id")
						.field("rooms.Room_type")
						.field("reservations_rooms.rr_id")
						.field("reservations_rooms.rr_reservation_id");
				qb.join("reservations_rooms reservations_rooms"
						,"reservations_rooms.rr_room_id=rooms.ROOM_ID and ('"+dateSend + "' BETWEEN rr_reservation_in and  rr_reservation_out)");
				
				if(sort != null && sort.length() > 0)
				{
					orderBy = sort;
				}
				else
				{ 
					orderBy = "Room_no";
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
				String is_delete = request.getParameter("IS_DELETE");
				if(is_delete != null && is_delete.length() > 0){
					filter.add(new DBFilter("IS_DELETE", DBFilter.EQ, is_delete));
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
				json.addProperty("count", count);
				json.addProperty("success", true);
				json.add("rooms", gson.toJsonTree(list));
				
				
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
		int reservation_id = 0;
		
		String id = request.getParameter("reservation_id");//
		if(id != null){
			reservation_id = Integer.parseInt(id);
		}
		@SuppressWarnings("unchecked")
		ArrayList<Rooms> roomsList = rooms.noReceivedRooms(reservation_id, DateParser.toTimestamp(checkin,"yyyy-M-dd HH:mm:ss"), DateParser.toTimestamp(checkout,"yyyy-M-dd HH:mm:ss"));
		
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		String json = gson.toJson(roomsList);
		
		response.getOutputStream().print("{rooms: " + json + "}");	
	}
	public ModelAndView details(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
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
			int guestSize = 0;
			int agencySize = 0;
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
				guestSize = guestCharges.size();
				agencySize = agencyCharges.size();
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
						mv.addObject("guestSize", guestSize);
						mv.addObject("agencySize", agencySize);
						
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
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print("{success: false, msg: 'Charges not found.'}");
		return null;
	}
	
	
	public void exportPDF(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, TransformerException, UnknownObjectException, ParseException {
		{
			try
			{
				String htmlFile = "";
				HashMap<String, Object> model = new HashMap<String, Object>();
				int id = Integer.parseInt(request.getParameter("room_id"));
				Rooms room = new Rooms(id);
				long now = System.currentTimeMillis();
				Timestamp dateSend = new Timestamp(now);
				Timestamp dateNow = new Timestamp(now);
				String date = request.getParameter("date");
				//long test = Long.parseLong(date);
				try
				{
					dateSend = Timestamp.valueOf(date);
				}
				catch(NumberFormatException nfe){
				
				}
				User user = User.getCurrentUser();
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
				int guestSize = 0;
				int agencySize = 0;
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
					guestSize = guestCharges.size();
					agencySize = agencyCharges.size();
				}
				String imgUrl = request.getScheme() + "://"
						+ request.getServerName() + ":"
						+ request.getServerPort() + request.getContextPath();
				model.put("imgUrl", imgUrl);
				DateFormater formaterNow = new DateFormater(dateNow);
				if (reservationrooms.size() != 0) {
					model.put("reservation", reservation);
					model.put("guestCharges", guestCharges);
					model.put("agencyCharges", agencyCharges);
					model.put("now", formaterNow);
					model.put("guest", guests);
					model.put("roomsrelated", roomsrelated);
					model.put("agency", agency);
					model.put("reservationrooms", reservationrooms);
					model.put("user", user);
					model.put("room", room);
					model.put("guestSize", guestSize);
					model.put("agencySize", agencySize);
				}			
				try {
					htmlFile = VelocityEngineUtils.mergeTemplateIntoString(
							velocityEngine, "config/template/charges.vm",
							model);
				} catch (Exception e) {

				}
				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation().create();
				JsonObject json = new JsonObject();
				json.addProperty("success", true);
				json.addProperty("html", htmlFile);

				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().print(htmlFile);
			}
			catch(Exception e)
			{
				
			}
		}
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
			response.getWriter().print(gson.toJson(json));
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
		
		response.getWriter().print(gson.toJson(json));
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
