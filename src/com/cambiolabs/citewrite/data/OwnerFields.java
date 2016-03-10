package com.cambiolabs.citewrite.data;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class OwnerFields extends Fields<OwnerField>
{
	public OwnerFields()
	{
		super("OWNER_FIELDS_XML", "owner-fields");
	}
	
	public ArrayList<OwnerField> getDefaultFields()
	{
		if(this.defaultFields == null)
		{			
			this.defaultFields = new ArrayList<OwnerField>();
			
			this.defaultFields.add(new OwnerField("First Name", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Last Name", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Email", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Username", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Password", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Home Phone", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Mobile Phone", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Address", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("City", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("State", OwnerField.TYPE_CODES));
			this.defaultFields.add(new OwnerField("Zip", OwnerField.TYPE_STANDARD));
			this.defaultFields.add(new OwnerField("Status", OwnerField.TYPE_STANDARD));
		}
				
		return this.defaultFields;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String name, String label, String type)
	{
		return (T)new OwnerField(name, label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String label, String type)
	{
		return (T)new OwnerField(label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(Element e)
	{
		return (T)new OwnerField(e);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(HttpServletRequest request)
	{
		return (T)new OwnerField(request);
	}
	
}
