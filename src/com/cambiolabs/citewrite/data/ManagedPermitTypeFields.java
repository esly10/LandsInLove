package com.cambiolabs.citewrite.data;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class ManagedPermitTypeFields extends Fields<ManagedPermitTypeField>
{
	public ManagedPermitTypeFields()
	{
		super("PERMIT_TYPE_FIELDS_XML", "permit-type-fields");
	}

	@Override
	public ArrayList<ManagedPermitTypeField> getDefaultFields()
	{
		return new ArrayList<ManagedPermitTypeField>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String name, String label, String type)
	{
		return (T)new ManagedPermitTypeField(name, label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String label, String type)
	{
		return (T)new ManagedPermitTypeField(label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(Element e)
	{
		return (T)new ManagedPermitTypeField(e);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(HttpServletRequest request)
	{
		return (T)new ManagedPermitTypeField(request);
	}
}
