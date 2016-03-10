package com.cambiolabs.citewrite.data;

import org.w3c.dom.Element;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class CitationAttribute extends DBObject
{
	@Expose public int attr_id = 0;
	@Expose public int citation_id = 0;
	@Expose public String field_ref = null;
	@Expose public String value_id = null;
	@Expose public String value = null;
	
	private boolean dirty = false;
	
	public CitationAttribute()
	{
		super("citation_attribute", "attr_id");
	}
	
	public CitationAttribute(String fieldRef)
	{
		this(fieldRef, null, null);
	}
	
	public CitationAttribute(String fieldRef, String valueId, String value)
	{
		this();
		this.field_ref = fieldRef;
		this.value_id = valueId;
		this.value = value;
		this.dirty = true;
	}
	
	public CitationAttribute(int attr_id)  throws UnknownObjectException
	{
		this();
		
		if(attr_id > 0)
		{
			this.attr_id = attr_id;
			this.populate();
		}
	}
	
	public CitationAttribute(Element attr)
	{
		this();
		
		this.field_ref = attr.getAttribute("name");
		this.value_id = attr.getAttribute("id");
		this.value = Xml.getElementContent(attr);
		this.dirty = true;
	}
	
	public void setFieldRef(String refName)
	{
		this.field_ref = refName;
		this.dirty = true;
	}

	public void setValue(String value)
	{
		this.setValue("", value);
	}
	
	public void setValue(String valueId, String value)
	{
		if(this.value_id == null || (valueId != null && !this.value_id.equals(valueId)))
		{
			this.value_id = valueId;
			this.dirty = true;
		}
		
		if(this.value == null || (value != null && !this.value.equals(value)))
		{
			this.value = value;
			this.dirty = true;
		}
	}
	
	public void setValue(FieldOption option)
	{
		this.setValue(option.id, option.name);
	}

	public boolean isDirty()
	{
		return this.dirty;
	}
}
