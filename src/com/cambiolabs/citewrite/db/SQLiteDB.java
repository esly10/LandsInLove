package com.cambiolabs.citewrite.db;

import com.almworks.sqlite4java.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteDB 
{
	private SQLiteConnection db = null;
	public SQLiteStatement st = null;
	
	public SQLiteDB(File dbFile) throws Exception
	{
		this.db = new SQLiteConnection(dbFile);
		this.db.open(true);

		  //turn logging off for sqlite4java
		  Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
	}
	
	public void close()
	{
	  if(this.db != null)
	  {
		  this.db.dispose();
	  }
	  
	  if(this.st != null)
	  {
		  this.st.dispose();
	  }
	} 
	
	public boolean hashExists(String table, int hashcode)
	{
		String sql = "SELECT hashcode from "+table+" where hashcode="+hashcode;
		if(this.prepare(sql))
		{
			try
			{
				if(this.st != null && this.st.step())
				{
					return true;
				}
			}
			catch(Exception e){}
		}
		
		return false;
	}
	
	public boolean tableExists(String tableName)
	{
		String sql ="SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'";
		if(this.prepare(sql))
		{
			try
			{
				if(this.st != null && this.st.step())
				{
					return true;
				}
			}
			catch(Exception e){}
		}
		
		return false;
	}
	
	public boolean execute(String sql)
	{
		try
		{
			this.st = db.prepare(sql);
			this.st.stepThrough();
			this.st.dispose();
			this.st = null;
			return true;
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return false;
	}
	
	public boolean prepare(String sql)
	{
		try
		{
			this.st = db.prepare(sql);
			return true;
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return false;
	}
	
	public boolean bindExec(Object[] binding, int max)
	{
		try
		{
			if(this.st != null)
			{
				this.st.reset(true);
				for(int i = 0; i < max; i++)
				{
					Object obj = binding[i];
					if(obj == null)
					{
						this.st.bind(i+1, (String)null);
					}
					else if(obj.getClass().equals(String.class))
					{
						this.st.bind(i+1, (String)obj);
					}
					else if (obj.getClass().equals(Integer.class))
					{
						this.st.bind(i+1, ((Integer)obj).intValue());
					}
					else if (obj.getClass().equals(Float.class))
					{
						this.st.bind(i+1, ((Float)obj).floatValue());
					}
					else
					{
						System.out.println("unknown column type");
					}
				}
				
				this.st.stepThrough();
				
				return true;
			}
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return false;
	}
	
	public boolean transaction()
	{
		return execute("begin transaction");
	}
	
	public boolean rollback()
	{
		return execute("rollback transaction");
	}
	
	public boolean commit()
	{
		return execute("commit transaction");
	}
	
	public Vector<Column> fetch(ArrayList<Column> columns)
	{
		try
		{
			if(this.st.step())
			{
				Vector<Column> row = new Vector<Column>();
				int count = this.st.columnCount();
				for(int i = 0; i < count; i++ )
				{
					Column column = columns.get(i);
					if(column.isIntType())
					{
						row.add(new Column(columns.get(i), this.st.columnInt(i)));
					}
					else
					{
						row.add(new Column(columns.get(i), this.st.columnString(i)));
					}
				}
				
				return row;
			}
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return null;
	}
	
	public Hashtable<String, Column> fetchAssoc(ArrayList<Column> columns)
	{
		try
		{
			if(this.st.step())
			{
				Hashtable<String, Column> row = new Hashtable<String, Column>();
				int count = this.st.columnCount();
				for(int i = 0; i < count; i++ )
				{
					row.put(this.st.getColumnName(i), new Column(columns.get(i), this.st.columnString(i)));
				}
				
				return row;
			}
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return null;
	}
	
	public int getColumnCount()
	{
		try
		{
			if(this.st == null){
				return 0;
			}
			
			return this.st.columnCount();
		}
		catch(SQLiteException sqe)
		{
			sqe.printStackTrace();
		}
		
		return 0;
	}
	
	public boolean buildTable(String tableName, ArrayList<Column> columns)
    {
		String sql = "CREATE TABLE IF NOT EXISTS "+tableName+" (";
		
		for(int i = 0; i < columns.size(); i++)
		{
			Column col = columns.get(i);
			
			if(i > 0) { sql += "," ;}
			sql += col.toString(DBConnection.DatabaseType.SQLITE);
		}
		sql += ")";
		
		
		if(!this.execute(sql))
		{
			return false;
		}
		
		return true;
    }
}

	