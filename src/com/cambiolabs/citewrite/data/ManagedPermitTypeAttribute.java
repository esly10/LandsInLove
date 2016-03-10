package com.cambiolabs.citewrite.data;

import org.w3c.dom.Element;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class ManagedPermitTypeAttribute extends DBObject
{
	@Expose public int mpermit_type_attr_id = 0;
	@Expose public int mpermit_type_id = 0;
	@Expose public String name = null;
	@Expose public String value = null;
	
	public boolean dirty = false;
	
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public ManagedPermitTypeAttribute()
	{
		super("mpermit_type_attribute", "mpermit_type_attr_id", (new String[]{"dirty"}));
	}
	
	public ManagedPermitTypeAttribute(int mpermit_type_id, String name, String value)
	{
		super("mpermit_type_attribute", "mpermit_type_attr_id", (new String[]{"dirty"}));
		
		this.mpermit_type_id = mpermit_type_id;
		this.name = name;
		this.value = value;
	}
	
	public ManagedPermitTypeAttribute(String name)
	{
		this(0, name, null);
		this.dirty = true;
	}
	
	
	public ManagedPermitTypeAttribute(String name, String value)
	{
		this(0, name, value);
		this.dirty = true;
	}
	
	public ManagedPermitTypeAttribute(int mpermit_type_id)  throws UnknownObjectException
	{
		super("mpermit_type_attribute", "mpermit_type_id");
		
		if(mpermit_type_id > 0)
		{
			this.mpermit_type_id = mpermit_type_id;
			this.populate();
		}
	}
	
	public ManagedPermitTypeAttribute(Element attr)
	{
		super("mpermit_type_attribute", "mpermit_type_id");
		
		this.name = attr.getAttribute("name");
		this.value = Xml.getElementContent(attr);
	}

}
