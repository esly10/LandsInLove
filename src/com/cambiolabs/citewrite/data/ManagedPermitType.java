package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.annotations.Expose;

public class ManagedPermitType extends DBObject
{
	public static String PERIOD_RELATIVE = "relative";
	public static String PERIOD_ABSOLUTE = "absolute";
	
	@Expose public int mpermit_type_id = 0;
	@Expose public String name = null;
	@Expose public String description = null;
	@Expose public int max_vehicles = 0; //zero is unlimited
	@Expose public String period_type = null;
	@Expose public Timestamp period_start_date = null;
	@Expose public Timestamp period_end_date = null;
	@Expose public int period_days = 0;
	@Expose public float cost = 0;
	@Expose public int requires_shipping = 0;
	@Expose public int can_pickup = 0;
	@Expose public int can_print = 0;
	@Expose protected ArrayList<ManagedPermitTypeAttribute> extra = null;

	public ManagedPermitType()
	{
		super("mpermit_type", "mpermit_type_id", (new String[]{"extra"}));
	}
	
	public ManagedPermitType(int mpermit_type_id)  throws UnknownObjectException
	{
		super("mpermit_type", "mpermit_type_id", (new String[]{"extra"}));
		
		if(mpermit_type_id > 0)
		{
			this.mpermit_type_id = mpermit_type_id;
			this.populate();
		}
	}

	public ManagedPermitType(String[] ignore)
	{
		super("mpermit_type", "mpermit_type_id", ignore);
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtra()
	{
		if(this.mpermit_type_id == 0)
		{
			this.extra = new ArrayList<ManagedPermitTypeAttribute>();
			return;
		}
		
		//check if there are any options
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("mpermit_type_id", "=", this.mpermit_type_id));
		
		ManagedPermitTypeAttribute attr = new ManagedPermitTypeAttribute();

		this.extra = (ArrayList<ManagedPermitTypeAttribute>)attr.get(0, 0, "", filter);
	}
	
	public void setExtra(ArrayList<ManagedPermitTypeAttribute> extra)
	{
		this.extra = extra;
	}
	
	public ManagedPermitTypeAttribute getAttribute(String name)
	{
		if(this.extra == null)
		{
			this.loadExtra();
		}
	
		for(ManagedPermitTypeAttribute attr: this.extra)
		{
			if(attr.name.equals(name))
			{
				return attr;
			}
		}
		
		return null;
	}
	
	public void addAttribute(ManagedPermitTypeAttribute attr)
	{
		if(this.extra == null)
		{
			this.loadExtra();
		}
	
		if(attr.mpermit_type_id == 0) //its a new attribute
		{
			this.extra.add(attr);
			return;
		}
		
		int size = this.extra.size();
		for(int i = 0; i < size; i++)
		{
			ManagedPermitTypeAttribute a = this.extra.get(i);
			if(a.mpermit_type_id == attr.mpermit_type_id)
			{
				this.extra.set(i, attr);
				return;
			}
		}
	}
	
	public boolean commit()
	{
		boolean rv = super.commit();
		
		if(rv && this.extra != null)
		{
			for(ManagedPermitTypeAttribute attr: this.extra)
			{
				if(attr.dirty)//only save dirty 
				{
					attr.mpermit_type_id = this.mpermit_type_id;
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
	
	public boolean delete()
	{
		super.delete();
		
		DBConnection conn = null;
		try 
		{	
			conn = new DBConnection();
			
			conn.execute("delete from mpermit_type_attribute where mpermit_type_id="+this.mpermit_type_id);
			
			return true;
		} 
		catch (Exception e) 
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
		
		return false;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}
	
	public int getId()
	{
		return mpermit_type_id;
	}
	
	public float getCost()
	{
		return cost;
	}
	
	public String getFormatCost()
	{
		
		return String.format("$%.02f", cost);
	}
	
	public int getMaxVehicles() {
		return max_vehicles;
	}

	public boolean requiresShipping()
	{
		return (this.requires_shipping == 1);
	}
	
	public boolean can_pickup()
	{
		return (this.can_pickup == 1);
	}
	
	public boolean can_print()
	{
		return (this.can_print == 1);
	}

	public Timestamp getStartDate()
	{
		if(this.period_type.equals(PERIOD_ABSOLUTE))
		{
			return this.period_start_date;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
	
		return new Timestamp(cal.getTimeInMillis());
	}
	
	public Timestamp getEndDate()
	{
		if(this.period_type.equals(PERIOD_ABSOLUTE))
		{
			return this.period_end_date;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.add(Calendar.DAY_OF_MONTH, this.period_days);
	
		return new Timestamp(cal.getTimeInMillis());
	}
	
	public ArrayList<OwnerType> getOwnerTypes()
	{
		ArrayList<OwnerType> rv = new ArrayList<OwnerType>();
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare("select owner_type_id from mpermit_type_to_owner_type where mpermit_type_id=?");
			pst.setInt(1, this.mpermit_type_id);
			
			if(connection.query(pst))
			{
				ResultSet rs = connection.getResultSet();
				while(rs.next())
				{
					try
					{
						OwnerType ot = new OwnerType(rs.getInt(1));
						rv.add(ot);
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
	
	public boolean setOwnerTypes(ArrayList<OwnerType> types)
	{
		boolean rv = false;
		
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			
			conn.execute("DELETE from mpermit_type_to_owner_type where mpermit_type_id="+this.mpermit_type_id);
			
			PreparedStatement pst = conn.prepare("INSERT INTO mpermit_type_to_owner_type (mpermit_type_id, owner_type_id) VALUES (?,?)");
			if(pst != null)
			{
				for(OwnerType type: types)
				{
					pst.setInt(1, this.mpermit_type_id);
					pst.setInt(2, type.owner_type_id);
					
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
	
	public static ArrayList<ManagedPermitType> getByOwnerType(int typeId)
	{	
		
		return getByOwnerType(typeId,false);
	}
	
	public static ArrayList<ManagedPermitType> getByOwnerType(int typeId ,boolean excludeExpired)
	{		
		ArrayList<ManagedPermitType> rv = new ArrayList<ManagedPermitType>();
		DBConnection conn = null;
		try
		{
			QueryBuilder qb = new QueryBuilder("mpermit_type");
			qb.field("DISTINCT mpermit_type.mpermit_type_id").field("name");
			qb.join("mpermit_type_to_owner_type", "mpermit_type_to_owner_type.mpermit_type_id=mpermit_type.mpermit_type_id");
						
			DBFilterList filter = new DBFilterList();
			filter.add( new DBFilter("mpermit_type_to_owner_type.owner_type_id", "=", typeId));
			if(excludeExpired)
			{
				DBFilter typefilter = new DBFilter("period_type", "=", PERIOD_RELATIVE);
				filter.add(typefilter);
				filter.addOr(typefilter, new DBFilter("period_end_date", ">", new DateParser(new Date()).firstHour().getTimestamp()));
			
			}
			
			conn = qb.orderBy("name").orderDir("ASC").where(filter).query();
			
			ResultSet rs = conn.getResultSet();
			while(rs.next())
			{
				try
				{
					rv.add(new ManagedPermitType(rs.getInt(1)));
				}
				catch(UnknownObjectException uoe){}
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
}
