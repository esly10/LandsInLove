package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;

public class Codes 
{
	private static CodeTable ct = null;
	
	public static Code getCode(String type, String id)
	{
		Code code = null;
		DBConnection conn = null;
		try 
		{
			if(Codes.ct == null)
			{
				Codes.ct = new CodeTable();
			}
			
			conn = new DBConnection();
			PreparedStatement ps = conn.prepare("SELECT * from "+Codes.ct.getTableName()+" where type=? AND (codeid=? OR description=?) ");
			ps.setString(1, type);
			ps.setString(2,id);
			ps.setString(3, id);
			
			if(conn.query(ps))
			{
				ResultSet rs = conn.getResultSet();
				if(rs.next())
				{
					code = new Code();
					conn.set(code);
				}
			}
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		
		return code;
	}

	public static Code getCode(String type, int id)
	{
		Code code = null;
		DBConnection conn = null;
		try 
		{
			if(Codes.ct == null)
			{
				Codes.ct = new CodeTable();
			}
			
			conn = new DBConnection();
			PreparedStatement ps = conn.prepare("SELECT * from "+Codes.ct.getTableName()+" where type=? AND codeid=?");
			ps.setString(1, type);
			ps.setInt(2,id);
			
			if(conn.query(ps))
			{
				ResultSet rs = conn.getResultSet();
				if(rs.next())
				{
					code = new Code();
					conn.set(code);
				}
			}
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
			}
		}
		
		return code;
	}
	
    public static ArrayList<Code> getCodes(String type)
    {
    	return getCodes(type, null);
    }
    
    @SuppressWarnings("unchecked")
	public static ArrayList<Code> getCodes(String type, String orderBy)
    {
		Code query = new Code();
			
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("type", "=", type));
		
		ArrayList<Code> rv = (ArrayList<Code>)query.get(0, 0, orderBy, filter);

		return rv;
    }
   
    /*
    public static ArrayList<Code> getViolations()
    {
    	return getCodes("violation", "codeid ASC");
    }
    
    public static ArrayList<Code> getComments(boolean required)
    {
    	return getCodes("comment", "codeid ASC");
    }
    
    public static ArrayList<Code> getMakes()
    {
    	return getCodes("make", "description ASC");
    }
    
    public static ArrayList<Code> getColors()
    {
    	return getCodes("color", "description ASC");
    }
    
    public static ArrayList<Code> getZones()
    {
    	return getCodes("location", "codeid ASC");
    }
    
    public static ArrayList<Code> getStates()
    {
    	return getStates("description ASC");
    }
    
    public static ArrayList<Code> getStates(String orderBy)
    {
    	return getCodes("state", true, false, orderBy);
    }
    
    public static ArrayList<Code> getLookupStates()
    {
        return getCodes("state", false, true);
    }
    
    public static Code getStateCode(String state)
    {
        return getCode("state", state);
    }
    
    public static  Code getColorCode(String id)
    {
        return getCode("color", id);
    }
    
    public static Code getMakeCode(String id)
    {
    	return getCode("make", id);
    }
    
    public static Code getViolationCode(String id)
    {
    	return getCode("violation", id);
    }
    
    public static Code getCommentCode(String id)
    {
    	return getCode("comment", id);
    }
    
    public static Code getLocationCode(String id)
    {
    	return getCode("location", id);
    }
    */
}
