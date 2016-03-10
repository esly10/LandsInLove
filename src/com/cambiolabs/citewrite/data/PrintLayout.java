package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.google.gson.annotations.Expose;

public class PrintLayout
{
	
	public static final String CONFIG_ROOT_NAME = "PRINT_FORMAT";
	private static final String PRINT_FORMAT_NEXT = "PRINT_FORMAT_NEXT";
	private static final String PRINT_FORMAT_NAME = "PRINT_FORMAT_NAME";
	private static final String PRINT_FORMAT_XML = "PRINT_FORMAT_XML";
	private static final String PRINT_FORMAT_DEFAULT = "PRINT_FORMAT_DEFAULT";
	
	@Expose
	private String name = null;
	@Expose
	private String value = null;
	@Expose
	private int groupId;
	@Expose
	private boolean isDefault;
	
	public PrintLayout() {
		super();
	}
	
	public PrintLayout(String name, String value, int groupId, boolean isDefault) {
		super();
		this.name = name;
		this.value = value;
		this.groupId = groupId;
		this.isDefault = isDefault;
	}

	public PrintLayout(ResultSet resultSet) throws SQLException {
		this.name = resultSet.getString("name");//$NON-NLS-1$
		this.value = resultSet.getString("xml");//$NON-NLS-1$
		this.groupId = resultSet.getInt("groupid");//$NON-NLS-1$
		this.isDefault = resultSet.getInt("defaultgroupid") == groupId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public Boolean getDefaultValue() {
		return isDefault;
	}

	public void setDefaultValue(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public static ArrayList<PrintLayout> load(String sort, String dir, String filter) throws Exception
	{
		QueryBuilder qb = null;
		ArrayList<PrintLayout> printLayoutList = new ArrayList<PrintLayout>();
		DBFilterList dBFilterList = new DBFilterList();
		
		try
		{
			
			qb = new QueryBuilder("config_item");//$NON-NLS-1$
			qb.field("config_item.text_value name")//$NON-NLS-1$
				.field("config_item.int_value groupid")//$NON-NLS-1$
				.field("config_xml.text_value xml")//$NON-NLS-1$
				.field("config_default.int_value defaultGroupId");//$NON-NLS-1$

			qb.join("config_item config_xml", "config_xml.int_value = config_item.int_value AND config_xml.name like 'PRINT_FORMAT_XML%'")//$NON-NLS-1$
			.join("config_item config_default", "config_default.name = '" + PRINT_FORMAT_DEFAULT + "' AND config_default.int_value =  config_item.int_value");//$NON-NLS-1$
			dBFilterList.add(new DBFilter("config_item.name", "LIKE", "PRINT_FORMAT_NAME%"));//$NON-NLS-1$
			
			if(filter != null && !filter.isEmpty()){
				dBFilterList.add(new DBFilter("config_item.text_value", "LIKE", filter));//$NON-NLS-1$
			}
			
			DBConnection configList = qb.orderBy(sort).orderDir(dir).where(dBFilterList).query();
			ResultSet resultSet = configList.getResultSet();
			
			while (resultSet.next()) {
				printLayoutList.add(new PrintLayout(resultSet));	
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		

		return printLayoutList;
	}
	
	public boolean save () throws Exception
	{
		boolean save = false;
		ConfigItem item;

		try {
			
			if(groupId< 0){
				this.groupId = getNextPrintFormatId();
			}
			
			item = ConfigItem.lookup(PRINT_FORMAT_XML + "_" + groupId); 
			item.setTextValue(value);
			item.setIntValue(groupId);
			item.commit();
			
			item = ConfigItem.lookup(PRINT_FORMAT_NAME + "_" + groupId); 
			item.setTextValue(name);
			item.setIntValue(groupId);
			item.commit();
			
			item = ConfigItem.lookup(PRINT_FORMAT_DEFAULT); 
			if(isDefault){
				item.setIntValue(groupId);
				item.commit();
			}else{
				if(item.int_value == groupId){
					item.delete();
				}
			}
			
			save = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return save;
	}
	
	public static void clear(int groupId) throws Exception
	{

		String sql = "DELETE from config_item where name=? or name=?";
		DBConnection connection = null;
		ConfigItem item;
		
		try
		{
			item = ConfigItem.lookup(PRINT_FORMAT_DEFAULT); 
			if(item.int_value == groupId){
				item.delete();
			}
					
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare(sql);
			
			pst.setString(1,PRINT_FORMAT_NAME + "_" + groupId);
			pst.setString(2,PRINT_FORMAT_XML + "_" + groupId);
			
			connection.execute(pst);
			
			sql = "UPDATE device SET format_index = 0 where format_index=?";

			pst = connection.prepare(sql);
			pst.setInt(1, groupId);
			
			connection.execute(pst);
			
		}catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			if(connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		
	}
	
	private int getNextPrintFormatId () throws Exception{
		
		ConfigItem item = null;
		
		try
		{
			item = ConfigItem.lookup(PRINT_FORMAT_NEXT);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		item.int_value += 1;
		item.commit();
		int rv = item.int_value;
		return rv;
	}
	
	public static String getDefaultPrintFormat(){
		
		ConfigItem item;
		int groupId;
		
		try
		{
			
			item = ConfigItem.lookup(PRINT_FORMAT_DEFAULT); 
			groupId = item.int_value;
			item = ConfigItem.lookup(PRINT_FORMAT_XML + "_" + groupId); 
			return item.text_value;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getPrintFormat(int groupId){
		
		try
		{
			ConfigItem item;
			item = ConfigItem.lookup(PRINT_FORMAT_XML + "_" + groupId); 
			return item.text_value;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
} 
