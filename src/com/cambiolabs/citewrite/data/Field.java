package com.cambiolabs.citewrite.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Xml;
import com.google.gson.annotations.Expose;

public class Field implements Comparator<Field>
{
	public static String TYPE_TEXT = "text";
	public static String TYPE_LIST = "list";
	public static String TYPE_STANDARD = "standard";
	public static String TYPE_CODES = "codes";
	public static String TYPE_DB = "database";
	
	@Expose public String name = "";
	@Expose public String label = "";
	@Expose public String type = TYPE_STANDARD;
	@Expose public boolean required = false;
	@Expose public String tableName = "";
	@Expose public String idField = "";
	@Expose public String descField = "";
	@Expose public String where = "";
	@Expose public String validation = "";
	
	@Expose public ArrayList<FieldOption> options = new ArrayList<FieldOption>(); 
	 	
	public Field()
	{
		
	}		
	public Field(String label, String type)
	{
		this(null, label, type);
	}
	
	public Field(String name, String label, String type)
	{
		this.name = name;
		this.label = label;
		this.type = type;
		
		if(this.name == null || this.name.length() == 0)
		{
			this.name = label.toLowerCase().replace(' ', '_');
		}
	}
	
	public Field(String label, String table, String id, String desc, String where)
	{
		this(null, label, null, table, id, desc, where);
	}
	
	public Field(String label, String type, String table, String id, String desc, String where)
	{
		this(null, label, type, table, id, desc, where);
	}
	
	public Field(String name, String label, String type, String table, String id, String desc, String where)
	{
		this(name, label, type);
		if(this.type == null)
		{
			this.type = Field.TYPE_DB;
		}
		
		this.setDatabaseOption(table, id, desc, where);
	}
	
	public Field(HttpServletRequest request)
	{
		this.name = request.getParameter("field-name");
		this.label = request.getParameter("field-label");
		this.type = request.getParameter("field-value-type");
		
		if(this.name == null || this.name.length() == 0)
		{
			this.name = this.label.toLowerCase().replace(' ', '_');
		}
		
		String strRequired = request.getParameter("field-required");
		this.required = (strRequired != null && strRequired.equals("on"));
		
		if(this.type.equals(Field.TYPE_LIST))
		{
			int count = 0;
		
			try
			{
				String strCount = request.getParameter("option-count");
				count = Integer.parseInt(strCount);
				
				
				
				if(count > 0)
				{
					for(int i = 1; i <= count; i++)
					{
						
						String id = request.getParameter("option_id_"+i);
						String value = request.getParameter("option_value_"+i);
						
						if(id != null && id.length() > 0)
						{
							this.addOption(new FieldOption(id.trim(), value.trim()));						
							
						}
					}
				}
			}
			catch(NumberFormatException nfe){}
		}
		else if(this.type.equals(Field.TYPE_DB))
		{
			this.setDatabaseOption(request.getParameter("field-table-name"), 
					request.getParameter("field-id-field"), 
					request.getParameter("field-description-field"),
					request.getParameter("field-where"));
		}
		else if(this.type.equals(Field.TYPE_TEXT))
		{
			this.setValidation(request.getParameter("field-validation"));
		}
	}

	public Field(Element field)
	{
		this.name = field.getAttribute("name");
		this.type = field.getAttribute("type");
		Element eLabel = Xml.getElementByName(field, "label");
		if(eLabel != null)
		{
			this.label = Xml.getElementContent(eLabel);
		}
		
		this.required = false;
		String attrRequired = field.getAttribute("required");
		if(attrRequired != null)
		{
			this.required = Boolean.parseBoolean(attrRequired);
		}
		
		if(this.type.equals(TYPE_LIST))
		{
			Element eList = Xml.getElementByName(field, "list");
			if(eList != null)
			{
				NodeList list = eList.getElementsByTagName("item");
				int size = list.getLength();
				for(int i = 0; i < size; i++)
				{
					Element eItem = (Element)list.item(i);
					String id = eItem.getAttribute("id");
					String value = Xml.getElementContent(eItem);
					
					this.options.add(new FieldOption(id, value));
				}		
			}
		}
		else if(this.type.equals(TYPE_DB))
		{
			Element table = Xml.getElementByName(field, "table");
			if(table != null)
			{
				this.tableName = table.getAttribute("name");
				this.idField = table.getAttribute("id-field");
				this.descField = table.getAttribute("desc-field");
				this.where = Xml.getElementContent(table);
			}
		}
		else if(this.type.equals(TYPE_TEXT))
		{
			Element valid = Xml.getElementByName(field, "validation");
			if(valid != null)
			{
				this.validation = Xml.getElementContent(valid);
			}
		}
	}
		
	public Element toElement(Document document)
	{
		return toElement(document, false);
	}
	
	public Element toElement(Document document, boolean toExport)
	{
		Element field = document.createElement("field");
		field.setAttribute("name", this.name);
		field.setAttribute("type", this.type);
		field.setAttribute("required", String.valueOf(this.required));
		
		Element label = document.createElement("label");
		CDATASection cdata = document.createCDATASection(this.label);
		
		label.appendChild(cdata);
		field.appendChild(label);
		
		//add for list
		if(this.type.equals(TYPE_LIST))
		{
			Element list = document.createElement("list");
			for(FieldOption option: this.options)
			{
				Element item = document.createElement("item");
				item.setAttribute("id", option.id);
				
				CDATASection itemData = document.createCDATASection(option.name);
				
				item.appendChild(itemData);
				list.appendChild(item);
			}
			
			field.appendChild(list);
		}
		else if(this.type.equals(TYPE_DB))
		{
			if(!toExport)
			{
				Element table = document.createElement("table");
				table.setAttribute("name", this.tableName);
				table.setAttribute("id-field", this.idField);
				table.setAttribute("desc-field", this.descField);
				
				table.appendChild(document.createCDATASection(this.where));
				field.appendChild(table);
			}
			else
			{
				field.setAttribute("type", TYPE_LIST); //when we are exporting, we act as a list
				this.loadDatabaseOptions();
				Element list = document.createElement("list");
				for(FieldOption option: this.options)
				{
					Element item = document.createElement("item");
					item.setAttribute("id", option.id);
					
					CDATASection itemData = document.createCDATASection(option.name);
					
					item.appendChild(itemData);
					list.appendChild(item);
				}
				
				field.appendChild(list);
			}
		}
		else if(this.type.equals(TYPE_TEXT))
		{
			Element validation = document.createElement("validation");
			validation.appendChild(document.createCDATASection(this.validation));
			field.appendChild(validation);
		}
		
		return field;
	}
	
	public void loadDatabaseOptions()
	{
		String orderBy = this.idField;
		String orderDir = "ASC";
		String orderType = "string";
		
		QueryBuilder qb = null;
		if(this.type.equals(TYPE_DB))
		{
			qb = new QueryBuilder(this.tableName);
			qb
			.field(this.idField)
			.field(this.descField)
			.field("0") //set place holder to match codes
			.orderBy(orderBy)
			.orderDir(orderDir)
			.where(this.where);
		}
		else if(type.equals(TYPE_CODES))
		{
			
			try
			{
				orderBy = "description";
				ConfigItem item = new ConfigItem("CODE_SORT_"+this.name.toUpperCase());
				if(item.text_value != null)
				{
					if(item.text_value.length() > 0)
					{
						String[] parts = item.text_value.split(":");
						if(parts.length == 3)
						{
							orderDir = parts[2];
							orderType = parts[1];
							orderBy = parts[0];
						}
					}
				}
			}
			catch(UnknownObjectException uoe){}
			
			qb = new QueryBuilder("codes");
			qb.field("codeid").field("description").field("is_other").orderBy(orderBy).orderDir(orderDir).where("type='"+this.name+"'");
		}
		else
		{
			return;
		}
		
		DBConnection connection = null;
		try
		{
			connection = qb.query();
			if(connection != null)
			{
				ResultSet rs = connection.getResultSet();
				while(rs.next())
				{
					this.options.add(new FieldOption(rs.getString(1), rs.getString(2), (rs.getInt(3)==1)));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		
		
		// see if we need to do more sorting
		if(type.equals(TYPE_CODES))
		{
			if(orderType.equals("number"))
			{
				final boolean asc = (orderBy.equalsIgnoreCase("asc"));
				FieldOption[] fields = this.options.toArray(new FieldOption[0]);
				Arrays.sort(fields, new Comparator<FieldOption>(){
	
					@Override
					public int compare(FieldOption f1, FieldOption f2)
					{
						try
			            {
			                int id1 = Integer.parseInt(f1.id, 10);
			                int id2 = Integer.parseInt(f2.id, 10);
			                
			                if(asc)
			                {
			                	return (id1-id2);
			                }
			                else
			                {
			                	return (id2-id1);
			                }
			            }
			            catch(Exception e)
			            {
			                return 0;
			            }
					}
					
				});
				
				this.options.clear();
				for(FieldOption option: fields){ this.options.add(option); }
			}
		}
	}
	
	public void addOption(FieldOption option)
	{
		
		this.options.add(option);
	}
	
	public FieldOption getFieldOption(String id)
	{
		for(FieldOption field: this.options)
		{
			if(field.id.equals(id))
			{
				return field;
			}
		}
		
		return null;
	}
	
	public void setValidation(String validation)
	{
		if(validation == null){ validation = ""; }
		this.validation = validation;
	}
	
	public void setDatabaseOption(String table, String id, String desc, String where)
	{
		this.tableName = (table != null)?table:"";
		this.idField = (id != null)?id:"";
		this.descField = (desc != null)?desc:"";
		this.where = (where != null)?where:"";
	}

	@Override
	public int compare(Field field1, Field field2)
	{
		return field1.name.compareTo(field2.name);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Field cfield = (Field)obj;
		return this.name.equals(cfield.name);
	}

	public String getName() {
		return name;
	}
	
	

	public String getValidation() {
		return validation;
	}
	

	public boolean getIsRequired() {
		return required;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<FieldOption> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<FieldOption> options) {
		this.options = options;
	}
	
	
}
