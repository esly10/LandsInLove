package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Services extends DBObject
{	

	@Expose public int service_id = 0;
	@Expose public int service_type = 0;
	@Expose public String service_description = null;
	@Expose public String service_name = null;
	@Expose public int service_bill_assigned = 0;
	@Expose public int service_status = 0;
	@Expose public String service_rate_base = null;
	
	//private static final String UTF_8 = "UTF-8";
	
	public Services() throws UnknownObjectException
	{
		this(0);
	}
	
	public Services(int service_id) throws UnknownObjectException
	{
		super("services", "service_id");
		if(service_id > 0)
		{
			this.service_id = service_id;
			this.populate();
		}
	}
	public int getServiceID() {
		return service_id;
	}
	public void setServiceID(int service_id) {
		this.service_id = service_id;
	}
	public String getDescription() {
		return service_description;
	}
	public void setDescription(String service_description) {
		this.service_description = service_description;
	}
	public int getType() {
		return service_type;
	}
	public void setType(int service_type) {
		this.service_type = service_type;
	}
	int getStatus() {
		return service_status;
	}
	public void setStatus(int service_status) {
		this.service_status = service_status;
	}
	public void setRate(String service_rate_base) {
		this.service_rate_base = service_rate_base;
	}
	public String getRate() {
		return service_rate_base;
	}
	public String getName() {
		return service_name;
	}
	public void setName(String service_name) {
		this.service_name = service_name;
	}
	public int getBilling() {
		return service_bill_assigned;
	}
	public void setBilling(int service_bill_assigned) {
		this.service_bill_assigned = service_bill_assigned;
	}
		
}
