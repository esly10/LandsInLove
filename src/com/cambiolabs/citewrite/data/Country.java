package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Country  extends DBObject
{	

	@Expose public int country_id = 0;
	@Expose public String country_code = null;
	@Expose public String country_name = null;
		
	private static final String UTF_8 = "UTF-8";
	
	public Country() throws UnknownObjectException
	{
		this(0);
	}
	
	public Country(int country_id) throws UnknownObjectException
	{
		super("countries", "country_id");
		if(country_id > 0)
		{
			this.country_id = country_id;
			this.populate();
		}
	}
	
	public int getCountryId() {
		return country_id;
	}
	public void setCountryId(int country_id) {
		this.country_id = country_id;
	}
	public String getCountryCode() {
		return country_code;
	}
	public void setCountryCode(String country_code) {
		this.country_code = country_code;
	}
	public String getCountryName() {
		return country_name;
	}
	public void setCountryName(String country_name) {
		this.country_name = country_name;
	}		
}
