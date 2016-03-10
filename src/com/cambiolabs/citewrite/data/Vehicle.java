package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.CartException;
import com.cambiolabs.citewrite.util.PermitWatcher;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.annotations.Expose;

public class Vehicle extends DBObject
{
	@Expose public int vehicle_id = 0;
	@Expose public int owner_id = 0;
	@Expose public String license = null;
	@Expose public String vin = null;
	@Expose public String make_id = null;
	@Expose public String color_id = null;
	@Expose public String state_id = null;
	@Expose public Timestamp create_date = null;
	@Expose public Timestamp update_date = null;
	
	@Expose protected ArrayList<VehicleAttribute> extra = new ArrayList<VehicleAttribute>();
	
	public Vehicle()
	{
		super("vehicle", "vehicle_id");
	}
	
	public Vehicle(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.vehicle_id = id;
			this.populate();
		}
	}
	
	
	public Vehicle(HttpServletRequest request) throws Exception
	{
		this();
		
		this.vehicle_id = Integer.parseInt(request.getParameter("vehicle_id"));
		this.owner_id = Owner.getCurrentOwner().owner_id;
		this.license = request.getParameter("license");
		this.vin = request.getParameter("vin");
		this.color_id = request.getParameter("color");
		this.state_id = request.getParameter("state");
		this.make_id = request.getParameter("make");
		
		if(vin != null && !vin.equals("") &&  !validVinByOwner (this.vin, this.owner_id, this.vehicle_id )){
			throw new Exception("The VIN already exists in our system. Please review and try again.");
		}
		
		if(license != null && !license.equals("")  && state_id != null && !state_id.equals("")   &&  !validLicenseByOwner(this.state_id, this.license, this.vehicle_id )){
			throw new Exception("The License already exists in our system. Please review and try again.");
		}
		
		VehicleFields vfields = new VehicleFields();
		ArrayList<VehicleField> extrafieldsvehicle = vfields.getFields(false);
		
		for(VehicleField field: extrafieldsvehicle)
		{
			
			String value = request.getParameter(field.name);
			this.addAttribute(new VehicleAttribute(field.name,value));
			
		}
			
		
	}
	
	public Vehicle validate() throws Exception
	{
		
		if (vin.length() != 0  || license.length() != 0)
		{
			if (license.length() != 0) 
			{
				if(! Util.isLicense(license ))
				{
					throw new Exception("Please enter a valid License.");
				}
				if(vin.length() != 0  )
				{
						if(!Util.isVin(vin))
						{
							throw new Exception("Please enter a valid VIN.");
						}
				}
			}
			else if(vin.length() != 0  )
			{
					if(!Util.isVin(vin))
					{
						throw new Exception("Please enter a valid VIN.");
					}
					if (license.length() != 0) 
					{
						if(! Util.isLicense(license ))
						{
							throw new Exception("Please enter a valid License.");
						}
					}
			}
		
		}
		else 
		{
			throw new Exception("Please enter a license and/or a VIN for this vehicle.");
		}
		
		VehicleFields vfields = new VehicleFields();
		ArrayList<VehicleField> extrafieldsvehicle = vfields.getFields(false);
		
		for(VehicleField field: extrafieldsvehicle)
		{
			VehicleAttribute attr = this.getAttribute(field.name);
			
			if(field.required && attr.value.length() == 0)
			{
				throw new CartException(field.label + " is required.");
			}
			
			if(field.validation.length()!= 0 && !Util.isValid(attr.value, field.validation))
			{
				throw new CartException("Please enter a valid"+field.label);
			}
		}
		
		return this;
	}
	
	public Vehicle(int id, int ownerId) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.vehicle_id = id;
			this.owner_id = ownerId;
			this.populate();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtra()
	{
		//check if there are any options
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("vehicle_id", "=", this.vehicle_id));
		
		VehicleAttribute attr = new VehicleAttribute();

		this.extra = (ArrayList<VehicleAttribute>)attr.get(0, 0, "", filter);
	}
	
	@Override
	public boolean commit()
	{
		if(this.vehicle_id == 0) //new
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
			this.create_date = this.update_date;
		}
		else//update
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
		}
		boolean rv = super.commit();
		
		if(rv){
			PermitWatcher.update();
		}
		
		if(rv && this.extra != null)
		{
			for(VehicleAttribute attr: this.extra)
			{
				if(attr.dirty)//only save dirty 
				{
					attr.vehicle_id = this.vehicle_id;
					attr.commit();
				}
				else
				{
					attr.delete();
				}
			}
		}
		
		this.extra = null; //reload this
		this.loadExtra();
		
		return rv;
	}
	
	@Override
	public boolean delete()
	{
		boolean rv = super.delete();
		
		if(rv)
		{
			DBConnection conn = null;
			try
			{
				conn = new DBConnection();
				
				conn.execute("DELETE from vehicle_attribute where vehicle_id="+this.vehicle_id);
				conn.execute("DELETE from mpermit_to_vehicle where vehicle_id="+this.vehicle_id);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
		}
		
		return rv;
	}
	
	public ArrayList<VehicleAttribute> getExtras()
	{
		return this.extra;
	}
	
	public void addAttribute(VehicleAttribute attr)
	{
		if(this.extra == null)
		{
			this.loadExtra();
		}
	
		if(attr.vehicle_attr_id == 0) //its a new attribute
		{
			this.extra.add(attr);
			return;
		}
		
		int size = this.extra.size();
		for(int i = 0; i < size; i++)
		{
			VehicleAttribute a = this.extra.get(i);
			if(a.vehicle_attr_id == attr.vehicle_attr_id)
			{
				this.extra.set(i, attr);
				return;
			}
		}
	}
	
	public VehicleAttribute getAttribute(String name)
	{
		for(VehicleAttribute va: this.extra)
		{
			if(va.name.equals(name))
			{
				return va;
			}
		}
		
		return null;
	}
	
	public String getAttributeValue(VehicleField field)
	{
		VehicleAttribute va = this.getAttribute(field.name);
		
		if(va != null && va.value.length() > 0)
		{
			if(field.type.equals(OwnerField.TYPE_LIST))
			{
				for(FieldOption option: field.options)
				{
					if(option.id.equals(va.value))
					{
						return option.name;
					}
				}
			}
			else
			{
				return va.value;
			}
		}
		
		return "";
	}
	
	public String getState()
	{
		Code code = Codes.getCode(Code.CT_STATE, this.state_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;		
	}
	
	public String getCreated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.create_date);
		}
		
		return "";
	}
	
	public String getUpdated()
	{
		if(this.update_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.update_date);
		}
		
		return "";
	}
	
	public String getColor()
	{
		Code code = Codes.getCode(Code.CT_COLOR, this.color_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;
	}
	
	public String getMake()
	{
		Code code = Codes.getCode(Code.CT_MAKE, this.make_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;
	}

	public String getLicense()
	{
		return license;
	}

	public String getVin()
	{
		return vin;
	}
	
	public int getId()
	{
		return this.vehicle_id;
	}
	
	public boolean hasValidPermit()
	{
		
		Timestamp today = new Timestamp(System.currentTimeMillis());
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("mpermit.valid_start_date","<=",today));
		filter.add(new DBFilter("mpermit.valid_end_date",">=",today));
		filter.add(new DBFilter("mpermit_to_vehicle.vehicle_id","=", this.vehicle_id));
		
		QueryBuilder qb = new QueryBuilder("mpermit_to_vehicle");
		qb.field("count(*) total")
			.join("mpermit", "mpermit.mpermit_id=mpermit_to_vehicle.mpermit_id");
		
		ArrayList<Hashtable<String, String>> result = qb.where(filter).select();
		
		try
		{
			return (Integer.parseInt(result.get(0).get("total")) > 0);
		}
		catch(NumberFormatException nfe){}
		
		return false;
	}
	
	public static boolean validVinByOwner (String vin, int ownerId, int vehicleId)
	{
		
		DBFilterList filter = new DBFilterList();

		filter.add(new DBFilter("vehicle.vin","=", vin));
		filter.add(new DBFilter("vehicle.owner_id","=", ownerId));
		filter.add(new DBFilter("vehicle.vehicle_id","!=", vehicleId));
		QueryBuilder qb = new QueryBuilder("vehicle");
		qb.field("count(*) total");
			
		ArrayList<Hashtable<String, String>> result = qb.where(filter).select();
		
		try
		{
			return (Integer.parseInt(result.get(0).get("total")) == 0);
		}
		catch(NumberFormatException nfe){}
		
		return false;
	}
	
	public static boolean validLicenseByOwner (String state_id, String license, int vehicleId)
	{
		
		DBFilterList filter = new DBFilterList();

		filter.add(new DBFilter("vehicle.state_id","=", state_id));
		filter.add(new DBFilter("vehicle.license","=", license));
		filter.add(new DBFilter("vehicle.vehicle_id","!=", vehicleId));
		QueryBuilder qb = new QueryBuilder("vehicle");
		qb.field("count(*) total");
			
		ArrayList<Hashtable<String, String>> result = qb.where(filter).select();
		
		try
		{
			return (Integer.parseInt(result.get(0).get("total")) == 0);
		}
		catch(NumberFormatException nfe){}
		
		return false;
	}
	
	@Override
	public boolean equals(Object vehicle)
	{
		return this.vehicle_id == ((Vehicle)vehicle).vehicle_id;
	}
	

	
}
