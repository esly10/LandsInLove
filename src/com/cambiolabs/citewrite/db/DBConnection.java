package com.cambiolabs.citewrite.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBConnection 
{
	
	public enum DatabaseType
	{
		MYSQL, ORACLE, SQLSERVER, SQLITE
	}
	private Connection conn = null;
	private Statement st = null;
    private ResultSet rs = null;
    private String databaseName = null;
    
    private static DatabaseType DB_TYPE = null;

	
	public DBConnection() throws NamingException, SQLException
	{	
		Context initContext = new InitialContext();
		Context envContext  = (Context)initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource)envContext.lookup("jdbc/CiteWriteDB");
		this.conn = ds.getConnection();
		
		DatabaseMetaData meta = this.conn.getMetaData();
		this.databaseName = meta.getDatabaseProductName();
		this.setSession();
		
	}
	
	public void setDatabaseType()
	{
		if (isMySQL())
		{
			DB_TYPE = DatabaseType.MYSQL;
		}
		else if (isOracle())
		{
			DB_TYPE = DatabaseType.ORACLE;
		}
		else if (isSQLServer())
		{
			DB_TYPE = DatabaseType.SQLSERVER;
		}
	}
	
	public static DatabaseType getDatabaseType()
	{
		return DB_TYPE;
	}
	
	public boolean isMySQL()
	{
		return this.databaseName.equalsIgnoreCase("mysql");
	}
	
	public boolean isOracle()
	{
		return this.databaseName.equalsIgnoreCase("oracle");
	}
	
	public boolean isSQLServer()
	{
		return this.databaseName.equalsIgnoreCase("Microsoft SQL Server");
	}
	
	private void setSession()
	{
		try
		{
			if(this.isOracle())
			{
				Statement statement = this.conn.createStatement();
				statement.execute("alter session set NLS_COMP=LINGUISTIC");
				statement.execute("alter session set NLS_SORT=BINARY_CI");
				statement.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean query(String sql)
	{
		try
		{
			this.st = conn.createStatement();
			this.rs = st.executeQuery(sql);
		    
		    return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public ResultSet getResultSet()
	{
		return this.rs;
	}
	
	public boolean execute(String sql)
	{
		try
		{
			this.st = conn.createStatement();
		    st.executeUpdate(sql);
		    
		    return true;
		}
		catch(Exception e)
		{
			System.out.println("DBConnection EXECUTE Exception for: "+sql);
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean query(PreparedStatement pst)
	{
		try
		{
		    this.rs = pst.executeQuery();
		    
		    return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean execute(PreparedStatement pst)
	{
		try
		{
		    pst.executeUpdate();
		    
		    return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public PreparedStatement prepare(String sql)
	{
		return this.prepare(sql, -1);
	}
	
	public PreparedStatement prepare(String sql, int generatedKeys)
	{
		try
		{
			if(generatedKeys == -1)
			{
				return conn.prepareStatement(sql);
			}
			else
			{
				return conn.prepareStatement(sql, generatedKeys);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean fetch(DBObject object)
	{
		try
		{
			if(this.rs != null)
			{
				if(this.rs.next())
				{
					this.set(object);
					
					return true;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void set(DBObject object) throws Exception
	{
		Field[] fields = object.getClass().getFields();
		for(int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			String columnName = field.getName();
			
			if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
			{
				try
				{
					Class<?> type = field.getType();
					if(type.equals(String.class))
					{
						field.set(object, this.rs.getString(columnName));
					}
					else if(type.equals(int.class))
					{
						field.set(object, this.rs.getInt(columnName));
					}
					else if(type.equals(long.class))
					{
						field.set(object, this.rs.getLong(columnName));
					}
					else if(type.equals(float.class))
					{
						field.set(object, this.rs.getFloat(columnName));
					}
					else if(type.equals(Timestamp.class))
					{
						field.set(object, this.rs.getTimestamp(columnName));
					}
					else if(type.equals(double.class)){
						field.setDouble(object, this.rs.getDouble(columnName));
					}
					else if(type.equals(Blob.class)){
						field.set(object, this.rs.getBlob(columnName));
					}
				}
				catch(SQLException e)
				{
					//column doesn't exist
				}
			}
		}
	}
	
	public boolean lookup(DBObject object)
	{
		try
		{
			String sql = "SELECT * from "+object.tableName+" where ";
			String where = "";
			Field[] fields = object.getClass().getFields();
			
			Vector<Object> parameters = new Vector<Object>();
			//first build query
			for(int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				String columnName = field.getName();
				
				if(field.getModifiers() == Modifier.PUBLIC) //only include public modifiers
				{
					Class<?> type = field.getType();
					if(type.equals(String.class))
					{
						if(this.isOracle())
						{
						    DatabaseMetaData meta = this.conn.getMetaData();
						    ResultSet rsColumns = meta.getColumns(null, null, object.tableName, columnName);
						    if(rsColumns != null && rsColumns.next())
						    {
						    	int ctype = rsColumns.getInt("DATA_TYPE");
						    	if(ctype == Types.CLOB)
						    	{
						    		columnName = "to_char("+columnName+")";
						    	}						    	
						    }
						}
						
						String value = (String)field.get(object);
						if(value != null)
						{
							if(where.length() > 0){ where += " AND "; }
							where += columnName+"=?";
							
							parameters.add(value);
						}
					}
					else if(type.equals(int.class))
					{
						int value = field.getInt(object);
						if(value > 0)
						{
							if(where.length() > 0){ where += " AND "; }
							where += columnName+"=?";
							
							parameters.add(new Integer(value));
						}
					}
					else if(type.equals(Timestamp.class))
					{
						Timestamp value = (Timestamp)field.get(object);
						if(value != null)
						{
							if(where.length() > 0){ where += " AND "; }
							where += columnName+"=?";
							
							parameters.add(value);
						}
					}
					else if(type.equals(double.class))
					{
						double value = field.getDouble(object);
						if(value > 0)
						{
							if(where.length() > 0){ where += " AND "; }
							where += columnName+"=?";
							
							parameters.add(new Double(value));
						}
					}
				}
			} //sql for loop
			
			if(where.length() > 0)
			{
				sql += where;
				
				PreparedStatement pst = this.conn.prepareStatement(sql);
				for(int i = 0; i < parameters.size(); i++)
				{
					Object field = parameters.get(i);
					Class<?> type = field.getClass();
					
					if(type.equals(String.class))
					{
						pst.setString(i+1, (String)field);
					}
					else if(type.equals(Integer.class))
					{
						pst.setInt(i+1, ((Integer)field).intValue());
					}
					else if(type.equals(Timestamp.class))
					{
						pst.setTimestamp(i+1, (Timestamp)field);
					}
					else if(type.equals(Double.class))
					{
						pst.setDouble(i+1, ((Double)field).doubleValue());
					}
				}
				
				this.rs = pst.executeQuery();
				if(this.rs.next())
				{
					this.set(object);
					return true;
				}
				
			}//end if where
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void close()
	{
		try
		{
			if(this.conn != null)
			{
				this.conn.close();
			}
			
			if(this.st != null)
			{
				this.st.close();
			}
			
			if(this.rs != null)
			{
				this.rs.close();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void closeCursor()
	{
		try
		{
			if(this.st != null)
			{
				this.st.close();
				this.st = null;
			}
			
			if(this.rs != null)
			{
				this.rs.close();
				this.rs = null;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	 
	public boolean buildTable(String tableName, ArrayList<Column> columns)
    {
		String check_table;
		if (isOracle())
		{
			check_table = "SELECT 1 FROM tabs WHERE table_name='"+tableName+"'";
		}
		else if (this.isMySQL())
		{
			check_table = "SHOW TABLES LIKE '"+tableName+"'";
		}
		else if (isSQLServer())
		{
			check_table = "SELECT table_name FROM information_schema.tables WHERE table_name LIKE '"+tableName+"'";
		}
		else
		{
			check_table = "invalid database service";
		}
		
		if (query(check_table))
		{
			try
			{
				if (rs.next())
				{
					return(false);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
				

		String sql = "CREATE TABLE "+tableName+" (";
		
		for(int i = 0; i < columns.size(); i++)
		{
			Column col = columns.get(i);
			
			if(i > 0) { sql += "," ;}
			sql += col.toString(this.DB_TYPE);
		}
		sql += ")";
		
		return execute(sql);
    }
	
	public boolean bindExec(PreparedStatement ps, Object[] binding, int max)
	{
		
		try
		{
			for(int i = 0; i < max; i++)
			{
				Object obj = binding[i];
				if(obj == null)
				{
					ps.setString(i+1, (String)null);
				}
				else if(obj.getClass().equals(String.class))
				{
					ps.setString(i+1, (String)obj);
				}
				else
				{
					ps.setInt(i+1, ((Integer)obj).intValue());
				}
			}
				
			return(this.execute(ps));
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	public Vector<Object> fetchObjects(ArrayList<Column> columns)
	{
		try
		{
			if(this.rs.next())
			{
				Vector<Object> row = new Vector<Object>();
				for (int i = 0; i< columns.size(); i++)
				{
					Column col = columns.get(i);
					switch (col.getType())
					{
						case INTEGER :
						case UNSIGNED_INTEGER:
							row.add(this.rs.getInt(i+1));
							break;
						case VARCHAR : 
						case TEXT :
							row.add(this.rs.getString(i+1));
							break;
						case FLOAT :
							row.add(this.rs.getFloat(i+1));
							break;
					}
				}
				return row;
			}
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
		}
		
		return null;

	}	
	
	public Vector<Column> fetch(ArrayList<Column> columns)
	{
		try
		{
			if(this.rs.next())
			{
				ResultSetMetaData rsm = rs.getMetaData();
				int count = rsm.getColumnCount();
				Vector<Column> row = new Vector<Column>();
				for(int i = 0; i < count; i++ )
				{
					Column column = columns.get(i);
					if(column.isIntType())
					{
						row.add(new Column(columns.get(i), this.rs.getInt(i+1)));
					}
					else
					{
						row.add(new Column(columns.get(i), this.rs.getString(i+1)));
					}
				}
				
				return row;
			}
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
		}
		
		return null;
	}
	
	public Hashtable<String, Column> fetchAssoc(ArrayList<Column> columns)
	{
		try
		{
			if(this.rs.next())
			{
				ResultSetMetaData rsm = rs.getMetaData();
				int count = rsm.getColumnCount();
				Hashtable<String, Column> row = new Hashtable<String, Column>();
				for(int i = 0; i < count; i++ )
				{
					row.put(rsm.getColumnName(i+1), new Column(columns.get(i), this.rs.getString(i+1)));
				}
				
				return row;
			}
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
		}
		
		return null;
	}
	
	public Hashtable<String, String> fetchAssoc()
	{
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			if(this.rs.next())
			{
				ResultSetMetaData rsm = rs.getMetaData();
				int count = rsm.getColumnCount();
				Hashtable<String, String> row = new Hashtable<String, String>();
				for(int i = 1; i <= count; i++ )
				{
					String name = rsm.getColumnName(i);
					String value = null;
					
					int type = rsm.getColumnType(i);
					if(type == Types.TIMESTAMP)
					{
						Timestamp ts = this.rs.getTimestamp(i);
						if(ts != null)
						{
							value = df.format(ts);
						}
						else
						{
							value = "";
						}
					}
					else
					{
						value = this.rs.getString(i); 

						if (value == null){
							value = "";
						}
						
					}
					
					row.put(name.toLowerCase(), value); //for oracle
				}
				
				return row;
			}
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
		}
		
		return null;
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		this.conn.setAutoCommit(autoCommit);
	}
	
	public void commit() throws SQLException
	{
		this.conn.commit();
	}

	public void rollback() throws SQLException
	{
		this.conn.rollback();
	}
}
