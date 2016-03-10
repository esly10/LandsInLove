package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;
import java.sql.ResultSet;

public class Reservations extends DBObject
{	

	@Expose public int reservation_id = 0;
	@Expose public String reservation_number = null;
	@Expose public int reservation_type = 0;
	@Expose public int reservation_status = 0;
	@Expose public int reservation_agency_id = 0;
	@Expose public int reservation_guest_id = 0;
	@Expose public int reservation_user_id = 0;
	@Expose public Timestamp reservation_check_in = null;
	@Expose public Timestamp reservation_check_out = null;
	@Expose public int reservation_rooms = 0;
	@Expose public int reservation_nights = 0;
	@Expose public int reservation_occupancy = 0;
	@Expose public int reservation_adults = 0;
	@Expose public int reservation_children = 0;
	@Expose public int reservation_guides = 0;
	@Expose public int reservation_meal_plan = 0;
	@Expose public int reservation_rate_type = 0;
	@Expose public int reservation_payment_terms = 0;
	@Expose public int reservation_payment_value = 0;
	@Expose public float reservation_agency_tax = 0;
	@Expose public float reservation_agency_amount = 0;
	@Expose public float reservation_guest_tax = 0;
	@Expose public float reservation_guest_amount = 0;
	@Expose public String reservation_service_notes = null;
	@Expose public String reservation_transport_notes = null;
	@Expose public String reservation_internal_notes = null;
	@Expose public Timestamp reservation_update_data = null;
	@Expose public Timestamp reservation_creation_date = null;
	
	//private static final String UTF_8 = "UTF-8";
	
	public Reservations() throws UnknownObjectException
	{
		this(0);
	}
	
	public Reservations(int reservation_id) throws UnknownObjectException
	{
		super("reservations", "reservation_id");
		if(reservation_id > 0)
		{
			this.reservation_id = reservation_id;
			this.populate();
		}
	}
	
	public int getAgencyID() {
		return reservation_id;
	}
	public void setAgencyID(int reservation_id) {
		this.reservation_id = reservation_id;
	}
	public String getReservation_number() {
		return reservation_number;
	}
	public void setReservation_number(String reservation_number) {
		this.reservation_number = reservation_number;
	}
	public int getReservation_type() {
		return reservation_type;
	}
	public void setReservation_type(int reservation_type) {
		this.reservation_type = reservation_type;
	}
	public int getReservation_status() {
		return reservation_status;
	}
	public void setReservation_status(int reservation_status) {
		this.reservation_status = reservation_status;
	}
	public int getReservation_agency_id() {
		return reservation_agency_id;
	}
	public void setReservation_agency_id(int reservation_agency_id) {
		this.reservation_agency_id = reservation_agency_id;
	}
	public int getReservation_guest_id() {
		return reservation_guest_id;
	}
	public void setReservation_guest_id(int reservation_guest_id) {
		this.reservation_guest_id = reservation_guest_id;
	}
	public int getReservation_user_id() {
		return reservation_user_id;
	}
	public void setReservation_user_id(int reservation_user_id) {
		this.reservation_user_id = reservation_user_id;
	}
	public Timestamp getReservation_check_in() {
		return reservation_check_in;
	}
	public void setReservation_check_in(Timestamp reservation_check_in) {
		this.reservation_check_in = reservation_check_in;
	}
	public Timestamp getReservation_check_out() {
		return reservation_check_out;
	}
	public void setReservation_check_out(Timestamp reservation_check_out) {
		this.reservation_check_out = reservation_check_out;
	}
	public int getReservation_rooms() {
		return reservation_rooms;
	}
	public void setReservation_rooms(int reservation_rooms) {
		this.reservation_rooms = reservation_rooms;
	}
	public int getReservation_nights() {
		return reservation_nights;
	}
	public void setReservation_nights(int reservation_nights) {
		this.reservation_nights = reservation_nights;
	}
	public void setReservation_occupancy(int reservation_occupancy) {
		this.reservation_occupancy = reservation_occupancy;
	}
	public int getReservation_occupancy() {
		return reservation_occupancy;
	}
	public int getReservation_adults() {
		return reservation_adults;
	}
	public void setReservation_adults(int reservation_adults) {
		this.reservation_adults = reservation_adults;
	}
	public int getReservation_children() {
		return reservation_children;
	}
	public void setReservation_children(int reservation_children) {
		this.reservation_children = reservation_children;
	}
	public int getReservation_guides() {
		return reservation_guides;
	}
	public void setReservation_guides(int reservation_guides) {
		this.reservation_guides = reservation_guides;
	}
	public int getReservation_meal_plan() {
		return reservation_meal_plan;
	}
	public void setReservation_meal_plan(int reservation_meal_plan) {
		this.reservation_meal_plan = reservation_meal_plan;
	}
	public int getReservation_rate_type() {
		return reservation_rate_type;
	}
	public void setReservation_rate_type(int reservation_rate_type) {
		this.reservation_rate_type = reservation_rate_type;
	}
	public int getReservation_payment_terms() {
		return reservation_payment_terms;
	}
	public void setReservation_payment_terms(int reservation_payment_terms) {
		this.reservation_payment_terms = reservation_payment_terms;
	}
	public int getReservation_payment_value() {
		return reservation_payment_value;
	}
	public void setReservation_payment_value(int reservation_payment_value) {
		this.reservation_payment_value = reservation_payment_value;
	}
	public float getReservation_agency_tax() {
		return reservation_agency_tax;
	}
	public void setReservation_agency_tax(float reservation_agency_tax) {
		this.reservation_agency_tax = reservation_agency_tax;
	}
	public float getReservation_agency_amount() {
		return reservation_agency_amount;
	}
	public void setReservation_agency_amount(float reservation_agency_amount) {
		this.reservation_agency_amount = reservation_agency_amount;
	}
	public float getReservation_guest_tax() {
		return reservation_guest_tax;
	}
	public void setReservation_guest_tax(float reservation_guest_tax) {
		this.reservation_guest_tax = reservation_guest_tax;
	}
	public float getReservation_guest_amount() {
		return reservation_guest_amount;
	}
	public void setReservation_guest_amount(float reservation_guest_amount) {
		this.reservation_guest_amount = reservation_guest_amount;
	}
	
	public String getReservation_service_notes() {
		return reservation_service_notes;
	}
	public void setReservation_service_notes(String reservation_service_notes) {
		this.reservation_service_notes = reservation_service_notes;
	}
	public String getReservation_transport_notes() {
		return reservation_transport_notes;
	}
	public void setReservation_transport_notes(String reservation_transport_notes) {
		this.reservation_transport_notes = reservation_transport_notes;
	}
	public String getReservation_internal_notes() {
		return reservation_internal_notes;
	}
	public void setReservation_internal_notes(String reservation_internal_notes) {
		this.reservation_internal_notes = reservation_internal_notes;
	}
	public Timestamp getReservation_update_date() {
		return reservation_update_data;
	}
	public void setReservation_update_date(Timestamp reservation_update_data) {
		this.reservation_update_data = reservation_update_data;
	}
	public Timestamp getReservation_creation_date() {
		return reservation_creation_date;
	}
	public void setReservation_creation_date(Timestamp reservation_creation_date) {
		this.reservation_creation_date = reservation_creation_date;
	}
	
	public static ArrayList<Reservations> MealPlan(Timestamp date) {
		
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * FROM reservations where ('"+ date +"' BETWEEN reservation_check_in and  reservation_check_out) ";
			if(conn.query(sql))
			{
				Reservations resrervation = new Reservations();
				while(conn.fetch(resrervation))
				{
					reservations.add(resrervation);
					resrervation = new Reservations();
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
	public String getRooms() {
		
		String roomsResult = "";
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT rr_reservation_id, rr_room_id, room_no FROM reservations_rooms left join rooms on rr_room_id = room_id where rr_reservation_id= "+this.reservation_id+";";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				while (rs.next())
				{
					roomsResult += rs.getString(3) + ", ";
				}
			}
			roomsResult = roomsResult.substring(0, roomsResult.length()-2); 
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
		return roomsResult;
	}
	public String getGuestName() {
		try {
			Guests guests = new Guests (this.reservation_guest_id);
			return guests.name;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
		
}