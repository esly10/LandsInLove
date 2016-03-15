
package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.fop.apps.FOPException;
import org.jsoup.select.Evaluator.IsEmpty;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.apache.velocity.app.VelocityEngine;

import au.com.bytecode.opencsv.CSVWriter;

import com.cambiolabs.citewrite.data.Agencies;
import com.cambiolabs.citewrite.data.Charges;
import com.cambiolabs.citewrite.data.Citation;
import com.cambiolabs.citewrite.data.CiteField;
import com.cambiolabs.citewrite.data.CiteFields;
import com.cambiolabs.citewrite.data.Code;
import com.cambiolabs.citewrite.data.Codes;
import com.cambiolabs.citewrite.data.DateFormater;
import com.cambiolabs.citewrite.data.Field;
import com.cambiolabs.citewrite.data.Guests;
import com.cambiolabs.citewrite.data.LateFee;
import com.cambiolabs.citewrite.data.ManagedPermitField;
import com.cambiolabs.citewrite.data.ManagedPermitFields;
import com.cambiolabs.citewrite.data.ManagedPermitTypeField;
import com.cambiolabs.citewrite.data.ManagedPermitTypeFields;
import com.cambiolabs.citewrite.data.Owner;
import com.cambiolabs.citewrite.data.Report;
import com.cambiolabs.citewrite.data.ReportCriteria;
import com.cambiolabs.citewrite.data.ReportField;
import com.cambiolabs.citewrite.data.ReservationRoom;
import com.cambiolabs.citewrite.data.Reservations;
import com.cambiolabs.citewrite.data.Rooms;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.Invoice;
import com.cambiolabs.citewrite.ecommerce.InvoiceItem;
//import com.cambiolabs.citewrite.ecommerce.InvoiceItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportController extends MultiActionController
{
	private static VelocityEngine velocityEngine;
	private String template_path = "config/template/mealplan.vm";
	//private String template_path_footer = "config/template/fo_footer.vm";

	public void setVelocityEngine(VelocityEngine vEngine) {
		velocityEngine = vEngine;
	}
	protected final Log logger = LogFactory.getLog(getClass());
	public static Properties properties;
	
	public void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String sort = request.getParameter("sort");
		String dir = request.getParameter("dir");
		
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		if(sort == null)
		{
			sort = "name";
		}
		
		if(dir == null)
		{
			dir = "ASC";
		}
		Report report = new Report();
		@SuppressWarnings("unchecked")
		ArrayList<Report> list = (ArrayList<Report>)report.get(0, 0, sort + " " + dir, null);
		int count = list.size();
		
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
		json.addProperty("success", true);
		json.addProperty("count", count);
	    json.add("reports", gson.toJsonTree(list));
		
	    response.getOutputStream().print(gson.toJson(json));
	}
	
	private ArrayList<CiteField> getSelectCiteFields()
	{

		CiteFields fields = new CiteFields();
		ArrayList<CiteField> citeFieldList = new ArrayList<CiteField>();
		
		citeFieldList.add(new CiteField("cite-citation_number", "Citation Number", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("cite-pin", "Pin", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("cite-status", "Status", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("users-first_name", "First Name", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("users-last_name", "Last Name", CiteField.TYPE_STANDARD));
		
		for(CiteField citeField : fields.getFields(false, false)){
			
			if(citeField.type.equals(CiteField.TYPE_STANDARD)){
				
				citeField.name = "cite-" + citeField.name;
				
			}else if (citeField.name.equals(CiteField.FN_VIOLATION)){
			
				citeFieldList.add(new CiteField("cite-violation_id", "Violation Id", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_type", "Violation Type", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_description", "Violation Description", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_amount", "Violation Amount", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_start", "Violation Start", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_end", "Violation End", CiteField.TYPE_STANDARD));
				continue;
				
			}else if (citeField.type.equals(CiteField.TYPE_CODES)){
				
				if(citeField.name.equals("comment")){
					citeFieldList.add(new CiteField("cite-" + "comments", citeField.label, CiteField.TYPE_CODES));
				}else{
					citeFieldList.add(new CiteField("cite-" + citeField.name + "_description", citeField.label, CiteField.TYPE_CODES));
					
				}
				
				citeField.name = "cite-" + citeField.name + "_id";
				citeField.label = citeField.label+" ID";
				
			} else if(citeField.type.equals(CiteField.TYPE_DB) || citeField.type.equals(CiteField.TYPE_LIST)){
			
				citeFieldList.add(new CiteField("attr-" + citeField.name , citeField.label, citeField.type));
				citeField.name = "attr-" + citeField.name + "_id";
				citeField.label = citeField.label+" ID";
				
			}else if(citeField.type.equals(CiteField.TYPE_TEXT)){
				
				citeField.name = "attr-" + citeField.name;
				
			}
			
			citeFieldList.add(citeField);
		}

		return citeFieldList;
	}
	
	private ArrayList<CiteField> getCriteriaCiteFields()
	{
		
		CiteFields fields = new CiteFields();
		ArrayList<CiteField> citeFieldList = new ArrayList<CiteField>();
		
		citeFieldList.add(new CiteField("cite-citation_number", "Citation Number", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("cite-pin", "Pin", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("cite-status", "Status", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("users-first_name", "First Name", CiteField.TYPE_STANDARD));
		citeFieldList.add(new CiteField("users-last_name", "Last Name", CiteField.TYPE_STANDARD));
		
		for(CiteField citeField : fields.getFields(false, true)){
			
			if (citeField.name.equals(CiteField.FN_VIOLATION)){
			
				citeFieldList.add(new CiteField("cite-violation_id", "Violation Id", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_type", "Violation Type", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_description", "Violation Description", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_amount", "Violation Amount", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_start", "Violation Start", CiteField.TYPE_STANDARD));
				citeFieldList.add(new CiteField("cite-violation_end", "Violation End", CiteField.TYPE_STANDARD));
				continue;
			}else if (citeField.type.equals(CiteField.TYPE_STANDARD) || citeField.type.equals(CiteField.TYPE_CODES)){
				
				citeField.name = "cite-" + citeField.name;
				
				if(citeField.type.equals(CiteField.TYPE_CODES)){
					citeField.name = citeField.name + "_id";
				}
				
			} else if(citeField.type.equals(CiteField.TYPE_DB) || citeField.type.equals(CiteField.TYPE_LIST) || citeField.type.equals(CiteField.TYPE_TEXT) ){
			
				citeField.name = "attr-" + citeField.name;
				if(citeField.type.equals(CiteField.TYPE_DB) || citeField.type.equals(CiteField.TYPE_LIST)){
					citeField.name = citeField.name + "_id";
				}
				
			}
			
			citeFieldList.add(citeField);
		}
		
		return citeFieldList;
	}
	
	private ArrayList<Field> getSelectPermitFields()
	{

		ManagedPermitFields fields = new ManagedPermitFields();
		ManagedPermitTypeFields fieldsType = new ManagedPermitTypeFields();
		ArrayList<Field> managedPermitFieldList = new ArrayList<Field>();
		
		
		managedPermitFieldList.add(new ManagedPermitField("owner-first_name", "First Name", ManagedPermitField.TYPE_STANDARD));
		managedPermitFieldList.add(new ManagedPermitField("owner-last_name", "Last Name", ManagedPermitField.TYPE_STANDARD));
		
		for (ManagedPermitField managedPermitField : fields.getDefaultFields()) {
			
			if(managedPermitField.name.equals("valid_end_date")){
				managedPermitFieldList.add(new ManagedPermitField("mper-valid_end_date", "End Date", ManagedPermitField.TYPE_STANDARD));
				managedPermitFieldList.add(new ManagedPermitField("mper-valid_start_date", "Start Date", ManagedPermitField.TYPE_STANDARD));
				continue;
				
			}else if (managedPermitField.name.equals("permit_type")){continue;}

			managedPermitField.name = "mper-" + managedPermitField.name;

			managedPermitFieldList.add(managedPermitField);
			
		}
		
		for(ManagedPermitField managedPermitField : fields.getFields(false, false)){
			
			if (managedPermitField.type.equals(ManagedPermitField.TYPE_CODES)){
				
				managedPermitFieldList.add(new ManagedPermitField("mper-" + managedPermitField.name + "_description", managedPermitField.label, ManagedPermitField.TYPE_CODES));
				managedPermitField.name = "mper-" + managedPermitField.name + "_id";
				managedPermitField.label = managedPermitField.label+" ID";
				
			} else if(managedPermitField.type.equals(ManagedPermitField.TYPE_DB) || managedPermitField.type.equals(ManagedPermitField.TYPE_LIST)){
			
				managedPermitFieldList.add(new ManagedPermitField("attr-" + managedPermitField.name , managedPermitField.label, managedPermitField.type));
				continue;
			}else if(managedPermitField.type.equals(ManagedPermitField.TYPE_TEXT)){
				
				managedPermitField.name = "attr-" + managedPermitField.name;
				
			}
			
			managedPermitFieldList.add(managedPermitField);
		}
		
		managedPermitFieldList.add(new ManagedPermitTypeField("mpermit_type-name", "Type Name", ManagedPermitTypeField.TYPE_STANDARD));
		managedPermitFieldList.add(new ManagedPermitTypeField("mpermit_type-description", "Type Description", ManagedPermitTypeField.TYPE_STANDARD));
		
		for(ManagedPermitTypeField managedPermitTypeField : fieldsType.getFields(false, false)){
			
			if (managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_CODES)){
				
				managedPermitFieldList.add(new ManagedPermitTypeField("mpermit_type-" + managedPermitTypeField.name + "_description", managedPermitTypeField.label, ManagedPermitTypeField.TYPE_CODES));
				managedPermitTypeField.name = "mpermit_type-" + managedPermitTypeField.name + "_id";
				managedPermitTypeField.label = managedPermitTypeField.label+" ID";
				
			} else if(managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_DB) || managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_LIST)){
			
				managedPermitFieldList.add(new ManagedPermitTypeField("attrType-" + managedPermitTypeField.name , managedPermitTypeField.label, managedPermitTypeField.type));
				continue;
			}else if(managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_TEXT)){
				
				managedPermitTypeField.name = "attrType-" + managedPermitTypeField.name;
				
			}
			
			managedPermitFieldList.add(managedPermitTypeField);
		}

		return managedPermitFieldList;
	}
	
	private ArrayList<Field> getCriteriaPermitFields()
	{

		ManagedPermitFields fields = new ManagedPermitFields();
		ManagedPermitTypeFields fieldsType = new ManagedPermitTypeFields();
		ArrayList<Field> managedPermitFieldList = new ArrayList<Field>();
		
		managedPermitFieldList.add(new ManagedPermitField("owner-first_name", "First Name", ManagedPermitField.TYPE_STANDARD));
		managedPermitFieldList.add(new ManagedPermitField("owner-last_name", "Last Name", ManagedPermitField.TYPE_STANDARD));

		for (ManagedPermitField managedPermitField : fields.getDefaultFields()) {
			
			if(managedPermitField.name.equals("valid_end_date")){
				managedPermitFieldList.add(new ManagedPermitField("mper-valid_end_date", "End Date", ManagedPermitField.TYPE_STANDARD));
				managedPermitFieldList.add(new ManagedPermitField("mper-valid_start_date", "Start Date", ManagedPermitField.TYPE_STANDARD));
				continue;
				
			}else if (managedPermitField.name.equals("permit_type")){continue;}
			
			managedPermitField.name = "mper-" + managedPermitField.name;

			managedPermitFieldList.add(managedPermitField);
			
		}
		
		for(ManagedPermitField managedPermitField : fields.getFields(false, false)){
			
			if (managedPermitField.type.equals(ManagedPermitField.TYPE_CODES)){

				managedPermitField.name = "mper-" + managedPermitField.name + "_id";
				
			} else if(managedPermitField.type.equals(ManagedPermitField.TYPE_DB) || managedPermitField.type.equals(ManagedPermitField.TYPE_LIST) || managedPermitField.type.equals(ManagedPermitField.TYPE_TEXT) ){
			
				managedPermitField.name = "attr-" + managedPermitField.name;
				
			}
			
			managedPermitFieldList.add(managedPermitField);
		}
		
		managedPermitFieldList.add(new ManagedPermitTypeField("mpermit_type-name", "Type Name", ManagedPermitTypeField.TYPE_STANDARD));
		managedPermitFieldList.add(new ManagedPermitTypeField("mpermit_type-description", "Type Description", ManagedPermitTypeField.TYPE_STANDARD));
		
		for(ManagedPermitTypeField managedPermitTypeField : fieldsType.getFields(false, false)){
			
			if (managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_CODES)){
				
				managedPermitTypeField.name = "mpermit_type-" + managedPermitTypeField.name + "_id";
				
			} else if(managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_DB) || managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_LIST) || managedPermitTypeField.type.equals(ManagedPermitTypeField.TYPE_TEXT)){
			
				managedPermitTypeField.name = "attrType-" + managedPermitTypeField.name;
				
			}
			
			managedPermitFieldList.add(managedPermitTypeField);
		}

		return managedPermitFieldList;
	}
	
/*	private  ArrayList<Field>  getSelectFinancialFields()
	{
		ArrayList<Field> financialFieldList = new ArrayList<Field>();

		financialFieldList.add(new Field("owner-first_name", "First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("owner-last_name", "Last Name", Field.TYPE_STANDARD));
		
		financialFieldList.add(new Field("invoice-user_full_name", "User Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-payment_method", "Payment Method", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-status", "Status", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-amount", "Amount", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_first_name", "Billing First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_last_name", "Billing Last Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_email", "Billing Email", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_address", "Billing Address", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_city", "Billing City", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_zip", "Billing Zip", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_number", "CC-Number", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_type", "CC-Type", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_exp_month", "CC-Expiration Month", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_exp_year", "CC-Expiration Year", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-will_pickup", "Will Pickup", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_first_name", "Shipping First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_last_name", "Shipping Last Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_address", "Shipping Address", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_city", "Shipping City", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_zip", "Shipping Zip", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-refund_date", "Refund Date", Field.TYPE_STANDARD));
		
		financialFieldList.add(new Field("invoice-billing_state_id", "Billing State", Field.TYPE_CODES));
		financialFieldList.add(new Field("invoice-shipping_state_id", "Shipping State", Field.TYPE_CODES));
		
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_LATE_FEE+"rec", "Late Fee", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_REFUND+"rec", "Refund", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_VOID+"rec", "Void", Field.TYPE_STANDARD));
		
		return financialFieldList;
	}*/
	/*
	private ArrayList<Field> getCriteriaFinancialFields()
	{
		ArrayList<Field> financialFieldList = new ArrayList<Field>();

		financialFieldList.add(new Field("owner-first_name", "First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("owner-last_name", "Last Name", Field.TYPE_STANDARD));
		
		financialFieldList.add(new Field("invoice-user_full_name", "User Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-payment_method", "Payment Method", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-status", "Status", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-amount", "Amount", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_first_name", "Billing First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_last_name", "Billing Last Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_email", "Billing Email", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_address", "Billing Address", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_city", "Billing City", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-billing_zip", "Billing Zip", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_number", "CC-Number", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_type", "CC-Type", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_exp_month", "CC-Expiration Month", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-cc_exp_year", "CC-Expiration Year", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-will_pickup", "Will Pickup", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_first_name", "Shipping First Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_last_name", "Shipping Last Name", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_address", "Shipping Address", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_city", "Shipping City", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-shipping_zip", "Shipping Zip", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("invoice-refund_date", "Refund Date", Field.TYPE_STANDARD));
		
		financialFieldList.add(new Field("invoice-billing_state_id", "Billing State", Field.TYPE_CODES));
		financialFieldList.add(new Field("invoice-shipping_state_id", "Shipping State", Field.TYPE_CODES));
		
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_LATE_FEE+"rec", "Late Fee", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_REFUND+"rec", "Refund", Field.TYPE_STANDARD));
		financialFieldList.add(new Field("attrFina-n"+InvoiceItem.TYPE_VOID+"rec", "Void", Field.TYPE_STANDARD));
		
		return financialFieldList;
	}*/
	
	/*public void fields(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		response.setContentType("text/json");
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		String section =  request.getParameter("section");
		int type = Integer.parseInt(request.getParameter("type"));
		
		
		switch (type) {
		case Report.CITATION_TYPE:
			
			if(section.equals("select")){
				json.add("selectCiteFields", gson.toJsonTree(this.getSelectCiteFields()));
			}else if (section.equals("criteria")){
				json.add("criteriaCiteFields", gson.toJsonTree(this.getCriteriaCiteFields()));
			}
			
			break;
			
		case Report.MPERMIT_TYPE:
			
			if(section.equals("select")){
				json.add("selectPermitFields", gson.toJsonTree(this.getSelectPermitFields()));
			}else if (section.equals("criteria")){
				json.add("criteriaPermitFields", gson.toJsonTree(this.getCriteriaPermitFields()));
			}
			
			break;
			
		case Report.FINANCIA_CITATION__TYPE:
			
			if(section.equals("select")){
				JsonArray  citeFieldJsonList= (JsonArray) gson.toJsonTree(this.getSelectCiteFields());
				JsonArray  financialFieldJsonList= (JsonArray) gson.toJsonTree(this.getSelectFinancialFields());
				
				for (JsonElement jsonElement : citeFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Citation"));
				}
				

				for (JsonElement jsonElement : financialFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Invoice"));
				}
				
				JsonArray jsonArray =  new JsonArray ();
				jsonArray.addAll(citeFieldJsonList);
				jsonArray.addAll(financialFieldJsonList);
				json.add("selectFinancialCiteFields", jsonArray);

			}else if (section.equals("criteria")){
				JsonArray  citeFieldJsonList= (JsonArray) gson.toJsonTree(this.getCriteriaCiteFields());
				JsonArray  financialFieldJsonList= (JsonArray) gson.toJsonTree(this.getCriteriaFinancialFields());
				
				for (JsonElement jsonElement : citeFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Citation"));
				}
				
				JsonArray  stateCodesJsonList= (JsonArray) gson.toJsonTree((new Codes()).getCodes(Code.CT_STATE));
				
				for (JsonElement jsonElement : stateCodesJsonList){
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("id", jsonObject.get("codeid"));
					jsonObject.add("name", jsonObject.get("description"));
					jsonObject.remove("codeid");
					jsonObject.remove("description");
				
				}
				
				for (JsonElement jsonElement : financialFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Invoice"));

					if((String.valueOf(jsonObject.get("name")).replaceAll("\"", "").equals("invoice-shipping_state_id"))|| (String.valueOf(jsonObject.get("name")).replaceAll("\"", "").equals("invoice-billing_state_id"))){
						jsonObject.add("options",  stateCodesJsonList);
					}
					
				}
				
				JsonArray jsonArray =  new JsonArray ();
				jsonArray.addAll(citeFieldJsonList);
				jsonArray.addAll(financialFieldJsonList);
				json.add("criteriaFinancialCiteFields", jsonArray);
				
			}
			
			break;
			
			case Report.FINANCIA_PERMIT__TYPE:
			
			if(section.equals("select")){
				JsonArray  citeFieldJsonList= (JsonArray) gson.toJsonTree(this.getSelectPermitFields());
				JsonArray  financialFieldJsonList= (JsonArray) gson.toJsonTree(this.getSelectFinancialFields());
				
				for (JsonElement jsonElement : citeFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Permit"));
				}
				

				for (JsonElement jsonElement : financialFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Invoice"));
				}
				
				JsonArray jsonArray =  new JsonArray ();
				jsonArray.addAll(citeFieldJsonList);
				jsonArray.addAll(financialFieldJsonList);
				json.add("selectFinancialPermitFields", jsonArray);

			}else if (section.equals("criteria")){
				JsonArray  citeFieldJsonList= (JsonArray) gson.toJsonTree(this.getCriteriaPermitFields());
				JsonArray  financialFieldJsonList= (JsonArray) gson.toJsonTree(this.getCriteriaFinancialFields());
				
				for (JsonElement jsonElement : citeFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Permit"));
				}
				
				JsonArray  stateCodesJsonList= (JsonArray) gson.toJsonTree((new Codes()).getCodes(Code.CT_STATE));
				
				for (JsonElement jsonElement : stateCodesJsonList){
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("id", jsonObject.get("codeid"));
					jsonObject.add("name", jsonObject.get("description"));
					jsonObject.remove("codeid");
					jsonObject.remove("description");
				
				}
				
				for (JsonElement jsonElement : financialFieldJsonList)
				{
					JsonObject jsonObject = (JsonObject)jsonElement;
					jsonObject.add("group", gson.toJsonTree("Invoice"));

					if((String.valueOf(jsonObject.get("name")).replaceAll("\"", "").equals("invoice-shipping_state_id"))|| (String.valueOf(jsonObject.get("name")).replaceAll("\"", "").equals("invoice-billing_state_id"))){
						jsonObject.add("options",  stateCodesJsonList);
					}
					
				}
				
				JsonArray jsonArray =  new JsonArray ();
				jsonArray.addAll(citeFieldJsonList);
				jsonArray.addAll(financialFieldJsonList);
				json.add("criteriaFinancialPermitFields", jsonArray);
				
			}
			
			break;

		}
		
		json.addProperty("success", true);
		response.getOutputStream().print(gson.toJson(json));
		
		return;
	}*/
	
	public void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Report report = null;
		User user = User.getCurrentUser();
		
		response.setContentType("text/json");
		
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("{success: false, msg: 'You don't have permission to perform this action.'}");
			return;	
		}

		try {

			int rid = Integer.parseInt(request.getParameter("report_id"));
			report = new Report(rid);
			report.clearFields();
			report.clearCriterias();
			
			int fieldCount = Integer.parseInt(request.getParameter("fieldCount"));
			int criteriaCount =  Integer.parseInt(request.getParameter("criteriaCount"));
			report.setName(request.getParameter("reportName"));
			report.setReport_type(Integer.parseInt(request.getParameter("reportType")));
			Boolean opVerify = true;
			
			
			DBFilterList filter = new DBFilterList();
			filter.add(new DBFilter("name", report.name));
			ArrayList <Report> reportList = (ArrayList<Report>)report.get(0, 0, "name ASC", filter);
			
			boolean content = false;
			for(Report r:  reportList){
				if(r.report_id == report.report_id){
					content = true;
				}
			}
			
			if((!reportList.isEmpty()) && (!content)){
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				response.setContentType("text/json");
				JsonObject json = new JsonObject();
				json.addProperty("success", false);
				json.addProperty("msg", "Invalid name.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			for(int i=0; i<fieldCount; i++){
				
				String field = request.getParameter("field"+"_"+i);
				String label = request.getParameter("label"+"_"+i);
				int fieldOrder = Integer.parseInt(request.getParameter("fieldOrder"+"_"+i));
				boolean view = request.getParameter("view"+"_"+i) != null && request.getParameter("view"+"_"+i).equals("on") ? true : false;
				boolean export = request.getParameter("export"+"_"+i) != null && request.getParameter("export"+"_"+i).equals("on") ? true : false;
				report.addField(new ReportField(field, label, view, export, fieldOrder));

			}
			
			if(criteriaCount != 0) 
			{
				for(int i=0; i<criteriaCount; i++){
					
					String criteria = request.getParameter("criteria"+"_"+i);
					int criteriaOrder = Integer.parseInt(request.getParameter("criteriaOrder"+"_"+i));
					String op = request.getParameter("op"+"_"+i);
					String value = request.getParameter("value"+"_"+i);
					String betweenValue = request.getParameter("betweenValue"+"_"+i);
					String logicOperator = request.getParameter("logicOperator"+"_"+i);
					
					if((!op.equalsIgnoreCase(DBFilter.BETWEEN))&&(!op.equalsIgnoreCase(DBFilter.EQ))&&
							(!op.equalsIgnoreCase(DBFilter.GE))&&(!op.equalsIgnoreCase(DBFilter.LE))&&(!op.equalsIgnoreCase(DBFilter.LIKE))
							&&(!op.equalsIgnoreCase(DBFilter.NE))&&(!op.equalsIgnoreCase(DBFilter.IS_NULL))&&(!op.equalsIgnoreCase(DBFilter.NOT_NULL))&&(!op.equalsIgnoreCase(DBFilter.NOT_BETWEEN))){
						opVerify = false;
						break;
					}
					
					report.addCriteria(new ReportCriteria(criteria, op, value, betweenValue, logicOperator, criteriaOrder));
					
				}
			} 
			else 
			{
				report.addCriteria(null);
			}
	
			if(!opVerify){
				
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				response.setContentType("text/json");
				JsonObject json = new JsonObject();
				json.addProperty("success", false);
				json.addProperty("msg", "Invalid operation.");
				response.getOutputStream().print(gson.toJson(json));
				return;
				
			}
			
			if(report.commit())
			{
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				response.setContentType("text/json");
				String json = gson.toJson(report);
				
				response.getOutputStream().print("{success: true, msg: 'Report has been saved.', report: "+json+"}");
				return;
			}
			
		} catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid report id.'}");
			return;
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Report not found.'}");
			return;
		}
		catch (Exception e) {
			response.getOutputStream().print("{success: false, msg: 'Error saving report.'}");
			return;
		}
		
	}
	
	public void run(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		User user = User.getCurrentUser();
		int start = 0;
		int limit = 0;
		int count = 0;
		
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		Report report = null;
		try
		{
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			start = Integer.parseInt(request.getParameter("start"));
			limit = Integer.parseInt(request.getParameter("limit"));
			
			int rid = Integer.parseInt(request.getParameter("report_id"));
			
			report = new Report(rid);
			QueryBuilder qb;
			if(report.report_type == 0 || report.report_type == 2 )
			{
				 qb = report.buildQuery("citation_number", "ASC", 0, 0, Report.RUN);
			}
			else
			{
				 qb = report.buildQuery("permit_number", "ASC", 0, 0, Report.RUN);
			}
			
			if(qb == null){
				return;
			}
			
			ArrayList<Hashtable<String, String>> sumTotal = qb.sum();
			
			ArrayList<Hashtable<String, String>> sumList = qb.select();
			
			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
														.orderDir(dir)
														.start(start)
														.max(limit)
														.select();
			
			
			if(limit > 0)
			{
				if((report.report_type == 2) || (report.report_type == 3)){
					//count = qb.count("DISTINCT(invoice.invoice_id)");
					count = sumList.size();
				}else{
					count = sumList.size();
				}

			}
			
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			response.setContentType("text/json");
			String json = gson.toJson(list);
		
			
			String sumjson = gson.toJson((sumTotal==null)? "" : sumTotal);
			
			
			response.getOutputStream().print("{count: "+count+", data: " + json + ", summaryRoot: " + sumjson + "}");
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid report id.'}");
			return;
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Report not found.'}");
			return;
		}catch (Exception e) {
			response.getOutputStream().print("{success: false, msg: 'Error.'}");
			return;
		}
		
	}
	
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		Report report = null;
		try
		{
			int rid = Integer.parseInt(request.getParameter("report_id"));
			report = new Report(rid);
			
			if(report.delete())
			{
				response.getOutputStream().print("{success: true, msg: 'Report has been deleted.'}");
				return;
			}
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid report id.'}");
			return;
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Report not found.'}");
			return;
		}
		
		response.getOutputStream().print("{success: false, msg: 'Error saving report.'}");
	}

	public void copy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}
		
		Report report = null;
		try
		{
			int rid = Integer.parseInt(request.getParameter("report_id"));
			report = new Report(rid);
			DBFilterList filter = new DBFilterList();
			filter.add(new DBFilter("name", "like", report.name));
			
			@SuppressWarnings("unchecked")
			ArrayList <Report> list = (ArrayList<Report>) report.get(0, 0, null, filter);
			
			int count = 1;
			if(!list.isEmpty())
			{
				Hashtable<String, Report> reportMap = new Hashtable<String, Report>();
				for(Report reportResult : list){
					reportMap.put(reportResult.name, reportResult);
				}
				
				while(reportMap.containsKey(report.name + " - " + "copy" + count)){
					++count;
				}
				reportMap = null;
				list = null;
			}
			
			Report newReport = new Report();
			newReport.name = report.name + " - " + "copy" + count;				
			newReport.report_type = Integer.parseInt(request.getParameter("report_type"));
			for(ReportField reportField: report.reportFields){
				newReport.addField(reportField);
			}
			
			for(ReportCriteria ReportCriteria: report.reportCriterias){
				newReport.addCriteria(ReportCriteria);
			}
			
			if(newReport.commit())
			{
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				response.setContentType("text/json");
				String json = gson.toJson(report);
				
				response.getOutputStream().print("{success: true, msg: 'Report has been copied.', report: "+json+"}");
				return;
			}
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("{success: false, msg: 'Invalid report id.'}");
			return;
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("{success: false, msg: 'Report not found.'}");
			return;
		}
		
		response.getOutputStream().print("{success: false, msg: 'Error copy report.'}");
	}
	
	public void export(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html");
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{
			response.getOutputStream().print("<h1>Error: You don't have permission to perform this action.</h1>");
			return;
		}
		
		Report report = null;
		try
		{
			int rid = Integer.parseInt(request.getParameter("report_id"));
			report = new Report(rid);
		}
		catch(NumberFormatException nfe)
		{
			response.getOutputStream().print("<h1>Error: Invalid report id.</h1>");
			return;
		}
		catch(UnknownObjectException uoe)
		{
			response.getOutputStream().print("<h1>Error: Report not found.</h1>");
			return;
		}
		
		
		DBConnection connection = null;
		try
		{
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			int start = 0;
			int limit = 0;
			int count = 0;
			
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

			QueryBuilder qb = report.buildQuery(sort, dir, start, limit, Report.EXPORT);
			if(qb == null){
				return;
			}
			connection = qb.query();
			
			if(connection != null)
			{
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
				DateFormat today = new SimpleDateFormat("MMM-dd-yyyy");
				
				Timestamp todayTS =  new Timestamp(System.currentTimeMillis());
				
				response.setContentType("application/octet-stream");
			    response.setHeader("Content-Disposition", "attachment; filename=\"" + report.name + " "+today.format(todayTS)+".csv\"");
			    response.setHeader("Pragma", "no-cache");
			    response.setHeader("Cache-Control", "no-cache");
			    
			    ResultSet rs = connection.getResultSet();
			    
			    if(rs != null)
			    {
				    CSVWriter csv = new CSVWriter(new OutputStreamWriter(response.getOutputStream())); 
				    ResultSetMetaData rsm = rs.getMetaData();
					count = rsm.getColumnCount();
				    String[] row = new String[count];
				    
				    //header row
				    for(int i = 0; i < count; i++)
				    {
				    	for(ReportField reportField : report.reportFields){
				    		String columnName = reportField.name.substring(reportField.name.indexOf("-")+1, reportField.name.length());
				    		if(columnName.equals(rsm.getColumnName(i+1))){
				    			row[i] = reportField.getLabel();
				    			break;
				    		}
				    		
				    	}
				    	
				    }
				    csv.writeNext(row);
				    
				    while(rs.next())
					{
						for(int i = 0; i < count; i++ )
						{
							String value = null;
							
							int type = rsm.getColumnType(i+1);
							if(type == Types.TIMESTAMP)
							{
								Timestamp ts = rs.getTimestamp(i+1);
								if(ts != null)
								{
									value = df.format(ts);
								}
							}
							else
							{
								value = rs.getString(i+1); 
							}
							
							row[i] = value;
						}
						
						csv.writeNext(row);
					}
				    
				    csv.close();
			    }
			    
			    return;
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		response.getOutputStream().print("<h1>Error: exporting report.</h1>");
	}
	
	public void  templateFinancial (HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
		
		response.setContentType("text/json");
		String jsonTemplate = null;
		
		try {
			
			String id = request.getParameter("idTemplate");
			
			if(properties == null){
				properties =  new Properties();
				properties.load(request.getSession().getServletContext().getResourceAsStream("/WEB-INF/config/report/reportTemplate.properties"));

			}
			
			jsonTemplate =  properties.getProperty("report.template.number."+ id);
			response.getOutputStream().print("{success: true, report: "+ jsonTemplate +"}");
			
		} catch (Exception e) {}
		
		if(jsonTemplate == null){
			response.getOutputStream().print("{success: false}");
		}
		
		return;
	}
	public ModelAndView reportList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		response.setContentType("text/json");
		ModelAndView mv =  new ModelAndView();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
			
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_REPORT_VIEW)))
		{

			response.getOutputStream().print("You don't have permission to perform this action.");
			return null;
		}
		
		try
		{
			int type = Integer.parseInt(request.getParameter("report_id"));
			switch (type){
				case 1:{
					
				}		
				break;
				case 2:{
									
				}		
				break;
				case 3:{
					
				}		
				break;
				case 4:{
					
				}		
				break;
				case 5:{
						
						long now = System.currentTimeMillis();
						Timestamp dateStart = new Timestamp(now);
						
						String date = request.getParameter("start");
						date = date.replace("T", " ");
						try
							{
								dateStart = Timestamp.valueOf(date);
							}
							catch(NumberFormatException nfe){
								json.addProperty("msg", "Invalid date, please select valid date");
								response.getOutputStream().print(gson.toJson(json));
								return null;
							}
						DateFormater mydate = new DateFormater(dateStart);
						ArrayList<Reservations> reservations = null;
						reservations = Reservations.MealPlan(dateStart);
						String test = mydate.getFormatdate();
						
						if (reservations.size()==0){
								ModelAndView msg =  new ModelAndView("no_result_report","message",
										"No reservations related on the date indicated: "+ dateStart.toString());
								return msg;
							}else{
								mv =  new ModelAndView("meal_plan");
								if (reservations != null){
									mv.addObject("reservations", reservations);
									mv.addObject("date", mydate);
								
								}
								mv.addObject("user", user);
								
							}
				}		
				break;
				case 6:{
					
				}		
				break;
				case 7:{
					
				}		
				break;
				case 8:{
					
				}		
				break;
			}
			return mv;
		}
		catch(Exception e)
		{
			
		}
		response.setContentType("text/json");
		response.getOutputStream().print("{success: false, msg: 'Report not found.'}");
		return null;
			
	}
	
	public void exportPDF(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, TransformerException, UnknownObjectException, ParseException {
		//response.setContentType("text/json");
						
		try
		{
			int type = Integer.parseInt(request.getParameter("report_id"));
			switch (type){
				case 1:{
					
				}		
				break;
				case 2:{
									
				}		
				break;
				case 3:{
					
				}		
				break;
				case 4:{
					
				}		
				break;
				case 5:{
					
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					
					String date = request.getParameter("start");
					date ="2016-01-21 00:00:00";
					DateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z");
					
					//Date formatdate = (Date)formatter.parse(dateStart);
					//Date formatdate2 = (Date)formatter.parse(date);
					 //YYYY-MM-DDThh:mm:ss TZD
					//date = date.replace("T", " ");
					
					try
					{
						dateStart = Timestamp.valueOf(date);
					}
					catch(NumberFormatException nfe){
						
					}
					DateFormater mydate = new DateFormater(dateStart);
					ArrayList<Reservations> reservations = null;
					reservations = Reservations.MealPlan(dateStart);
					String test = mydate.getFormatdate();
					model.put("reservations", reservations);
					model.put("date", mydate);
													
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/mealplan.vm", model);
					} 
					catch (Exception e) 
					{
						
					}
								
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
					
					//response.setContentType("text/json");	
					
					//response.getWriter().print(gson.toJson(json));	
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
					
				}		
				break;
				case 6:{
					
				}		
				break;
				case 7:{
					
				}		
				break;
				case 8:{
					
				}		
				break;
			}
			
		}
		catch(Exception e)
		{
			
		}
		
}
	
	
	
	
	
	
}
