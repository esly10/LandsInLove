package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Agencies extends DBObject
{	

	@Expose public int agency_id = 0;
	@Expose public String agency_name = null;
	@Expose public String agency_identification = null;
	@Expose public int agency_type = 0;
	@Expose public String agency_address = null;
	@Expose public String agency_zip = null;
	@Expose public String agency_web_site = null;
	@Expose public int agency_country = 0;
	@Expose public String agency_email = null;
	@Expose public String agency_phone = null;
	@Expose public String agency_fax = null;
	@Expose public String agency_notes = null;
	
	
	//private static final String UTF_8 = "UTF-8";
	
	public Agencies() throws UnknownObjectException
	{
		this(0);
	}
	
	public Agencies(int agency_id) throws UnknownObjectException
	{
		super("agencies", "agency_id");
		if(agency_id > 0)
		{
			this.agency_id = agency_id;
			this.populate();
		}
	}
	
	public int getAgencyID() {
		return agency_id;
	}
	public void setAgencyID(int agency_id) {
		this.agency_id = agency_id;
	}
	public String getAgency_name() {
		return agency_name;
	}
	public void setAgency_name(String agency_name) {
		this.agency_name = agency_name;
	}
	public String getDni() {
		return agency_identification;
	}
	public void setDni(String agency_identification) {
		this.agency_identification = agency_identification;
	}
	public int getType() {
		return agency_type;
	}
	public void setType(int agency_type) {
		this.agency_type = agency_type;
	}
	public String getAddress() {
		return agency_address;
	}
	public void setAddress(String agency_address) {
		this.agency_address = agency_address;
	}
	public String getZip() {
		return agency_zip;
	}
	public void setZip(String agency_zip) {
		this.agency_zip = agency_zip;
	}
	public String getWeb() {
		return agency_web_site;
	}
	public void setWeb(String agency_web_site) {
		this.agency_web_site = agency_web_site;
	}
	public int getCountry() {
		return agency_country;
	}
	public void setCountry(int agency_country) {
		this.agency_country = agency_country;
	}
	
	public String getAgency_email() {
		return agency_email;
	}
	public void setAgency_email(String agency_email) {
		this.agency_email = agency_email;
	}
	public String getAgency_phone() {
		return agency_phone;
	}
	public void setAgency_phone(String agency_phone) {
		this.agency_phone = agency_phone;
	}
	public String getFax() {
		return agency_fax;
	}
	public void setFax(String agency_fax) {
		this.agency_fax = agency_fax;
	}
	public String getNotes() {
		return agency_notes;
	}
	public void setNotes(String agency_notes) {
		this.agency_notes = agency_notes;
	}
		
}