package com.cambiolabs.citewrite.data;

import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class LateFee extends DBObject
{
	@Expose public int late_fee_id = 0;
	@Expose public String violation_id = "";
	@Expose public int days_late = 0;
	@Expose public float fee_amount = 0;
	
	public LateFee()
	{
		super("late_fee", "late_fee_id");
	}
	
	public LateFee(int late_fee_id) throws UnknownObjectException
	{
		this();
		if(late_fee_id > 0)
		{
			this.late_fee_id = late_fee_id;
			this.violation_id = null;
			this.populate();
		}
	}
	
	public LateFee(float fee_amount) throws UnknownObjectException
	{
		this();
		this.late_fee_id = 0;
		this.violation_id = null;
		this.fee_amount = fee_amount;
		this.populate();
		
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<LateFee> getFees(String violationId)
	{
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("violation_id", "=", violationId));
		
		LateFee fee = new LateFee();
		return (ArrayList<LateFee>)fee.get(0, 0, "days_late ASC", filter);
	}

	public String getFormatFeeAmount() {
		return String.format("$%.02f", fee_amount);
	}
	
	
	
	
}
