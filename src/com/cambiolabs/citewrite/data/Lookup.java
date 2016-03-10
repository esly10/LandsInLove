package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Lookup extends DBObject 
{
	@Expose public int lookup_id = 0;
	@Expose public String license = null;
	@Expose public String vin = null;
	@Expose public String state = null;
	@Expose public Timestamp lookup_date = null;
	@Expose public int user_id = 0;
	@Expose public String user_name = null;		
	
	public Lookup() throws UnknownObjectException
	{
		this(0);
	}
	
	public Lookup(int lookup_id) throws UnknownObjectException
	{
		super("lookup_history", 
				"lookup_id");
		
		this._ignore.add("user_name");
		
		if(lookup_id > 0)
		{
			this.lookup_id = lookup_id;
			this.populate();
		}
	}
		
	public String getQuery(DBConnection connection, int start, int max, String filter, String order)
	{
		String limit = "";
		if(connection.isMySQL())
		{
			
			if(max > 0)
			{
				limit = " limit " + start + ", " + max;
			}
							
			return String.format(
					"SELECT lookup_history.*, CONCAT(users.first_name, ' ', users.last_name) as user_name " +
					"from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s %s %s ", 
					this.tableName, 
					filter,
					order,
					limit);
		}
		else if(connection.isOracle())
		{
			String fields = this.getFieldList(true).replace(", lookup_history.user_name", ""); //already accounted for
			String sqlFormat = "SELECT "+fields+", CONCAT(CONCAT(users.first_name, ' '), users.last_name) user_name " +
			"from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s %s %s ";
			if(max > 0)
			{
				sqlFormat = "SELECT "+this.getFieldList()+", rn from (" +
								"SELECT "+fields+", CONCAT(CONCAT(users.first_name, ' '), users.last_name) user_name, ROWNUM-1 rn " +
								"from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s %s " +
							") %s";
				limit = " WHERE rn BETWEEN " + start + " AND " + (start+max);
			}
			
			return String.format(
					sqlFormat, 
					this.tableName, 
					filter,
					order,
					limit);
		}
		else if(connection.isSQLServer())
		{
			
			if(max > 0)
			{
				limit = " WHERE rn BETWEEN " + start + " AND " + (start+max);
				String sqlFormat = "SELECT * from ("+
								"SELECT "+this.tableName+".*, users.first_name + ' ' + users.last_name as user_name, row_number() OVER(%s) as rownum from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s " +
							") as temptable %s";
				limit = " WHERE temptable.rownum BETWEEN " + start + " AND " + (start+max);
				
				return String.format(
							sqlFormat, 
							order,
							this.tableName, 
							filter,						
							limit);
			}
			else
			{
				String sqlFormat = "SELECT *, users.first_name + ' ' + users.last_name as user_name " +
				"from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s %s ";
			
				return String.format(
						sqlFormat, 
						this.tableName, 
						filter,
						order);
			}
		}
		
		return "";
	}
	
	public String getCountQuery(DBConnection connection, String filter)
	{
		return String.format("SELECT count(lookup_id) from %s LEFT JOIN users on lookup_history.user_id=users.user_id %s", 
				this.tableName, 
				filter);
	}
	
	public static Lookup factory(String license, String state, String vin, User user)
	{
		Lookup lp;
		try {
			lp = new Lookup();
			lp.setLicense(license);
			lp.setState(state);
			lp.setVin(vin);
			lp.setLookup_date(new Timestamp(System.currentTimeMillis()));
			
			if(user != null)
			{
				lp.setUser_id(user.user_id);
			}
			
			lp.commit();
			
			return lp;
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static Lookup factory(Permit permit, User user)
	{
		return factory(permit.getLicense(), permit.getState(), permit.getVin(), user);	
	}

	public int getLookup_id() {
		return lookup_id;
	}

	public void setLookup_id(int lookup_id) {
		this.lookup_id = lookup_id;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Timestamp getLookup_date() {
		return lookup_date;
	}

	public void setLookup_date(Timestamp lookup_date) {
		this.lookup_date = lookup_date;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	
	public boolean clear(Timestamp start, Timestamp end)
	{
		String sql = "DELETE from "+this.tableName;
		String where = "";
		
		if(start != null)
		{
			where += " lookup_date >= ?";
		}
		
		if(end != null)
		{
			if(where.length() > 0) { where += " AND ";}
			where += " lookup_date <= ? ";
		}
		
		if(where.length() > 0)
		{
			sql += " where " + where;
		}
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare(sql);
			
			int index = 1;
			if(start != null)
			{
				pst.setTimestamp(index, start);
				index++;
			}
			
			if(end != null)
			{
				pst.setTimestamp(index, end);
			}
			
			if(connection.execute(pst))
			{
				return true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		
		return false;
	}
}
