package com.cambiolabs.citewrite.data;

import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Device extends DBObject
{	

	@Expose public int device_id = 0;
	@Expose public String name = null;
	@Expose public String device_uid = null;
	@Expose public String citation_prefix = null;
	@Expose public int citation_next = 0;
	@Expose public int active = 0;
	@Expose public int force_update = 0;
	@Expose public int sync_interval = 0;
	@Expose public int format_index = 0;
	
	private static final String UTF_8 = "UTF-8";
	
	public Device() throws UnknownObjectException
	{
		this(0);
	}
	
	public Device(int device_id) throws UnknownObjectException
	{
		super("device", "device_id");
		if(device_id > 0)
		{
			this.device_id = device_id;
			this.populate();
		}
	}
	
	public int getDeviceID() {
		return device_id;
	}
	public void setDeviceID(int deviceID) {
		this.device_id = deviceID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUID() {
		return device_uid;
	}
	public String getUid() {
		return device_uid;
	}
	public void setUID(String uid) {
		this.device_uid = uid;
	}
	public String getCitationPrefix() {
		return citation_prefix;
	}
	public void setCitationPrefix(String citation_prefix) {
		this.citation_prefix = citation_prefix;
	}
	public int getCitationNext() {
		return citation_next;
	}
	public void setCitationNext(int citation_next) {
		this.citation_next = citation_next;
	}
	public int getSyncInterval(){
		return this.sync_interval;
	}
	public void setSyncInterval(int sync_interval) {
		this.sync_interval = sync_interval;
	}
	public void setActive(int active){
		this.active = active;
	}
	public void setForceUpdate(int force_update){
		this.force_update = force_update;
	}
	public int getActive(){
		return this.active;
	}
	public boolean getForceUpdate(){
		if(this.force_update == 1)
		{
			this.setForceUpdate(0);
			this.commit();
			return true;
		}
		else
		{
			return false;
		}
	}
	public int getFormat_index() {
		return format_index;
	}

	public void setFormat_index(int format_index) {
		this.format_index = format_index;
	}

	public boolean isActive(){
		return (this.active == 1);
	}
	
	public static Device getByUID(String uid)
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			Device device = new Device();
			device.setUID(uid);
			
			if(conn.lookup(device))
			{
				return device;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		return null;
	}
	
	public static int getActiveCount(int excludeId)
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			
			String sql = "SELECT count(device_id) from device where active=1 AND device_id NOT IN("+excludeId+")";
			if(conn.query(sql))
			{
				ResultSet rs = conn.getResultSet();
				if(rs.next())
				{
					return rs.getInt(1);
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		return 0;
	}
	
	
	public String getPrintFormat (){
		
		String printFormat = "";
		
		try {
			
			if(this.format_index>0){
				printFormat = PrintLayout.getPrintFormat(format_index);
			}else{
				printFormat = PrintLayout.getDefaultPrintFormat();
			}
			
			if(printFormat != null){
				printFormat = URLEncoder.encode(printFormat, UTF_8);	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return printFormat;
			
	}

	public boolean validateUID() 
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			
			String sql = "SELECT device_id from "+this.tableName+" where device_id != ? AND device_uid=?";
			
			PreparedStatement pst = conn.prepare(sql);
			
			pst.setInt(1, this.device_id);
			pst.setString(2, this.device_uid);
			
			ResultSet rs = pst.executeQuery();
			if(rs.next())
			{
				return false;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		
		return true;
	}	
}
