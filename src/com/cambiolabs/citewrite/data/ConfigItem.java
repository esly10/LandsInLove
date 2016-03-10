package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class ConfigItem extends DBObject
{	
	@Expose public int config_item_id = 0;
	@Expose public String name = null;
	@Expose public String text_value = null;
	@Expose public int int_value = 0;
	@Expose public int item_order = 0;
	
	
	public ConfigItem(String name, String text_value, int int_value, int item_order)
	{
		super("config_item", "config_item_id");
		this.name = name;
		this.text_value = text_value;
		this.int_value = int_value;
		this.item_order = item_order;
	}
	
	public ConfigItem(int config_item_id, String name, String text_value, int int_value, int item_order)
	{
		super("config_item", "config_item_id");
		this.config_item_id = config_item_id;
		this.name = name;
		this.text_value = text_value;
		this.int_value = int_value;
		this.item_order = item_order;
	}
	
	public ConfigItem()
	{
		super("config_item", "config_item_id");
	}
	
	public ConfigItem(int item_id) throws UnknownObjectException
	{
		this();
		if(item_id > 0)
		{
			this.config_item_id = item_id;
			this.populate();
		}
	}
	
	public ConfigItem(String name) throws UnknownObjectException
	{
		this();
		this.name = name;
		this.populate();
	}
	
	public ConfigItem(String name, String textValue) throws UnknownObjectException
	{
		this();
		this.name = name;
		this.text_value = textValue;
		this.populate();
	}
	
	public ConfigItem(String name, int intValue) throws UnknownObjectException
	{
		this();
		this.name = name;
		this.int_value = intValue;
		this.populate();
	}

	public int getConfigItemID() {
		return config_item_id;
	}

	public void setConfigItemID(int config_item_id) {
		this.config_item_id = config_item_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTextValue() {
		return text_value;
	}

	public void setTextValue(String text_value) {
		this.text_value = text_value;
	}

	public int getIntValue() {
		return int_value;
	}

	public void setIntValue(int int_value) {
		this.int_value = int_value;
	}

	public int getItemOrder() {
		return item_order;
	}

	public void setItemOrder(int item_order) {
		this.item_order = item_order;
	}
	
	public static ConfigItem lookup(String name)
	{
		ConfigItem item = null;
		try
		{
			item = new ConfigItem(name);
		}
		catch(Exception e)
		{
			try
			{
				item = new ConfigItem();
				item.name = name;
				item.text_value = "";
			}
			catch(Exception ignore){}
		}
		
		return item;
	}
	
	public static ConfigItem find(String name)
	{
		try
		{
			return new ConfigItem(name);
		}
		catch(Exception e)
		{
			
		}
		return null;
	}
}
