package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class OwnerNote extends DBObject
{
	@Expose public int owner_note_id = 0;
	@Expose public int owner_id = 0;
	@Expose public String created_by = null;
	@Expose public String updated_by = null;
	@Expose public String note = null;
	@Expose public Timestamp create_date = null;
	@Expose public Timestamp update_date = null;
		
	public OwnerNote()
	{
		super("owner_note", "owner_note_id");
	}
	
	public OwnerNote(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.owner_note_id = id;
			this.populate();
		}
	}
	
	public OwnerNote(int id, int ownerId) throws UnknownObjectException
	{
		this();

		this.owner_id = ownerId;
		if(id > 0)
		{
			this.owner_note_id = id;
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
		this.update_date = new Timestamp(System.currentTimeMillis());
		this.updated_by = user.getName();
		
		if(this.owner_note_id == 0)
		{
			this.create_date = this.update_date;
			this.created_by = this.updated_by;
		}
	}

	public int getId()
	{
		return owner_note_id;
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
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.create_date);
		}
		
		return "";
	}
	
	public String getUpdated()
	{
		if(this.update_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.update_date);
		}
		
		return "";
	
	}	
}
