package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;


public class ManagedPermitTable extends PermitTable
{
	public ManagedPermitTable()
	{
		super();
		
		DBConnection conn = null;
		try
		{			
			this.metaData = PermitColumnMetaData.get();
			int size = this.metaData.size();
			if (size > 0) 
			{
				this.deleteSQL = "UPDATE " + this.tableName + " set ";
				this.updateSQL = "UPDATE " + this.tableName + " set updated=? where hashcode IN (select hashcode from "+this.tableName + "_temp) AND added>0";
				this.insertSQL = " VALUES (";
				
				this.columns = new ArrayList<Column>();
				this.columns.add(new Column("owner.owner_id", Column.ColumnType.UNSIGNED_INTEGER));
				this.columns.add(new Column("vehicle.vehicle_id", Column.ColumnType.UNSIGNED_INTEGER));
				this.columns.add(new Column("mpermit.permit_number", Column.ColumnType.VARCHAR, 255));
				
				this.deleteSQL += Column.columnNameFromName("owner.") + "owner_id = 0,";
				this.deleteSQL += Column.columnNameFromName("vehicle.") + "vehicle_id = 0,";
				this.deleteSQL += Column.columnNameFromName("mpermit.") + "permit_number = NULL,";
				
				this.indexes = new ArrayList<Column>();
				for (int i = 0; i < size; i++) 
				{
					PermitColumnMetaData meta = this.metaData.get(i);
					if(meta.columnName.equals("mp_permit_number"))//we already have it
					{
						continue;
					}
					Column column = new Column(meta.label, meta.columnName);
						
					this.columns.add(column);
					
					this.insertSQL += "?,";
					this.deleteSQL += column.getColumnName() + "=";
					if(column.isIntType())
					{
						this.deleteSQL += "0, ";
					}
					else
					{
						this.deleteSQL += "NULL, ";
					}
					
					//see if this is a searchable column
					if(meta.searchable)
					{
						this.indexes.add(column);
					}
				}
				
				//include data and time column
				this.columns.add(new Column("added", Column.ColumnType.INTEGER));
				this.columns.add(new Column("updated", Column.ColumnType.INTEGER));
				this.columns.add(new Column("hashcode", Column.ColumnType.UNSIGNED_INTEGER));
				insertSQL += "?, ?, ?)";
				
				conn = new DBConnection();
				if (conn.buildTable(this.tableName, this.columns)) 
				{
					//create index based on searchable fields
					for(Column index: this.indexes)
					{
						try
						{
							conn.execute("CREATE INDEX "+index.getColumnName()+"_index on " + this.tableName + " ("+index.getColumnName()+")");
						}
						catch(Exception e){} //error during creating an index
					}
					
					conn.execute("CREATE INDEX permit_hashcode on " + this.tableName + " (hashcode)");
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
	}
	
	
	public void load()
	{
		DBConnection conn = null;
		try
		{
			PreparedStatement ps;
			String tempTable = this.tableName + "_temp";
			
			String ts = String.valueOf((int)(System.currentTimeMillis()/1000));
			conn = new DBConnection();
			conn.setAutoCommit(false);
			
			if (conn.buildTable(tempTable, this.columns)) 
			{
				conn.execute("CREATE INDEX permit_temp_hashcode on " + tempTable + " (hashcode)");
			}

			//build sql insert statement
			
			int size = this.metaData.size();
			if (size > 0) 
			{
				String insert = "INSERT into "+tempTable+" (o_owner_id, v_vehicle_id, mp_permit_number, ";
				String select = "SELECT owner.owner_id, vehicle.vehicle_id, mpermit.permit_number, ";
				String join = 	" FROM owner "+
								"JOIN mpermit on mpermit.owner_id=owner.owner_id "+
								"JOIN mpermit_type on mpermit.mpermit_type_id=mpermit_type.mpermit_type_id "+
								"JOIN mpermit_to_vehicle on mpermit_to_vehicle.mpermit_id=mpermit.mpermit_id "+
								"JOIN vehicle on vehicle.vehicle_id=mpermit_to_vehicle.vehicle_id ";
				

				ArrayList<String> selectColumns = new ArrayList<String>();
				ArrayList<String> binds = new ArrayList<String>();
				binds.add(ts); //added
				binds.add(ts); //updated
				for (int i = 0; i < size; i++) 
				{
					PermitColumnMetaData meta = this.metaData.get(i);
					if(meta.columnName.equals("mp_permit_number"))//we already have it
					{
						continue;
					}
					insert += meta.columnName + ", ";
					
					String[] dbParts = meta.queryName.split("\\.");
					if(dbParts.length == 2)
					{
						String table = dbParts[0];
						String column = dbParts[1]; //this is also the attribute name
						int index = table.indexOf("_attribute"); 
						if(index != -1)
						{
							String mainTable = table.substring(0, index);
							String alias = "attr_"+i;
							select += alias+".value, ";
							selectColumns.add(alias+".value");
							join += "JOIN "+table+" "+alias+" on "+alias+"."+mainTable+"_id="+mainTable+"."+mainTable+"_id AND "+alias+".name=? ";
							
							binds.add(column);
						}
						else
						{
							if(column.equalsIgnoreCase("state") || column.equalsIgnoreCase("make") || column.equalsIgnoreCase("color"))
							{
								select += meta.queryName + "_id, ";
								selectColumns.add(meta.queryName + "_id");
							}
							else
							{
								select += meta.queryName + ", ";
								selectColumns.add(meta.queryName);
							}
						}
					}
					else
					{
						select += meta.queryName + ", ";
						selectColumns.add(meta.queryName);
					}
				}
				
				String sql = insert + "added, updated, hashcode) " + select ;
				
				if(conn.isSQLServer())
				{
					sql += "?, ?, CHECKSUM(owner.owner_id, vehicle.vehicle_id, mpermit.permit_number, ";
					sql += StringUtils.join(selectColumns.toArray(new String[0]), ","); //use a pipe to create more uniqueness
					sql += ") ";
				}
				else if(conn.isOracle())
				{
					sql += "?, ?, OWA_OPT_LOCK.CHECKSUM(owner.owner_id || vehicle.vehicle_id || mpermit.permit_number || ";
					sql += StringUtils.join(selectColumns.toArray(new String[0]), " || "); //use a pipe to create more uniqueness
					sql += ") ";
				}
				else //mysql
				{
					sql += "?, ?, CRC32(CONCAT(owner.owner_id,'|',vehicle.vehicle_id,'|',mpermit.permit_number" + (!selectColumns.isEmpty() ? ",":" ");
					sql += StringUtils.join(selectColumns.toArray(new String[0]), ",'|',"); //use a pipe to create more uniqueness
					sql += ")) ";
				}
				
				sql += join;
				
				
				sql += " GROUP BY mpermit.mpermit_id, vehicle.vehicle_id";
				if(conn.isSQLServer() || conn.isOracle())
				{
					sql += ", owner.owner_id, mpermit.permit_number," + StringUtils.join(selectColumns.toArray(new String[0]), ",");
				}
				
				if((ps=conn.prepare(sql)) != null)
				{
					String[] arBinds = binds.toArray(new String[0]);
					conn.bindExec(ps, arBinds, arBinds.length);
				}
				
				if((ps=conn.prepare(this.updateSQL)) != null)
				{
					String[] updateBindings = {ts};//updated
					conn.bindExec(ps, updateBindings, 1);
				}
				
				//copy over new columns
				String columnList = Column.list(this.columns);
				String copy = "insert into "+this.tableName+" ("+columnList+") " +
								"select "+columnList+" from "+tempTable+" t1 " +
								"where NOT EXISTS (select * from "+this.tableName+" t2 where t1.hashcode = t2.hashcode AND t2.added > 0) ";
				conn.execute(copy);
			
				conn.execute("DROP table " + tempTable);
				
			}
			
			conn.execute(deleteSQL + "added=0, updated=" + ts + " where updated !=" + ts + " AND added>0");
			
		} 
		catch (Exception e) 
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
}
