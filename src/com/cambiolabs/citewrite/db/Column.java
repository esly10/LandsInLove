package com.cambiolabs.citewrite.db;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class Column 
{
	public enum ColumnType
	{
		VARCHAR, INTEGER, TEXT, FLOAT, UNSIGNED_INTEGER
	}
	
	private String name = "Column"; //display name
	private String columnName = "column";
	private ColumnType type= ColumnType.VARCHAR;
	private int size = 255;
	private String value = "";
	private int intValue = 0;
	private int displayOrder = 10000;//make sure these are last
	private String defaultValue = null;
	
	public Column()
	{
	}
	
	public Column(String name)
	{
		this.setName(name);
	}
	
	public Column(String name, String columnName)
	{
		this.name = name;
		this.columnName = columnName;
	}
	
	public Column(String name, int displayOrder)
	{
		this.displayOrder = displayOrder;
		this.setName(name);
	}
	
	public Column(String name, String columnName, int displayOrder)
	{
		this.displayOrder = displayOrder;
		this.name = name;
		this.columnName = columnName;
	}
	
	public Column(String name, String columnName, int displayOrder, ColumnType type)
	{
		this.displayOrder = displayOrder;
		this.name = name;
		this.columnName = columnName;
		this.type = type;
		this.size = ((type == ColumnType.INTEGER || type == ColumnType.UNSIGNED_INTEGER) ? 11 : 255);
	}
	
	public Column(String name, ColumnType type)
	{
		this(name, type, (type == ColumnType.INTEGER || type == ColumnType.UNSIGNED_INTEGER) ? 11 : 255);
				
	}
	
	public Column(String name, ColumnType type, int size)
	{
		setName(name);
		setSize(size);
		setType(type);
	}
	
	public Column(String name, ColumnType type, int size, String defaultValue)
	{
		setName(name);
		setSize(size);
		setType(type);
		this.defaultValue = defaultValue;
	}
	
	public Column(Column copy, String value)
	{
		this.name = copy.getName();
		this.columnName = copy.getColumnName();
		this.size = copy.getSize();
		this.type = copy.getType();
		this.displayOrder = copy.getDisplayOrder();
		this.value = value;
	}
	
	public Column(Column copy, int value)
	{
		this.name = copy.getName();
		this.columnName = copy.getColumnName();
		this.size = copy.getSize();
		this.type = copy.getType();
		this.displayOrder = copy.getDisplayOrder();
		this.intValue = value;
	}
	
	public boolean isIntType()
	{
		return (this.type == ColumnType.INTEGER || this.type == ColumnType.UNSIGNED_INTEGER);
	}
	
	public String typeToString(DBConnection.DatabaseType dbType, ColumnType colType, int size)
	{
		String str = null;
		switch(dbType)
		{
			case MYSQL:
				switch(colType)
				{
					case INTEGER :
						str="INT("+size+")";
						break;
					case UNSIGNED_INTEGER :
						str="INT("+size+") UNSIGNED";
						break;
					case VARCHAR : 
						str="VARCHAR("+size+")";
						break;
					case FLOAT :
						str = "FLOAT";
						break;
					case TEXT :
						str = "TEXT";
						break;
				}
				break;
			case ORACLE:
				switch(colType)
				{
					case INTEGER :
					case UNSIGNED_INTEGER :
						str="NUMBER";
						break;
					case VARCHAR : 
						str="VARCHAR2("+size+")";
						break;
					case FLOAT :
						str = "FLOAT";
						break;
					case TEXT :
						str = "CLOB";
						break;
				}
				break;
			case SQLSERVER:
				switch(colType)
				{
					case INTEGER :
						str="INTEGER";
						break;
					case UNSIGNED_INTEGER :
						str="BIGINT";
						break;
					case VARCHAR : 
						str="VARCHAR("+size+")";
						break;
					case FLOAT :
						str = "FLOAT";
						break;
					case TEXT :
						str = "TEXT";
						break;
				}
				break;
			case SQLITE:
				switch(colType)
				{
					case INTEGER :
						str="INTEGER";
						break;
					case UNSIGNED_INTEGER :
						str="INTEGER UNSIGNED";
						break;
					case VARCHAR : 
						str="VARCHAR("+size+")";
						break;
					case FLOAT :
						str = "FLOAT";
						break;
					case TEXT :
						str = "TEXT";
						break;
				}				
		}
		
		return(str);
	}
	
	public String toString(DBConnection.DatabaseType dbType)
	{
		String rv = this.columnName + " " + typeToString(dbType, this.type, this.size);
		
		if(this.defaultValue != null)
		{
			rv += " DEFAULT " + this.defaultValue;
		}
		
		return rv;
	}
	
	public void setName(String name)
	{
		this.setNames(name, null);
	}
	
	public void setNames(String name, String columnName)
	{
		this.name = name;
		if(columnName == null || columnName.length() == 0)
		{
			this.setColumnName(columnNameFromName(this.name));
		}
		else
		{
			this.columnName = columnName;
		}
	}
	
	public static String columnNameFromName(String name) 	
	{
		return name.toLowerCase().replace("mpermit.", "mp_").replace("mpermit_type.", "mpt_").replace("mpermit_attribute.", "mpa_").replace("vehicle.", "v_").replace("vehicle_attribute.", "va_").replace("owner.", "o_").replace("owner_attribute.", "oa_").replace(' ', '_').replace('.', '_');
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setType(ColumnType type)
	{
		this.type = type;
		if (type == ColumnType.INTEGER)
			setSize(11);
	}
	
	public void setType(String type)
	{
		if (type.equals("INTEGER"))
		{
			this.type=ColumnType.INTEGER;
			this.setSize(11);
		}
		else if(type.equals("VARCHAR"))
			this.type=ColumnType.VARCHAR;
		else if(type.equals("TEXT"))
			this.type=ColumnType.TEXT;
		else if(type.equals("FLOAT"))
			this.type=ColumnType.FLOAT;
		else if(type.equals("UNSIGNED_INTEGER"))
		{
			this.type=ColumnType.UNSIGNED_INTEGER;
			this.setSize(11);
		}
			
	}
	
	public ColumnType getType()
	{
		return this.type;
	}
	
	public void setSize(int size)
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return this.size;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public int getIntValue(){
		return this.intValue;
	}
	
	public void setDisplayOrder(int displayOrder)
	{
		this.displayOrder = displayOrder;
	}
	
	public int getDisplayOrder()
	{
		return this.displayOrder;
	}
	
	public void setDefaultValue(String dv)
	{
		this.defaultValue = dv;
	}
	
	public String getDefaultValue()
	{
		return this.defaultValue;
	}
	
	public static String list(ArrayList<Column> columns)
	{
		return list(columns, ",");
	}
	
	public static String list(ArrayList<Column> columns, String separator)
	{
		String[] names = new String[columns.size()];
		for(int i = 0; i < names.length; i++)
		{
			Column c = columns.get(i);
			names[i] = c.getColumnName();
		}
		
		return StringUtils.join(names, separator);
	}
	
	public static String sqlValueList(Vector<Column> columns)
	{
		return "(" + valueList(columns) + ")";
	}
	
	public static String valueList(Vector<Column> columns)
	{
		String[] names = new String[columns.size()];
		for(int i = 0; i < names.length; i++)
		{
			Column c = columns.get(i);
			if(c.isIntType())
			{
				names[i] = String.valueOf(c.getIntValue());
			}
			else
			{
				names[i] = "'"+StringEscapeUtils.escapeSql(c.getValue())+"'";
			}
		}
		
		return StringUtils.join(names, ",");
	}
	
	
}
