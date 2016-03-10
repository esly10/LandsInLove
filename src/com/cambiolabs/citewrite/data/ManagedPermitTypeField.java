package com.cambiolabs.citewrite.data;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class ManagedPermitTypeField extends Field
{
	public ManagedPermitTypeField()
	{
		super();
	}
	
	public ManagedPermitTypeField(String label, String type)
	{
		super(label, type);
	}
	
	public ManagedPermitTypeField(String name, String label, String type)
	{
		super(name, label, type);
	}
	
	public ManagedPermitTypeField(HttpServletRequest request)
	{
		super(request);
	}
	
	public ManagedPermitTypeField(Element field)
	{
		super(field);
	}
}
