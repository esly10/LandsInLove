package com.cambiolabs.citewrite.data;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class ManagedPermitField extends Field
{
	public ManagedPermitField()
	{
		super();
	}
	
	public ManagedPermitField(String label, String type)
	{
		super(label, type);
	}
	
	public ManagedPermitField(String name, String label, String type)
	{
		super(name, label, type);
	}
	
	public ManagedPermitField(HttpServletRequest request)
	{
		super(request);
	}
	
	public ManagedPermitField(Element field)
	{
		super(field);
	}
}
