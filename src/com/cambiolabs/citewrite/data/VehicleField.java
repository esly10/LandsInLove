package com.cambiolabs.citewrite.data;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class VehicleField extends Field
{
	public VehicleField()
	{
		super();
	}
	
	public VehicleField(String label, String type)
	{
		super(label, type);
	}
	
	public VehicleField(String name, String label, String type)
	{
		super(name, label, type);
	}
		
	public VehicleField(Element field)
	{
		super(field);
	}
	
	public VehicleField(HttpServletRequest request)
	{
		super(request);
	}
}
