
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.cambiolabs.citewrite.data.Country;
import com.cambiolabs.citewrite.data.DateFormater;
import com.cambiolabs.citewrite.data.Guests;
import com.cambiolabs.citewrite.data.PaymentMethod;
import com.cambiolabs.citewrite.data.Payments;
import com.cambiolabs.citewrite.data.ReservationRoom;
import com.cambiolabs.citewrite.data.ReservationType;
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
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
						}
						catch(NumberFormatException nfe){
							json.addProperty("msg", "Invalid date, please select valid date");
							response.getOutputStream().print(gson.toJson(json));
							return null;
						}
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					Timestamp dateAdd = null;
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					while((formaterStart.datecomplete.before(dateEnd)) || (formaterStart.datecomplete.equals(dateEnd))){
						calendar.add(formaterStart);
						dateAdd =formaterStart.getAddDays(formaterStart.datecomplete);
						formaterStart = new DateFormater(dateAdd);
					}
					
					DateFormater formaterNow = new DateFormater(dateStart);
					
					
					if (calendar.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No reservations related on the date indicated: "+ dateStart.toString());
							return msg;
						}else{
							mv =  new ModelAndView("expected_report");
							if (calendar != null){
								mv.addObject("calendar", calendar);
								mv.addObject("start", formaterStart);
								mv.addObject("end", formaterEnd);
								mv.addObject("now", formaterNow);
							}
							mv.addObject("user", user);
							
						}
				}		
				break;
				case 2:{
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
						}
						catch(NumberFormatException nfe){
							json.addProperty("msg", "Invalid date, please select valid date");
							response.getOutputStream().print(gson.toJson(json));
							return null;
						}
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					DateFormater formaterNow = new DateFormater(dateNow);
					ArrayList<Reservations> reservations = null;
					
					reservations = Reservations.Marketing(dateStart, dateEnd);
				
					
					if (reservations.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No reservations related on the date indicated: "+ dateStart.toString());
							return msg;
						}else{
							mv =  new ModelAndView("marketing_report");
							if (reservations != null){
								mv.addObject("reservations", reservations);
								mv.addObject("start", formaterStart);
								mv.addObject("end", formaterEnd);
								mv.addObject("now", formaterNow);
							}
							mv.addObject("user", user);
							
						}
				}		
				break;
				case 3:{
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
					reservations = Reservations.EventsReport(dateStart);
					String test = mydate.getFormatdate();
					
					if (reservations.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No reservations related on the date indicated: "+ dateStart.toString());
							return msg;
						}else{
							mv =  new ModelAndView("groups_report");
							if (reservations != null){
								mv.addObject("reservations", reservations);
								mv.addObject("date", mydate);
							
							}
							mv.addObject("user", user);
							
						}
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
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					int method = 0;
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					
					String pay = request.getParameter("type");
					String password = null;
					password=request.getParameter("password");
					if(password == null || !password.equals("1234"))
					{
						response.getOutputStream().print("Incorrect Password!");
						return null;
					}
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
							method = Integer.parseInt(pay);
						}
						catch(NumberFormatException nfe){
							json.addProperty("msg", "Invalid date, please select valid date");
							response.getOutputStream().print(gson.toJson(json));
							return null;
						}
					
					DateFormater mydate = new DateFormater(dateNow);
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterWhile = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					Timestamp dateAdd = null;
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					while((formaterWhile.datecomplete.before(dateEnd)) || (formaterStart.datecomplete.equals(dateEnd))){
						calendar.add(formaterWhile);
						dateAdd =formaterWhile.getAddMonths(formaterWhile.datecomplete);
						formaterWhile = new DateFormater(dateAdd);
					}
					ArrayList<Payments> payments = null;
					if (method == 0 || method==7){
						payments = Payments.getPaymentsAll(dateStart, dateEnd);
					}else{
						payments = Payments.getPayments(dateStart, dateEnd, method);
					}
					
					String paymentMethod = "All";
					PaymentMethod payment = new PaymentMethod(method);
					paymentMethod = payment.getPayment_method_description();
					if (payments.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No payments related on the date indicated: "+ dateStart.toString());
							return msg;
						}else{
							mv =  new ModelAndView("payments_reports");
							if (payments != null){
								mv.addObject("calendar", calendar);
								mv.addObject("paymentMethod", paymentMethod);
								mv.addObject("payments", payments);
								mv.addObject("date", mydate);
								mv.addObject("start", formaterStart);
								mv.addObject("end", formaterEnd);
							}
							mv.addObject("user", user);
							
						}
				}		
				break;
				case 7:{
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					
					DateFormater mydate = new DateFormater(dateStart);
					ArrayList<Reservations> reservations = null;
					reservations = Reservations.StatusesUnhandled(dateStart);
					String test = mydate.getFormatdate();
					
					if (reservations.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No reservations related on the date indicated: "+ dateStart.toString());
							return msg;
						}else{
							mv =  new ModelAndView("statuses_report");
							if (reservations != null){
								mv.addObject("reservations", reservations);
								mv.addObject("date", mydate);
							}
							mv.addObject("user", user);
						}
				}		
				break;
				case 8:{
					long now = System.currentTimeMillis();
					int year = 2000;
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String yearString = request.getParameter("year");
					try
						{
							year = Integer.parseInt(yearString);
						}
						catch(NumberFormatException nfe){
							json.addProperty("msg", "Invalid year, please select valid date");
							response.getOutputStream().print(gson.toJson(json));
							return null;
						}
					
					DateFormater formaterStart = new DateFormater(dateNow);
					DateFormater formaterNow = new DateFormater(dateNow);
					Timestamp dateAdd = null;
					ArrayList<Timestamp> months = new ArrayList<Timestamp>();
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					int totalGuests =0;
					int totalNights = 0;
					for (int i=0; i<12; i++){
						months.add(formaterStart.getYearPerMonth(year, i));
					
					}
					for(int x=0;x<months.size();x++) {
						formaterStart = new DateFormater(months.get(x));
						totalGuests += formaterStart.getMonthGuests();
						totalNights += formaterStart.getMonthNights();
						calendar.add(formaterStart);
					}
						
					if (months.size()==0){
							ModelAndView msg =  new ModelAndView("no_result_report","message",
									"No reservations related on the date indicated: "+ year);
							return msg;
						}else{
							mv =  new ModelAndView("year_report");
							if (months != null){
								mv.addObject("calendar", calendar);
								mv.addObject("months", months);
								mv.addObject("start", formaterStart);
								mv.addObject("year", year);
								mv.addObject("totalGuests", totalGuests);
								mv.addObject("totalNights", totalNights);
								mv.addObject("now", formaterNow);
							}
							mv.addObject("user", user);
							
						}
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
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					start ="2016-03-20 00:00:00";
					end ="2016-04-01 00:00:00";
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
						}
						catch(NumberFormatException nfe){
							
						}
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					Timestamp dateAdd = null;
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					while((formaterStart.datecomplete.before(dateEnd)) || (formaterStart.datecomplete.equals(dateEnd))){
						calendar.add(formaterStart);
						dateAdd =formaterStart.getAddDays(formaterStart.datecomplete);
						formaterStart = new DateFormater(dateAdd);
					}
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					DateFormater formaterNow = new DateFormater(dateStart);
					if (calendar.size()!=0){
						model.put("calendar", calendar);
						model.put("start", formaterStart);
						model.put("end", formaterEnd);
						model.put("now", formaterNow);
					}
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/expected.vm", model);
					} 
					catch (Exception e) 
					{
						
					}
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);	
					
				}		
				break;
				case 2:{
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					start ="2016-03-20 00:00:00";
					end ="2016-04-01 00:00:00";
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
						}
						catch(NumberFormatException nfe){
						
						}
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					DateFormater formaterNow = new DateFormater(dateNow);
					ArrayList<Reservations> reservations = null;
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					reservations = Reservations.Marketing(dateStart, dateEnd);
					if (reservations.size()!=0){
						model.put("reservations", reservations);
						model.put("start", formaterStart);
						model.put("end", formaterEnd);
						model.put("now", formaterNow);
					}
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/marketing.vm", model);
					} 
					catch (Exception e) 
					{
						
					}
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
					
								
				}		
				break;
				case 3:{
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					
					String date = request.getParameter("start");
					date = date.replace("T", " ");
					date ="2016-03-23 00:00:00";
					try
						{
							dateStart = Timestamp.valueOf(date);
						}
						catch(NumberFormatException nfe){
							
						}
					DateFormater mydate = new DateFormater(dateStart);
					ArrayList<Reservations> reservations = null;
					reservations = Reservations.EventsReport(dateStart);
					String test = mydate.getFormatdate();
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					if (reservations.size()!=0){
						model.put("reservations", reservations);
						model.put("date", mydate);
					}
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/groups.mv", model);
					} 
					catch (Exception e) 
					{
						
					}
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
					
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
					long test = Long.parseLong(date);
					Timestamp test2 = new Timestamp(test);
					date ="2016-03-31 00:00:00";
					//DateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z");
					
					try
					{
						dateStart = Timestamp.valueOf(date);
					}
					catch(NumberFormatException nfe){
						
					}
					DateFormater mydate = new DateFormater(dateStart);
					ArrayList<Reservations> reservations = null;
					reservations = Reservations.MealPlan(dateStart);
					//String test = mydate.getFormatdate();
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					if (reservations.size()!=0){
						model.put("reservations", reservations);
						model.put("date", mydate);
					}
													
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
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
					
				}		
				break;
				case 6:{
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					int method = 0;
					String start = request.getParameter("start");
					start = start.replace("T", " ");
					
					String end = request.getParameter("end");
					end = end.replace("T", " ");
					start ="2015-03-31 00:00:00";
					end ="2017-03-31 00:00:00";
					String pay = request.getParameter("type");
					String password = null;
					password=request.getParameter("password");
					if(password == null || !password.equals("1234"))
					{
						response.getOutputStream().print("Incorrect Password!");
					}
					try
						{
							dateStart = Timestamp.valueOf(start);
							dateEnd = Timestamp.valueOf(end);
							method = Integer.parseInt(pay);
						}
						catch(NumberFormatException nfe){
							
						}
					
					DateFormater mydate = new DateFormater(dateNow);
					DateFormater formaterStart = new DateFormater(dateStart);
					DateFormater formaterWhile = new DateFormater(dateStart);
					DateFormater formaterEnd = new DateFormater(dateEnd);
					Timestamp dateAdd = null;
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					while((formaterWhile.datecomplete.before(dateEnd)) || (formaterStart.datecomplete.equals(dateEnd))){
						calendar.add(formaterWhile);
						dateAdd =formaterWhile.getAddMonths(formaterWhile.datecomplete);
						formaterWhile = new DateFormater(dateAdd);
					}
					ArrayList<Payments> payments = null;
					if (method == 0 || method==7){
						payments = Payments.getPaymentsAll(dateStart, dateEnd);
					}else{
						payments = Payments.getPayments(dateStart, dateEnd, method);
					}
						
						String paymentMethod = "All";
						PaymentMethod payment = new PaymentMethod(method);
						paymentMethod = payment.getPayment_method_description();
						
						
						String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
						model.put("imgUrl", imgUrl);
						if (payments.size()!=0){
							model.put("calendar", calendar);
							model.put("paymentMethod", paymentMethod);
							model.put("payments", payments);
							model.put("date", mydate);
							model.put("start", formaterStart);
							model.put("end", formaterEnd);
						}
						
						try 
						{
							htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/payments.vm", model);
						} 
						catch (Exception e) 
						{
							
						}
						Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
						JsonObject json = new JsonObject();
						json.addProperty("success", true);
						json.addProperty("html", htmlFile);
							
						response.setContentType("text/html");			
						response.getOutputStream().print(htmlFile);		
				}		
				break;
				case 7:{
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					Timestamp dateStart = new Timestamp(now);
					
					DateFormater mydate = new DateFormater(dateStart);
					ArrayList<Reservations> reservations = null;
					reservations = Reservations.StatusesUnhandled(dateStart);
					String test = mydate.getFormatdate();
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					if (reservations.size()!=0){
						model.put("reservations", reservations);
						model.put("date", mydate);
					}
					
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/statuses.vm", model);
					} 
					catch (Exception e) 
					{
						
					}
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
				}		
				break;
				case 8:{
					String htmlFile = "";
					HashMap<String, Object> model = new HashMap<String, Object>();
					long now = System.currentTimeMillis();
					int year = 2000;
					Timestamp dateEnd = new Timestamp(now);
					Timestamp dateNow = new Timestamp(now);
					
					String yearString = request.getParameter("year");
					try
						{
							year = Integer.parseInt(yearString);
						}
						catch(NumberFormatException nfe){
							
						}
					
					DateFormater formaterStart = new DateFormater(dateNow);
					DateFormater formaterNow = new DateFormater(dateNow);
					Timestamp dateAdd = null;
					ArrayList<Timestamp> months = new ArrayList<Timestamp>();
					ArrayList<DateFormater> calendar = new ArrayList<DateFormater>();
					int totalGuests =0;
					int totalNights = 0;
					for (int i=0; i<12; i++){
						months.add(formaterStart.getYearPerMonth(year, i));
					
					}
					for(int x=0;x<months.size();x++) {
						formaterStart = new DateFormater(months.get(x));
						totalGuests += formaterStart.getMonthGuests();
						totalNights += formaterStart.getMonthNights();
						calendar.add(formaterStart);
					}
					
					//model.put("user", user);
					
					String imgUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
					model.put("imgUrl", imgUrl);
					if (months.size()!=0){ 
						model.put("calendar", calendar);
						model.put("months", months);
						model.put("start", formaterStart);
						model.put("year", year);
						model.put("totalGuests", totalGuests);
						model.put("totalNights", totalNights);
						model.put("now", formaterNow);
					}
					try 
					{
						htmlFile =  VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "config/template/year.vm", model);
					} 
					catch (Exception e) 
					{
						
					}
								
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					JsonObject json = new JsonObject();
					json.addProperty("success", true);
					json.addProperty("html", htmlFile);
						
					response.setContentType("text/html");			
					response.getOutputStream().print(htmlFile);		
				}		
				break;
			}
			
		}
		catch(Exception e)
		{
			
		}
		
}
	
	public void paymentTypeList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
	
		try
		{
			User user = User.getCurrentUser();
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			
			DBFilterList filter = new DBFilterList();
			
			String value = request.getParameter("filter_id");
			if(value != null && value.length() > 0)
			{
				filter.add(new DBFilter("name", "LIKE", value));
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
			
			PaymentMethod method = new PaymentMethod();
			ArrayList<PaymentMethod> list = (ArrayList<PaymentMethod>)method.get(start, limit, sort + " " + dir, filter);
		
			int count = list.size();
			if(limit > 0)
			{
				count = method.count(filter);
			}
			
			json.addProperty("count", count);
			json.add("method", gson.toJsonTree(list));
			json.addProperty("success", true);
			
			response.getOutputStream().print(gson.toJson(json));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	
}
