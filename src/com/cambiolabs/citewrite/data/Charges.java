package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Charges extends DBObject
{	

	@Expose public int charge_id = 0;
	@Expose public int charge_reservation_id = 0;
	@Expose public Timestamp charge_date = null;
	@Expose public String charge_item_name = null;
	@Expose public String charge_item_desc = null;
	@Expose public int charge_qty = 0;
	@Expose public int charge_nights = 0;
	@Expose public float charge_rate = 0;
	@Expose public float charge_total = 0;
	@Expose public float charge_tax = 0;
	@Expose public String charge_folio = null;
	@Expose public String unique_id = null;
	
	
	//private static final String UTF_8 = "UTF-8";
	
	public Charges() throws UnknownObjectException
	{
		this(0);
	}
	
	public Charges(int charge_id) throws UnknownObjectException
	{
		super("charges", "charge_id");
		if(charge_id > 0)
		{
			this.charge_id = charge_id;
			this.populate();
		}
	}
	
	public Charges(String unique_id) throws UnknownObjectException
	{
		super("charges", "charge_id");
		
		try {
			if(unique_id.length() > 0)
			{
				this.unique_id = unique_id;
				this.populate();
			}
		} catch (Exception e) {

		}
		
	}
	public int getChargeID() {
		return charge_id;
	}
	public void setChargeID(int charge_id) {
		this.charge_id = charge_id;
	}
	public int getReservationId() {
		return charge_reservation_id;
	}
	public void setReservationId(int charge_reservation_id) {
		this.charge_reservation_id = charge_reservation_id;
	}
	public Timestamp getDate() {
		return charge_date;
	}
	public void setDate(Timestamp charge_date) {
		this.charge_date = charge_date;
	}
	public String getCharge_item_name() {
		return charge_item_name;
	}
	public void setCharge_item_name(String charge_item_name) {
		this.charge_item_name = charge_item_name;
	}
	public String getCharge_item_desc() {
		return charge_item_desc;
	}
	public void setCharge_item_desc(String charge_item_desc) {
		this.charge_item_desc = charge_item_desc;
	}
	public int getCharge_qty() {
		return charge_qty;
	}
	public void setCharge_qty(int charge_qty) {
		this.charge_qty = charge_qty;
	}
	public int getCharge_nights() {
		return charge_nights;
	}
	public void setcharge_nights(int charge_nights) {
		this.charge_nights = charge_nights;
	}
	public float getCharge_rate() {
		return charge_rate;
	}
	public void setCharge_rate(float charge_rate) {
		this.charge_rate = charge_rate;
	}
	
	public float getCharge_total() {
		return charge_total;
	}
	
	public String getCharge_total_format() {		
		return String.format("$%.02f", charge_total);
	}
	
	public void setCharge_total(float charge_total) {
		this.charge_total = charge_total;
	}
	public float getCharge_tax() {
		return charge_tax;
	}
	public void setCharge_tax(float charge_tax) {
		this.charge_tax = charge_tax;
	}
	public String getCharge_folio() {
		return charge_folio;
	}
	public void setCharge_folio(String charge_folio) {
		this.charge_folio = charge_folio;
	}
	
	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public static ArrayList<Charges> GuestCharges(int reservation_id) {
		
		ArrayList<Charges> charges = new ArrayList<Charges>();
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * FROM charges where charge_reservation_id = "+reservation_id+" and charge_folio = 'Guest';";
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
	
	public static ArrayList<Charges> AgencyCharges(int reservation_id) {
		
		ArrayList<Charges> charges = new ArrayList<Charges>();
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * FROM charges where charge_reservation_id = "+reservation_id+" and charge_folio = 'Agency';";
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
		
}