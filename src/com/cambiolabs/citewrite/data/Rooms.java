package com.cambiolabs.citewrite.data; // FIXIT change landsinlove path

import java.util.ArrayList;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Rooms extends DBObject
{
	@Expose public int ROOM_ID = 0;
	@Expose public int ROOM_NO = 0;
	@Expose public int ROOM_TYPE = 0;
	@Expose public int STATUS = 0;
	@Expose public float LOCATION_X = 0;
	@Expose public float LOCATION_Y = 0;
	@Expose public int IS_DELETE = 0;
	
	public Rooms()
	{
		super("rooms", "ROOM_ID");
	}
	
	
	public Rooms(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.ROOM_ID = id;
			this.populate();
		}
	}
	
	public ArrayList<Rooms> getRooms()
	{
		ArrayList<Rooms> rooms = new ArrayList<Rooms>();
		
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * from rooms WHERE";
			if(conn.query(sql))
			{
				Rooms room = new Rooms();
				while(conn.fetch(room))
				{
					rooms.add(room);
					room = new Rooms();
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		return rooms;
	}
	
	
	@Override
	public boolean commit()
	{
		return super.commit();
	}


	public int getROOM_NO() {
		return ROOM_NO;
	}


	public void setROOM_NO(int rOOM_NO) {
		ROOM_NO = rOOM_NO;
	}


	public int getROOM_TYPE() {
		return ROOM_TYPE;
	}


	public void setROOM_TYPE(int rOOM_TYPE) {
		ROOM_TYPE = rOOM_TYPE;
	}


	public float getLOCATION_X() {
		return LOCATION_X;
	}


	public void setLOCATION_X(float lOCATION_X) {
		LOCATION_X = lOCATION_X;
	}


	public float getLOCATION_Y() {
		return LOCATION_Y;
	}


	public void setLOCATION_Y(float lOCATION_Y) {
		LOCATION_Y = lOCATION_Y;
	}


	public int getIS_DELETE() {
		return IS_DELETE;
	}


	public void setIS_DELETE(int iS_DELETE) {
		IS_DELETE = iS_DELETE;
	}


	public int getSTATUS() {
		return STATUS;
	}


	public void setSTATUS(int STATUS) {
		STATUS = STATUS;
	}


	public int getROOM_ID() {
		return ROOM_ID;
	}


	public void setROOM_ID(int rOOM_ID) {
		ROOM_ID = rOOM_ID;
	}

	

}
