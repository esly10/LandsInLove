
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
import com.cambiolabs.citewrite.data.DateFormater;
import com.cambiolabs.citewrite.data.Guests;

import com.cambiolabs.citewrite.data.ReservationRoom;
import com.cambiolabs.citewrite.data.Reservations;
import com.cambiolabs.citewrite.data.Rooms;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
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
	
	
	public ModelAndView reportList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
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
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print("{success: false, msg: 'Report not found.'}");
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
