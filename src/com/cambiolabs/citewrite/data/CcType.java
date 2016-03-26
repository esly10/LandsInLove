package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;
import com.cambiolabs.citewrite.db.DBObject;

public class CcType  extends DBObject{
	@Expose public int cc_type_id = 0;
	@Expose public String cc_type_description = null;
	
	public CcType() throws UnknownObjectException
	{
		this(0);
	}
	
	public CcType(int cc_type_id) throws UnknownObjectException
	{
		super("cc_type", "cc_type_id");
		if(cc_type_id > 0)
		{
			this.cc_type_id = cc_type_id;
			this.populate();
		}
	}
	
	public int getCc_type_id() {
		return cc_type_id;
	}
	public void setCc_type_id(int cc_type_id) {
		this.cc_type_id = cc_type_id;
	}
	public String getCc_type_description() {
		return cc_type_description;
	}
	public void setCc_type_description(String cc_type_description) {
		this.cc_type_description = cc_type_description;
	}
	
}
