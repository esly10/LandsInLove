package com.cambiolabs.citewrite.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

public class DBFilter 
{
	
	private int index = 0;
	private String operator = "=";
	private String fieldName = "field";
	private Object value = null;
	private Object betweenValue = null;
	private ArrayList<DBFilter> orFilter = new ArrayList<DBFilter>();
	private ArrayList<DBFilter> andFilter = new ArrayList<DBFilter>();
	
	public static final String EQ = "=";
	public static final String NE = "!=";
	public static final String GE = ">=";
	public static final String LE = "<=";
	public static final String LIKE = "like";
	public static final String BETWEEN = "between";
	public static final String NOT_BETWEEN = "not between";
	public static final String IS_NULL = "is null";
	public static final String NOT_NULL = "is not null";

	public DBFilter()
	{
		
	}
	
	public DBFilter(int index, String fieldName, String operator, Object value, Object betweenValue)
	{
		this.index = index;
		this.operator = operator;
		this.fieldName = fieldName;
		this.value = value;
		this.betweenValue = betweenValue;
	}
	
	public DBFilter(String fieldName, String operator, Object value, Object betweenValue)
	{
		this(0, fieldName, operator, value, betweenValue);
	}
	
	public DBFilter(int index, String fieldName, String operator, Object value)
	{
		this(index, fieldName, operator, value, null);
	}
	
	public DBFilter(String fieldName, String operator, Object value)
	{
		this(0, fieldName, operator, value);
	}
	
	public DBFilter(String fieldName, Object value)
	{
		this(0, fieldName, "=", value);
	}
	
	public DBFilter(String fieldName)
	{
		this(0, fieldName, "=", null);
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public void and(DBFilter filter)
	{
		this.andFilter.add(filter);
	}
	
	public void or(DBFilter filter)
	{
		this.orFilter.add(filter);
	}
	
	public String toString()
	{
		String sql = this.fieldName;
		
		if((this.operator.trim().equalsIgnoreCase(IS_NULL)) || (this.operator.trim().equalsIgnoreCase(NOT_NULL)))
		{
			sql +=  " " + this.operator;
	
		}
		else if(this.operator.trim().equalsIgnoreCase(BETWEEN))
		{
			sql +=  " " + this.operator + " ? AND ?";
		}
		else if(this.operator.trim().equalsIgnoreCase(NOT_BETWEEN))
		{
			sql +=  " < ? AND "+this.fieldName+" > ? ";
		}
		else
		{
			sql +=  " " + this.operator + " ? ";
		}
		
		if(!this.orFilter.isEmpty() || !this.andFilter.isEmpty())
		{
			sql = " ("+sql;
		}
		
		if(this.orFilter.size() > 0)
		{
			for (Iterator<DBFilter> iter = this.orFilter.iterator(); iter.hasNext(); ) {
			    DBFilter filter = iter.next();
			    sql += " OR ";
			    sql += filter.toString();
			}
		}
		
		if(this.andFilter.size() > 0)
		{
			for (Iterator<DBFilter> iter = this.andFilter.iterator(); iter.hasNext(); ) {
			    DBFilter filter = iter.next();
			    sql += " AND ";
			    sql += filter.toString();
			}
		}
		
		if(!this.orFilter.isEmpty() || !this.andFilter.isEmpty())
		{
			sql += ") ";
		}
		
		
		return sql;
	}
	
	public void set(PreparedStatement pst) throws SQLException
	{

		if(!(this.operator.trim().equalsIgnoreCase(IS_NULL)) && (!this.operator.trim().equalsIgnoreCase(NOT_NULL)))
		{
			
			Class<?> type = this.value.getClass();
			if(type.equals(String.class))
			{
				String sValue = (String)value;
				if(this.operator.equalsIgnoreCase("like"))
				{
					sValue = "%"+sValue+"%";
				}
				
				pst.setString(this.index, sValue);	
			}
			else if(type.equals(int.class) || type.equals(Integer.class))
			{
				pst.setInt(this.index, ((Integer)value).intValue());
			}
			else if(type.equals(Timestamp.class))
			{
				pst.setTimestamp(this.index, (Timestamp)value);
				if(this.operator.trim().equalsIgnoreCase(BETWEEN) || this.operator.trim().equalsIgnoreCase(NOT_BETWEEN))
				{
					pst.setTimestamp(this.index+1, (Timestamp)betweenValue);
				}
			}
			
		}
	
		for (Iterator<DBFilter> iter = this.orFilter.iterator(); iter.hasNext(); ) {
		    DBFilter filter = iter.next();
		    filter.set(pst);
		}
		
		for (Iterator<DBFilter> iter = this.andFilter.iterator(); iter.hasNext(); ) {
		    DBFilter filter = iter.next();
		    filter.set(pst);
		}
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
	
}
