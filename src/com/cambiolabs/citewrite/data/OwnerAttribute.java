package com.cambiolabs.citewrite.data;

import org.w3c.dom.Element;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class OwnerAttribute extends DBObject
{
	@Expose public int owner_attr_id = 0;
	@Expose public int owner_id = 0;
	@Expose public String name = null;
	@Expose public String value = null;
	
	public boolean dirty = false;
	
	public OwnerAttribute()
	{
		super("owner_attribute", "owner_attr_id", (new String[]{"dirty"}));
		this.dirty = true;
	}
	
	public OwnerAttribute(String name)
	{
		this(name, "");
	}
	
	public OwnerAttribute(String name, String value)
	{
		this();
		this.name =name;
		this.value=value;
	}
	
	public OwnerAttribute(int attr_id)  throws UnknownObjectException
	{
		super("owner_attribute", "owner_attr_id", (new String[]{"dirty"}));
		
		if(attr_id > 0)
		{
			this.owner_attr_id = attr_id;
			this.populate();
		}
	}
	
	public OwnerAttribute(Element attr)
	{
		super("owner_attribute", "owner_attr_id", (new String[]{"dirty"}));
		
		this.name = attr.getAttribute("name");
		this.value = Xml.getElementContent(attr);
	}
	
	public void setValue(String value)
	{
		this.dirty = true;
		this.value = value;
	}

}
