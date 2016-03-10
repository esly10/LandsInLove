package com.cambiolabs.citewrite.data;

import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;

public class HotListColumnMetaData
{
	

	public String label = "";
	public String queryName = "";
	public String columnName = "";
	public String mapping = NONE; //used for mapping to the permit fields
	public String path = "";
	public int order = 0;
	public int displayOrder = 0;
	public boolean searchable = false;
	
	public static final String CN_COLUMN = "HOTLIST_COLUMN";
	public static final String CN_SEARCHABLE = "HOTLIST_COLUMN_SEARCH";
	private static final String HOTLIST_PATH = "HOTLIST_PATH";
	public static final String NONE = "none";
	
	public HotListColumnMetaData()
	{
		
	}
	
	public HotListColumnMetaData(String path) {
		super();
		this.path = path;
	}

	public HotListColumnMetaData(String label, String queryName,String columnName, String mapping, int displayOrder, int order) {
		super();
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
		this.order = order;
		this.displayOrder = displayOrder;
	}
	
	public HotListColumnMetaData(String label, String queryName,String columnName, String mapping, String path, int displayOrder, int order) {
		super();
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
		this.path = path;
		this.order = order;
		this.displayOrder = displayOrder;
	}

	public HotListColumnMetaData(String label, String queryName,String columnName, String mapping, String path, int order,int displayOrder, boolean searchable) {
		super();
		this.label = label;
		this.queryName = queryName;
		this.columnName = columnName;
		this.mapping = mapping;
		this.path = path;
		this.order = order;
		this.displayOrder = displayOrder;
		this.searchable = searchable;
	}

	public HotListColumnMetaData(ConfigItem item) throws Exception
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
		String sql = "DELETE from config_item where name='"+CN_COLUMN+"' OR name='"+HOTLIST_PATH+"'";
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
	
	public static ArrayList<HotListColumnMetaData> get(boolean filedOnlySearchable)
	{
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("name", CN_COLUMN));
		
		ArrayList<HotListColumnMetaData> rv = new ArrayList<HotListColumnMetaData>();
		try
		{
			ConfigItem citem = new ConfigItem();
			@SuppressWarnings("unchecked")
			ArrayList<ConfigItem> list = (ArrayList<ConfigItem>)citem.get(0, 0, "item_order ASC", filter);
			int size = list.size();
			for (int i = 0; i < size; i++) 
			{
				ConfigItem item = list.get(i);
				HotListColumnMetaData meta = new HotListColumnMetaData(item);
				if(!filedOnlySearchable){
					rv.add(meta);			
				}else if(meta.searchable){
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
		return item.commit();
	}
	
	public boolean savePath()
	{
		ConfigItem item = new ConfigItem(HOTLIST_PATH, path, 0, 0);
		return item.commit();
	}
	
	
}
