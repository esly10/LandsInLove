package com.cambiolabs.citewrite.ecommerce;

import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class CreditCardType extends DBObject
{

	@Expose public int cc_type_id = 0;
	@Expose public String name = null;
	public String regex = null;
	@Expose public int accepted = 0;
	@Expose public String image_name = null;
	
	public CreditCardType()
	{
		super("cc_type", "cc_type_id", (new String[]{"regex", "name","image_name"}));
	}
	
	public CreditCardType(int ccTypeId) throws UnknownObjectException
	{
		this();
		if(ccTypeId > 0)
		{
			this.cc_type_id = ccTypeId;
			this.populate();
		}
	}
	
	public boolean is(String ccNumber)
	{
		return ccNumber.matches(this.regex);
	}
	
	public static ArrayList<CreditCardType> getAccepted()
	{
		return get(true);
	}
	
	public static ArrayList<CreditCardType> get()
	{
		return get(false);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<CreditCardType> get(boolean acceptedOnly)
	{
		CreditCardType cct = new CreditCardType();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("cc_type_id", ">", 0));
		if(acceptedOnly)
		{
			filter.add(new DBFilter("accepted", "=", 1));
		}
		return (ArrayList<CreditCardType>)cct.get(0, 0, "cc_type_id ASC", filter);
	}

	public int getId()
	{
		return cc_type_id;
	}

	public String getName()
	{
		return name;
	}
	
	public String getImageName()
	{
		return image_name;
	}
	
	public boolean isAccepted()
	{
		return (this.accepted == 1);
	}
}
