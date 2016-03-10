package com.cambiolabs.citewrite.data;

import org.w3c.dom.Element;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class VehicleAttribute extends DBObject
{
	@Expose public int vehicle_attr_id = 0;
	@Expose public int vehicle_id = 0;
	@Expose public String name = null;
	@Expose public String value = null;
	
	public boolean dirty = false;
	
	public VehicleAttribute()
	{
		super("vehicle_attribute", "vehicle_attr_id", (new String[]{"dirty"}));
		this.dirty = true;
	}
	
	public VehicleAttribute(String name)
	{
		this(name, "");
	}
	
	public VehicleAttribute(String name, String value)
	{
		this();
		this.name =name;
		this.value=value;
	}
	
	public VehicleAttribute(int attr_id)  throws UnknownObjectException
	{
		this();
		
		if(attr_id > 0)
		{
			this.vehicle_attr_id = attr_id;
			this.populate();
			this.dirty = false;
		}
	}
	
	public VehicleAttribute(Element attr)
	{
		this();
		
		this.name = attr.getAttribute("name");
		this.value = Xml.getElementContent(attr);
	}

	public void setValue(String value)
	{
		this.dirty = true;
		this.value = value;
	}
}
