package com.cambiolabs.citewrite.data;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

/**
 * @author Techuso
 *
 */
public class CitationPaymentPlan extends DBObject
{
	@Expose public int payment_plan_id = 0;
	@Expose public int citation_id = 0;
	@Expose public float amount = 0;
	@Expose public String frequency = null;
	@Expose public String type = null;
	@Expose public String date = null;
	@Expose public int number_payment = 0;
	@Expose public int paid = 0;
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		
	public CitationPaymentPlan()
	{
		super("payment_plan", "payment_plan_id");
	}
	
	public CitationPaymentPlan(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.payment_plan_id = id;
			this.populate();
		}
	}
	
	public CitationPaymentPlan(int id, int citationId) throws UnknownObjectException
	{
		this();
		this.citation_id = citationId;
		if(id > 0)
		{
			this.payment_plan_id = id;
			this.populate();
		}
	}
	
	@Override
	public boolean commit()
	{
		return super.commit();
	}
	

	public int getId()
	{
		return payment_plan_id;
	}
	
	public int getNumberPayment()
	{
		return number_payment;
	}
	
	public int getPaid()
	{
		return paid;
	}
	
	public int getCitationId()
	{
		return citation_id;
	}
	
	public float getAmount()
	{
		return amount;
	}
	
	public String getFrequency()
	{
		return frequency;
	}
	
	public String getDate()
	{
		return date;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setDate(String date) {
		
		DateFormat formatter;
	    formatter = new SimpleDateFormat("yyyy/MM/dd");
	    //Date date = (Date) formatter.parse(date);	      
		//this.date =  new Timestamp(date.getTime());
	}
	
	
}
