package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class CitationOwner extends DBObject
{
	@Expose public int citation_owner_id = 0;
	@Expose public String status = "Inactive"; //Active, Inactive, Pending
	@Expose public int type_id = 0;
	@Expose public String first_name = null;
	@Expose public String last_name = null;
	@Expose public String email = null;
	@Expose public String home_phone = null;
	@Expose public String mobile_phone = null;
	@Expose public String address = null;
	@Expose public String city = null;
	@Expose public String state_id = null;
	@Expose public String zip = null;
	@Expose public int citation_id = 0;
		
	public CitationOwner()
	{
		super("citation_owner", "citation_owner_id");
	}
	
	public CitationOwner(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.citation_owner_id = id;
			this.status = null;
			this.populate();
		}
	}
	
	public CitationOwner(int id, int ownerId) throws UnknownObjectException
	{
		this();

		this.citation_owner_id = ownerId;
		if(id > 0)
		{
			this.citation_owner_id = id;
			this.populate();
		}
	}
	
	@Override
	public boolean commit()
	{
		return super.commit();
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
				conn.execute("DELETE from citation_owner where owner_id="+this.citation_owner_id);
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
	
	public int getOwnerId()
	{
		return citation_owner_id;
	}

	public String getStatus()
	{
		return status;
	}

	public int getCitationId()
	{
		return citation_id;
	}

	public int getTypeId()
	{
		return type_id;
	}
	
	public OwnerType getOwnerType()
	{
		try {
			return new OwnerType(this.type_id);
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getType()
	{
		OwnerType type = this.getOwnerType();
		if(type != null)
		{
			return type.name;
		}
		return "Unknown";
	}

	public String getFirstName()
	{
		return first_name;
	}

	public String getLastName()
	{
		return last_name;
	}

	public String getEmail()
	{
		return email;
	}

	public String getHomePhone()
	{
		return home_phone;
	}

	public String getMobilePhone()
	{
		return mobile_phone;
	}

	public String getAddress()
	{
		return address;
	}

	public String getCity()
	{
		return city;
	}

	public String getStateId()
	{
		return state_id;
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
	

	public String getZip()
	{
		return zip;
	}

}