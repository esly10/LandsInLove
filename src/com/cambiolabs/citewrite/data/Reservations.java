package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.annotations.Expose;

import java.sql.ResultSet;

public class Reservations extends DBObject
{	
	public static final int STATUS_CONFIRMED 	= 1;
	public static final int STATUS_CANCELED 	= 2;
	public static final int STATUS_CHECKIN 	= 3;
	public static final int STATUS_CHECKOUT 	= 4;
	public static final int STATUS_OPEN	= 5;
	public static final int STATUS_NOSHOW 	= 6;
	public static final int TYPE_FIT 		= 1;
	public static final int TYPE_GROUP 		= 2;
	public static final int TYPE_EVENT 		= 3;
	public static final String FOLIO_GUEST 		= "Guest";
	public static final String FOLIO_AGENCY 		= "Agency";
	public static final int TERMS_ONCHECKIN 	= 1;
	public static final int TERMS_ONCHECKOUT 	= 2;
	public static final int TERMS_DAYSADVANCE 	= 3;
	public static final int TERMS_DAYSCREDIT 	= 4;
	public static final int TERMS_INSTALAMENTS	= 5;
	public static final int TERMS_CPL 	= 6;
	public static final int TERMS_OTHER 	= 7;
	
	@Expose public int reservation_id = 0;
	@Expose public String reservation_number = null;
	@Expose public int reservation_type = 0;
	@Expose public int reservation_status = 0;
	@Expose public int reservation_agency_id = 0;
	@Expose public int reservation_guest_id = 0;
	@Expose public int reservation_user_id = 0;
	@Expose public Timestamp reservation_check_in = null;
	@Expose public Timestamp reservation_check_out = null;
	@Expose public Timestamp reservation_event_date = null;
	@Expose public int reservation_rooms_qty = 0;
	@Expose public int reservation_rooms_occupancy = 0;
	@Expose public String reservation_rooms = null;
	@Expose public int reservation_nights = 0;
	@Expose public int reservation_adults = 0;
	@Expose public int reservation_children = 0;
	@Expose public int reservation_guides = 0;
	@Expose public int reservation_meal_plan = 0;
	@Expose public int reservation_event_participants = 0;
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
	@Expose public String reservation_update_date = null;
	@Expose public String reservation_creation_date = null;
	
	@Expose public String card_name = null;
	@Expose public String card_no = null;
	@Expose public String card_exp = null;
	@Expose public String card_type = null;
	@Expose public int reservation_tax = 0;
	@Expose public int reservation_ignore_service = 0;
	@Expose public int reservation_ignore_tax = 0;
	
	
	
	
	
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
	
	public MealPlan getMealPlan() throws UnknownObjectException
	{
		MealPlan mealPlan = new MealPlan(this.reservation_meal_plan);
		return mealPlan;
	}
	
	public Payments getLastPayment() throws UnknownObjectException
	{
	
		Payments payment = new Payments();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("reservation_id", "=", this.reservation_id));
		ArrayList<Payments> list = (ArrayList<Payments>)payment.get(0, 0, "payment_id DESC", filter);
		for(Payments pay: list){
			return  pay;
		}
		return null;
	}
	
	public Guests getGuest() throws UnknownObjectException
	{
		Guests Guest = new Guests(this.reservation_guest_id);
		return Guest;
	}
	
	public ArrayList<Charges> getCharges() throws UnknownObjectException
	{
	
		Charges charges = new Charges();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("charge_reservation_id", "=", this.reservation_id));
		return (ArrayList<Charges>)charges.get(0, 10000, "charge_id desc", filter);	
	}
	
	public boolean deleteRooms() throws UnknownObjectException{

		boolean rv = true;			
	
		ReservationRoom rRoom = new ReservationRoom();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("rr_reservation_id", "=", this.reservation_id));
		ArrayList<ReservationRoom> rList = (ArrayList<ReservationRoom>)rRoom.get(0, 10000, "rr_reservation_id desc", filter);			
		
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			for(ReservationRoom room :rList){
								
				conn.execute("DELETE from reservations_rooms where rr_id="+room.rr_id);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
	
	
		return rv;
	
	}
	
	public Timestamp getReservation_event_date() {
		return reservation_event_date;
	}

	public void setReservation_event_date(Timestamp reservation_event_date) {
		this.reservation_event_date = reservation_event_date;
	}

	public int getReservation_tax() {
		return reservation_tax;
	}

	public void setReservation_tax(int reservation_tax) {
		this.reservation_tax = reservation_tax;
	}

	public int getReservation_id() {
		return reservation_id;
	}

	public void setReservation_id(int reservation_id) {
		this.reservation_id = reservation_id;
	}

	public int getReservation_rooms_occupancy() {
		return reservation_rooms_occupancy;
	}

	public void setReservation_rooms_occupancy(int reservation_rooms_occupancy) {
		this.reservation_rooms_occupancy = reservation_rooms_occupancy;
	}

	public void setReservation_update_date(String reservation_update_date) {
		this.reservation_update_date = reservation_update_date;
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
	
	public int getReservation_event_participants() {
		return reservation_event_participants;
	}

	public void setReservation_event_participants(int reservation_event_participants) {
		this.reservation_event_participants = reservation_event_participants;
	}

	public void setReservation_status_by_string(String reservation_status) {
		if(reservation_status.equalsIgnoreCase("Confirmmed")){
			this.reservation_status = 1;
		}else if(reservation_status.equalsIgnoreCase("Canceled")){
			this.reservation_status = 2;
		}else if(reservation_status.equalsIgnoreCase("Check-In")){
			this.reservation_status = 3;
		}else if(reservation_status.equalsIgnoreCase("Check-Out")){
			this.reservation_status = 4;
		}else {
			this.reservation_status = 6;
		}				
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
	
	
	public String getReservation_check_in_format() {
		return DateParser.toString(this.reservation_check_in, "dd-MMM-YY (EEE)");
	}
	
	public String getReservation_check_out_format() {
		return DateParser.toString(this.reservation_check_out, "dd-MMM-YY (EEE)");
	}
	
	public void setReservation_check_in(Timestamp reservation_check_in) {
		this.reservation_check_in = reservation_check_in;
	}
	
	public void setReservation_check_out(Timestamp reservation_check_out) {
		this.reservation_check_out = reservation_check_out;
	}
	public String getReservation_rooms() {
		return reservation_rooms;
	}
	public void setReservation_rooms(String reservation_rooms) {
		this.reservation_rooms = reservation_rooms;
	}
	public int getReservation_nights() {
		return reservation_nights;
	}
	public void setReservation_nights(int reservation_nights) {
		this.reservation_nights = reservation_nights;
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
	
	public String getReservation_payment_terms_name() {
		if(this.reservation_payment_terms == 1)
		{
			return "On Check-In";
		}
		else if(this.reservation_payment_terms == 2)
		{
			return "On Check-Out";
		}
		else if(this.reservation_payment_terms == 3)
		{
			return "Days Advance: "+ this.reservation_payment_value;
		}
		else if(this.reservation_payment_terms == 4)
		{
			return "Days Credit: "+ this.reservation_payment_value;
		}
		else if(this.reservation_payment_terms == 5)
		{
			return "Instalments: "+ this.reservation_payment_value;
		}
		else if(this.reservation_payment_terms == 6)
		{
			return "CPL";
		}
		else
		{
			return "Other";
		}
		
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
	public String getReservation_update_date() {
		return reservation_update_date;
	}
	
	public String getReservation_creation_date() {
		return reservation_creation_date;
	}
	public void setReservation_creation_date(String reservation_creation_date) {
		this.reservation_creation_date = reservation_creation_date;
	}
	

	public void setReservation_rooms_qty(int reservation_rooms_qty) {
		this.reservation_rooms_qty = reservation_rooms_qty;
	}
	
	public int getReservation_rooms_qty() {
		return reservation_rooms_qty;
	}

	public String getCard_name() {
		return card_name;
	}

	public void setCard_name(String card_name) {
		this.card_name = card_name;
	}

	public String getCard_no() {
		return card_no;
	}

	

	public String getCard_exp() {
		return card_exp;
	}

	public void setCard_exp(String card_exp) {
		this.card_exp = card_exp;
	}

	public String getCard_type() {
		return card_type;
	}

	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}

		public void setCard_no(String ccNumber)
	{
		if(ccNumber == null || ccNumber.length() == 0) //anything less than 13 is not valid
		{
			this.card_no = "";
		}
		else
		{
			this.card_no = ccNumber;
			
			//need to mask the credit card and remove the cvv
		    int end = this.card_no.length() - 4;
		    
		    this.card_no = this.card_no.substring(0, 4) + StringUtils.repeat("*", end - 4) + this.card_no.substring(end) ;
		    
		}
	}
	
	
	public int getReservation_ignore_service() {
		return reservation_ignore_service;
	}

	public void setReservation_ignore_service(int reservation_ignore_service) {
		this.reservation_ignore_service = reservation_ignore_service;
	}

	public int getReservation_ignore_tax() {
		return reservation_ignore_tax;
	}

	public void setReservation_ignore_tax(int reservation_ignore_tax) {
		this.reservation_ignore_tax = reservation_ignore_tax;
	}

	public static ArrayList<Reservations> MealPlan(Timestamp date){
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
	
	public static ArrayList<Reservations> EventsReport(Timestamp date){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where ('"+ date +"' <= reservation_check_in and reservation_type = "+ TYPE_EVENT +") ";
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
	
	public static ArrayList<Reservations> Marketing(Timestamp start, Timestamp end){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where '"+ start +"' <= reservation_check_in and '"+ end +"' >= reservation_check_in";
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
	
	public static ArrayList<Reservations> StatusesUnhandledConfirmed(Timestamp date){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where reservation_status= "+ STATUS_CONFIRMED +" and reservation_check_in < '"+date+"'";
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
	
	public static ArrayList<Reservations> ReservationCheckin(Timestamp date){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
		double charge = 0;
		double paid = 0;
		double balance = 0;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where reservation_status= "+ STATUS_CHECKIN +";";
				if(conn.query(sql))
				{
					Reservations resrervation = new Reservations();
					while(conn.fetch(resrervation))
					{
						paid=resrervation.getTotalPaid();
						charge= resrervation.getTotalWOPaid();
						balance = charge- paid;
						if(balance>0){
							reservations.add(resrervation);
							resrervation = new Reservations();
						}					
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
	
	public static ArrayList<Reservations> ReservationCheckout(Timestamp date){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
		double charge = 0;
		double paid = 0;
		double balance = 0;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where reservation_status= "+ STATUS_CHECKOUT +";";
				if(conn.query(sql))
				{
					Reservations resrervation = new Reservations();
					while(conn.fetch(resrervation))
					{
						paid=resrervation.getTotalPaid();
						charge= resrervation.getTotalWOPaid();
						balance = charge- paid;
						if(balance>0){
							reservations.add(resrervation);
							resrervation = new Reservations();
						}					
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
	
	public static ArrayList<Reservations> StatusesUnhandledOpen(Timestamp date){
		ArrayList<Reservations> reservations = new ArrayList<Reservations>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM reservations where reservation_status="+ STATUS_CONFIRMED +" and reservation_check_in > '"+date+"'";
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
	public String getAgencyName() {
		try {
			Agencies agency = new Agencies (this.reservation_agency_id);
			return agency.agency_name;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getUserName() {
		try {
			User user = new User (this.reservation_user_id);
			return user.username +" ("+user.first_name+" "+user.last_name+")";
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getStatusName() {
		try {
			ReservationStatus status = new ReservationStatus (this.reservation_status);
			return status.getReservation_status_description();
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getMealPlanName() {
		try {
			MealPlan plan = new MealPlan (this.reservation_meal_plan);
			return plan.getMeal_plan_description();
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getTypeName() {
		try {
			ReservationType type = new ReservationType (this.reservation_type);
			return type.getReservation_type_description();
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCheckInFormated() {
		return getFormatDate(this.reservation_check_in);
	}
	public String getCheckOutFormated() {
		return getFormatDate(this.reservation_check_out);
	}
	
	
	public String getFormatDate(Timestamp Date) {
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.setTime(Date); 
		int dateyear = cal.get(Calendar.YEAR);
		int dateday = cal.get(Calendar.DAY_OF_MONTH) ; // Note: zero based!
		int datemonth = cal.get(Calendar.MONTH)+1;
		String Format =dateday+"/"+datemonth+"/ "+dateyear;
		return Format;
	}
	public double getTotalGuest() {
		double total =  getGuestCharges()+getGuestTax()+getGuestService()-getGuestPaid();
		
		return total;
	}
	
	public double getTotalAgency() {
		double total =  getAgencyCharges()+getAgencyTax()+getAgencyService()-getAgencyPaid();
		return total;
	}
	
	public double getTotalCharges() {
		double total = getTotalGuest()+getTotalAgency();
		return total;
	}
	
	public double getTotalWOPaid() {
		double total = getAgencyCharges()+getAgencyTax()+getAgencyService()+getGuestCharges()+getGuestTax()+getGuestService();
		return total;
	}
	
	public double getTotalBalance() {
		double total = getTotalWOPaid()-getTotalPaid();
		return total;
	}
	
	public double getTotalPaid() {
		return getGuestPaid()+getAgencyPaid();
	}
	
	
	public double getGuestPaid() {
		float Result = 0;
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT SUM(amount) FROM payments where reservation_id= "+this.reservation_id+" and bill_to='"+ FOLIO_GUEST +"';";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				while (rs.next())
				{
					Result = rs.getFloat(1);
				}
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return Result;
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return Result;
	}
	
	
	public double getAgencyPaid() {
		float Result = 0;
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT SUM(amount) FROM payments where reservation_id= "+this.reservation_id+" and bill_to='"+ FOLIO_AGENCY +"';";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				while (rs.next())
				{
					Result = rs.getFloat(1);
				}
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return Result;
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return Result;
	}
	
	public double getGuestCharges() {
		float Result = 0;
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT SUM(charge_total) FROM charges where charge_reservation_id= "+this.reservation_id+" and charge_folio='"+ FOLIO_GUEST +"';";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				while (rs.next())
				{
					Result = rs.getFloat(1);
				}
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return Result;
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return Result;
	}
	
	
	public double getAgencyCharges() {
		float Result = 0;
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT SUM(charge_total) FROM charges where charge_reservation_id= "+this.reservation_id+" and charge_folio='"+ FOLIO_AGENCY +"';";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				while (rs.next())
				{
					Result = rs.getFloat(1);
				}
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return Result;
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return Result;
	}
	
	public ArrayList<Charges> getChargesItems(){
		ArrayList<Charges> charges = new ArrayList<Charges>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "select * from charges where charge_reservation_id = "+this.reservation_id+";";
				if(conn.query(sql))
				{
					Charges charge = new Charges();
					while(conn.fetch(charge))
					{
						charges.add(charge);
						charge = new Charges();
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
			return charges;
	}
	
	public double getAgencyTax() {
		double total = 0;
		double tax = 0;
		 ArrayList<Charges> charges = new  ArrayList<Charges>();
		 charges = getChargesItems();
		 for (int i =0; i<charges.size(); i++){
			 if (charges.get(i).charge_folio.equals(FOLIO_AGENCY) ){
				if (charges.get(i).charge_item_name.equals("Room") || charges.get(i).charge_item_name.equals("Room NS") ){
					tax =charges.get(i).charge_total*0.13;
					total += tax;
				}
			 }
		 }
		return total;
	}
	
	public double getAgencyService() {
		double total = 0;
		double tax = 0;
		 ArrayList<Charges> charges = new  ArrayList<Charges>();
		 charges = getChargesItems();
		 for (int i =0; i<charges.size(); i++){
			 if (charges.get(i).charge_folio.equals(FOLIO_AGENCY) ){
				if (charges.get(i).charge_item_name.equals("Restaurant") || charges.get(i).charge_item_name.equals("Rest/Bar") ){
					tax =charges.get(i).charge_total*0.1;
					total += tax;
				}
			 }
		 }
		return total;
	}
	
	public double getGuestTax() {
		double total = 0;
		double tax = 0;
		 ArrayList<Charges> charges = new  ArrayList<Charges>();
		 charges = getChargesItems();
		 for (int i =0; i<charges.size(); i++){
			 if (charges.get(i).charge_folio.equals(FOLIO_GUEST) ){
				if (charges.get(i).charge_item_name.equals("Room") || charges.get(i).charge_item_name.equals("Room NS") ){
					tax =charges.get(i).charge_total*0.13;
					total += tax;
				}
			 }
		 }
		return total;
	}
	
	public double getGuestService() {
		double total = 0;
		double tax = 0;
		 ArrayList<Charges> charges = new  ArrayList<Charges>();
		 charges = getChargesItems();
		 for (int i =0; i<charges.size(); i++){
			 if (charges.get(i).charge_folio.equals(FOLIO_GUEST) ){
				if (charges.get(i).charge_item_name.equals("Restaurant") || charges.get(i).charge_item_name.equals("Rest/Bar") ){
					tax =charges.get(i).charge_total*0.1;
					total += tax;
				}
			 }
		 }
		return total;
	}
	
	public String getPaymentTerms() {
		switch (this.reservation_payment_terms){
		case TERMS_ONCHECKIN: return "Checkin";
		case TERMS_ONCHECKOUT: return "Checkout "+this.reservation_payment_value;
		case TERMS_DAYSADVANCE: return "Advance "+this.reservation_payment_value;
		case TERMS_DAYSCREDIT: return "Credit";
		case TERMS_INSTALAMENTS: return "Instalaments";
		case TERMS_CPL: return "CPL";
		case TERMS_OTHER: return "Other";
	}
	return null;
	}
	
	
}