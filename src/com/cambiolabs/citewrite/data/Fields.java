package com.cambiolabs.citewrite.data;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import com.cambiolabs.citewrite.db.UnknownObjectException;

import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Fields<T extends Field>
{
	protected Document document = null;
	protected Element fields = null;
	protected ConfigItem item = null;
	
	protected ArrayList<T> defaultFields = null;
	
	protected String itemName = "FIELDS_XML";
	protected String rootName = "fields";
	
	public Fields()
	{
		
	}
	
	public Fields(String itemName, String rootName)
	{
		try
		{
			this.itemName = itemName;
			this.rootName = rootName;
			this.item = new ConfigItem(this.itemName);
		}
		catch(UnknownObjectException uoe)
		{
			this.item = new ConfigItem();
		
			this.item.setName(this.itemName);
			this.item.setTextValue("<"+this.rootName+" />");
			this.item.commit();
		}
		
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
	
	public String getXML()
	{
		return this.item.text_value;
	}

	public abstract ArrayList<T> getDefaultFields();
	
	public T getDefaultField(String name)
	{
		for(T field: this.getDefaultFields())
		{
			if(field.name.equals(name))
			{
				return field;
			}
		}
		
		return null;
	}
	
	public ArrayList<T> getFields()
	{
		return this.getFields(false, false);
	}
	
	public ArrayList<T> getFields(boolean loadDatabase)
	{
		return this.getFields(false, loadDatabase);
	}
	
	public ArrayList<T> getFields(boolean includeDefault, boolean loadDatabase)
	{
		ArrayList<T> rv = new ArrayList<T>();
		
		if(includeDefault)
		{
			rv.addAll(this.getDefaultFields());
		}
		
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				 
				Element field = (Element)list.item(i);
				T cfield = this.factory(field);
				rv.add(cfield);
			}
		}
		
		if(loadDatabase)
		{
			for(T cfield: rv)
			{
				cfield.loadDatabaseOptions();
			}
		}
		
		return rv;
	}
	
	
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T extends Field>T factory(String name, String label, String type)
	{
		return (T)new Field(name, label, type);
	}
	
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T extends Field>T factory(String label, String type)
	{
		return (T)new Field(label, type);
	}
	
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T extends Field>T factory(Element e)
	{
		return (T)new Field(e);
	}
		
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T extends Field>T factory(HttpServletRequest request)
	{
		return (T)new Field(request);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> getFieldsForDisplay(boolean includeDefault, boolean loadDatabase)
	{
		ArrayList<T> rv = new ArrayList<T>();
		
		if(includeDefault)
		{
			rv.addAll(this.getDefaultFields());
			rv.remove((T) new Field("Password", Field.TYPE_STANDARD));
			rv.remove((T) new Field("Username", Field.TYPE_STANDARD));
			rv.remove((T) new Field("Status", Field.TYPE_STANDARD));
			rv.remove((T) new Field("Validity", Field.TYPE_STANDARD));
		}
		
		if(this.fields != null)
		{
			NodeList list = fields.getElementsByTagName("field");
			int size = list.getLength();
			for(int i = 0; i < size; i++)
			{
				 
				Element field = (Element)list.item(i);
				T cfield = this.factory(field);
				rv.add(cfield);
			}
		}
		
		if(loadDatabase)
		{
			for(T cfield: rv)
			{
				cfield.loadDatabaseOptions();
			}
		}
			
		
		return rv;
	}
	
	public T getField(String name)
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
					return this.factory(field);
				}
			}
		}
		
		return null;
	}
	
	public boolean exists(T cField)
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
	
	public boolean add(T field)
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
	
	public boolean update(T oldField, T newField)
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
	
	public boolean move(T field, String dir)
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
