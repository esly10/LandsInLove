package com.cambiolabs.citewrite.data;

import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import au.com.bytecode.opencsv.CSVWriter;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.Invoice;
import com.cambiolabs.citewrite.ecommerce.InvoiceItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.cambiolabs.citewrite.util.DateParser;
import com.cambiolabs.citewrite.util.Xml;



import org.joda.time.DateTime;
import org.joda.time.Days;

public class Citation extends DBObject
{

	public static final int CS_NOT_PAID = 2;
	public static final int CS_DISPUTED = 3;
	public static final int CS_PAID = 1;
	public static final int CS_COMMUNITY_SERVICE = 4;
	public static final int CS_COURT_HOLD = 10;
	public static final int CS_PAYMENT_PLAN = 11;
	
	public static final float LAT_LNG_MULTIPLIER = 1000000;
	
	@Expose public int citation_id = 0;
	@Expose public String citation_number = null;
	@Expose public String pin = null;
	@Expose public int status_id = 0;
	@Expose public int owner_id = 0;
	@Expose public int vehicle_id = 0;
	@Expose public String permit_number = null;
	@Expose public String officer_id = null;
	@Expose public Timestamp citation_date = null;
	@Expose public String license = null;
	@Expose public String vin  = null;
	@Expose public String color_id = null;
	@Expose public String color_description = null;
	@Expose public String make_id = null;
	@Expose public String make_description = null;
	@Expose public String state_id = null;
	@Expose public String state_description = null;
	@Expose public String violation_id = null;
	@Expose public String violation_type = null;
	@Expose public String violation_description = null;
	@Expose public float violation_amount = 0;
	@Expose public Timestamp violation_start = null;
	@Expose public Timestamp violation_end = null;
	@Expose public String location_id = null;
	@Expose public String location_description = null;
	@Expose public float lat = 0;
	@Expose public float lng = 0;
	@Expose public String comment_id = null;
	@Expose public String comments = null;
	@Expose public float override_fine_amount = 0;
	@Expose public float override_late_fee = 0;
	@Expose public Timestamp override_expiration = null;
	@Expose public int exported = 0;
	@Expose Timestamp update_date = null;
	@Expose public Timestamp community_service_end = null;
	@Expose public String notes = null;
	
	@Expose public Status status = null;	
	@Expose protected ArrayList<CitationAttribute> extra = new ArrayList<CitationAttribute>();
	
	
	public Citation()
	{
		super("citation", "citation_id");
		this._ignore.add("status");
	}
	
	public Citation(int citationId) throws UnknownObjectException
	{
		this();		
		if(citationId > 0)
		{
			this.citation_id = citationId;
			this.status_id = 0;			
			this.populate();
			
			this.status = new Status(this.status_id);
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<CitationNote> getNotes()
	{
		CitationNote note = new CitationNote();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("citation_id", "=", this.citation_id));
		return (ArrayList<CitationNote>)note.get(0, 0, "create_date DESC", filter);
	}
	
	public Citation(int citationId, int ownerId) throws UnknownObjectException
	{
		this();		
		if(citationId > 0)
		{
			this.citation_id = citationId;
			this.owner_id = ownerId;
			this.populate();
			
			this.status = new Status(this.status_id);
		}
	}
	
	public Citation(String citationNumber) throws UnknownObjectException
	{
		this();		
		if(citationNumber != null && citationNumber.length() > 0)
		{
			this.citation_number = citationNumber;
			this.status_id = 0;
			this.populate();
			
			this.status = new Status(this.status_id);
		}
	}
	
	public Citation(String citationNumber, String pin) throws UnknownObjectException
	{
		this();		
		if(citationNumber != null && citationNumber.length() > 0)
		{
			this.citation_number = citationNumber;
			this.pin = pin;
			this.status_id = 0;
			this.populate();
			
			this.status = new Status(this.status_id);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtra()
	{
		//check if there are any options
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("citation_id", "=", this.citation_id));
		
		CitationAttribute attr = new CitationAttribute();

		this.extra = (ArrayList<CitationAttribute>)attr.get(0, 0, "", filter);
	}
	
	public ArrayList<CitationAttribute> getExtras()
	{
		return this.extra;
	}
	
	public CitationAttribute getAttribute(String name)
	{
		for(CitationAttribute ca: this.extra)
		{
			if(ca.field_ref.equals(name))
			{
				return ca;
			}
		}
		
		return null;
	}
	
	public void removeAttribute(String name)
	{
		for(int i = 0; i < this.extra.size(); i++)
		{
			CitationAttribute ca = this.extra.get(i);
			if(ca.field_ref.equals(name))
			{
				this.extra.remove(i);
				break;
			}
		}
	}
	
	public void addAttribute(CitationAttribute attr)
	{
		if(this.extra == null)
		{
			this.loadExtra();
		}
		
		CitationAttribute exists = this.getAttribute(attr.field_ref);
		if(exists != null) //if it already exists, we will just replace it
		{
			this.extra.remove(exists);
		}
		
		this.extra.add(attr);
	}

	public Timestamp getCommunity_service_end() {
		return community_service_end;
	}

	public void setCommunity_service_end(Timestamp community_service_end) {
		this.community_service_end = community_service_end;
	}
	
	public Integer getStatus() {
		return status_id;
	}
	
	public String getViolationDescription() {
		return violation_description;
	}
	

	public String getCitationNumber() {
		return citation_number;
	}

	public String getMakeDescription() {
		return make_description;
	}

	public Timestamp getViolationStart() {
		return violation_start;
	}

	public Timestamp getViolationEnd() {
		return violation_end;
	}
	public String getStartDate()
	{
		return DateParser.toString(this.violation_start, null);
	}
	
	public String getEndDate()
	{
		return DateParser.toString(this.violation_end, null);
	}

	public void setCitationNumber(String citation_number) {
		this.citation_number = citation_number;
	}

	public String getOfficerID() {
		return officer_id;
	}

	public void setOfficerID(String officerID) {
		this.officer_id = officerID;
	}

	public Timestamp getCitation_date() {
		return citation_date;
	}
	
	public String getCitationDate()
	{
		return this.getCitationDate(null);
	}
	
	public String getCitationDate(String format)
	{
		return DateParser.toString(this.citation_date, format);
	}

	public void setCitation_date(Timestamp citation_date) {
		this.citation_date = citation_date;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getViolationId() {
		return violation_id;
	}

	public void setViolationId(String violation_id) {
		this.violation_id = violation_id;
	}

	public String getViolation_type() {
		return violation_type;
	}

	public void setViolation_type(String violation_type) {
		this.violation_type = violation_type;
	}


	public void setViolationDescription(String violation_description) {
		this.violation_description = violation_description;
	}

	public float getViolation_amount() {
		return violation_amount;
	}
	
	public String getFormatAmount()
	{
		
		return String.format("$%.02f", violation_amount);
	}
	
	
	public int getCitationId() {
		return citation_id;
	}

	public void setViolation_amount(float violation_amount) {
		this.violation_amount = violation_amount;
	}

	public String getLocation_id() {
		return location_id;
	}
	
	public void setLat(float lat)
	{
		this.lat = (lat * LAT_LNG_MULTIPLIER);		
	}
	
	public void setLng(float lng)
	{
		this.lng = (lng * LAT_LNG_MULTIPLIER);		
	}
	
	public float getLat() 
	{
		return this.lat/LAT_LNG_MULTIPLIER;
	}

	public float getLng() 
	{
		return this.lng/LAT_LNG_MULTIPLIER;
	}

	public Integer getCitationStatus() {
		return status_id;
	}

	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}

	public String getLocation_descprition() {
		return location_description;
	}

	public void setLocation_descprition(String location_descprition) {
		this.location_description = location_descprition;
	}

	public String getComment_id() {
		return comment_id;
	}

	public void setComment_id(String comment_id) {
		this.comment_id = comment_id;
	}

	public String getComment() {
		return comments;
	}

	public void setComment(String message) {
		this.comments = message;
	}
	

	public boolean isExported() {
		return (this.exported == 1);
	}
	
	public boolean getViolationOvertime()
	{
		Code code = Codes.getCode(Code.CT_VIOLATION, this.violation_id);
		return code.isOvertime();
	}

	public void setExported(int exported) {
		this.exported = exported;
	}
	
	public int getExported() {
		return this.exported;
	}
	
	public float getOverride_fine_amount() {
		return override_fine_amount;
	}
	
	public String getOverrideFineAmountFormat() {
		return String.format("$%.02f", override_fine_amount);
	}

	public void setOverride_fine_amount(float override_fine_amount) {
		this.override_fine_amount = override_fine_amount;
	}

	public String getOverrideLateFeeFormat() {
		return String.format("$%.02f", override_late_fee);
		
	}
	
	public float getOverride_late_fee() {
		return override_late_fee;
	}

	public void setOverride_late_fee(float override_late_fee) {
		this.override_late_fee = override_late_fee;
	}

	public Timestamp getOverride_expiration() {
		return override_expiration;
	}
	
	public boolean getHasOverride(){
		return hasOverride();
	}
	
	public String getOverrideExpirationFormat() {
		return DateParser.toString(this.override_expiration, "MMMMM dd, yyyy");
	}

	public void setOverride_expiration(Timestamp override_expiration) {
		this.override_expiration = override_expiration;
	}

	public String createCitationNumber() {
		
		int count = 0;
		String prefix = "LCP0";
		ArrayList<String> prefixList = getPrefixCoincidences(prefix);
		ArrayList<Integer> prefixNumbers = new ArrayList<Integer>();
		int prefixTransition = 0;
		
		int maxPrefix = 0; 
		
		if (prefixList.size()>0){
			for (int i =0;i<=prefixList.size(); i++){
				try {	
					prefixTransition=Integer.parseInt(prefixList.get( i ).substring(6));
					
					prefixNumbers.add(prefixTransition);
				}catch (Exception e) {
					//json.addProperty("msg", "Error loading the general administration");
				}
			}	
			
		}
		if(prefixNumbers!= null){
			for(int j=0; j<+prefixNumbers.size(); j++) 
			{ 
				if(prefixNumbers.get(j) > maxPrefix) 
				{ 
					maxPrefix = prefixNumbers.get(j); 
				} 
			} 
		}
		maxPrefix++;
		String intermediateNumber = "0";
		
		switch (String.valueOf(maxPrefix).length()) {
	        case 1:
	        	intermediateNumber = "0000";
	            break;
	        case 2:
	        	intermediateNumber = "000";
	        	break;
	        case 3:
	        	intermediateNumber = "00";
	            break;
	        case 4:
	        	intermediateNumber = "0";
	            break;
	         default:
	        	intermediateNumber = "0";
	            break;
		}
		
		prefix = prefix + intermediateNumber +String.valueOf(maxPrefix);
		
		return prefix;
	}
	
	public ArrayList<String> getPrefixCoincidences(String prefix)
	{
		ArrayList<String> prefixList = new ArrayList<String>();
		
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT citation_number FROM citation where citation_number like '"+prefix+"%'";
			if(conn.query(sql))
			{
				Citation citation = new Citation();
				while(conn.fetch(citation))
				{
					prefixList.add(citation.citation_number);
					citation = new Citation();
				}
			}
		} 
		catch (Exception e) 
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
		return prefixList;
	}
	
	public boolean commit()
	{
		this.update_date = new Timestamp(System.currentTimeMillis());
		
		//make sure nothing is null
		
		if(this.pin == null){ this.pin = ""; }
		if(this.permit_number == null){ this.permit_number = ""; }
		if(this.officer_id == null){ this.officer_id = ""; }
		if(this.license == null){ this.license = ""; }
		if(this.vin  == null){ this.vin = ""; }
		if(this.color_id == null){ this.color_id = ""; }
		if(this.color_description == null){ this.color_description = ""; }
		if(this.make_id == null){ this.make_id = ""; }
		if(this.make_description == null){ this.make_description = ""; }
		if(this.state_id == null){ this.state_id = ""; }
		if(this.state_description == null){ this.state_description = ""; }
		if(this.location_id == null){ this.location_id = ""; }
		if(this.location_description == null){ this.location_description = ""; }
		if(this.comment_id == null){ this.comment_id = ""; }
		if(this.comments == null){ this.comments = ""; }
		
		boolean rv = super.commit();
		
		if(rv && this.extra != null)
		{
			for(CitationAttribute attr: this.extra)
			{
				if(attr.isDirty())//only save dirty 
				{
					attr.citation_id = this.citation_id;
					attr.commit();
				}
			}
		}
		
		return rv;
	}
	
	public boolean delete()
	{
		super.delete();
		
		DBConnection conn = null;
		try 
		{	
			conn = new DBConnection();
			
			conn.execute("delete from citation_photo where citation_id="+this.citation_id);
			conn.execute("delete from citation_attribute where citation_id="+this.citation_id);
			conn.execute("delete from citation_appeal where citation_id="+this.citation_id);
			
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		
		return false;
	}
	
	public boolean clear(Timestamp start, Timestamp end)
	{
		String sql = "DELETE from "+this.tableName+" where exported=? ";
		
		if(start != null)
		{
			sql += " AND citation_date >= ?";
		}
		
		if(end != null)
		{
			sql += " AND citation_date <= ? ";
		}
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare(sql);
			
			pst.setInt(1, 1);
			
			int index = 2;
			if(start != null)
			{
				pst.setTimestamp(index, start);
				index++;
			}
			
			if(end != null)
			{
				pst.setTimestamp(index, end);
			}
			
			if(connection.execute(pst))
			{
				//delete from photo directory
				connection.execute("DELETE from citation_photo where citation_id not in (select citation_id from citation)");
				connection.execute("DELETE from citation_attribute where citation_id not in (select citation_id from citation)");
				return true;
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
		
		return false;
	}
	
	public ArrayList<CitationPhoto> getPhotos()
	{
		ArrayList<CitationPhoto> photos = new ArrayList<CitationPhoto>();
		
		DBConnection conn = null;
		try 
		{
			conn = new DBConnection();
			String sql = "SELECT * from citation_photo where citation_id="+this.citation_id;
			if(conn.query(sql))
			{
				CitationPhoto photo = new CitationPhoto();
				while(conn.fetch(photo))
				{
					photos.add(photo);
					photo = new CitationPhoto();
				}
			}
		} 
		catch (Exception e) 
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
		return photos;
	}
	
	public boolean setExported(boolean exported)
	{
		DBConnection conn = null;
		try 
		{
			this.exported = (exported)?1:0;
			conn = new DBConnection();
			String sql = "update citation set exported="+this.exported+" where citation_id="+this.citation_id;
			return conn.execute(sql);
		} 
		catch (Exception e) 
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
		return false;
	}
	
	//make sure that the citation number is unique and that the id give is the only with it, if it exists
	public static boolean unique(String citationNumber, int id)
    {
		try
        {
        	Citation cite = new Citation(citationNumber);
        	if(cite.citation_id != id)
        	{
        		return false;
        	}
        }
        catch(UnknownObjectException uoe)
        {
        	
        }
        
        return true;
    }
	
	public static boolean exists(String citationNumber)
    {
        try
        {
        	new Citation(citationNumber);
        	return true;
        }
        catch(UnknownObjectException uoe)
        {
        	
        }
        
        return false;
    }

	public static Citation add(String xml)
	{
		return Citation.add(new StringReader(xml));
	}
	
    public static Citation add(Reader reader)
    {
        try
        {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(reader));

            Citation citation = new Citation();
            
            Element cite = document.getDocumentElement();

            //first check we don't already have a 
            String number = cite.getAttribute("number");
            if (number == null || number.length() == 0)
            {
                return null;
            }
            else if(exists(number))
            {
                return new Citation(number); //already in the system, so ignore
            }

            citation.citation_number = number;
            
            String strId = cite.getAttribute("owner_id");
            if(strId != null && strId.length() > 0)
            {
            	try
            	{
            		citation.owner_id = Integer.parseInt(strId);
            	}
            	catch(NumberFormatException nfe){}
            }
            
            String pin = cite.getAttribute("pin");
            if(pin != null && pin.length() > 0)
            {
            	citation.pin = pin;
            }
            
            citation.officer_id = cite.getAttribute("officer_id");
            
            String ts = cite.getAttribute("ts");
            if(ts.length() > 0)
            {
            	try
            	{
            		citation.citation_date = new Timestamp(Long.parseLong(ts));
            	}
            	catch(NumberFormatException e)
            	{
            		
            	}
            }
            
            Element permit = Xml.getElementByName(cite, "permit");
            if(permit != null)
            {
            	citation.permit_number = Xml.getElementContent(permit);
            }
            
            Element vehicle = Xml.getElementByName(cite, "vehicle");
            if(vehicle != null)
            {
            	strId = vehicle.getAttribute("id");
                if(strId != null && strId.length() > 0)
                {
                	try
                	{
                		citation.vehicle_id = Integer.parseInt(strId);
                	}
                	catch(NumberFormatException nfe){}
                }
                
	            citation.license = vehicle.getAttribute("license");
	            citation.vin = vehicle.getAttribute("vin");
	            
	            Element element = Xml.getElementByName(vehicle, "state");
	            if(element != null)
	            {
	            	citation.state_id = element.getAttribute("id");
	            	citation.state_description = Xml.getElementContent(element);
	            }
	            
	            element = Xml.getElementByName(vehicle, "make");
	            if(element != null)
	            {
	            	citation.make_id = element.getAttribute("id");
	            	citation.make_description = Xml.getElementContent(element);
	            }
	            
	            element = Xml.getElementByName(vehicle, "color");
	            if(element != null)
	            {
	            	citation.color_id = element.getAttribute("id");
	            	citation.color_description = Xml.getElementContent(element);
	            }
            }
            
            Element violation = Xml.getElementByName(cite, "violation");
            if (violation != null)
            {
            	citation.violation_id = violation.getAttribute("id");
            	citation.violation_type = violation.getAttribute("type");
            	citation.violation_description = Xml.getElementContent(violation);
            	
            	String strAmount = violation.getAttribute("amount");
            	if(strAmount != null)
            	{
            		try
            		{
            			citation.violation_amount = Float.parseFloat(strAmount);
            		}
            		catch(NumberFormatException nfe){}
            	}
            
            	ts = violation.getAttribute("start_ts");
            	if(ts != null && ts.length() > 0)
            	{
            		try
            		{
            			citation.violation_start = new Timestamp(Long.parseLong(ts));
            		}
            		catch(NumberFormatException nfe){}
            	}
            	
            	ts = violation.getAttribute("end_ts");
            	if(ts != null && ts.length() > 0)
            	{
            		try
            		{
            			citation.violation_end = new Timestamp(Long.parseLong(ts));
            		}
            		catch(NumberFormatException nfe){}
            	}            		
            }

            Element location = Xml.getElementByName(cite, "location");
            if (location != null)
            {
            	String strLat = location.getAttribute("lat");
                if(strLat != null && strLat.length() > 0)
                {
                	try
                	{
                		citation.setLat(Float.parseFloat(strLat));
                	}
                	catch(NumberFormatException nfe){}
                }
                
                String strLng = location.getAttribute("lng");
                if(strLng != null && strLng.length() > 0)
                {
                	try
                	{
                		citation.setLng(Float.parseFloat(strLng));
                	}
                	catch(NumberFormatException nfe){}
                }
                
            	citation.location_id = location.getAttribute("id");
            	citation.location_description = Xml.getElementContent(location);
            }

            Element comment = Xml.getElementByName(cite, "comment");
            if (comment != null)
            {
            	citation.comment_id = comment.getAttribute("id");
            	citation.comments = Xml.getElementContent(comment);
            }
            
            Element attributes = Xml.getElementByName(cite, "attributes");
            if (attributes != null)
            {
            	NodeList list = attributes.getElementsByTagName("attribute");
        		int size = list.getLength();
        		for(int i = 0; i < size; i++)
        		{
        			Element attr = (Element)list.item(i);
        			citation.addAttribute(new CitationAttribute(attr));
        		}
            }

            if (citation.commit())
            {
                //now see if we have a photo
                Element photo = Xml.getElementByName(cite, "photo");
                if (photo != null)
                {
                	CitationPhoto cphoto = new CitationPhoto();
                	cphoto.photo = Xml.getElementContent(photo);
                	cphoto.citation_id = citation.citation_id;
                    cphoto.commit();
                }
                else //check if they sent photos
                {
                	Element photos = Xml.getElementByName(cite, "photos");
                	if(photos != null)
                	{
                		NodeList list = photos.getElementsByTagName("photo");
                		int size = list.getLength();
                		for(int i = 0; i < size; i++)
                		{
                			photo = (Element)list.item(i);
                			CitationPhoto cphoto = new CitationPhoto();
                        	cphoto.photo = Xml.getElementContent(photo);
                        	cphoto.citation_id = citation.citation_id;
                            cphoto.commit();
                		}
                	}
                }

                return citation;
            }
        }
        catch (Exception xe)
        {
           xe.printStackTrace();
        }

        return null;   
    }
    
    public boolean appendToElement(Element parent)
    {
    	try
    	{
    		
    		Document document = parent.getOwnerDocument();
    		
    		Element citation = document.createElement("citation");
    		citation.setAttribute("citation_number", this.citation_number);
    		citation.setAttribute("officer_id", this.officer_id);
    		citation.setAttribute("date", new SimpleDateFormat("MMM d, yyyy h:mm a").format(this.citation_date));
    		
    		
    		Element vehicle = document.createElement("vehicle");
    		if(this.license != null)
    		{
    			vehicle.setAttribute("license", this.license);
    		}
    		if(this.vin != null)
    		{
    			vehicle.setAttribute("vin", this.vin);
    		}
    		
    		if(this.state_id != null)
    		{
	    		Element element = document.createElement("state");
	    		element.setAttribute("id", this.state_id);
	    		if(this.state_description != null)
	    		{
	    			element.appendChild(document.createCDATASection(this.state_description));
	    		}
	    		vehicle.appendChild(element);
    		}
    		
    		if(this.make_id != null)
    		{
	    		Element element = document.createElement("make");
	    		element.setAttribute("id", this.make_id);
	    		if(this.make_description != null)
	    		{
	    			element.appendChild(document.createCDATASection(this.make_description));
	    		}
	    		vehicle.appendChild(element);
    		}
    		
    		if(this.color_id != null)
    		{
	    		Element element = document.createElement("color");
	    		element.setAttribute("id", this.color_id);
	    		if(this.color_description != null)
	    		{
	    			element.appendChild(document.createCDATASection(this.color_description));
	    		}
	    		vehicle.appendChild(element);
    		}
    		
    		Element violation = document.createElement("violation");
    		violation.setAttribute("id", this.violation_id);
    		violation.setAttribute("type", this.violation_type);
    		violation.setAttribute("amount", String.valueOf(this.violation_amount));
    		
    		if(this.violation_start != null)
    		{
    			violation.setAttribute("chalk_start", new SimpleDateFormat("MMM d, yyyy h:mm a").format(this.violation_start));
    		}
    		
    		if(this.violation_end != null)
    		{
    			violation.setAttribute("chalk_end", new SimpleDateFormat("MMM d, yyyy h:mm a").format(this.violation_end));
    		}
    		
    		violation.appendChild(document.createCDATASection(this.violation_description));
    		
    		citation.appendChild(violation);
    		
    		Element location = document.createElement("location");
    		location.setAttribute("id", this.location_id);
    		location.appendChild(document.createCDATASection(this.location_description));
    		
    		citation.appendChild(location);
    		
    		Element comment = document.createElement("comment");
    		comment.setAttribute("id", this.comment_id);
    		comment.appendChild(document.createCDATASection(this.comments));
    		
    		citation.appendChild(comment);
    		
    		if(this.extra.size() > 0)
    		{
    			Element attrs = document.createElement("attributes");
    			
    			for(CitationAttribute ca: this.extra)
    			{
    				Element attr = document.createElement("attribute");
    				attr.setAttribute("name", ca.field_ref);
    				attr.setAttribute("id", ca.value_id);
    				
    				CDATASection cdata = document.createCDATASection(ca.value);
    				attr.appendChild(cdata);
    				
    				attrs.appendChild(attr);
    			}
    			
    			citation.appendChild(attrs);
    		}
    		
    		ArrayList<CitationPhoto> photos = this.getPhotos();
    		if(photos.size() > 0)
    		{
    			Element ePhotos = document.createElement("photos");
    			for(CitationPhoto photo:photos)
    			{
    				if(photo.exists())
    				{
	    				Element ephoto = document.createElement("photo");
	    				ephoto.appendChild(document.createCDATASection(photo.getBase64Photo()));
	    				ePhotos.appendChild(ephoto);
    				}    				
    			}
    			citation.appendChild(ePhotos);
    		}
    		
    		parent.appendChild(citation);
    		
    		return true;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return false;
    	
    }
    
    private static Object citeNumberLock = new Object();
    public boolean setCitationNumber()
    {
    	synchronized(citeNumberLock)
    	{
	    	ConfigItem item = ConfigItem.lookup("BO_CERTIFICATE_INFO");
	        this.citation_number = item.text_value + "-" + this.format(item.int_value);
	        item.int_value++;
	        
	        item.commit();
	        
	        return this.commit();
    	}
    }
    
    private String format(int number)
    {
        String rv = String.valueOf(number);
        
        int size = rv.length();
        for(int i = size; i < 5; i++)
        {
            rv = "0"+rv;
        }
        
        return rv;
    }

    public static boolean writeCSVHeader(CSVWriter csv)
	{
    	try
    	{
    		CiteFields cfields = new CiteFields();
    		ArrayList<CiteField> fields = cfields.getFields(true);
    		
    		String[] row = new String[18+(2*fields.size())];
    		 
    		
    		row[0] = "Citation Number";
    		row[1] = "Officer ID";
    		row[2] = "Citation Date";
    		row[3] = "License";
    		row[4] = "VIN";
    		row[5] = "State";
    		row[6] = "Make";
    		row[7] = "Color";
    		
    		
    		row[8] = "Violation ID";
    		row[9] = "Violation";
    		row[10] = "Violation Type";
    		row[11] = "Violation Amount";
    		row[12] = "Chalk Start";
    		row[13] = "Chalk End";
    		    		
    		
    		
    		row[14] = "Location ID";
    		row[15] = "Location";
    		
    		row[16] = "Comment ID";
    		row[17] = "Comment";
    		
    		int count = 18;
    		for(CiteField field: fields)
    		{
    			row[count++] = field.label + " ID";
    			row[count++] = field.label;
    		}
    		
    		csv.writeNext(row);
    		
    		return true;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return false;
	}
    
	public boolean writeCSV(CSVWriter csv)
	{
		try
    	{
    		
    		CiteFields cfields = new CiteFields();
    		ArrayList<CiteField> fields = cfields.getFields(true);
    		String[] row = new String[18+(2*fields.size())];
    		 
    		//need to make date format configurable
    		String dateFormat = "MMddyy HHmmss";
    		
    		row[0] = this.citation_number;
    		row[1] = this.officer_id;
    		row[2] = new SimpleDateFormat(dateFormat).format(this.citation_date);
    		row[3] = this.license;
    		row[4] = this.vin;
    		row[5] = this.state_id;
    		row[6] = this.make_id;
    		row[7] = this.color_id;
    		
    		
    		row[8] = this.violation_id;
    		row[9] = this.violation_description;
    		row[10] = this.violation_type;
    		row[11] = String.valueOf(this.violation_amount);
    		
    		if(this.violation_start != null)
    		{
    			row[12] = new SimpleDateFormat(dateFormat).format(this.violation_start);
    		}
    		else
    		{
    			row[12] = "";
    		}
    		
    		if(this.violation_end != null)
    		{
    			row[13] = new SimpleDateFormat(dateFormat).format(this.violation_end);
    		}
    		else
    		{
    			row[13] = "";
    		}
    		
    		row[14] = this.location_id;
    		row[15] = this.location_description;
    		
    		row[16] = this.comment_id;
    		row[17] = this.comments;
    		
    		
    		int count = 18;
    		for(CiteField field: fields)
    		{
    			CitationAttribute ca = this.getAttribute(field.name);
    			if(ca != null)
    			{
	    			row[count++] = (ca.value_id == null)?"":ca.value_id;
					row[count++] = ca.value;
    			}
    			else
    			{
    				row[count++] = "";
					row[count++] = "";
    			}
    		}
    		
    		csv.writeNext(row);
    		
    		return true;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return false;
		
	}
	
	 public static String generatePIN()
	    {
	    	try 
	    	{  
	            // get an instance using the SHA1 Pseudo Random Number Generator  
	            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");  
	            int myInt = sr.nextInt(99999);  
	          
	            return String.valueOf(myInt);
	       
	        } 
	    	catch (NoSuchAlgorithmException e) 
	    	{  
	    		
	        }  
	    	
	    	Random random = new Random();
			return String.valueOf(random.nextInt(9999));
	    	
	    }
	
	public CitationAppeal getAppeal()
	{
		return CitationAppeal.getByCitationId(this.citation_id);
	}
	
	public Invoice getInvoice()
	{
		return Invoice.getByReference(this.citation_id, Invoice.TYPE_CITATION);
	}
	
	public String getFormatAmountDue()
	{
		if (this.getLateFee() != null)
		{
			float mount = (this.violation_amount)+(this.getLateFee().fee_amount);
			return String.format("$%.02f", mount);
		}
		return String.format("$%.02f", this.violation_amount);
	}
	
	public String getOverrideFormatAmountDue()
	{
		if (this.getLateFee() != null)
		{
			float mount = (this.override_fine_amount)+(this.override_late_fee);
			return String.format("$%.02f", mount);
		}
		return String.format("$%.02f", this.violation_amount);
	}
	
	public boolean sendNitification(int count){
	
		if(this.status_id != CS_PAID){
			int days = this.disregardDays();
			
			Calendar today = Calendar.getInstance();
			Calendar compare = Calendar.getInstance();
			long citeTime = this.citation_date.getTime();
			//
			if(days > 0){				
			    Long miliseconds = dayToMiliseconds(days);
			    citeTime = new Timestamp(this.citation_date.getTime() - miliseconds).getTime();
			}
			
			Long sumdays = dayToMiliseconds(count);
			citeTime = citeTime + sumdays;
			compare.setTimeInMillis(citeTime);		
			if(compare.equals(today) || today.after(compare))
			{
				return true;
			} else {
				return false;
			}
		}else {
			return false;
		}	
		
	}
	
	public LateFee getLateFee()
	{
		if(this.status_id == CS_COMMUNITY_SERVICE){
			//Timestamp today = Calendar.getInstance();
			java.util.Date today = new java.util.Date();				    
			if(this.community_service_end.after(new java.sql.Timestamp(today.getTime()))){
				return null;
			}		
			
		}
		
		if(this.status_id == CS_COURT_HOLD){
			return null;			
		}
		
		if (this.status_id == CS_PAID) 
		{
			Invoice invoice = this.getInvoice();
			if (invoice != null) 
			{
				InvoiceItem item = invoice.getItem(Invoice.TYPE_CITATION);
				if(item != null)
				{
					LateFee fee = new LateFee();
					fee.fee_amount = item.amount;
					
					return fee;
				}
			}
			
			return null;				
			
		}
		else
		{
		
			LateFee rv = null;
			ArrayList<LateFee> fees = LateFee.getFees(this.violation_id);
		
			int days = this.disregardDays();
			
			Calendar today = Calendar.getInstance();
			Calendar compare = Calendar.getInstance();
			long citeTime = this.citation_date.getTime();
			//
			if(days > 0){				
			    Long miliseconds = dayToMiliseconds(days);
			    citeTime = new Timestamp(this.citation_date.getTime() - miliseconds).getTime();
			}
			
			for(LateFee fee: fees)
			{
				compare.setTimeInMillis(citeTime);
				compare.add(Calendar.DATE, fee.days_late);
				if(compare.equals(today) || today.after(compare))
				{
					rv = fee;
				}
			}
			
			return rv;
		}
		
		
		
	}
	
	private Long dayToMiliseconds(int days){
	    //Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);	    
	    return TimeUnit.DAYS.toMillis(days);
	}
	
	public int disregardDays(){
		
		StatusHistory statusHistory = new StatusHistory();

		ArrayList<StatusHistory> citationStatus = statusHistory.getStatusHistory(this);
		
		Timestamp date1 = null; 
		Timestamp date2  = null; 
		int diference = 0;
	    
		for (StatusHistory hystory : citationStatus) {
			if(hystory.status_id == CS_COURT_HOLD || hystory.status_id == CS_COMMUNITY_SERVICE){
				if(date1 == null)
					date1 = hystory.date;			 				 
			}
			
			if(hystory.status_id == CS_NOT_PAID && date1 != null){
				 date2 = hystory.date;
			}
			
			if(date1 != null && date2 != null){
				diference = diference + Days.daysBetween(new DateTime(date1), new DateTime(date2)).getDays(); //
				date1 = null; 
				date2  = null; 
			}
		}
	 
	    return diference;
	}
	
	public boolean hasOverride (){
		
		return (this.override_expiration != null && (this.override_expiration.compareTo(new Date())>=0));
	}
	

}
