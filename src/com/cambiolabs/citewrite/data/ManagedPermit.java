package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.Invoice;
import com.cambiolabs.citewrite.util.PermitWatcher;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.annotations.Expose;

public class ManagedPermit extends DBObject
{
	public final String STATUS_ACTIVE = "Active";
	public final String STATUS_EXPIRED = "Expired";
	public final String STATUS_REVOKED = "Revoked";
	public final String STATUS_PENDING = "Pending";
	public final String STATUS_VOIDED = "Voided";
	public final String STATUS_CANCELED = "Canceled";
	
	@Expose public int mpermit_id = 0;
	@Expose public int owner_id = 0;
	@Expose public String permit_number = null;
	@Expose public String status = STATUS_ACTIVE;
	@Expose public int mpermit_type_id = 0;	
	@Expose public Timestamp valid_start_date = null;
	@Expose public Timestamp valid_end_date = null;
	@Expose public Timestamp create_date = null;
	@Expose public Timestamp update_date = null;
	
	
	@Expose protected ArrayList<ManagedPermitAttribute> extra = new ArrayList<ManagedPermitAttribute>();

	public ManagedPermit()
	{
		super("mpermit", "mpermit_id");
	}

	public ManagedPermit(int id) throws UnknownObjectException
	{
		this(id, 0);
	}
	
	public ManagedPermit(int id, int ownerId) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.mpermit_id = id;
			this.owner_id = ownerId;
			this.status = null;
			this.populate();
		}
	}
	
	private static Object _permitNumberLock = new Object();
	
	@Override
	public boolean commit()
	{
		if(this.mpermit_id == 0) //new
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
			this.create_date = this.update_date;
		}
		else//update
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
		}
		
		//check if we need to generate number
		if(this.permit_number == null || this.permit_number.length() == 0)
		{
			synchronized (_permitNumberLock)
			{
				ConfigItem ciNumber = null;
				try
				{
					ciNumber = new ConfigItem("PERMIT_NUMBER");
				}
				catch(UnknownObjectException e)
				{
					ciNumber = new ConfigItem();
					ciNumber.name = "PERMIT_NUMBER";
					ciNumber.int_value = 0;
					ciNumber.text_value = "";
				}
				
				ConfigItem ciFormat = null;
				try
				{
					ciFormat = new ConfigItem("PERMIT_NUMBER_FORMAT");
				}
				catch(UnknownObjectException e)
				{
					ciFormat = new ConfigItem();
					ciFormat.name = "PERMIT_NUMBER_FORMAT";
					ciFormat.text_value = "%XXXXX%";
				}
				
				this.permit_number = Util.numberFormat(ciFormat.text_value, ciNumber.int_value);
				ciNumber.int_value += 1;
				ciNumber.commit();
			}
		}
		
		
		boolean rv = super.commit();
		
		if(rv && this.extra != null)
		{
			for(ManagedPermitAttribute attr: this.extra)
			{
				if(attr.isDirty())//only save dirty 
				{
					attr.mpermit_id = this.mpermit_id;
					attr.commit();
				}
			}
		}
		
		if(rv){
			PermitWatcher.update();
		}
		
		this.extra = null; //reload this
		this.loadExtra();
		
		return rv;
	}
	
	public static int getNextPermitNumber()
	{
		try
		{
			ConfigItem ci = new ConfigItem("PERMIT_NUMBER");
			return ci.int_value;
		}
		catch(UnknownObjectException e){}
		
		return -1; //error
	}
	
	public static boolean setNextPermitNumber(int next)
	{
		
		ConfigItem ciNumber = null;
		try
		{
			ciNumber = new ConfigItem("PERMIT_NUMBER");
		}
		catch(UnknownObjectException e)
		{
			ciNumber = new ConfigItem();
			ciNumber.name = "PERMIT_NUMBER";
			ciNumber.text_value = "";
		}
		
		ciNumber.int_value = next;

		return ciNumber.commit();
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
				
				conn.execute("DELETE from mpermit_attribute where mpermit_id="+this.mpermit_id);
				conn.execute("DELETE from mpermit_to_vehicle where mpermit_id="+this.mpermit_id);
				
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
	
	public boolean setVehicles(ArrayList<Vehicle> vehicles)
	{
		boolean rv = false;
		
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			
			conn.execute("DELETE from mpermit_to_vehicle where mpermit_id="+this.mpermit_id);
			
			PreparedStatement pst = conn.prepare("INSERT INTO mpermit_to_vehicle (mpermit_id, vehicle_id) VALUES (?,?)");
			if(pst != null)
			{
				for(Vehicle vehicle: vehicles)
				{
					pst.setInt(1, this.mpermit_id);
					pst.setInt(2, vehicle.vehicle_id);
					
					conn.execute(pst);
				}
				
				return true;
			}
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
		
		return rv;
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtra()
	{
		//check if there are any options
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("mpermit_id", "=", this.mpermit_id));
		
		ManagedPermitAttribute attr = new ManagedPermitAttribute();

		this.extra = (ArrayList<ManagedPermitAttribute>)attr.get(0, 0, "", filter);
	}
	
	public void addAttribute(ManagedPermitAttribute attr)
	{
		this.extra.add(attr);
	}
	
	public ManagedPermitAttribute getAttribute(String name)
	{
		for(ManagedPermitAttribute va: this.extra)
		{
			if(va.name.equals(name))
			{
				return va;
			}
		}
		
		return null;
	}
	
	public String getAttributeValue(ManagedPermitField field)
	{
		ManagedPermitAttribute va = this.getAttribute(field.name);
		
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
	
	public String getCreated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.create_date);
		}
		
		return "";
	}
	
	
	public int getMpermitId() {
		return mpermit_id;
	}
	
	

	public int getMpermitTypeId() {
		return mpermit_type_id;
	}

	public String getUpdated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.update_date);
		}
		
		return "";
	}
	
	public void setValidity()
	{
		ManagedPermitType type = this.getType();
		this.valid_start_date = type.getStartDate();
		this.valid_end_date = type.getEndDate();
	}
	
	public String getValidStart()
	{
		if(this.valid_start_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy";
			return new SimpleDateFormat(dateFormat).format(this.valid_start_date);
		}
		
		return "";
	}
	
	public void setValidStartDate()
	{
		this.setValidStartDate(null, null);
	}
	
	public void setValidStartDate(String strStart)
	{
		this.setValidStartDate(strStart, null);
	}
	
	public void setValidStartDate(String strStart, String format)
	{
		if(strStart != null && strStart.length() > 0)
		{
			if(format == null)
			{
				format = "MM/dd/yyyy";
			}
			
			DateFormat df = new SimpleDateFormat(format);
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(strStart, new ParsePosition(0)));
			
			this.valid_start_date = new Timestamp(cal.getTimeInMillis());
		}
		else
		{
			this.valid_start_date = this.getType().getStartDate();
		}
	}
	
	public void setValidEndDate()
	{
		this.setValidEndDate(null, null);
	}
	
	public void setValidEndDate(String strEnd)
	{
		this.setValidEndDate(strEnd, null);
	}
	
	public void setValidEndDate(String strEnd, String format)
	{
		if(strEnd != null && strEnd.length() > 0)
		{
			if(format == null)
			{
				format = "MM/dd/yyyy";
			}
			
			DateFormat df = new SimpleDateFormat(format);
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(strEnd, new ParsePosition(0)));
			
			this.valid_end_date = new Timestamp(cal.getTimeInMillis());
		}
		else
		{
			this.valid_end_date = this.getType().getStartDate();
		}
	}
	
	public String getValidEnd()
	{
		if(this.valid_end_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy";
			return new SimpleDateFormat(dateFormat).format(this.valid_end_date);
		}
		
		return "";
	}
	
	public String getPermitNumber()
	{
		return permit_number;
	}

	public String getStatus()
	{
		return status;
	}

	public void setPermitType(ManagedPermitType type)
	{
		this.mpermit_type_id = type.mpermit_type_id;
		
	}
	public ManagedPermit setPermit(HttpServletRequest request) throws Exception 
	{
		ManagedPermitFields pfields = new ManagedPermitFields();
		
		for(ManagedPermitField field: pfields.getFields())
		{
			ManagedPermitAttribute attr = new ManagedPermitAttribute(field.name);
			String param = request.getParameter("mpermit-extra-" + field.name);
			if(param != null && param.length() > 0)
			{
				attr.setValue(param);
			}
			else
			{
				attr.setValue("");
			}
			
			this.addAttribute(attr);
		}
		
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(); //reset the list
		
		Owner owner = Owner.getCurrentOwner();
		
		ArrayList<Vehicle> ownerVehicles = owner.getVehicles();
		for(Vehicle vehicle: ownerVehicles)
		{
			String on = request.getParameter("mpermit-vehicle-"+vehicle.vehicle_id);
			if(on != null && on.equals("on"))
			{
				vehicles.add(vehicle);
			}
		}
		
		ManagedPermitType type = this.getType();
		if (type.max_vehicles < vehicles.size()) 
		{
			String msg = "Only " + type.max_vehicles + " vehicle";
			if(type.max_vehicles > 1)
			{
				msg += "s ";
			}
			msg += " can be associated with this permit.";
			
			throw new Exception(msg);
		}
		else
		{
			this.setVehicles(vehicles);
		}
	
		
		
		return this;
	}
	
	public ManagedPermitType getType()
	{
		if(this.mpermit_type_id > 0)
		{
			try
			{
				ManagedPermitType mpt = new ManagedPermitType(this.mpermit_type_id);
				mpt.loadExtra();
				return  mpt;
			}
			catch(UnknownObjectException uoe){}
		}
		
		return null;
	}
	
	public String getTypeName()
	{
		String name;
		String description;
		ManagedPermitType mPT = this.getType();
		
		if (mPT==null)
		{
			name = "";
			description = "";
		}else
		{
			name = mPT.name;
			description = mPT.description;
		}
		String type = name + " - " + description;
		return type;
		
	}
	public String getTypeCost()
	{
		String cost;
		
		ManagedPermitType mPT = this.getType();
		
		if (mPT==null)
		{
			cost = "";
			
		}else
		{
			cost = mPT.getFormatCost();
			
		}
		
		return  cost;
		
	}
	
	public Boolean getRequiresShipping()
	{
		ManagedPermitType mPT = this.getType();

		if (mPT == null)
		{
			return false;
		}

		return mPT.requiresShipping();
	}
	
	public Boolean getCanPrint()
	{
		ManagedPermitType mPT = this.getType();
		return mPT.can_print();
	}
	

	
	public boolean validate ()
	{
		Calendar cal = Calendar.getInstance();
	  Timestamp now = new Timestamp(cal.getTimeInMillis());
		
		
		if(now.before(this.valid_end_date) && status.equals(this.STATUS_ACTIVE) ) 
		{
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public ArrayList<Vehicle> getVehicles()
	{
		ArrayList<Vehicle> rv = new ArrayList<Vehicle>();
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare("select vehicle_id from mpermit_to_vehicle where mpermit_id=?");
			pst.setInt(1, this.mpermit_id);
			
			if(connection.query(pst))
			{
				ResultSet rs = connection.getResultSet();
				while(rs.next())
				{
					try
					{
						Vehicle v = new Vehicle(rs.getInt(1));
						v.loadExtra();
						rv.add(v);
					}
					catch(UnknownObjectException obe){}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		
		
			return rv;
		
	
		
	}
	
	
	
	
	public ArrayList<ManagedPermitAttribute> getExtra() {
		return extra;
	}

	public static ManagedPermit getByPermitNumber(String permitNumber)
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			ManagedPermit rv = new ManagedPermit();
			rv.status = null;
			rv.permit_number = permitNumber;
			
			if(conn.lookup(rv))
			{
				return rv;
			}
			
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
		return null;
	}
	
	public Invoice getInvoice()
	{
		if(this.mpermit_id > 0)
		{
			return Invoice.getByReference(this.mpermit_id, Invoice.TYPE_PERMIT);
		}
		
		return null;
	}
	
	public ManagedPermit validateP() throws Exception
	{
		Owner owner = Owner.getCurrentOwner();
		ManagedPermitType type = this.getType();
		
		boolean found = false;
		ArrayList<OwnerType> ownerTypeList = type.getOwnerTypes();
		for(OwnerType ownerType: ownerTypeList)
		{
			if (ownerType.owner_type_id == owner.type_id) 
			{
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			throw new Exception("You cannot update this permit.");
		}
		
		if (type.max_vehicles < this.getVehicles().size()) 
		{
			String msg = "Only " + type.max_vehicles + " vehicle";
			if(type.max_vehicles > 1)
			{
				msg += "s ";
			}
			msg += " can be associated with this permit.";
			
			throw new Exception(msg);
		}
		
		ManagedPermitFields pfields = new ManagedPermitFields();
		for(ManagedPermitField field: pfields.getFields())
		{
			ManagedPermitAttribute attr = this.getAttribute(field.name);
			if(field.required && attr.value.length() == 0)
			{
				throw new Exception(field.label + " is required.");
			}
			if(field.validation.length()!= 0 && !Util.isValid(attr.value, field.validation))
			{
				throw new Exception(field.label + " malformat.");
			}
		}
		
		return this;
	}
}
