package com.cambiolabs.citewrite.data;

import org.w3c.dom.Element;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class ManagedPermitAttribute extends DBObject
{
	@Expose public int mpermit_attr_id = 0;
	@Expose public int mpermit_id = 0;
	@Expose public String name = null;
	@Expose public String value = null;
	
	private boolean dirty = false;
	
	public ManagedPermitAttribute()
	{
		super("mpermit_attribute", "mpermit_attr_id", (new String[]{"dirty"}));
		this.dirty = true;
	}
	
	public ManagedPermitAttribute(String name)
	{
		this(name, "");
	}
	
	public ManagedPermitAttribute(String name, String value)
	{
		this();
		this.name =name;
		this.value=value;
	}
	
	public ManagedPermitAttribute(int attr_id)  throws UnknownObjectException
	{
		this();
		
		if(attr_id > 0)
		{
			this.mpermit_attr_id = attr_id;
			this.populate();
			this.dirty = false;
		}
	}
	
	public ManagedPermitAttribute(Element attr)
	{
		this();
		
		this.name = attr.getAttribute("name");
		this.value = Xml.getElementContent(attr);
		this.dirty = false;
	}
	
	public void setValue(String value)
	{
		this.dirty = true;
		this.value = value;
	}
	
	public boolean isDirty()
	{
		return this.dirty;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	

}
