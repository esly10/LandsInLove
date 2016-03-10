package com.cambiolabs.citewrite.data;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class CiteField extends Field
{
	//codes
	public static final String FN_VIOLATION    = "violation";
    public static final String FN_STATE        = "state";
    public static final String FN_MAKE         = "make";
    public static final String FN_COLOR        = "color";
    public static final String FN_LOCATION     = "location";
    public static final String FN_LATITUDE     = "lat";
    public static final String FN_LONGITUDE     = "lng";
    public static final String FN_COMMENT      = "comment";
    
    //standard
    public static final String FN_LICENSE      	= "license";
    public static final String FN_VIN      		= "vin";
    public static final String FN_DATE_TIME     = "date_time";
    public static final String FN_OFFICER_ID     = "officer_id";
    
	public CiteField()
	{
		super();
	}
	
	public CiteField(String label, String type)
	{
		super(label, type);
	}
	
	public CiteField(String name)
	{
		super();
		this.name = name;
		this.setLabel();
		this.setType();
	}
	
	public CiteField(String name, String label, String type)
	{
		super(name, label, type);
	}
	
	public CiteField(Element field)
	{
		super(field);
	}
	
	public CiteField(HttpServletRequest request)
	{
		super(request);
	}
	
	public CiteField(String label, String table, String id, String desc, String where)
	{
		super(label, table, id, desc, where);
	}
	
	public CiteField(String label, String type, String table, String id, String desc, String where)
	{
		super(label, type, table, id, desc, where);
	}
	
	public CiteField(String name, String label, String type, String table, String id, String desc, String where)
	{
		super(name, label, type, table, id, desc, where);
	}
	
	//used to get get the standard label
	private void setLabel()
	{
		if(name.equals(FN_OFFICER_ID)){ this.label = "Officer ID"; }
		else if(name.equals(FN_DATE_TIME)){ this.label = "Date and Time"; }
		else if(name.equals(FN_LICENSE)){ this.label = "License"; }
		else if(name.equals(FN_VIN)){ this.label = "VIN"; }
		else if(name.equals(FN_STATE)){ this.label = "State"; }
		else if(name.equals(FN_VIOLATION)){ this.label = "Violation"; }
		else if(name.equals(FN_MAKE)){ this.label = "Make"; }
		else if(name.equals(FN_COLOR)){ this.label = "Color"; }
		else if(name.equals(FN_LOCATION)){ this.label = "Location"; }
		else if(name.equals(FN_LATITUDE)){ this.label = "Latitude"; }
		else if(name.equals(FN_LONGITUDE)){ this.label = "Longitude"; }
		else if(name.equals(FN_COMMENT)){ this.label = "Comments"; }
	}
	
	private void setType()
	{
		if(name.equals(FN_OFFICER_ID) || name.equals(FN_DATE_TIME) || name.equals(FN_LICENSE) || name.equals(FN_VIN))
		{ 
			this.type = CiteField.TYPE_STANDARD; 
		}
		else
		{
			this.type = CiteField.TYPE_CODES;
		}
	}
}
