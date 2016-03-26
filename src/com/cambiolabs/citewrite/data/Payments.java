package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Payments extends DBObject
{	

	@Expose public int payment_id = 0;
	@Expose public int reservation_id = 0;	
	@Expose public Timestamp payment_date = null;	
	@Expose public int payment_method = 0;
	@Expose public Timestamp receive_date = null;
	@Expose public int transaction_no = 0;
	@Expose public String back_account = null;
	@Expose public float amount = 0;
	@Expose public String bill_to = null;
	@Expose public String payment_notes = null;
	

	//private static final String UTF_8 = "UTF-8";
	
	public Payments() throws UnknownObjectException
	{
		this(0);
	}
	
	public Payments(int payment_id) throws UnknownObjectException
	{
		super("payments", "payment_id");
		if(payment_id > 0)
		{
			this.payment_id = payment_id;
			this.populate();
		}
	}

	public int getPayment_id() {
		return payment_id;
	}

	public void setPayment_id(int payment_id) {
		this.payment_id = payment_id;
	}

	public Timestamp getPayment_date() {
		return payment_date;
	}

	public void setPayment_date(Timestamp payment_date) {
		this.payment_date = payment_date;
	}

	public int getPayment_method() {
		return payment_method;
	}

	public void setPayment_method(int payment_method) {
		this.payment_method = payment_method;
	}

	public Timestamp getReceive_date() {
		return receive_date;
	}

	public void setReceive_date(Timestamp receive_date) {
		this.receive_date = receive_date;
	}

	public int getTransaction_no() {
		return transaction_no;
	}

	public void setTransaction_no(int transaction_no) {
		this.transaction_no = transaction_no;
	}

	public String getBack_account() {
		return back_account;
	}

	public void setBack_account(String back_account) {
		this.back_account = back_account;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public String getBill_to() {
		return bill_to;
	}

	public void setBill_to(String bill_to) {
		this.bill_to = bill_to;
	}

	public String getPayment_notes() {
		return payment_notes;
	}

	public void setPayment_notes(String payment_notes) {
		this.payment_notes = payment_notes;
	}

	public int getReservation_id() {
		return reservation_id;
	}

	public void setReservation_id(int reservation_id) {
		this.reservation_id = reservation_id;
	}
	public String getReceiveDateFormated() {
		return getFormatDate(this.receive_date);
	}
	public String getGuestName() {
		try {
			Reservations reservation = new Reservations (this.reservation_id);
			Guests guests = new Guests (reservation.reservation_guest_id);	
			return guests.name;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getAgencyName() {
		try {
			Reservations reservation = new Reservations (this.reservation_id);
			Agencies agency = new Agencies (reservation.reservation_agency_id);
			return agency.agency_name;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getReservationNumber() {
		try {
			Reservations reservation = new Reservations (this.reservation_id);
			return reservation.reservation_number;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getMethodName() {
		try {
			PaymentMethod method = new PaymentMethod (this.payment_method);
			return method.getPayment_method_description();
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<Payments> getPayments(Timestamp start, Timestamp end, int method){
		ArrayList<Payments> payments = new ArrayList<Payments>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM payments where '"+ start +"' <= receive_date and '"+ end +"' >= receive_date and payment_method = "+method;
				
				if(conn.query(sql))
				{
					Payments paid = new Payments();
					while(conn.fetch(paid))
					{
						payments.add(paid);
						paid = new Payments();
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
			return payments;
		}
	
	public static ArrayList<Payments> getPaymentsAll(Timestamp start, Timestamp end){
		ArrayList<Payments> payments = new ArrayList<Payments>();
		DBConnection conn = null;
			try 
			{
				conn = new DBConnection();
				String sql = "SELECT * FROM payments where '"+ start +"' <= receive_date and '"+ end +"' >= receive_date";
				
				if(conn.query(sql))
				{
					Payments paid = new Payments();
					while(conn.fetch(paid))
					{
						payments.add(paid);
						paid = new Payments();
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
			return payments;
		}
	
	public String getMonth() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		calendar.setTime(this.receive_date); 
		
		int month = calendar.get(Calendar.MONTH);
		
		switch (month){
			case 0: return "January";
			case 1: return "February";
			case 2: return "March";
			case 3: return "April";
			case 4: return "May";
			case 5: return "June";
			case 6: return "July";
			case 7: return "August";
			case 8: return "September";
			case 9: return "October";
			case 10: return "November";
			case 11: return "December";
		}
		return null;
	}	
	public int getMonthNumber() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		calendar.setTime(this.receive_date); 
		
		int month = calendar.get(Calendar.MONTH);
		
		return month-1;
	}
	public int getYear() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		calendar.setTime(this.receive_date); 
		
		int year = calendar.get(Calendar.YEAR);
		
		return year;
	}
	public String getFormatDate(Timestamp Date) {
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.setTime(Date); 
		int dateyear = cal.get(Calendar.YEAR);
		int dateday = cal.get(Calendar.DAY_OF_MONTH) ; // Note: zero based!
		int datemonth = cal.get(Calendar.MONTH);
		String Format =dateday+"/"+datemonth+"/ "+dateyear;
		return Format;
	}
}