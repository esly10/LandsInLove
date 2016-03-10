package com.cambiolabs.citewrite.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;

public class CodeTable
{
	private String tableName = "codes";
	private ArrayList<Column> columns = null;
	
	private String deleteSQL = null;
	private String updateSQL = null;
	
	public CodeTable()
	{
		DBConnection conn = null;
		
		try
		{
			conn = new DBConnection();
			this.deleteSQL = "UPDATE " + this.tableName + " set ";
			this.updateSQL = "UPDATE " + this.tableName + " set updated=? where hashcode IN (select hashcode from "+this.tableName + "_temp) AND added>0 AND type=?";
			
			this.columns = new ArrayList<Column>();
			this.columns.add(new Column("type"));
			this.columns.add(new Column("codeid"));
			this.columns.add(new Column("description"));
			this.columns.add(new Column("fine_amount", Column.ColumnType.VARCHAR, 10));
			this.columns.add(new Column("fine_type", Column.ColumnType.VARCHAR, 50));
			this.columns.add(new Column("is_overtime", Column.ColumnType.INTEGER, 1, "0"));
			this.columns.add(new Column("is_other", Column.ColumnType.INTEGER, 1, "0"));
			this.columns.add(new Column("added", Column.ColumnType.INTEGER));
			this.columns.add(new Column("updated", Column.ColumnType.INTEGER));
			this.columns.add(new Column("hashcode", Column.ColumnType.INTEGER));
			
			this.deleteSQL += "type=NULL,codeid=NULL,description=NULL,fine_amount=NULL,fine_type=NULL,is_overtime=0,is_other=0,";
			if (conn.buildTable(this.tableName, this.columns))
			{
				conn.execute("CREATE INDEX codes_id on " + this.tableName + " (type, codeid)");
				conn.execute("CREATE INDEX codes_description on " + this.tableName	+ " (type, description)");
				conn.execute("CREATE INDEX codes_hashcode on " + this.tableName	+ " (hashcode)");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch(Exception e){}
				conn = null;
			}
		}
	}
	
	public void load(InputStreamReader isr, String type)
	{
		DBConnection conn = null;
		CSVReader reader = null;
		try
		{
			conn = new DBConnection();
			conn.setAutoCommit(false);
			if (conn.buildTable(this.tableName+"_temp", columns))
			{
				conn.execute("CREATE INDEX codes_hashcode_temp on " + this.tableName + "_temp (hashcode, type)");
			}

			Integer ts = new Integer((int)(System.currentTimeMillis()/1000));
			
			reader = new CSVReader(isr, ',', '"', '\\');
			

			if(type.equalsIgnoreCase("violation"))
			{
				populateViolations(conn, reader, ts);
			}
			else
			{
				populateCodeTable(type, conn, reader, ts);
			}

			PreparedStatement ps = null;
			if ((ps = conn.prepare(this.updateSQL)) != null)
			{
				Object[] updateBindings = {ts, type};
				conn.bindExec(ps, updateBindings, updateBindings.length);
			}
			
			//copy over new fields
			String columnList = Column.list(this.columns);
			String copy = "insert into "+this.tableName+" ("+columnList+") " +
							"select "+columnList+" from " + this.tableName + "_temp t1 " +
							"where NOT EXISTS (select * from "+this.tableName+" t2 where t1.hashcode = t2.hashcode AND type='"+type+"') ";
			
			conn.execute(copy);
			conn.execute("DROP table " + this.tableName + "_temp");
			conn.execute(this.deleteSQL + "added=0, updated=" + ts + " where updated !=" + ts + " AND added>0 AND type='"+type+"'");
		}
		catch(Exception e)
		{
			
			try 
			{
				if (conn != null)
					conn.rollback();
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try
				{
					conn.commit();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				conn.close();
			}
			
			if(reader != null)
			{
				try
				{
					reader.close();
					reader = null;
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void populateCodeTable(String type, DBConnection conn, CSVReader reader, Integer ts) throws Exception 
	{
		
		String[] nextLine;
		
		PreparedStatement vps = conn.prepare("SELECT count(*) as total from "+this.tableName + "_temp where codeid=? AND type=?");
		PreparedStatement ps = conn.prepare("INSERT INTO " + this.tableName + "_temp (type, codeid, description, is_other, added, updated, hashcode) VALUES (?, ?, ?, ?, ?, ?, ?)");
		while ((nextLine = reader.readNext()) != null) 
		{
			String id = nextLine[0];
			
			if(id == null || id.length() == 0 || nextLine.length < 2)
			{
				continue; //don't store empty values
			}
			
			vps.setString(1, id);
			vps.setString(2, type);
			if(vps.execute())
			{
				ResultSet rs = vps.getResultSet();
				if(rs != null && rs.next())
				{
					if(rs.getInt(1) > 0) //there is already one of this type
					{
						continue;
					}
				}
			}
			
			String desc = nextLine[1];
			String other = null;
			
			if(nextLine.length == 3)
			{
				other = nextLine[2];
			}

			int iother = 0;
			if(other != null && (other.equalsIgnoreCase("true") || other.equals("1")))
			{
				iother = 1;
			}
				
			String[] hashes = {
					type,
					id,
					desc};

			int hashcode = Arrays.hashCode(hashes);

			Object params[] = new Object[] 
			        {
					type,
					id,
					desc,
					iother,
					ts,
					ts,
					String.valueOf(hashcode)
					};
			conn.bindExec(ps, params, params.length);
		}
	}
	
	private void populateViolations(DBConnection conn, CSVReader reader, Integer ts) throws Exception 
	{
		
		String[] nextLine;
		String type = "violation";		
		
		PreparedStatement ps = conn.prepare("INSERT INTO " + this.tableName + "_temp (type, codeid, description, fine_type, fine_amount, is_overtime, added, updated, hashcode) VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		while ((nextLine = reader.readNext()) != null) 
		{
			String strId = nextLine[0];
			if(strId == null || strId.length() == 0 || nextLine.length != 5)
			{
				continue; //don't store empty values
			}
			String strDesc = nextLine[1];
			String strAmt = nextLine[2];
			String strType = nextLine[3];
			String ot = nextLine[4];

			int iot = 0;
			if(ot != null && (ot.equalsIgnoreCase("true") || ot.equals("1")))
			{
				iot = 1;
			}
			
			String[] hashes = {
					type,
					strId,
					strDesc,
					strType,
					strAmt,
					String.valueOf(iot)};

			int hashcode = Arrays.hashCode(hashes);
			Object params[] = {
					type,
					strId,
					strDesc,
					strType,
					strAmt,
					new Integer(iot),
					ts,
					ts,
					hashcode};
			conn.bindExec(ps, params, 9);
		}
	}

	
	public int getDeltaCount(int ts)
	{
		int count = 0;
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
						
			conn.query("SELECT count(hashcode) as total from " + this.tableName + " where (added=0 AND updated > " + ts + ") OR added > " + ts);
			
			ResultSet rs = conn.getResultSet();
			if (rs.next())
				count = rs.getInt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				conn.close();
			}
		}
		
		return count;
	}
	
	public String getDelta(int ts)
	{
		StringBuilder builder = new StringBuilder();
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();

			//get deletes
			if(conn.query("SELECT hashcode from " + this.tableName + " where added=0 AND updated > " + ts))
			{
				StringBuilder deletes = new StringBuilder();
				boolean results_available;
				ResultSet rs = conn.getResultSet();
				for (results_available = rs.next(); results_available; results_available = rs.next())
				{
					deletes.append("<delete><![CDATA[DELETE from "+this.tableName+" where hashcode = "+rs.getString(1)+"]]></delete>");
				}
				
				if(deletes.length() > 0)
				{
					builder.append("<deletes>"+deletes+"</deletes>");
				}
			}
			
			//get adds
			if(conn.query("SELECT * from " + this.tableName + " where added > " + ts))
			{
				StringBuilder adds = new StringBuilder();
				Vector<Column> values = null;
				
				while((values = conn.fetch(this.columns)) != null)
				{
					adds.append("<add><![CDATA[INSERT INTO  "+this.tableName+" ("+Column.list(this.columns)+") VALUES ("+Column.valueList(values)+")]]></add>" );
				}
				
				if(adds.length() > 0)
				{
					builder.append("<adds>"+adds+"</adds>");
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
		}
				
		if(builder.length() > 0)
		{
			return "<code>" + builder.toString() + "</code>";
		}
		
		return "";
	}
	
	public ArrayList<Column> getColumns()
	{
		return this.columns;
	}
	
	public String getTableName()
	{
		return this.tableName;
	}
}
