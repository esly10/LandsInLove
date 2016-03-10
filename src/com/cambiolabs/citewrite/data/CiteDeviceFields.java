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

public class CiteDeviceFields
{
	private Document document = null;
	private Element citePages = null;
	private ConfigItem item = null;
	
	public CiteDeviceFields()
	{
		try
		{
			this.item = new ConfigItem("CITE_DEVICE_PAGES_XML");
			if(this.item.text_value.length() > 0)
			{
				try
				{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				    DocumentBuilder builder = factory.newDocumentBuilder();
				    this.document = builder.parse(new InputSource(new StringReader(item.text_value)));
				    
				    this.citePages = this.document.getDocumentElement();
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
			
			this.item.setName("CITE_DEVICE_PAGES_XML");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder;
			try
			{
				builder = factory.newDocumentBuilder();
				this.document = builder.newDocument();
				this.citePages = this.document.createElement("cite-pages");
				this.document.appendChild(this.citePages);

				this.save();
				
				addPage();
			} 
			catch (ParserConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	
	private Element getPageElement(int pageIndex) //zero based
	{
		if(this.citePages != null)
		{
			NodeList list = this.citePages.getElementsByTagName("page");
			if(pageIndex <= list.getLength())
			{
				CiteFields citeFields = new CiteFields();
				Element page = (Element)list.item(pageIndex);
				return page;
			}
		}
		
		return null;
	}
	

	public int pagesSize()
	{

		if(this.citePages != null)
		{
			NodeList pages = citePages.getElementsByTagName("page");
			int sizeP = pages.getLength();
			
			return sizeP;
		}
		
		return -1;

	}
	
	public ArrayList<ArrayList<CiteField>> getPages() 
	{
		ArrayList<ArrayList<CiteField>> rv = new ArrayList<ArrayList<CiteField>>();
		
		if(this.citePages != null)
		{
			NodeList pages = citePages.getElementsByTagName("page");
			int sizeP = pages.getLength();
			for(int i = 0; i < sizeP; i++)
			{
				rv.add(this.getPage(i));
			}
		}
		
		return rv;

	}
	
	public ArrayList<CiteField> getPage(int pageIndex) // starts with 0
	{
		ArrayList<CiteField> rv = new ArrayList<CiteField>();
		
		Element page = this.getPageElement(pageIndex);
		if(page != null)
		{
			CiteFields citeFields = new CiteFields();
			
			NodeList fields = page.getElementsByTagName("field");
			int size = fields.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)fields.item(i);
				String name = field.getAttribute("name");
				if(name != null)
				{
					CiteField cField = citeFields.getField(name);
					if(cField != null)
					{
						rv.add(cField);
					}
				}
			}
		}
		
		return rv;
	}
	
	
	public boolean exists(CiteField cField)
	{
		NodeList pages = citePages.getElementsByTagName("page");
		
		int sizeP = pages.getLength();
		for(int i = 0; i < sizeP; i++)
		{
			Element page = this.getPageElement(i);
			NodeList fields = page.getElementsByTagName("field");
			
			int size = fields.getLength();
			for(int j = 0; j < size; j++)
			{
				Element field = (Element)fields.item(j);
				String name = field.getAttribute("name");
				if(name != null && name.equals(cField.name))
				{
					return true;
				}
			}
			
		}		
		
		return false;
	}
		
	
	public boolean addPage()
	{

		Element page = this.document.createElement("page");
		this.citePages.appendChild(page);
		
		return this.save();

	}
	
	public boolean removePage(int pageIndex)
	{
		Element page = this.getPageElement(pageIndex);
		
		if(page != null)
		{
			this.citePages.removeChild(page);
			
			return this.save();
		}
		
		return false;
	}
	
	public boolean addField(int pageIndex, CiteField field)
	{
		Element page = this.getPageElement(pageIndex);
		if(page != null)
		{
			Element eField = this.document.createElement("field");
			eField.setAttribute("name", field.name);
			
			page.appendChild(eField);
			
			return this.save();
		}
		
		return false;
	}
	
	public boolean removeFiel(int pageIndex, String name)
	{
		Element page = this.getPageElement(pageIndex);
		
		if(page != null)
		{
			
			NodeList fields = page.getElementsByTagName("field");
			int size = fields.getLength();
			for(int i = 0; i < size; i++)
			{
				Element field = (Element)fields.item(i);
				String aName = field.getAttribute("name");
				if(aName != null && aName.equals(name))
				{
					page.removeChild(field);
					return this.save();
				}
			}
		}
		
		return false;
	}
	
	public boolean move(int pageIndex, CiteField field, String dir)
	{	
		String name = field.name;
		Element page = this.getPageElement(pageIndex);
		
		NodeList list = page.getElementsByTagName("field");
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
						page.removeChild(eField);
						
						page.insertBefore(eField, prev);
						
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
				page.removeChild(eField);
				page.insertBefore(eField, foundField);
				
				return this.save();
			}
			
			prev = eField;
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
