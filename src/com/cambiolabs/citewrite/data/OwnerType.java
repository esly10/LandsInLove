package com.cambiolabs.citewrite.data;


import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class OwnerType extends DBObject
{
	@Expose public int owner_type_id = 0;
	@Expose public String name = null; 
	@Expose public int requires_auth= 0;	
	@Expose public int active = 0;
	
	
	
	public OwnerType()
	{
		super("owner_type", "owner_type_id");
	}
	
	public OwnerType(String name)
	{
		super("owner_type", "owner_type_id");
		this.name = name;
	}
	
	public OwnerType(int id) throws UnknownObjectException
	{
		super("owner_type", "owner_type_id");
		
		if(id > 0)
		{
			this.owner_type_id = id;
			this.populate();
		}
	}
	
	public int getOwner_type_id() {
		return owner_type_id;
	}

	public void setOwner_type_id(int owner_type_id) 
	{
		this.owner_type_id = owner_type_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}
	
	public boolean requiresAuth() 
	{
		return (this.requires_auth == 1);
	}
}
