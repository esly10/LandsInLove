package com.cambiolabs.citewrite.data;

import java.util.Hashtable;

import com.cambiolabs.citewrite.db.Column;

public class HotList extends Permit
{
	static final long serialVersionUID = 2L;
	
	public HotList(Hashtable<String, Column> row) {
		super(row);
		this.type = PT_HOTLIST;
	}
		
	public HotList(String license, String state, String vin)
	{
		super(license, state, vin);
		this.type = PT_HOTLIST;
	}
	
}
