package com.cambiolabs.citewrite.data;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class OwnerField extends Field
{
	public OwnerField()
	{
		super();
	}
	
	public OwnerField(String label, String type)
	{
		super(label, type);
	}
	
	public OwnerField(String name, String label, String type)
	{
		super(name, label, type);
	}
		
	public OwnerField(Element field)
	{
		super(field);
	}
	
	public OwnerField(HttpServletRequest request)
	{
		super(request);
	}
	
}
