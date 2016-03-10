package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.DateParser;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.annotations.Expose;

public class CitationAppeal extends DBObject
{
	public static final String CAS_NEW = "New";
	public static final String CAS_UNDER_REVIEW = "Under Review";
	public static final String CAS_UPHELD = "Upheld";
	public static final String CAS_DISMISSED = "Dismissed";
	
	@Expose public int citation_appeal_id = 0;
	@Expose public int citation_id = 0;
	@Expose public Timestamp appeal_date = null;
	@Expose public String status = CAS_NEW;
	@Expose public String name = null;
	@Expose public String email = null;
	@Expose public String phone = null;
	@Expose public String address = null;
	@Expose public String city = null;
	@Expose public String state_id = null;
	@Expose public String zip = null;
	@Expose public String reason = null;
	@Expose public Timestamp decision_date = null;
	@Expose public String decision_reason = null;
	@Expose public String officer_notes = null;
	
	public CitationAppeal()
	{
		super("citation_appeal", "citation_appeal_id");
	}
	
	public CitationAppeal(int id)  throws UnknownObjectException
	{
		super("citation_appeal", "citation_appeal_id");
		
		if(id > 0)
		{
			this.citation_appeal_id = id;
			this.status = null;
			this.populate();
		}
	}
	
	public CitationAppeal(HttpServletRequest request) throws UnknownObjectException
	{
		this();
		
		this.citation_id = Integer.parseInt(request.getParameter("citation_id"));
		this.name = request.getParameter("appeal_name");
		this.email = request.getParameter("appeal_email");
		this.phone = request.getParameter("appeal_phone");
		this.address = request.getParameter("appeal_address");
		this.city = request.getParameter("appeal_city");
		this.state_id = request.getParameter("appeal_state_id");
		this.zip = request.getParameter("appeal_zip");
		this.reason = request.getParameter("appeal_reason");
		
	}
	
	public static CitationAppeal getCitationAppeal(int id)
	{
		try
		{
			CitationAppeal rv = new CitationAppeal();
			rv.citation_appeal_id = id;
			rv.status = null;
			rv.populate();
			
			return rv;
		}
		catch(UnknownObjectException uoe){}
		
		return null;
	}
	
	public static CitationAppeal getByCitationId(int citationId)
	{
		try
		{
			CitationAppeal rv = new CitationAppeal();
			rv.citation_id = citationId;
			rv.status = null;
			rv.populate();
			
			return rv;
		}
		catch(UnknownObjectException uoe){}
		
		return null;
	}

	public boolean commit()
	{
		if(this.citation_appeal_id == 0) //new
		{
			this.appeal_date = new Timestamp(System.currentTimeMillis());
			this.decision_date = this.appeal_date;
			this.decision_reason = "";
		}
		
		return super.commit();
	}

	public String getStatus() {
		return status;
	}
	
	
	
	public Timestamp getAppealDate() {
		return appeal_date;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<CitationAppealNote> getNotes()
	{
		CitationAppealNote note = new CitationAppealNote();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("citation_appeal_id", "=", this.citation_appeal_id));
		return (ArrayList<CitationAppealNote>)note.get(0, 0, "create_date DESC", filter);
	}
	
	public String getAppealDateFormat()
	{
	return DateParser.toString(this.appeal_date, null);
	}

	public int getCitationAppealId() {
		return citation_appeal_id;
	}

	public String getReason() {
		return reason;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state_id;
	}
	
	public String getZip() {
		return zip;
	}
	
	public Timestamp getDecisionDate() {
		return decision_date;
	}
	
	public String getDecisionDateFormat() 
	{
		return DateParser.toString(this.decision_date, null);
	}

	public String getDateFormat() 
	{
		return DateParser.toString(this.appeal_date, null);
	}
	
	public String getDecisionReason() {
		return decision_reason;
	}
	
	public String getOfficerNotes() {
		return officer_notes;
	}

	public CitationAppeal validate() throws Exception
	{
		if(this.name == null || this.name.length() == 0 ||
				this.email == null || this.email.length() == 0 ||
						this.address == null || this.address.length() == 0 ||
								this.city == null || this.city.length() == 0 ||
										this.state_id == null || this.state_id.length() == 0 ||
												this.zip == null || this.zip.length() == 0 ||
														this.reason == null || this.reason.length() == 0 )
				
		{
			throw new Exception("All fields are Required.");
		}
		else
		{
			if(! Util.isPhone(this.phone))
			{
				throw new Exception("Please enter a valid Phone Number.");
			}
			
			if(! Util.isEmail(this.email))
			{
				throw new Exception("Please enter a valid Email.");
			}
			
			if(! Util.isZip(this.zip))
			{
				throw new Exception("Please enter a valid Zip.");
			}
		}
		
		return this;
	}
	
	
}

