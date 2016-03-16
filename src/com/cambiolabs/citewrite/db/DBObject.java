package com.cambiolabs.citewrite.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import java.sql.ResultSet;

import javax.naming.NamingException;

public abstract class DBObject implements Cloneable {

	private static Object _lock = new Object();
	protected String tableName = null;
	protected String primarykey = null;
	protected ArrayList<String> _ignore = new ArrayList<String>();
	
	public DBObject(String tableName, String primaryKey)
	{
		this.tableName = tableName;
		this.primarykey = primaryKey;
	}
	
	public DBObject(String tableName, String primaryKey, String[] ignore)
	{
		this.tableName = tableName;
		this.primarykey = primaryKey;
		
		if(ignore != null && ignore.length > 0)
		{
			for(String i: ignore)
			{
				this._ignore.add(i);
			}
		}
	}
	
	protected void populate()  throws UnknownObjectException
	{
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			if(!connection.lookup(this))
			{
				throw new UnknownObjectException("Object not found");
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		catch(NamingException e)
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
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getPrimaryKey() {
		return this.primarykey;
	}
	
	public String newfields(DBConnection connection)
	{
		Field[] fields = this.getClass().getFields();
		
		String list = "";
		String prep = "";
		//first build query
		for(int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			String columnName = field.getName();
			if(this._ignore.contains(columnName))
			{
				continue;
			}
			else if(columnName.equals(this.primarykey))
			{
				
				if(connection.isOracle())
				{
					if(list.length() > 0){ list += ", "; prep += ", "; }
					list += columnName;
					prep += this.primarykey + "_seq.nextval";
				}
				
				continue;
			}
			
			if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
			{
				if(list.length() > 0){ list += ", "; prep += ", "; }
				list += columnName;
				prep += "?";
			}
		} //sql for loop
		
		return "(" + list +") VALUES (" + prep +")";
	}
	
	public String getFieldList()
	{
		return this.getFieldList(false);
	}
	
	public String getFieldList(boolean precatTable)
	{
		Field[] fields = this.getClass().getFields();
		
		String list = "";
		for(int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			String columnName = field.getName();
			if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
			{
				if(list.length() > 0){ list += ", "; }
				if(precatTable)
				{
					list += this.tableName+".";
				}
				list += columnName;
			}
		} //sql for loop
		
		return list;
	}
	
	public String updatefields()
	{
		Field[] fields = this.getClass().getFields();
		
		String list = "";
		//first build query
		for(int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			String columnName = field.getName();
			
			if(columnName.equals(this.primarykey) || this._ignore.contains(columnName))
			{
				continue;
			}
			
			if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
			{
				if(list.length() > 0){ list += ", "; }
				list += columnName+"=?";
			}
		} //sql for loop
		
		return list;
	}

	public boolean commit()
	{
		synchronized(_lock) // need to make sure we are getting a unique id and only adding it
		{
			DBConnection connection = null;
			try
			{
				connection = new DBConnection();
				
				String sql = "";
				boolean isNew = this.isNew();
				if(isNew)
				{
					sql = "INSERT INTO "+this.tableName + this.newfields(connection);
				}
				else
				{
					sql = "UPDATE "+this.tableName + " set " + updatefields() + " where " + this.getPrimaryWhere();
				}
	
				PreparedStatement pst = connection.prepare(sql, PreparedStatement.RETURN_GENERATED_KEYS);
				if(pst != null)
				{
					Field[] fields = this.getClass().getFields();
	
					//first build query
					int count = 1;
					for(int i = 0; i < fields.length; i++)
					{
						Field field = fields[i];
						String columnName = field.getName();
						if(columnName.equals(this.primarykey) || this._ignore.contains(columnName))
						{
							continue;
						}
						
						if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
						{
							Class<?> type = field.getType();
							if(type.equals(String.class))
							{
								pst.setString(count, (String)field.get(this));
							}
							else if(type.equals(int.class))
							{
								pst.setInt(count, field.getInt(this));
							}
							else if(type.equals(long.class))
							{
								pst.setLong(count, field.getLong(this));
							}
							else if(type.equals(float.class))
							{
								pst.setFloat(count, field.getFloat(this));
							}
							else if(type.equals(double.class))
							{
								pst.setDouble(count, field.getDouble(this));
							}
							else if(type.equals(Timestamp.class))
							{
								pst.setTimestamp(count, (Timestamp)field.get(this));
							}
							else if(type.equals(Blob.class)){
								pst.setBinaryStream(count,  ((Blob)field.get(this)).getBinaryStream(), (int)((Blob)field.get(this)).length());
							}
							
							count++;
						}
					} //sql for loop
					
					
					boolean rv = connection.execute(pst);
					if(rv == true && isNew)
					{
						if(connection.isOracle())
						{
							connection.closeCursor();
							sql = "SELECT "+this.primarykey + "_seq.currval from "+this.tableName ;
							if(connection.query(sql))
							{
								this.setPrimaryKey(connection.getResultSet());
							}
						}
						else
						{
							this.setPrimaryKey(pst.getGeneratedKeys());
						}
					}
					
					return rv;
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
	
	private boolean isNew()
	{
		try 
		{
			Field field = this.getClass().getDeclaredField(this.primarykey);
			Class<?> type = field.getType();
			if(type.equals(String.class))
			{
				if(field.get(this) != null)
				{
					return false;
				}
			}
			else if(type.equals(int.class))
			{
				if(field.getInt(this) > 0)
				{
					return false;
				}
			} else if(type.equals(double.class)){
				if(field.getDouble(this) > 0)
				{
					return false;
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		return true;
	}
	
	private void setPrimaryKey(ResultSet keys)
	{
		try 
		{
			if(keys.next())
			{		        
				Field field = this.getClass().getDeclaredField(this.primarykey);
				Class<?> type = field.getType();
				if(type.equals(String.class))
				{
					field.set(this, keys.getString(1));
				}
				else if(type.equals(int.class))
				{
					field.set(this, keys.getInt(1));
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
		
	private String getPrimaryWhere()
	{
		try 
		{
			Field field = this.getClass().getDeclaredField(this.primarykey);
			Class<?> type = field.getType();
			if(type.equals(String.class))
			{
				String value = (String)field.get(this);
				if(value != null)
				{
					return this.primarykey + "='" + value + "'";
				}
			}
			else if(type.equals(int.class))
			{
				int value = field.getInt(this);
				if(value > 0)
				{
					return this.primarykey + "=" + value;
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public boolean delete()
	{
		String sql = "DELETE from "+this.tableName+" where "+this.getPrimaryWhere();
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			return connection.execute(sql);
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
	
	public String getQuery(DBConnection connection, int start, int max, String filter, String order)
	{
		String limit = "";
		if(connection.isMySQL())
		{
			
			if(max > 0)
			{
				limit = " limit " + start + ", " + max;
			}
							
			return String.format("SELECT * from %s %s %s %s", 
					this.tableName, 
					filter,
					order,
					limit);
		}
		else if(connection.isOracle())
		{
			String sqlFormat = "SELECT * from %s %s %s %s";
			if(max > 0)
			{
				String fields = this.getFieldList();
				sqlFormat = "SELECT "+fields+", rn from (SELECT "+fields+", ROWNUM-1 rn from %s %s %s ) %s";
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
				String sqlFormat = "SELECT * from ("+
										"SELECT "+this.tableName+".*, row_number() OVER(%s) as rownum from %s %s " +
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
			
				return String.format(
						"SELECT * from %s %s %s", 
						this.tableName, 
						filter,
						order);
			}
		}
		
		return "";
	}
	
	public String getCountQuery(DBConnection connection, String filter)
	{
		return String.format("SELECT count(*) from %s %s", 
				this.tableName, 
				filter);
	}
	
	public ArrayList<? extends DBObject> get(int start, int max, String orderBy, DBFilterList filterList)
	{
		ArrayList<DBObject> list = new ArrayList<DBObject>();
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			String filter = "";
			String order = "";
			String sql = "";
			
			if(filterList != null && !filterList.isEmpty())
			{
				filter = " WHERE " + filterList.toString();
			}
			
			if(orderBy != null && orderBy.length() > 0)
			{
				order = " order by " + orderBy;
			}
			
			sql = this.getQuery(connection, start, max, filter, order);
			
			PreparedStatement pst = connection.prepare(sql);
			
			if(filterList != null)
			{
				filterList.set(pst);
			}
			
			if(connection.query(pst))
			{
				DBObject obj = (DBObject)this.clone();
				while(connection.fetch(obj))
				{
					list.add(obj);
					obj = (DBObject)this.clone();
				}
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
		return list;
	}
	
	public int count(DBFilterList filterList)
	{
		int rv = 0;
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			String filter = "";
			
			if(!filterList.isEmpty())
			{
				filter = " WHERE " + filterList.toString();
			}
			
			String sql = this.getCountQuery(connection, filter);
			
			PreparedStatement pst = connection.prepare(sql);
			filterList.set(pst);
			if(connection.query(pst))
			{
				ResultSet rs = connection.getResultSet();
				if(rs.next())
				{
					rv = rs.getInt(1);
				}
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
		
		return rv;
	}
	
	
}
