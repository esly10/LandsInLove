package com.cambiolabs.citewrite.data;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.catalina.util.FastDateFormat;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class CitationAppealNote extends DBObject
{
	@Expose public int citation_appeal_note_id = 0;
	@Expose public int citation_appeal_id = 0;
	@Expose public String created_by = null;
	@Expose public String updated_by = null;
	@Expose public String note = null;
	@Expose public String create_date = null;
	@Expose public String update_date = null;
	@Expose public int is_email = 0;
	@Expose public String to_email = null;
	@Expose public String subject_email = null;
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		
	public CitationAppealNote()
	{
		super("citation_appeal_note", "citation_appeal_note_id");
	}
	
	public CitationAppealNote(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.citation_appeal_note_id = id;
			this.populate();
		}
	}
	
	public CitationAppealNote(int id, int citationId) throws UnknownObjectException
	{
		this();

		this.citation_appeal_id = citationId;
		if(id > 0)
		{
			this.citation_appeal_note_id = id;
			this.populate();
		}
	}
	
	@Override
	public boolean commit()
	{
		return super.commit();
	}
	
	public void setUpdated(User user)
	{
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);				
		
		this.update_date = sdf.format(cal.getTime());
		this.updated_by = user.getName();
		
		if(this.citation_appeal_note_id == 0)
		{
			this.create_date = this.update_date;
			this.created_by = this.updated_by;
		}
	}

	public int getId()
	{
		return citation_appeal_note_id;
	}

	public String getCreatedBy()
	{
		return created_by;
	}

	public String getUpdatedBy()
	{
		return updated_by;
	}

	public String getNote()
	{
		return note.replace("\r\n", "<br/>").replace("\r", "<br/>").replace("\n", "");
	}

	public String getCreated()
	{
		
		return this.create_date;
	}
	
	public String getUpdated()
	{
		
		return this.update_date;
	
	}

	public int getIs_email() {
		return is_email;
	}

	public void setIs_email(int is_email) {
		this.is_email = is_email;
	}

	public String getTo() {
		return to_email;
	}

	public void setTo(String to) {
		this.to_email = to;
	}

	public String getSubject() {
		return subject_email;
	}

	public void setSubject(String subject) {
		this.subject_email = subject;
	}	
	
	
}
