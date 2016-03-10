package com.cambiolabs.citewrite.data;

import java.io.File;

import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.SQLiteDB;
import com.cambiolabs.citewrite.license.LicenseManager;
import com.cambiolabs.citewrite.util.CodesWatcher;
import com.cambiolabs.citewrite.util.FileWatcher;
import com.cambiolabs.citewrite.util.PermitWatcher;

import java.sql.ResultSet;

import java.util.*;
import java.util.concurrent.Semaphore;


public class Config 
{

	public static Version VERSION = new Version(2, 0,43);

	public static int MAX_DEVICES = 0; //without a license we don't allow for any more w
	
	public static PermitTable permitTable = null;
	public static HotListTable hotlistTable = null;
	public static File downloadDatabase = null;
	public static File downloadCodesDatabase = null; // this is used for remote devices that only need the codes
	
	public static String citationFormat = null;
	
	public static Semaphore dataWriteSemaphore = new Semaphore(1);
	public static Semaphore downloadDBSemaphore = new Semaphore(1);
	public static Semaphore downloadInProgress = new Semaphore(Integer.MAX_VALUE);
	
	private static long minUpdateTS = -1;
	private static long minCodeUpdateTS = -1;
	
	public static final String URL_GUEST_LOGIN = "/guest/login";
	public static final String ENABLE_EDIT = "ENABLE_EDIT";
	public static final String ENABLE_REGISTRATION = "ENABLE_REGISTRATION";

	public static void load(File dbPath, Timer timer) 
	{
		try 
		{
			DBConnection conn = new DBConnection();
			conn.setDatabaseType();
			conn.close(); //only need to set the database type
			
			Config.downloadDatabase = new File(dbPath, "mobile-download.db");
			Config.downloadCodesDatabase = new File(dbPath, "mobile-codes-download.db");

			if(LicenseManager.isManagedPermitsEnabled())
			{
				//create the manageed permit table
				permitTable = new ManagedPermitTable();
				permitTable.load();
				
				PermitWatcher watcher = new PermitWatcher(){
				      protected void onChange() {
				    	  System.out.println("Updating from config change!");
				    	  permitChanged();
				        }
				      };
				timer.scheduleAtFixedRate(watcher, FileWatcher.delay, FileWatcher.poll);
						
			}
			else
			{
				ConfigItem permitPath = ConfigItem.lookup("PERMIT_PATH");
				if (permitPath != null && permitPath.text_value.length() > 0) 
				{
					File permitFile = new File(permitPath.text_value);
					if(permitFile.exists())
					{
						permitTable = new PermitTable(permitFile);
						if(timer != null)
						{
							FileWatcher watcher = new FileWatcher(permitFile){
							      protected void onChange(File file ) {
							          Config.permitChanged();
							        }
							      };
							timer.scheduleAtFixedRate(watcher, FileWatcher.delay, FileWatcher.poll);
						}
						permitTable.load();
					}
					else
					{
						System.err.println("Permit file, " + permitPath.text_value + ", does not exist.");
					}
				}
			}

			ConfigItem hotlistPath = ConfigItem.lookup("HOTLIST_PATH");
			if (hotlistPath != null && hotlistPath.text_value.length() > 0) 
			{
				File hotlistFile = new File(hotlistPath.text_value);
				if(hotlistFile.exists())
				{		
					hotlistTable = new HotListTable(hotlistFile);
					if(timer != null)
					{
						FileWatcher watcher = new FileWatcher(hotlistFile){
						      protected void onChange(File file) {
						          Config.hotlistChanged();
						        }
						      };
						timer.scheduleAtFixedRate(watcher, FileWatcher.delay, FileWatcher.poll);
					}
					hotlistTable.load();
				}
				else
				{
					System.err.println("Hotlist file, " + hotlistPath.text_value + ", does not exist.");
				}
			}
			
			
			//this will watch and make sure the sql lite db gets updated after a code update
			CodesWatcher watcher = new CodesWatcher(){
			      protected void onChange() {
			    	  System.out.println("Updating from config change!");
			          Config.createSQLiteDB();
			        }
			      };
			timer.scheduleAtFixedRate(watcher, FileWatcher.delay, FileWatcher.poll);
								
			createSQLiteDB();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean copyEntriesToSQLite(DBConnection conn, SQLiteDB sqlite, String tableName, ArrayList<Column> cols, String where)
	{
		try
		{
			Vector<Object> row = null;
			String sql = "SELECT "+Column.list(cols)+" FROM "+tableName;
			if (where.trim().length() > 0)
			{
				sql += " WHERE "+where;
			}
			
			String insert = "INSERT INTO "+tableName+" ("+Column.list(cols)+") values (";
			for(int i=0;i<cols.size(); i++)
			{
				if (i==0)
					insert += " ?";
				else
					insert += ", ?";
			}
			insert += " )";
			sqlite.prepare(insert);
			if (conn.query(sql))
			{
				while((row = conn.fetchObjects(cols)) != null)
				{
					sqlite.bindExec(row.toArray(), row.size());
				}
			}
			return(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return(false);
	}
	
	public static boolean createSQLiteDB()
	{
		SQLiteDB sqlite = null;
		DBConnection conn = null;
		try
		{
			downloadDBSemaphore.acquire();
			
			//wait until the file isn't being downloaded
			try
			{
				while(downloadInProgress.availablePermits() != Integer.MAX_VALUE)
				{
					Thread.sleep(1000); //check every second 	
				}
			}
			catch(Exception e)
			{
			}
			// create the database

			Config.downloadDatabase.delete();
			Config.downloadDatabase.createNewFile();
			sqlite = new SQLiteDB(Config.downloadDatabase);
			conn = new DBConnection();
			
			sqlite.transaction();
			
			ArrayList<Column> cols = null;
			if(Config.permitTable != null)
			{
				cols = Config.permitTable.getColumns();
				if(cols != null)
				{
					sqlite.buildTable(Config.permitTable.getTableName(), cols);
					copyEntriesToSQLite(conn, sqlite, Config.permitTable.getTableName(), cols, "added != 0");
					
					//create indexes
					for(Column index: Config.permitTable.getIndexes())
					{
						try
						{
							sqlite.execute("CREATE INDEX "+index.getColumnName()+"_index on " + Config.permitTable.getTableName() + " ("+index.getColumnName()+")");
						}
						catch(Exception e){} //error during creating an index
					}
					
					sqlite.execute("CREATE INDEX permit_hashcode on " + Config.permitTable.getTableName() + " (hashcode)");
				}
			}
			
			if(Config.hotlistTable != null)
			{
				cols = Config.hotlistTable.getColumns();
				if(cols != null)
				{
					sqlite.buildTable(Config.hotlistTable.getTableName(), cols);
					copyEntriesToSQLite(conn, sqlite, Config.hotlistTable.getTableName(), cols, "added != 0");
					
					//create indexes
					for(Column index: Config.hotlistTable.getIndexes())
					{
						try
						{
							sqlite.execute("CREATE INDEX "+index.getColumnName()+"_index on " + Config.hotlistTable.getTableName() + " ("+index.getColumnName()+")");
						}
						catch(Exception e){} //error during creating an index
					}					
					
					sqlite.execute("CREATE INDEX hotlist_hash on " + Config.hotlistTable.getTableName() + " (hashcode)");
				}
			}
			
			CodeTable codeTable = new CodeTable();
			cols = codeTable.getColumns();
			sqlite.buildTable(codeTable.getTableName(), cols);
			copyEntriesToSQLite(conn, sqlite, codeTable.getTableName(), cols, "added != 0");
			
			sqlite.execute("CREATE INDEX codes_id on " + codeTable.getTableName() + " (type, codeid)");
			sqlite.execute("CREATE INDEX codes_description on " + codeTable.getTableName()	+ " (type, description)");
			
			sqlite.commit();
			try
			{
				sqlite.close();
			}
			catch(Exception e){}
			sqlite = null;
						
			//this files is used for codes only
			Config.downloadCodesDatabase.delete();
			Config.downloadCodesDatabase.createNewFile();
			sqlite = new SQLiteDB(Config.downloadCodesDatabase);
			conn = new DBConnection();
			
			sqlite.transaction();
			
			//create empty tables for the permit and hotlist
			if(Config.permitTable != null)
			{
				cols = Config.permitTable.getColumns();
				if(cols != null)
				{
					sqlite.buildTable(Config.permitTable.getTableName(), cols);
				}
			}
			
			if(Config.hotlistTable != null)
			{
				cols = Config.hotlistTable.getColumns();
				if(cols != null)
				{
					sqlite.buildTable(Config.hotlistTable.getTableName(), cols);
				}
			}
						
			//actual table for the codes
			cols = codeTable.getColumns();
			sqlite.buildTable(codeTable.getTableName(), cols);
			copyEntriesToSQLite(conn, sqlite, codeTable.getTableName(), cols, "added != 0");
			
			sqlite.execute("CREATE INDEX codes_id on " + codeTable.getTableName() + " (type, codeid)");
			sqlite.execute("CREATE INDEX codes_description on " + codeTable.getTableName()	+ " (type, description)");
			
			sqlite.commit();
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			downloadDBSemaphore.release();
			if(sqlite != null)
			{
				try
				{
					sqlite.close();
				}
				catch(Exception e){}
				sqlite = null;
			}
			
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
		
		return false;
	}

	public static void permitChanged()
	{
		//create delta
		permitTable.load();
		createSQLiteDB();
	}
	
	public static void hotlistChanged()
	{
		//create delta
		hotlistTable.load();
		createSQLiteDB();
	}
	
	public static void resetMinUpdate()
	{
		minUpdateTS = -1;
		minCodeUpdateTS = -1;
	}
	public static long getMinUpdate()
	{
		if(minUpdateTS > -1)
		{
			return minUpdateTS;
		}
				
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
						
			//get deletes
			if (conn.query("select min(u.updated) from (select updated from permit union select updated from codes"+ (Config.hotlistTable != null ? " union select updated from hotlist":"")+") u"))
			{
				ResultSet rs = conn.getResultSet();
				if (rs.next())
				{
					minUpdateTS = rs.getInt(1);
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
		
		return minUpdateTS;
	}			
	public static long getMinCodeUpdate()
	{
		if(minCodeUpdateTS > -1)
		{
			return minCodeUpdateTS;
		}
				
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
						
			//get deletes
			if (conn.query("select min(updated) from codes"))
			{
				ResultSet rs = conn.getResultSet();
				if (rs.next())
				{
					minCodeUpdateTS = rs.getInt(1);
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
		
		return minCodeUpdateTS;
	}		
	
	public static String getPermitFormat()
	{
		String rv = "<fields>";
		
		ArrayList<PermitColumnMetaData> metas = PermitColumnMetaData.get();
		for(PermitColumnMetaData meta: metas)
		{
			rv += meta.toXmlString();
		}
		
		return rv+"</fields>";
	}
	
	public static String getHotListFormat()
	{
		String rv = "<fields>";
		
		ArrayList<HotListColumnMetaData> metas = HotListColumnMetaData.get(false);
		for(HotListColumnMetaData meta: metas)
		{
			rv += meta.toXmlString();
		}
		
		return rv+"</fields>";
	}
	
	public static boolean isEnableRegistration (){
		ConfigItem item;
		item = ConfigItem.lookup(ENABLE_REGISTRATION);
		return item.int_value == 1;
	}
	
	public static boolean isEnableEdit(){
		ConfigItem item;
		item = ConfigItem.lookup(ENABLE_EDIT);
		return item.int_value == 1;
	}
	
}
