package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class RoomType extends DBObject{
	@Expose public int room_type_id = 0;
	@Expose public String room_type_description = null;
	
	public RoomType() throws UnknownObjectException
	{
		this(0);
	}
	
	public RoomType(int room_type_id) throws UnknownObjectException
	{
		super("room_type", "room_type_id");
		if(room_type_id > 0)
		{
			this.room_type_id = room_type_id;
			this.populate();
		}
	}
	
	public int getRoom_type_id() {
		return room_type_id;
	}
	public void setRoom_type_id(int room_type_id) {
		this.room_type_id = room_type_id;
	}
	public String getRoom_type_description() {
		return room_type_description;
	}
	public void setRoom_type_description(String meal_plan_description) {
		this.room_type_description = meal_plan_description;
	}
	
}
