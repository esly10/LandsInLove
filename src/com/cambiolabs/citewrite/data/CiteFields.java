package com.cambiolabs.citewrite.data;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.UnknownObjectException;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CiteFields
{
	private Document document = null;
	private Element fields = null;
	private ConfigItem item = null;
	
	public CiteFields()
	{
		try
		{
			this.item = new ConfigItem("CITE_FIELDS_XML");
			if(this.item.text_value.length() > 0)
			{
				try
				{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				    DocumentBuilder builder = factory.newDocumentBuilder();
				    this.document = builder.parse(new InputSource(new StringReader(item.text_value)));
				    
				    this.fields = this.document.getDocumentElement();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(UnknownObjectException uoe)
		{
			this.item = new ConfigItem();
			
			this.item.setName("CITE_FIELDS_XML");
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder;
			try
			{
				builder = factory.newDocumentBuilder();
				this.document = builder.newDocument();
				this.fields = this.document.createElement("citation-fields");
				this.document.appendChild(this.fields);
				
				this.createDefaultFields();
				this.save();
			} 
			catch (ParserConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void createDefaultFields()
	{
		this.fields.appendChild(new CiteField(CiteField.FN_OFFICER_ID, "Officer ID", CiteField.TYPE_STANDARD).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_DATE_TIME, "Date and Time", CiteField.TYPE_STANDARD).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_LICENSE, "License", CiteField.TYPE_STANDARD).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_VIN, "VIN", CiteField.TYPE_STANDARD).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_STATE, "State", CiteField.TYPE_CODES).toElement(this.document));
		CiteField citeField = new CiteField(CiteField.FN_VIOLATION, "Violation", CiteField.TYPE_CODES);
		citeField.required = true;
		this.fields.appendChild(citeField.toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_MAKE, "Make", CiteField.TYPE_CODES).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_COLOR, "Color", CiteField.TYPE_CODES).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_LOCATION, "Location", CiteField.TYPE_CODES).toElement(this.document));
		this.fields.appendChild(new CiteField(CiteField.FN_COMMENT, "Comments", CiteField.TYPE_CODES).toElement(this.document));
	}
	
	public String getXML()
	{
		Document export = (Document)this.document.cloneNode(true);
		Element exportFields = export.getDocumentElement();
		
		if(exportFields != null)
		{
			NodeList list = exportFields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)list.item(i);
				String type = field.getAttribute("type");
				if(type.equals(CiteField.TYPE_DB))
				{
	
					CiteField cfield = new CiteField(field);
					Element newField = cfield.toElement(export, true);
					exportFields.replaceChild(newField, field);
				}
			}
		}
		
		try
		{
	        StringWriter stringWriter = new StringWriter(); 
	        Transformer transformer = TransformerFactory.newInstance().newTransformer(); 
	        transformer.transform(new DOMSource(export), new StreamResult(stringWriter));
	        
	        return stringWriter.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return "";
	}
	
	public ArrayList<CiteField> getFields()
	{
		return this.getFields(false, false);
	}
	
	public ArrayList<CiteField> getFields(boolean nonStandardOnly)
	{
		return this.getFields(nonStandardOnly, false);
	}
	
	public ArrayList<CiteField> getFields(boolean nonStandardOnly, boolean loadOptions)
	{
		ArrayList<CiteField> rv = new ArrayList<CiteField>();
		
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				 
				Element field = (Element)list.item(i);
				CiteField cfield = new CiteField(field);
				if(nonStandardOnly && (cfield.type.equals(CiteField.TYPE_CODES) || cfield.type.equals(CiteField.TYPE_STANDARD))){ continue; }
				if(loadOptions)
				{
					cfield.loadDatabaseOptions();
				}
				rv.add(cfield);
			}
		}
		
		return rv;
	}
	
	public CiteField getField(String name)
	{
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)list.item(i);
				String aName = field.getAttribute("name");
				if(aName != null && aName.equals(name))
				{
					return new CiteField(field);
				}
			}
		}
		
		return null;
	}
	
	public boolean exists(CiteField cField)
	{
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)list.item(i);
				String aName = field.getAttribute("name");
				if(aName != null && aName.equals(cField.name))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean add(CiteField field)
	{
		Element eField = field.toElement(this.document);
		
		this.fields.appendChild(eField);
		
		return this.save();
	}
	
	public boolean remove(String name)
	{
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)list.item(i);
				String aName = field.getAttribute("name");
				if(aName != null && aName.equals(name))
				{
					this.fields.removeChild(field);
					return this.save();
				}
			}
		}
		
		return false;
	}
	
	public boolean update(CiteField oldField, CiteField newField)
	{
		if(this.fields != null)
		{
			String name = oldField.name;
			Element eField = newField.toElement(this.document);
			
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)list.item(i);
				String aName = field.getAttribute("name");
				if(aName != null && aName.equals(name))
				{
					this.fields.insertBefore(eField, field);
					this.fields.removeChild(field);
					return this.save();
				}
			}
		}
		
		return false;
	}
	
	public boolean move(CiteField field, String dir)
	{
		if(this.fields != null)
		{
			String name = field.name;
			
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			Element prev = null;
			Element foundField = null;
			for(int i = 0; i < size; i++)
			{
				Element eField = (Element)list.item(i);
				String aName = eField.getAttribute("name");
				if(aName != null && aName.equals(name))
				{
					if(dir.equals("down"))
					{
						foundField = eField;
					}
					else
					{
						if(prev != null)
						{
							this.fields.removeChild(eField);
							
							this.fields.insertBefore(eField, prev);
							
							return this.save();
						}
						else
						{
							return false;
						}
					}	
				}
				else if(foundField != null && dir.equals("down"))
				{
					this.fields.removeChild(eField);
					this.fields.insertBefore(eField, foundField);
					
					return this.save();
				}
				
				prev = eField;
			}
		}
		
		return false;
	}
	
	private boolean save()
	{
		try
		{
	        StringWriter stringWriter = new StringWriter(); 
	        Transformer transformer = TransformerFactory.newInstance().newTransformer(); 
	        transformer.transform(new DOMSource(this.document), new StreamResult(stringWriter));
	        
	        this.item.setTextValue(stringWriter.toString());
	        return this.item.commit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
