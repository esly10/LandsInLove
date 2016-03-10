package com.cambiolabs.citewrite.data;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class ManagedPermitFields extends Fields<ManagedPermitField>
{
	public ManagedPermitFields()
	{
		super("PERMIT_FIELDS_XML", "permit-fields");
	}
	
	public ArrayList<ManagedPermitField> getDefaultFields()
	{
		if(this.defaultFields == null)
		{
			this.defaultFields = new ArrayList<ManagedPermitField>();
			
			this.defaultFields.add(new ManagedPermitField("Permit Number", ManagedPermitField.TYPE_STANDARD));
			this.defaultFields.add(new ManagedPermitField("Permit Type", ManagedPermitField.TYPE_STANDARD));
			this.defaultFields.add(new ManagedPermitField("Status", ManagedPermitField.TYPE_STANDARD));
			this.defaultFields.add(new ManagedPermitField("valid_end_date", "Expiration", ManagedPermitField.TYPE_STANDARD));
		}
		
		return this.defaultFields;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String name, String label, String type)
	{
		return (T)new ManagedPermitField(name, label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String label, String type)
	{
		return (T)new ManagedPermitField(label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(Element e)
	{
		return (T)new ManagedPermitField(e);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(HttpServletRequest request)
	{
		return (T)new ManagedPermitField(request);
	}
}
