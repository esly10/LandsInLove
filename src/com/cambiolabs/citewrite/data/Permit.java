package com.cambiolabs.citewrite.data;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;

public class Permit implements Serializable
{
	static final long serialVersionUID = 1L;
	static final int PT_PERMIT = 1;
	static final int PT_HOTLIST = 2;
	
	protected int type = PT_PERMIT;
	private String license = null;
	private String state = null;
	private String vin = null;
	private Hashtable<String, Column> data = new Hashtable<String, Column>();
	
		
	public Permit(Hashtable<String, Column> row)
	{
		this.data = row;
		
		Enumeration<String> keys = row.keys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			Column col = this.data.get(key);
			String columnName = col.getColumnName();
			if(columnName.equals("license"))
			{
				this.license = col.getValue();
			}
			else if(columnName.equals("state"))
			{
				this.state = col.getValue();
			}
			else if(columnName.equals("vin"))
			{
				this.vin = col.getValue();
			}
		}
	}
		
	public Permit(String license, String state, String vin)
	{
		this.license = license;
		this.state = state;
		this.vin = vin;
	}
	
	public void setLicense(String license) {
		this.license = license;
	}
	public String getLicense() {
		return license;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getState() {
		return state;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getVin() {
		return vin;
	}
	public void setExtra(Hashtable<String, Column> data) {
		this.data = data;
	}
	public Hashtable<String, Column> getData() {
		return this.data;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public String getLookupDisplay()
	{
		ArrayList<Column> columns = new ArrayList<Column>(this.data.values());
		Collections.sort(columns, new Comparator<Column>(){
			 
            public int compare(Column o1, Column o2) 
            {
               return (o1.getDisplayOrder() - o2.getDisplayOrder());
            }
 
        });
		
		
		String rv = "<dl class=\"details\">";
		
		int size = columns.size();
		for(int i = 0; i < size; i++)
		{
			Column column = columns.get(i);
			String name = column.getName();
			String value = column.getValue();
			
			if(name.equalsIgnoreCase("added") || name.equalsIgnoreCase("updated")
					|| name.equalsIgnoreCase("hashcode"))
			{
				continue;
			}
			
			rv += "<dt>"+name+"</dt>" +
					"<dd>"+value+"</dd>";
		}
		
		return rv + "</dl>";
	}
	
	public static Permit lookup(String license, String state)
	{
		DBConnection conn = null;
		PreparedStatement pst = null;
		String sql = null;
		
		try
		{
			conn = new DBConnection();
			HotListTable ht = Config.hotlistTable;
			
			if(ht != null)
			{
				
				//first check hotlist
				sql = "SELECT * from "+ht.getTableName()+" where license=? ";
				if(state != null && state.length() > 0)
				{
					sql += " AND state=? ";
				}
				
				sql += " ORDER BY updated DESC";
				
				pst = conn.prepare(sql);
				pst.setString(1, license);
				
				if(state != null && state.length() > 0)
				{
					pst.setString(2, state);
				}
				
				if(conn.query(pst))
				{
					
					Hashtable<String, Column> row = conn.fetchAssoc(ht.getColumns());
					if(row != null)
					{
						HotList hotlist = new HotList(row);
						
						return hotlist;
					}
				}
				
				if(pst != null)
				{
					try
					{
						pst.close();
						pst = null;
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				
			}
			
			PermitTable pt = Config.permitTable;
			sql = "SELECT * from "+pt.getTableName()+" where license=? ";
			if(state != null && state.length() > 0)
			{
				sql += " AND state=? ";
			}
			sql += " ORDER BY updated DESC";
			
			pst = conn.prepare(sql);
			pst.setString(1, license);
			
			if(state != null && state.length() > 0)
			{
				pst.setString(2, state);
			}
			
			if(conn.query(pst))
			{
				
				Hashtable<String, Column> row = conn.fetchAssoc(pt.getColumns());
				if(row != null)
				{
					Permit permit = new Permit(row);
					
					return permit;
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
			}
			
			if(pst != null)
			{
				try
				{
					pst.close();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static Permit lookupByVIN(String vin)
	{
		DBConnection conn = null;
		PreparedStatement pst = null;
		try
		{
			conn = new DBConnection();
			
			HotListTable ht = Config.hotlistTable;
			//first check hotlist
			String sql = "SELECT * from "+ht.getTableName()+" where vin=? ORDER BY updated DESC";
			pst = conn.prepare(sql);
			pst.setString(1, vin);
			if(conn.query(pst))
			{
				
				Hashtable<String, Column> row = conn.fetchAssoc(ht.getColumns());
				if(row != null)
				{
					HotList hotlist = new HotList(row);
					
					return hotlist;
				}
			}
			
			if(pst != null)
			{
				try
				{
					pst.close();
					pst = null;
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			
			PermitTable pt = Config.permitTable;
			sql = "SELECT * from "+pt.getTableName()+" where vin=? ORDER BY updated DESC";
			
			pst = conn.prepare(sql);
			pst.setString(1, vin);
			if(conn.query(pst))
			{
				
				Hashtable<String, Column> row = conn.fetchAssoc(pt.getColumns());
				if(row != null)
				{
					Permit permit = new Permit(row);
					
					return permit;
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
			}
			
			if(pst != null)
			{
				try
				{
					pst.close();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static Permit lookupByColumn(String column, String value)
	{
		DBConnection conn = null;
		PreparedStatement pst = null;
		try
		{
			conn = new DBConnection();
			
			PermitTable pt = Config.permitTable;
			String sql = "SELECT * from "+pt.getTableName()+" where "+column+"=? ORDER BY updated DESC";
			pst = conn.prepare(sql);
			pst.setString(1, value);
			if(conn.query(pst))
			{
				
				Hashtable<String, Column> row = conn.fetchAssoc(pt.getColumns());
				if(row != null)
				{
					Permit permit = new Permit(row);
					
					return permit;
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
			}
			
			if(pst != null)
			{
				try
				{
					pst.close();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
}
