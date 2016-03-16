package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class ReservationRoom  extends DBObject
{	

	@Expose public int rr_id = 0;
	@Expose public int rr_reservation_id = 0;
	@Expose public int rr_room_id = 0;
	@Expose public Timestamp rr_reservation_in = null;
	@Expose public Timestamp rr_reservation_out = null;

	//private static final String UTF_8 = "UTF-8";
	
	public ReservationRoom() throws UnknownObjectException
	{
		this(0);
	}
	
	public ReservationRoom(int rr_id) throws UnknownObjectException
	{
		super("reservations_rooms", "rr_id");
		if(rr_id > 0)
		{
			this.rr_id = rr_id;
			this.populate();
		}
	}
	
	public int getRr_id() {
		return rr_id;
	}
	public void setRr_id(int rr_id) {
		this.rr_id = rr_id;
	}
	public int getRr_reservation_id() {
		return rr_reservation_id;
	}
	public void setRr_reservation_id(int rr_reservation_id) {
		this.rr_reservation_id = rr_reservation_id;
	}
	public int getRr_room_id() {
		return rr_room_id;
	}
	public void setRr_room_id(int rr_room_id) {
		this.rr_room_id = rr_room_id;
	}
	public Timestamp gerRr_reservation_in() {
		return rr_reservation_in;
	}
	public void setRr_reservation_in(Timestamp rr_reservation_in) {
		this.rr_reservation_in = rr_reservation_in;
	}
	public Timestamp getRr_reservation_out() {
		return rr_reservation_out;
	}
	public void setRr_reservation_out(Timestamp rr_reservation_out) {
		this.rr_reservation_out = rr_reservation_out;
	}
	
	public static int ReservationsId(int room_id, Timestamp date) {
		DBConnection conn = null;
		int ReservationId = 0;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * FROM reservations_rooms where ('"+date+"' BETWEEN rr_reservation_in and  rr_reservation_out) and rr_room_id ="+room_id+";";
			if(conn.query(sql))
			{
				ReservationRoom resrervation = new ReservationRoom();
				while(conn.fetch(resrervation))
				{
					ReservationId= resrervation.rr_reservation_id;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return ReservationId;
	} 		
	
	
	public static ArrayList<ReservationRoom> Reservations(int reservation_id, Timestamp date) {
		
		ArrayList<ReservationRoom> reservations = new ArrayList<ReservationRoom>();
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * FROM reservations_rooms where ('"+ date +"' BETWEEN rr_reservation_in and  rr_reservation_out) and rr_reservation_id = "+reservation_id+";";
			if(conn.query(sql))
			{
				ReservationRoom resrervation = new ReservationRoom();
				while(conn.fetch(resrervation))
				{
					reservations.add(resrervation);
					resrervation = new ReservationRoom();
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return reservations;
	} 		
		
	public ArrayList<Rooms> noReceivedRooms(int reservation_id, Timestamp checkIn, Timestamp checkOut) {
		
		ArrayList<Rooms> noReceivedRooms = new ArrayList<Rooms>();
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			
			String sql = "SELECT "+ 
							    "rooms.* "+
							"FROM "+
							    "landsinlove_server.rooms "+
							"WHERE "+
							    "room_id NOT IN "+ 
							    "( "+
									"SELECT "+ 
										"rr_room_id "+
									"FROM "+
										"reservations_rooms AS rr "+
									"WHERE "+
										"rr_reservation_id != "+ reservation_id +
											" AND "+
										"(rr_reservation_in BETWEEN '"+ checkIn +"' AND '"+ checkOut +"') "+
											"OR "+
										"(rr_reservation_out BETWEEN '"+ checkIn +"' AND '"+ checkOut +"') "+
								") order by room_no";
			if(conn.query(sql))
			{
				Rooms rooms = new Rooms();
				while(conn.fetch(rooms))
				{
					noReceivedRooms.add(rooms);
					rooms = new Rooms();
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return noReceivedRooms;
	} 	
}