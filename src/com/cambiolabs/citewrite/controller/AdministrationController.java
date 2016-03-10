package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import com.cambiolabs.citewrite.data.CiteDeviceFields;
//import com.cambiolabs.citewrite.data.CiteField;
//import com.cambiolabs.citewrite.data.CiteFields;
import com.cambiolabs.citewrite.data.ConfigItem;
//import com.cambiolabs.citewrite.data.FieldOption;
//import com.cambiolabs.citewrite.data.HotListColumnMetaData;
//import com.cambiolabs.citewrite.data.LateFee;
import com.cambiolabs.citewrite.data.PasswordConfig;
import com.cambiolabs.citewrite.data.PasswordConfig.AuthorizationType;
import com.cambiolabs.citewrite.data.PasswordConfig.Intervals;
import com.cambiolabs.citewrite.data.PermitColumnMetaData;
import com.cambiolabs.citewrite.data.PrintLayout;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
//import com.cambiolabs.citewrite.ecommerce.CreditCardType;
//import com.cambiolabs.citewrite.ecommerce.merchant.Merchant;
//import com.cambiolabs.citewrite.ecommerce.merchant.MerchantException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class AdministrationController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	public ModelAndView onLoad(HttpServletRequest request,
			HttpServletResponse response)
			throws Exception {
			return null;
			}
	
	public ModelAndView permit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		PermitColumnMetaData meta = null;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action. '}");
			return null;
		}
		
		String action = request.getParameter("action");
		if(action != null && action.equals("save"))
		{
			Map <String, String> verify = new HashMap<String, String>(); 
			int count = Integer.parseInt(request.getParameter("count"));
			String path = request.getParameter("file_path");
			
			try
			{
				for(int i = 0; i < count; i++){	
					
					if((!request.getParameter("mapping_value_"+i).equals("")) && verify.containsValue(request.getParameter("mapping_value_"+i)))
					{
						json.addProperty("msg", "The permit Mapping" + request.getParameter("mapping_value_"+i) + "exist");
						response.getOutputStream().print(gson.toJson(json));
						return null;
					}
					
					if(verify.containsKey(request.getParameter("name_"+i))){
						json.addProperty("msg", "The field name" + request.getParameter("name_"+i) + "exist");
						response.getOutputStream().print(gson.toJson(json));
						return null;
					}
					
					verify.put(request.getParameter("name_"+i), request.getParameter("mapping_value_"+i));
					
				}
				
				if(PermitColumnMetaData.clear())
				{
					meta = new PermitColumnMetaData(path);
					if(meta.savePath())
					{
						for(int i = 0; i < count; i++)
						{
							
							String queryName = request.getParameter("name_"+i);
							String columnName = Column.columnNameFromName(queryName);
							String label = queryName;
							Boolean searchable = BooleanUtils.toBoolean(Integer.parseInt(request.getParameter("searchable_"+i)));
							int order = Integer.parseInt(request.getParameter("order_"+i));
							int displayOrder = Integer.parseInt(request.getParameter("display_order_"+i));
							String mapping = request.getParameter("mapping_value_"+i);
							
							if(mapping == null || mapping.length() == 0)
							{
								mapping = "none";
							}
							
							meta = new PermitColumnMetaData(label, queryName, columnName, mapping, displayOrder, order, searchable);
							meta.save();
						
						}
					}
				}
				
				response.setContentType("text/json");
				response.getOutputStream().print("{success: true}");
				return null;
			}
			catch(Exception e)
			{
				response.setContentType("text/json");
				response.getOutputStream().print("{success: false, msg: 'Error saving configuration: "+e.getMessage()+"'}");
				return null;
			}
		}
		return new ModelAndView("permit_file");
	}
	
	public void vehiclemultiple(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}

		AuthorizationType type = AuthorizationType.USER;
		String action = request.getParameter("action");
		
		if(action != null && action.equals("save-general")){
			//String sql = "DELETE from config_item where name like 'MULTIPLE_VEHICLE_%'";
			DBConnection connection = null;
			int IDsearch = 0;
			int IDtime = 0;
			int IDmax = 0;
			try {
				//connection = new DBConnection();
				//if(connection.execute(sql)){
					
					//String time = request.getParameter("timeMultiple") != null && !request.getParameter("timeMultiple").isEmpty() ? request.getParameter("timeMultiple"): "0";
					
					IDsearch = Integer.parseInt(request.getParameter("id_config_item"));
					IDtime = Integer.parseInt(request.getParameter("id_config_minutes"));
					IDmax = Integer.parseInt(request.getParameter("id_max"));
					
					ConfigItem itemSelect = new ConfigItem(IDsearch);
					ConfigItem itemTime = new ConfigItem(IDtime);
					ConfigItem itemMax = new ConfigItem(IDmax);
					
					String filter = request.getParameter("searchType");
					String time = request.getParameter("timeMultiple");
					String max = request.getParameter("timeMax");
					
					
					
			        itemSelect.text_value = filter;
			        itemSelect.name = "MULTIPLE_VEHICLE_SELECT";
			        itemSelect.int_value=0;
			        itemSelect.item_order=0;
					
			        itemTime.text_value = time;
			        itemTime.name = "MULTIPLE_VEHICLE_TIME";
			        itemTime.int_value=0;
			        itemTime.item_order=0;
			        
			        itemMax.text_value = max;
			        itemMax.name = "MULTIPLE_VEHICLE_MAX";
			        itemMax.int_value=0;
			        itemMax.item_order=0;
			        
			        itemTime.commit();
			        itemSelect.commit();
			        itemMax.commit();
					/*ConfigItem itemTime = new ConfigItem("MULTIPLE_VEHICLE_TIME",time, 0, 0);
					itemTime.commit();
						
					ConfigItem itemSelect = new ConfigItem("MULTIPLE_VEHICLE_SELECT",filter, 0, 0);
					itemSelect.commit();*/
					
					
				//}
			} catch (PatternSyntaxException exception) {
	        	json.addProperty("msg", " Please enter a valid regular expression.");
	        	return;
	        }catch (Exception e) {
				json.addProperty("msg", "Error saving the general administration");
				return;
			}
		}
		
		else if (action != null && action.equals("read-general")){
			try {
				
												
				ConfigItem itemSearch = ConfigItem.lookup("MULTIPLE_VEHICLE_SELECT");				
				//json.add("searchType",  gson.toJsonTree(itemSearch));
				
				ConfigItem itemTime = ConfigItem.lookup("MULTIPLE_VEHICLE_TIME");		
				//json.add("timeMultiple",  gson.toJsonTree(itemTime));
				
				ConfigItem itemMax = ConfigItem.lookup("MULTIPLE_VEHICLE_MAX");		
				
				Gson mygson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, searchType: "+mygson.toJson(itemSearch)+", timeMultiple: "+mygson.toJson(itemTime)+", timeMax: "+mygson.toJson(itemMax)+"}");

			}catch (Exception e) {
				json.addProperty("msg", "Error loading the general administration");
			}
		}
		
		
	}
		
	public ModelAndView codes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return null;
		}
		
		String action = request.getParameter("action");
		if(action != null && action.equals("save"))
		{
			String sql = "DELETE from config_item where name LIKE 'CODES_%'";
			DBConnection connection = null;
			try
			{
				connection = new DBConnection();
				if(connection.execute(sql))
				{
					ConfigItem item = new ConfigItem("CODES_PATH", request.getParameter("file_path"), 0, 0);
					
					if(item.commit())
					{
						String[] types = {"STATES", "MAKES", "COLORS", "LOCATIONS", "COMMENTS", "VIOLATIONS"};
						for(int i = 0; i < types.length; i++)
						{
							String param = types[i]+"_ENABLED";
							item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
							item.commit();
							
							param = types[i]+"_XPATH";
							item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
							item.commit();
							
							param = types[i]+"_ID";
							item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
							item.commit();
							
							param = types[i]+"_DESCRIPTION";
							item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
							item.commit();
						}
					
						String param = "VIOLATIONS_OVERTIME";
						item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
						item.commit();
						
						param = "VIOLATIONS_AMOUNT";
						item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
						item.commit();
						
						param = "VIOLATIONS_TYPE";
						item = new ConfigItem("CODES_"+param, request.getParameter(param), 0, 0);
						item.commit();

					}
					response.setContentType("text/json");
					response.getOutputStream().print("{success: true}");
					return null;
				}
			}
			catch(Exception e)
			{
				response.setContentType("text/json");
				response.getOutputStream().print("{success: false, msg: 'Error saving configuration: "+e.getMessage()+"'}");
				return null;
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
		
		// save General
		else if(action != null && action.equals("save-general"))
		{
			String sql = "DELETE from config_item where name LIKE 'CODE_SORT_%' OR name LIKE 'CODE_DEFAULT_%'";
			DBConnection connection = null;
			try
			{
				connection = new DBConnection();
				if(connection.execute(sql))
				{					
					String[] types = {"STATE", "MAKE", "COLOR", "LOCATION", "COMMENT", "VIOLATION"};
					for(int i = 0; i < types.length; i++)
					{
						String param = types[i];
						ConfigItem item = new ConfigItem("CODE_SORT_"+param, request.getParameter(param.toLowerCase()+"-sort"), 0, 0);
						item.commit();
						
						 param = types[i];
						item = new ConfigItem("CODE_DEFAULT_"+param, request.getParameter(param.toLowerCase()+"-default"), 0, 0);
						item.commit();
					}
					
					sql = "DELETE from config_item where name = 'CODE_USE_STICKY'";
					connection = new DBConnection();
					if(connection.execute(sql))
					{					
						
						String resp =request.getParameter("use-stick-check-name");
						if(resp != null && resp.equals("on"))
						{
							resp = "true";
						}
						else
						{
							resp = "false";
						}
							
						ConfigItem item = new ConfigItem("CODE_USE_STICKY", resp, 0, 0);
						item.commit();

					}
					
				
					response.setContentType("text/json");
					response.getOutputStream().print("{success: true}");
					return null;
				}
			}
			catch(Exception e)
			{
				response.setContentType("text/json");
				response.getOutputStream().print("{success: false, msg: 'Error saving configuration: "+e.getMessage()+"'}");
				return null;
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
		
		// end save general
		
		
		// read general configuration
		else if(action != null && action.equals("read-general"))
		{
			try
			{
				ConfigItem item = new ConfigItem();
				DBFilterList filter = new DBFilterList();
				DBFilter configFilter = new DBFilter("name", "LIKE","CODE_SORT_%");
				filter.add(configFilter);
				filter.addOr(configFilter, new DBFilter("name", "LIKE","CODE_DEFAULT_%"));
				filter.addOr(configFilter, new DBFilter("name", "=","CODE_USE_STICKY" ));
				
				@SuppressWarnings("unchecked")
				ArrayList<ConfigItem> configDefault = (ArrayList<ConfigItem>)item.get(0, 0, "name ASC", filter);
													
				response.setContentType("text/json");
				Gson gson = new Gson();
				String json = gson.toJson(configDefault);
				
				response.getOutputStream().print("{ configDefault: " + json + "}");
				return null;
						
			}
			catch(Exception ignore){}
		}
		// end read general configuration
		
		return new ModelAndView("codes");
	}
	
	public void printList(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		ArrayList<PrintLayout> printLayoutList = null;
		
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}

		try {
			
			String sort = request.getParameter("sort");//$NON-NLS-1$
			String dir = request.getParameter("dir");//$NON-NLS-1$
			String filter = request.getParameter("filter");//$NON-NLS-1$
			
			printLayoutList = PrintLayout.load(sort,dir,filter);
			json.add("printLayout", gson.toJsonTree(printLayoutList)); //$NON-NLS-1$
			json.addProperty("count", printLayoutList.size()); //$NON-NLS-1$
			response.setContentType("text/json");//$NON-NLS-1$
			response.getOutputStream().print(gson.toJson(json));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void printSave(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		PrintLayout printLayout = null;
		boolean success = false;
		boolean defaultValue;
		String msg = "Error Save";//$NON-NLS-1$
		String name = null;
		String value = null;
		int groupId;

		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}

		try {

			response.setContentType("text/json");//$NON-NLS-1$
			name = request.getParameter("name");//$NON-NLS-1$
			value = request.getParameter("value");//$NON-NLS-1$
			groupId = Integer.parseInt(request.getParameter("groupId"));//$NON-NLS-1$
			defaultValue = (request.getParameter("isDefault") == null) || !(request.getParameter("isDefault").equals("on")) ? false : true;

		    DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document document = parser.parse(new InputSource(new StringReader(value)));
		    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		    InputStream inputStream = this.getClass().getResourceAsStream("/printLayout.xsd");
		    Source schemaFile = new StreamSource(inputStream);
		    Schema schema = factory.newSchema(schemaFile);

		    Validator validator = schema.newValidator();
		    validator.validate(new DOMSource(document));
	    
			printLayout = new PrintLayout(name, value, groupId,defaultValue);

			if(printLayout.save()){
				success = true;
				JsonObject printResponse = new JsonObject();
				printResponse.addProperty("name", name);//$NON-NLS-1$
				printResponse.addProperty("groupId", printLayout.getGroupId());//$NON-NLS-1$
				json.add("print", printResponse);//$NON-NLS-1$
			}
	
		} catch (SAXException e) {
			msg = e.getMessage();
		}catch (Exception e) {
			e.printStackTrace();
		}

		json.addProperty("success", success);//$NON-NLS-1$
		json.addProperty("msg", msg);//$NON-NLS-1$
		response.getOutputStream().print(gson.toJson(json));

	}
	
	
	public void deletePrint(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {

		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		boolean success = false;
		int groupId;

		try {

			response.setContentType("text/json");//$NON-NLS-1$
			groupId = Integer.parseInt(request.getParameter("groupId"));//$NON-NLS-1$
			PrintLayout.clear(groupId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		json.addProperty("success", success);//$NON-NLS-1$
		json.addProperty("msg", "");//$NON-NLS-1$
		response.getOutputStream().print(gson.toJson(json));

	}
	
	/*public ModelAndView merchant(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return null;
		}
		
		ArrayList<CreditCardType> types = CreditCardType.get();
		String action = request.getParameter("xaction");
		if(action != null)
		{
			response.setContentType("text/json");
			JsonObject json = new JsonObject();
			json.addProperty("success", false);
			if(action.equalsIgnoreCase("save"))
			{
				 String merchantClass = request.getParameter("merchant-class");
				 if(merchantClass != null && merchantClass.length() > 0)
				 {
					try
					{
						Merchant merchant = Merchant.factory(merchantClass);
						merchant.set(request);
						if(merchant.save())
						{
							// need to save
							for(CreditCardType ct: types)
							{
								String on = request.getParameter("cc_type-"+ct.cc_type_id);
								if(on != null && on.length() > 0)
								{
									ct.accepted = 1;
								}
								else
								{
									ct.accepted = 0;
								}
								
								ct.commit();
							}
							json.addProperty("success", true);
						}
						else
						{
							json.addProperty("msg", "Error saving merchant information.");
						}
					} 
					catch (ClassNotFoundException e)
					{
						 json.addProperty("msg", "Unknown merchant.");
					} 
					catch (MerchantException e)
					{

						 json.addProperty("msg", "Error saving merchant: " + e.getMessage());
					}
				 }
				 else
				 {
					 json.addProperty("msg", "Unknown merchant.");
				 }
					 
			}
			else if(action.equalsIgnoreCase("details"))
			{
				 String merchantClass = request.getParameter("merchant-class");
				 if(merchantClass != null && merchantClass.length() > 0)
				 {
					try
					{
						Merchant merchant = Merchant.factory(merchantClass);						
						
						json.addProperty("success", true);
						json.addProperty("details", merchant.getDetails());
					} 
					catch (ClassNotFoundException e)
					{
						json.addProperty("msg", "Unknown merchant.");
					}
				 }
				 else
				 {
					 json.addProperty("msg", "Unknown merchant.");
				 }
			}
			
			Gson gson = new GsonBuilder().create();
			response.getOutputStream().print(gson.toJson(json));
			return null;
		}
		
		Merchant merchant = null;
		try
		{
			ConfigItem item = new ConfigItem("MERCHANT_CLASS");
			merchant = Merchant.factory(item.text_value);
		}
		catch(Exception e){}
		
		ModelAndView mv = new ModelAndView("merchant");
		mv.addObject("merchants", Merchant.getMerchants());
		mv.addObject("merchant", merchant);
		mv.addObject("cc_types", types);
		
		return mv;
	}*/
	
	/*public void lateFees(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		boolean exist = false;
		
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		String action = request.getParameter("xaction");
		if(action != null)
		{
			response.setContentType("text/json");
			JsonObject json = new JsonObject();
			json.addProperty("success", false);
			
			if(action.equals("save"))
			{
				try 
				{
					int lateFeeId = Integer.parseInt(request.getParameter("late_fee_id"));
					LateFee lf = new LateFee(lateFeeId);
					
					lf.violation_id = request.getParameter("latefee_violation_id");
					lf.days_late = Integer.parseInt(request.getParameter("latefee_days_late"));
					lf.fee_amount = Float.parseFloat(request.getParameter("latefee_fee_amount"));
					
					
					LateFee latefee = new LateFee();
					@SuppressWarnings("unchecked")
					ArrayList<LateFee> list = (ArrayList<LateFee>)latefee.get(0, 0, "", null);
					
					for(LateFee lateFee : list){
						if((lateFee.days_late == lf.days_late) && (lateFee.violation_id.equals(lf.violation_id)) ){
							json.addProperty("msg", "The late fee configuration exist.");
							exist = true;
							break;
						}
					}
			
					if(!exist){
						
						if(lf.commit())
						{
							json.addProperty("success", true);
						}
						else
						{
							json.addProperty("msg", "Error saving late fee.");
						}
						
					}
					
				}
				catch(NumberFormatException nfe)
				{
					json.addProperty("msg", "Inalid late fee id.");
				}
				catch(UnknownObjectException uoe)
				{
					json.addProperty("msg", "Inalid late fee id.");
				}
			}
			
			if(action.equals("delete"))
			{
				try 
				{
					
					int lateFeeId = Integer.parseInt(request.getParameter("late_fee_id"));
					LateFee lf = new LateFee(lateFeeId);
					if(lf.delete()){
						json.addProperty("success", true);
					}
					
				}
				catch(NumberFormatException nfe)
				{
					json.addProperty("msg", "Inalid late fee id.");
				}
				catch(UnknownObjectException uoe)
				{
					json.addProperty("msg", "Inalid late fee id.");
				}
			}
			
			Gson gson = new GsonBuilder().create();
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		try
		{
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("filter_violation_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("violation_id", "LIKE", value));
			}
			
			value = request.getParameter("filter_days_late");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("days_late", "=", value));
			}
			
			value = request.getParameter("filter_fee_amount");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("fee_amount", "=", value));
			}
						
			int start = 0;
			int limit = 0;
			
			if(request.getParameter("start") != null)
			{
				try
				{
					start = Integer.parseInt(request.getParameter("start"));
				}
				catch(NumberFormatException nfe){}
			}
			
			if(request.getParameter("limit") != null)
			{
				try
				{
					limit = Integer.parseInt(request.getParameter("limit"));
				}
				catch(NumberFormatException nfe){}
			}
			
			LateFee latefee = new LateFee();
			@SuppressWarnings("unchecked")
			ArrayList<LateFee> list = (ArrayList<LateFee>)latefee.get(start, limit, sort + " " + dir, filter);
			
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			response.setContentType("text/json");
			String json = gson.toJson(list);
			
			int count = list.size();
			if(limit > 0)
			{
				count = latefee.count(filter);
			}
			
			response.getOutputStream().print("{count: "+count+", latefees: " + json + "}");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}*/
	
/*	public ModelAndView citation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return null;
		}
		
		String action = request.getParameter("xaction");
		if(action != null)
		{
			response.setContentType("text/json");
			
			CiteFields fields = new CiteFields();
			if(action.equals("get"))
			{
				String name = request.getParameter("name");
				CiteField field = fields.getField(name);
				if(field != null)
				{
					Gson gson = new GsonBuilder().create();
					response.getOutputStream().print("{success: true, field: "+gson.toJson(field)+"}");
				}
				else
				{
					response.getOutputStream().print("{success: false, msg: 'Field not found.'}");
				}
			}
			else if(action.equals("add"))
			{
				String name = request.getParameter("field-name");
				String label = request.getParameter("field-label");
				String type = request.getParameter("field-value-type");
				String required;
				
				if(name.equals("violation")){
					required = "on";
				}else{
					required = request.getParameter("field-required");	
				}
				
				CiteField field = null;
				if(name.equals("ot"))
				{
					field = new CiteField(label, type);
					if(type.equals(CiteField.TYPE_LIST))
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
										field.addOption(new FieldOption(id.trim(), value.trim()));
									}
								}
							}
						}
						catch(NumberFormatException nfe){}
					}
					else if(type.equals(CiteField.TYPE_DB))
					{
						field.setDatabaseOption(request.getParameter("field-table-name"), 
								request.getParameter("field-id-field"), 
								request.getParameter("field-description-field"),
								request.getParameter("field-where"));
					}
					else if(type.equals(CiteField.TYPE_TEXT))
					{
						field.setValidation(request.getParameter("field-validation"));
					}
				}
				else
				{
					field = new CiteField(name);
				}
				

				field.required = (required != null && required.equals("on"));
				if(fields.exists(field))
				{
					response.getOutputStream().print("{success: false, msg: 'Field already exists.'}");
				}
				else
				{
					fields.add(field);
					Gson gson = new GsonBuilder().create();
					response.getOutputStream().print("{success: true, field: "+gson.toJson(field)+"}");
				}			
			}
			else if(action.equals("update"))
			{
				String orig = request.getParameter("orig_name");
				
				CiteField field = fields.getField(orig);
				if(field == null)
				{
					response.getOutputStream().print("{success: false, msg: 'Field not found.'}");
				}
				else
				{
					String name = request.getParameter("field-name");
					String label = request.getParameter("field-label");
					String type = request.getParameter("field-value-type");
					String required;
					
					if(name.equals("violation")){
						required = "on";
					}else{
						required = request.getParameter("field-required");	
					}
					
					CiteField newField = null;
					if(name.equals("ot"))
					{
						newField = new CiteField(label, type);
						if(type.equals(CiteField.TYPE_LIST))
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
											newField.addOption(new FieldOption(id.trim(), value.trim()));
										}
									}
								}
							}
							catch(NumberFormatException nfe){}
						}
						else if(type.equals(CiteField.TYPE_DB))
						{
							newField.setDatabaseOption(request.getParameter("field-table-name"), 
									request.getParameter("field-id-field"), 
									request.getParameter("field-description-field"),
									request.getParameter("field-where"));
						}
						else if(type.equals(CiteField.TYPE_TEXT))
						{
							newField.setValidation(request.getParameter("field-validation"));
						}
					}
					else
					{
						newField = new CiteField(name);
					}
					
					newField.required = (required != null && required.equals("on"));
					if(fields.exists(newField) && field.equals(newField) == false) //if the fields are not the same, then we are naming as an existing field
					{
						response.getOutputStream().print("{success: false, msg: 'Field already exists.'}");
					}
					else
					{
						fields.update(field, newField);
						Gson gson = new GsonBuilder().create();
						response.getOutputStream().print("{success: true, field: "+gson.toJson(newField)+"}");
					}
				}
			}
			else if(action.equals("move"))
			{
				String name = request.getParameter("field-name");
				String dir = request.getParameter("dir");
				
				CiteField field = fields.getField(name);
				if(field == null)
				{
					response.getOutputStream().print("{success: false, msg: 'Field not found.'}");
				}
				else
				{
					if(fields.move(field, dir))
					{
						response.getOutputStream().print("{success: true, msg: 'Field moved.'}");
					}
					else
					{
						response.getOutputStream().print("{success: false, msg: 'Error moving field.'}");
					}
				}
			}
			else if(action.equals("remove"))
			{
				String name = request.getParameter("field-name");
				
				if(fields.remove(name))
				{

					CiteDeviceFields dPagesField = new CiteDeviceFields();				
					Boolean msg = false;
					int size = dPagesField.pagesSize();
					if(size != -1){
						for (int i = 0; i < size; i++) {
							
							if(dPagesField.removeFiel(i, name)){
								msg = true;
								break;
							}
						}
						
					}
					
					response.getOutputStream().print("{success: true, msg: 'Field removed.', msg2: "+msg+"}");
				}
				else
				{
					response.getOutputStream().print("{success: false, msg: 'Error removing field.'}");
				}
			}
			else if(action.equals("cite-general"))
			{
				String format = request.getParameter("format");
				String editTime = request.getParameter("editTime");
				String daysDispute = request.getParameter("daysDispute");
				ConfigItem itemNumberFormat = null;
				ConfigItem itemEditTimet = null;
				ConfigItem itemDaysDispute = null;
				
				try
				{
					itemNumberFormat = ConfigItem.lookup("CITE_NUMBER_FORMAT");
					itemEditTimet = ConfigItem.lookup("CITE_EDIT_TIMER");
					itemDaysDispute = ConfigItem.lookup("DAYS_TO_DISPUTE");
				}
				catch(Exception e)
				{
					try
					{
						itemNumberFormat = new ConfigItem();
					}
					catch(Exception uoe)
					{
						uoe.printStackTrace();
						response.getOutputStream().print("{success: false, msg: 'Error saving citation fields config.'}");
						return null;
					}
				}
				
				itemNumberFormat.name = "CITE_NUMBER_FORMAT";
				itemNumberFormat.text_value = format;
				itemNumberFormat.commit();
						
				itemEditTimet.name = "CITE_EDIT_TIMER";
				itemEditTimet.text_value = editTime;
				itemEditTimet.commit();
				
				itemDaysDispute.name = "DAYS_TO_DISPUTE";
				itemDaysDispute.text_value = daysDispute;
				itemDaysDispute.commit();
	
				response.getOutputStream().print("{success: true, msg: 'Format saved.'}");
				
				
			}else if (action.equals("read-general"))
			{
				ConfigItem itemNumberFormat = ConfigItem.lookup("CITE_NUMBER_FORMAT");
				ConfigItem itemEditTimet = ConfigItem.lookup("CITE_EDIT_TIMER");
				ConfigItem itemDaysDispute = ConfigItem.lookup("DAYS_TO_DISPUTE");
				
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, numberFormat: "+gson.toJson(itemNumberFormat)+", editTime: "+gson.toJson(itemEditTimet)+", daysDispute: "+gson.toJson(itemDaysDispute)+"}");

			}else
			{
				response.getOutputStream().print("{success: false, msg: 'Error: Unrecognized command.'}");
			}
			
			return null;
		}
		

		ModelAndView mv = new ModelAndView("citation");
		
		CiteFields cFields = new CiteFields();
		mv.addObject("fields", cFields.getFields());
		
		return mv;
	}*/
	
	/*public ModelAndView ReadHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		User user = User.getCurrentUser();
		HotListColumnMetaData meta = null;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return null;
		}
		
		String action = request.getParameter("action");
		if(action != null && action.equals("save"))
		{
			Map <String, String> verify = new HashMap<String, String>(); 
			int count = Integer.parseInt(request.getParameter("count"));
			String path = request.getParameter("file_path");
			
			for(int i = 0; i < count; i++){	
				
				if((!request.getParameter("mapping_value_"+i).equals("")) && verify.containsValue(request.getParameter("mapping_value_"+i)))
				{
					json.addProperty("msg", "The permit Mapping" + request.getParameter("mapping_value_"+i) + "exist");
					response.getOutputStream().print(gson.toJson(json));
					return null;
				}
				
				if(verify.containsKey(request.getParameter("name_"+i))){
					json.addProperty("msg", "The field name" + request.getParameter("name_"+i) + "exist");
					response.getOutputStream().print(gson.toJson(json));
					return null;
				}
				
				verify.put(request.getParameter("name_"+i), request.getParameter("mapping_value_"+i));
				
			}
			
			if(HotListColumnMetaData.clear())
			{
				meta = new HotListColumnMetaData (path);
				if(meta.savePath())
				{
					for(int i = 0; i < count; i++)
					{
						
						String queryName = request.getParameter("name_"+i);
						String columnName = Column.columnNameFromName(queryName);
						String label = queryName;
						int order = Integer.parseInt(request.getParameter("order_"+i));
						int displayOrder = Integer.parseInt(request.getParameter("display_order_"+i));
						String mapping = request.getParameter("mapping_value_"+i);
						
						if(mapping == null || mapping.length() == 0)
						{
							mapping = "none";
						}
						
						meta = new HotListColumnMetaData(label, queryName, columnName, mapping, displayOrder, order);
						meta.save();
					
					}
				}
			}
			
			response.setContentType("text/json");
			response.getOutputStream().print("{success: true}");
			return null;
		}
		
		return new ModelAndView("ReadHistory");
	}*/
	
	/*public ModelAndView citationDevice(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		 
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			response.setContentType("text/json");
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return null;
		}
		
		String action = request.getParameter("action");
		if(action != null)
		{
			
			if (action.equals("loadPages"))
			{
				
				CiteDeviceFields dPagesFields = new CiteDeviceFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(dPagesFields.getPages())+"}");
				
				
			}else if (action.equals("listFields"))
			{
				
				CiteFields cFields = new CiteFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(cFields.getFields())+"}");

			}else if (action.equals("addFiled")){
				
				
				int page_num = Integer.parseInt(request.getParameter("page_num"));
				String name = request.getParameter("field-name");
		
				CiteField field = null;
				CiteDeviceFields deviceField = new CiteDeviceFields();			
				
				field = new CiteField(name);
				

				if(deviceField.exists(field))
				{
					response.getOutputStream().print("{success: false, msg: 'Field already exists.'}");
				}
				else
				{
					deviceField.addField(page_num, field);
					Gson gson = new GsonBuilder().create();
					response.getOutputStream().print("{success: true, field: "+gson.toJson(field)+"}");
				}
				
				
			}else if (action.equals("createPage"))
			{
				
				CiteDeviceFields dPagesFields = new CiteDeviceFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(dPagesFields.addPage())+"}");
				

			}else if (action.equals("deletePage"))
			{
				
				int page_num = Integer.parseInt(request.getParameter("page_num"));
				
				CiteDeviceFields dPagesFields = new CiteDeviceFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(dPagesFields.removePage(page_num))+"}");
				

			}else if (action.equals("deleteFild"))
			{
				
				int page_num = Integer.parseInt(request.getParameter("page_num"));
				String name = request.getParameter("field-name");
				
				
				CiteDeviceFields dPagesField = new CiteDeviceFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(dPagesField.removeFiel(page_num, name))+"}");
				

			}else if (action.equals("move"))
			{
				
				int page_num = Integer.parseInt(request.getParameter("page_num"));
				String name = request.getParameter("field-name");
				String dir = request.getParameter("dir");
				
				CiteField field = null;	
				field = new CiteField(name);
				
				CiteDeviceFields dPagesField = new CiteDeviceFields();				
				JsonObject json = new JsonObject();
				Gson gson = new GsonBuilder().create();
				response.getOutputStream().print("{success: true, result: "+gson.toJson(dPagesField.move(page_num, field, dir))+"}");

			}
			
			
			return null;
		}

		ModelAndView mv = new ModelAndView("citationDevice");
		/*CiteDeviceFields cFields = new CiteDeviceFields();
		mv.addObject("fields", cFields.getPages()); */
		/*
		return mv;
	}*/
	
	/*public void general (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || !user.isAdmin())
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}

		AuthorizationType type = AuthorizationType.USER;
		String action = request.getParameter("action");
		
		if(action != null && action.equals("save")){
			try {
				
				String message = request.getParameter("userMessage") != null ? request.getParameter("userMessage") : "";
				String regExpresion = request.getParameter("userRegExpression") != null ? request.getParameter("userRegExpression") : "";
				boolean isEnable = request.getParameter("user-password-check-name") != null && request.getParameter("user-password-check-name").equals("on") ? true : false;
				boolean isExpirationEnable =  request.getParameter("user-expiration-password-check-name") != null  && request.getParameter("user-expiration-password-check-name").equals("on") ? true : false;
				int time = request.getParameter("userTime") != null && !request.getParameter("userTime").isEmpty() ? Integer.parseInt(request.getParameter("userTime")) : 0;
				Intervals intervalsTime = request.getParameter("userIntervals") != null && (request.getParameter("userIntervals").equals(Intervals.DAY.name()) || request.getParameter("userIntervals").equals(Intervals.WEEK.name())) ? Intervals.valueOf(request.getParameter("userIntervals")) : null;
				
				Pattern.compile(regExpresion);
				PasswordConfig passwordConfig = new PasswordConfig(message, regExpresion, type, isEnable, intervalsTime, time, isExpirationEnable);
				passwordConfig.save();
				
				String processPath =  request.getParameter("processPath");
				ConfigItem item = ConfigItem.lookup("PROCESS_INTERCEPTOR_CLASS");
				item.setTextValue(processPath);
				item.commit();
				
				String imgPath =  request.getParameter("imgPath");
				ConfigItem itemPath  = ConfigItem.lookup("IMG_PATH");
				itemPath.setTextValue(imgPath);
				itemPath.commit();

			} catch (PatternSyntaxException exception) {
	        	json.addProperty("msg", " Please enter a valid regular expression.");
	        	return;
	        }catch (Exception e) {
				json.addProperty("msg", "Error saving the general administration");
				return;
			}
		}
		
		else if (action != null && action.equals("read-general")){
			try {
				PasswordConfig passwordConfig = PasswordConfig.get(type);
				json.add("passwordConfig",  gson.toJsonTree(passwordConfig));
								
				ConfigItem item = ConfigItem.lookup("PROCESS_INTERCEPTOR_CLASS");				
				json.add("processClass",  gson.toJsonTree(item));
				
				ConfigItem itemPath = ConfigItem.lookup("IMG_PATH");				
				json.add("imgPath",  gson.toJsonTree(itemPath));
				
			}catch (Exception e) {
				json.addProperty("msg", "Error loading the general administration");
			}
		}
		
		json.addProperty("success", true);
		response.getOutputStream().print(gson.toJson(json));
	}*/
	
}

	