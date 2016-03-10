package com.cambiolabs.citewrite.data;

import java.util.ArrayList;
import java.util.TreeSet;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.google.gson.annotations.Expose;

public class PermitColumnMetaData
{

	@Expose public String label = "";
	@Expose public String queryName = "";
	@Expose public String columnName = "";
	@Expose public String mapping = NONE;
	@Expose public int displayOrder = 0;
	@Expose public boolean searchable = false;
	public String path = "";
	public int order = 0;
	public static String CN_COLUMN = "PERMIT_COLUMN";
	public static String CN_SEARCHABLE = "PERMIT_COLUMN_SEARCH";
	private static final String PERMIT_PATH = "PERMIT_PATH";
	public static final String NONE = "none";
		
	public PermitColumnMetaData()
	{
		
	}
	
	public PermitColumnMetaData(String path) {
		super();
		this.path = path;
	}

	public PermitColumnMetaData(String label, String queryName, String columnName, String mapping)
	{
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
	}
	
	public PermitColumnMetaData(String label, String queryName, String columnName, String mapping, int displayOrder)
	{
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
		this.displayOrder = displayOrder;
		this.order = displayOrder;
	}
	
	public PermitColumnMetaData(String label, String queryName, String columnName, String mapping, int displayOrder, int order, boolean searchable)
	{
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
		this.displayOrder = displayOrder;
		this.searchable = searchable;
		this.order = order;
	}
	                   
	public PermitColumnMetaData(ConfigItem item) throws Exception
	{
		
		this.label = item.text_value;
		this.displayOrder = item.int_value;
		this.order = item.item_order;
		
		String[] parts = this.label.split("::");
		if(parts.length == 4)
		{
			this.label = parts[0]; 
			this.queryName = parts[1];
			this.columnName = parts[2];
			this.mapping = parts[3];
		}
		else
		{
			throw new Exception("Invalid column configuration");
		}
		
		try
		{
			ConfigItem si = new ConfigItem(CN_SEARCHABLE, this.getSearchableName());
			if(si != null)
			{
				this.searchable = true;
			}
		}
		catch(Exception UnknownObjectException){}		
	}
	
	public static boolean clear()
	{
		String sql = "DELETE from config_item where name='"+CN_COLUMN+"' OR name='"+CN_SEARCHABLE+"' OR name='"+PERMIT_PATH+"'";
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			return connection.execute(sql);
		}
		catch(Exception e){}
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
	
	public static ArrayList<PermitColumnMetaData> get(){
		return get(false);
	}
	
	public static ArrayList<PermitColumnMetaData> get(boolean filedOnlySearchable)
	{
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("name", CN_COLUMN));

		ArrayList<PermitColumnMetaData> rv = new ArrayList<PermitColumnMetaData>();
		try
		{
			ConfigItem citem = new ConfigItem();
			@SuppressWarnings("unchecked")
			ArrayList<ConfigItem> list = (ArrayList<ConfigItem>)citem.get(0, 0, "item_order ASC", filter);
			int size = list.size();
			for (int i = 0; i < size; i++) 
			{
				ConfigItem item = list.get(i);
				PermitColumnMetaData meta = new PermitColumnMetaData(item);	
				if(!filedOnlySearchable)
				{
					rv.add(meta);			
				}
				else if(meta.searchable)
				{
					rv.add(meta);	
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return rv;
	}
	
	public String toXmlString()
	{
		return "<field mapping=\""+this.mapping+"\" display-order=\""+this.displayOrder+"\" searchable=\""+Boolean.toString(this.searchable)+"\" name=\""+this.columnName+"\"><![CDATA["+this.label+"]]></field>";
	}
	
	public String toString()
	{
		return this.label + "::" + this.queryName + "::" + this.columnName + "::" + this.mapping;
	}
	
	public String getSearchableName()
	{
		return this.columnName;
	}
	
	public boolean save()
	{
		ConfigItem item = new ConfigItem(CN_COLUMN, this.toString(), this.displayOrder, this.order);
		if(!item.commit())
		{
			return false;
		}
		
		if(this.searchable)
		{
			item = new ConfigItem(CN_SEARCHABLE, this.getSearchableName(), 0, 0);
			item.commit();
		}
		
		return true;
	}
	
	public boolean savePath()
	{
		ConfigItem item = new ConfigItem(PERMIT_PATH, path, 0, 0);
		return item.commit();
	}
	
}
