package com.cambiolabs.citewrite.data;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Element;

public class VehicleFields extends Fields<VehicleField>
{
	public VehicleFields()
	{
		super("VEHICLE_FIELDS_XML", "vehicle-fields");
	}
	
	public String getXML()
	{
		return this.item.text_value;
	}
	
	public ArrayList<VehicleField> getDefaultFields()
	{
		if(this.defaultFields == null)
		{			
			this.defaultFields = new ArrayList<VehicleField>();
			
			this.defaultFields.add(new VehicleField("License", Field.TYPE_STANDARD));
			this.defaultFields.add(new VehicleField("VIN", Field.TYPE_STANDARD));
			this.defaultFields.add(new VehicleField("Make", Field.TYPE_CODES));
			this.defaultFields.add(new VehicleField("Color", Field.TYPE_CODES));
			this.defaultFields.add(new VehicleField("State", Field.TYPE_CODES));
		}
				
		return this.defaultFields;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String name, String label, String type)
	{
		return (T)new VehicleField(name, label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(String label, String type)
	{
		return (T)new VehicleField(label, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(Element e)
	{
		return (T)new VehicleField(e);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Field>T factory(HttpServletRequest request)
	{
		return (T)new VehicleField(request);
	}
	
}
