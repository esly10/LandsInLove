package com.cambiolabs.citewrite.data;

import com.google.gson.annotations.Expose;

public class FieldOption
{
	@Expose public String id = null;
	@Expose public String name = null;
	@Expose public boolean isOther = false;
	
	public FieldOption(String id, String name)
	{
		this(id, name, false);
	}
	
	public FieldOption(String id, String name, boolean isOther)
	{
		this.id = id;
		this.name = name;
		this.isOther = isOther;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isOther()
	{
		return isOther;
	}

	public void setOther(boolean isOther)
	{
		this.isOther = isOther;
	}	
}
