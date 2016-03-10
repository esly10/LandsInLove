package com.cambiolabs.citewrite.data;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;


public class HotListTable 
{
	private String tableName = "hotlist";
	private ArrayList<Column> columns = null;
	protected ArrayList<Column> indexes = null;
	
	private String deleteSQL = null;
	private String updateSQL = null;
	private String insertSQL = null;
	private String hashcodeSQL = null;
	
	private File hotlistFile = null;
	
	private List <HotListColumnMetaData> hotListColumnMetaDataList = null;
	
	public HotListTable(File hotlistFile)
	{
		this.hotlistFile = hotlistFile;
		
		DBConnection conn = null;
		Map<String,Column> indexesMap = null;
		
		try
		{
			hotListColumnMetaDataList = HotListColumnMetaData.get(false);

			int size = hotListColumnMetaDataList.size();
			if (size > 0) 
			{
				this.deleteSQL = "UPDATE " + this.tableName + " set ";
				this.updateSQL = "UPDATE " + this.tableName + " set updated=? where hashcode IN (select hashcode from "+this.tableName + "_temp) AND added>0";
				this.insertSQL = " VALUES (";
				this.hashcodeSQL = "UPDATE " + this.tableName + "_temp set hashcode=";
				
				this.columns = new ArrayList<Column>();
				this.indexes = new ArrayList<Column>();
				indexesMap = new HashMap <String,Column>();
				
				for (HotListColumnMetaData hotListColumnMetaData : hotListColumnMetaDataList){
					Column column = new Column();
					column.setName(hotListColumnMetaData.columnName);
					column.setDisplayOrder(hotListColumnMetaData.displayOrder);
					this.columns.add(column);
					insertSQL += "?,";
					deleteSQL += column.getColumnName() + "=";
					if(column.isIntType())
					{
						deleteSQL += "0, ";
					}
					else
					{
						deleteSQL += "NULL, ";
					}
					if(hotListColumnMetaData.mapping != null && !hotListColumnMetaData.mapping.equals(HotListColumnMetaData.NONE)){
						if(indexesMap.get(column.getColumnName()) == null){
							indexesMap.put(column.getColumnName(), column);
						}else{
							System.err.println("The index column name exist.");
						}
					}
				}

				conn = new DBConnection();				
				if(conn.isSQLServer())
				{
					this.hashcodeSQL += "CHECKSUM("+Column.list(this.columns)+")";
				}
				else if(conn.isOracle())
				{
					this.hashcodeSQL += "OWA_OPT_LOCK.CHECKSUM("+Column.list(this.columns, " || ")+")";
				}
				else
				{
					this.hashcodeSQL += "CRC32(CONCAT("+Column.list(this.columns)+"))";
				}
				
				this.columns.add(new Column("added", Column.ColumnType.INTEGER));
				this.columns.add(new Column("updated", Column.ColumnType.INTEGER));
				this.columns.add(new Column("hashcode", Column.ColumnType.UNSIGNED_INTEGER));
				insertSQL += "?, ?, ?)";	
				
				if (conn.buildTable(this.tableName, this.columns)) 
				{
					conn.execute("CREATE INDEX hotlist_hash on " + this.tableName + " (hashcode)");	
					for(Column columnIndex  : indexesMap.values()){
						conn.execute("CREATE INDEX hotlist_"+ columnIndex.getColumnName()  +" on " + this.tableName + " ("+ columnIndex.getColumnName()  +")");
						indexes.add(columnIndex);
					}
				}
				indexesMap = null;
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
				try
				{
					conn.close();
				}
				catch(Exception e){}
				conn = null;
			}
		}
	}
	
	public void load()
	{
		DBConnection conn = null;
		try
		{
			PreparedStatement ps = null;
			String tempTable = this.tableName + "_temp";
			
			String ts = String.valueOf((int)(System.currentTimeMillis()/1000));
			conn = new DBConnection();
			conn.setAutoCommit(false);
			
			if (conn.buildTable(tempTable, this.columns)) 
			{
				conn.execute("CREATE INDEX hotlist_temp_hashcode on " + tempTable + " (hashcode)");
			}
			
			CSVReader reader = new CSVReader(new FileReader(hotlistFile), ',', '"', '\\');
			String[] nextLine;
			
			int maxBindings = this.columns.size();
			String[] bindings = new String[maxBindings];
			bindings[maxBindings-3] = ts;//added
			bindings[maxBindings-2] = ts;//updated
			
			if ((ps = conn.prepare("INSERT INTO " + tempTable + this.insertSQL)) != null) 
			{
				while ((nextLine = reader.readNext()) != null) 
				{
					try
					{
						if(nextLine.length != maxBindings-3)
						{
							System.err.println("****Bad hotlist csv line for " + nextLine[0]);
							continue;
						}
						
						System.arraycopy(nextLine, 0, bindings, 0, maxBindings-3);								
						bindings[maxBindings-1] = "0";
						
						conn.bindExec(ps, bindings, maxBindings);
					}
					catch(Exception e)
					{
						continue;
					}
				}
			}
			reader.close();
			
			//add the hashcodes
			conn.execute(this.hashcodeSQL);
		
			if((ps = conn.prepare(this.updateSQL)) != null)
			{
				String[] updateBindings = {ts};//updated
				conn.bindExec(ps, updateBindings, 1);
			}
			
			//copy over new rows
			String columnList = Column.list(this.columns);
			String copy = "insert into "+this.tableName+" ("+columnList+") " +
							"select "+columnList+" from "+tempTable+" t1 " +
							"where NOT EXISTS (select * from "+this.tableName+" t2 where t1.hashcode = t2.hashcode) ";
			conn.execute(copy);
			conn.execute("DROP table " + tempTable);
			conn.execute(deleteSQL + "added=0, updated=" + ts + " where updated !=" + ts + " AND added>0");
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
		}
	}
	
	public int getDeltaCount(int ts)
	{
		int count = 0;
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
						
			if(conn.query("SELECT count(hashcode) as total from " + this.tableName + " where (added=0 AND updated > " + ts + ") OR added > " + ts))
			{
				ResultSet rs = conn.getResultSet();
				if (rs.next())
				{
					count = rs.getInt(1);
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
				ResultSet rs = conn.getResultSet();
				while(rs.next())
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
			return "<hotlist>" + builder.toString() + "</hotlist>";
		}
		
		return "";
	}
	
	public ArrayList<Column> getColumns()
	{
		return this.columns;
	}
	
	public List<HotListColumnMetaData> getHotListColumnMetaDataList() {
		return hotListColumnMetaDataList;
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public ArrayList<Column> getIndexes() {
		return indexes;
	}

	public void setIndexes(ArrayList<Column> indexes) {
		this.indexes = indexes;
	}
	
	
	
	
}
