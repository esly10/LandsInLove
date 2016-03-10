package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class CitationNote extends DBObject
{
	@Expose public int citation_note_id = 0;
	@Expose public int citation_id = 0;
	@Expose public String created_by = null;
	@Expose public String updated_by = null;
	@Expose public String note = null;
	@Expose public String create_date = null;
	@Expose public String update_date = null;
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		
	public CitationNote()
	{
		super("citation_note", "citation_note_id");
	}
	
	public CitationNote(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.citation_note_id = id;
			this.populate();
		}
	}
	
	public CitationNote(int id, int citationId) throws UnknownObjectException
	{
		this();

		this.citation_id = citationId;
		if(id > 0)
		{
			this.citation_note_id = id;
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
		
		if(this.citation_note_id == 0)
		{
			this.create_date = this.update_date;
			this.created_by = this.updated_by;
		}
	}

	public int getId()
	{
		return citation_note_id;
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
}
