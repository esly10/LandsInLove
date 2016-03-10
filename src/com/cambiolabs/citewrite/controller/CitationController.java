package com.cambiolabs.citewrite.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.cambiolabs.citewrite.data.*;
import com.cambiolabs.citewrite.data.PasswordConfig.AuthorizationType;
import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.Cart;
import com.cambiolabs.citewrite.ecommerce.CartException;
import com.cambiolabs.citewrite.ecommerce.Invoice;
import com.cambiolabs.citewrite.ecommerce.InvoiceItem;
import com.cambiolabs.citewrite.email.MailerTask;
import com.cambiolabs.citewrite.email.preparator.AppealNoteMessage;
import com.cambiolabs.citewrite.email.preparator.AppealUpdate;
import com.cambiolabs.citewrite.email.preparator.CitationMessage;
import com.cambiolabs.citewrite.email.preparator.PaymentMessage;
import com.cambiolabs.citewrite.task.Executor;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.velocity.app.VelocityEngine;

import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import com.cambiolabs.citewrite.license.LicenseManager;

public class CitationController extends MultiActionController {
	protected final Log logger = LogFactory.getLog(getClass());
	private static VelocityEngine velocityEngine;
	private String first_template_path = "config/email/template/citation-message.vm";
	private String final_template_path = "config/email/template/citation-message-final.vm";

	public void setVelocityEngine(VelocityEngine vEngine) {
		velocityEngine = vEngine;
	}

	public void list(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/json");

 		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;

		try {
			user = User.getCurrentUser();

			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("citation");
			qb.field("DISTINCT citation.citation_id")
					.field("citation.citation_number").field("citation.status_id")
					.field("citation.officer_id").field("citation.license")
					.field("citation.vin").field("citation.violation_id")
					.field("citation.violation_description")
					.field("citation.violation_amount").field("citation.notes")
					.field("citation.community_service_end")
					.field("citation.exported").field("citation.citation_date");

			qb.join("citation_appeal appeal",
					"appeal.citation_id=citation.citation_id").field(
					"appeal.status appeal_status");			
			qb.join("status status",
					"citation.status_id=status.status_id").field(
					"status.status_name  status");

			DBFilterList filter = new DBFilterList();

			String value = request.getParameter("filter_citation_number");
			if (value != null && value.length() > 0) {

				String value2 = request.getParameter("filter_citation_license");
				if (value2.length() == 0) {

					DBFilter numberFilterSeveral = new DBFilter(
							"citation_number", "LIKE", value);
					filter.add(numberFilterSeveral);
					DBConnection conn = null;
					conn = new DBConnection();
					if (conn.query("SELECT license FROM citation where citation_number like '%"
							+ value + "%'")) {
						ResultSet rs = conn.getResultSet();
						if (rs.next()) {
							filter.addOr(numberFilterSeveral, new DBFilter(
									"license", "=", rs.getString(1)));
							// filter.add(new DBFilter("license", "LIKE",
							// rs.getInt(1)));;
						}
					}

				} else {
					filter.add(new DBFilter("citation_number", "LIKE", value));
				}

			}

			value = request.getParameter("filter_citation_license");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("license", "LIKE", value));
			}

			value = request.getParameter("filter_vehicle_id");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("vehicle_id", "=", value));
			}

			value = request.getParameter("filter_citation_status");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("status.name", "=", value));
			}

			value = request.getParameter("filter_citation_appeal_status");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("appeal.status", "=", value));
			}

			value = request.getParameter("filter_citation_vin");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("vin", "LIKE", value));
			}

			value = request.getParameter("filter_citation_officer_id");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("officer_id", "LIKE", value));
			}

			value = request.getParameter("filter_citation_start_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss")
						.parse(value);
				Timestamp start = dp.firstHour().getTimestamp();

				filter.add(new DBFilter("citation_date", ">=", start));

			}

			value = request.getParameter("filter_citation_end_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss")
						.parse(value);
				Timestamp end = dp.lastHour().getTimestamp();

				filter.add(new DBFilter("citation_date", "<=", end));

			}

			value = request.getParameter("owner_id");
			if (value != null && value.length() > 0) {
				try {
					int oid = Integer.parseInt(value);
					if (oid > 0) {
						filter.add(new DBFilter("owner_id", "=", oid));
					}
				} catch (NumberFormatException nfe) {
				}
			}

			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir).where(filter).start(start).max(limit)
					.select();

			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("citations", gson.toJsonTree(list));
			json.addProperty("count", count);

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.getOutputStream().print(gson.toJson(json));

	}

	public void officerNotes(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = User.getCurrentUser();
		if (user == null || (!user.hasPermission(User.PL_CITATION_VIEW))) {
			json.addProperty("msg",	"You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			
		}

		try {
			int citation_appeal_id = Integer.parseInt(request.getParameter("citation_appeal_id"));
			CitationAppeal citationAppeal = new CitationAppeal(citation_appeal_id);

			String noteData = "";
			noteData += request.getParameter("officer_notes");
			if (noteData != null) {
				Whitelist whitelist = new Whitelist();
				whitelist.addTags("b", "i", "u", "br");
				whitelist.addAttributes("a", "href");//$NON-NLS-1$
				noteData = Jsoup.clean(noteData, whitelist);
				
				byte ptext[] = noteData.getBytes("ISO-8859-1");
				noteData = new String(ptext, "UTF-8");
				citationAppeal.officer_notes = noteData;
				if (citationAppeal.commit()) {
					json.add("citationAppeal", gson.toJsonTree(citationAppeal));
					json.addProperty("success", true);
				} else {
					json.addProperty("msg","Unknown error saving note.");
				}
			}			

			response.getOutputStream().print(gson.toJson(json));				

		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error retrieving notes.");
			response.getOutputStream().print(gson.toJson(json));
		}
	}
	
	
	public ModelAndView notes(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = User.getCurrentUser();
		if (user == null || (!user.hasPermission(User.PL_CITATION_VIEW|User.PL_CITATION_MANAGE ))) {
			json.addProperty("msg",
					"You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return null;
		}

		try {
			int citation_id = Integer.parseInt(request
					.getParameter("citation_id"));
			Citation citation = new Citation(citation_id);

			String xaction = request.getParameter("xaction");
			if (xaction != null) {
				CitationNote note = new CitationNote(Integer.parseInt(request
						.getParameter("note_id")), citation_id);
				if (xaction.equals("save")) {
					String noteData = "";
					noteData += request.getParameter("note");
					if (noteData != null) {
						Whitelist whitelist = new Whitelist();
						whitelist.addTags("b", "i", "u", "br");
						whitelist.addAttributes("a", "href");//$NON-NLS-1$
						noteData = Jsoup.clean(noteData, whitelist);
						note.setUpdated(user);
						byte ptext[] = noteData.getBytes("ISO-8859-1");
						noteData = new String(ptext, "UTF-8");
						note.note = noteData;
						if (note.commit()) {
							json.add("note", gson.toJsonTree(note));
							json.addProperty("success", true);
						} else {
							json.addProperty("msg",
									"Unknown error saving note.");
						}
					}
				} else if (xaction.equals("delete")) {
					if (note.delete()) {
						json.addProperty("success", true);
					} else {
						json.addProperty("msg", "Error deleting note.");
					}
				} else {
					json.addProperty("msg", "Unknown xaction.");
				}

				response.getOutputStream().print(gson.toJson(json));
				return null;
			} else {
				// citation.getNotes();
				ModelAndView mv = new ModelAndView("citation_notes");
				mv.addObject("citation", citation);

				return mv;
			}
		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error retrieving notes.");
			response.getOutputStream().print(gson.toJson(json));
		}

		return null;
	}

	public ModelAndView appealDetailsView(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;
		int citationAppealID = 0;

		try {

			user = User.getCurrentUser();
			citationAppealID = Integer.parseInt(request
					.getParameter("citation_appeal_id"));
			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {

				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return null;
			}

			CitationAppeal citationAppeal = new CitationAppeal(citationAppealID);
			Citation citation = new Citation(Integer.parseInt(request
					.getParameter("citation_id")));

			// citation.getNotes();
			ModelAndView mv = new ModelAndView("citation_appeal_general");
			mv.addObject("appeal", citationAppeal);
			mv.addObject("citation", citation);

			return mv;

		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error retrieving appeal information.");
			response.getOutputStream().print(gson.toJson(json));
		}

		return null;

	}

	public ModelAndView appealNotes(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = User.getCurrentUser();
		if (user == null || (!user.hasPermission(User.PL_CITATION_VIEW))) {
			json.addProperty("msg",
					"You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return null;
		}

		try {
			int citation_appeal_id = Integer.parseInt(request.getParameter("citation_appeal_id"));
			
			CitationAppeal citationAppeal = new CitationAppeal(citation_appeal_id);
						
			Citation citation = new Citation(citationAppeal.citation_id);
			
			String xaction = request.getParameter("xaction");
			if (xaction != null) {
				CitationAppealNote noteAppeal = new CitationAppealNote(Integer.parseInt(request.getParameter("note_id")),citation_appeal_id);
				if (xaction.equals("save")) {
					Integer is_email = 0;
					
					if(request.getParameter("is_email") != null){
						if(request.getParameter("is_email").equalsIgnoreCase("1")){
							is_email = 1;
						}else{
							is_email = 0;
						}
					}													
					
					noteAppeal.is_email = is_email;
					if(request.getParameter("subject") != null)
						noteAppeal.subject_email = request.getParameter("subject");
					if(request.getParameter("to") != null)
						noteAppeal.to_email = request.getParameter("to");
					
					String noteData = "";
					noteData += request.getParameter("note");
					if (noteData != null) {
						Whitelist whitelist = new Whitelist();
						whitelist.addTags("b", "i", "u", "br");
						whitelist.addAttributes("a", "href");//$NON-NLS-1$
						noteData = Jsoup.clean(noteData, whitelist);
						noteAppeal.setUpdated(user);
						byte ptext[] = noteData.getBytes("ISO-8859-1");
						noteData = new String(ptext, "UTF-8");
						noteAppeal.note = noteData;

						if (noteAppeal.commit()) {
							
							if(request.getParameter("email") != null){
								MailerTask task = new MailerTask(); // send message (email)
								task.setMessagePreparator(new AppealNoteMessage(noteAppeal, citation, citationAppeal)); //
								Executor.getIntance().addTask(task);
							}							
							
							if(request.getParameter("email") != null){
								
							}								
								
							json.add("note", gson.toJsonTree(noteAppeal));
							json.addProperty("success", true);
						} else {
							json.addProperty("msg",
									"Unknown error saving note.");
						}
					}
				} else if (xaction.equals("delete")) {
					if (noteAppeal.delete()) {
						json.addProperty("success", true);
					} else {
						json.addProperty("msg", "Error deleting note.");
					}
				} else {
					json.addProperty("msg", "Unknown xaction.");
				}

				response.getOutputStream().print(gson.toJson(json));
				return null;
			} else {
				// citation.getNotes();
				ModelAndView mv = new ModelAndView("citation_appeal_notes");
				mv.addObject("citationAppeal", citationAppeal);

				return mv;
			}
		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error retrieving notes.");
			response.getOutputStream().print(gson.toJson(json));
		}

		return null;
	}

	
	public void statsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		try {
			user = User.getCurrentUser();

			if (user == null || (!user.hasPermission(User.PL_CITATION_VIEW|User.PL_CITATION_MANAGE))) {
				json.addProperty("msg", "You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("status");
			qb.field("status_id")
			  .field("status_name")
			  .field("fee_check");

			DBFilterList filter = new DBFilterList();
			String value = "";

			filter.add(new DBFilter("status_name", "!=", "Sent to collections"));
			filter.add(new DBFilter("status_name", "!=", "Bad Debt"));
			
			
			value = request.getParameter("status_name");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("status_name", "=", value));
			}

			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
														  .orderDir(dir)
														  .where(filter)
														  .select();
			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("status", gson.toJsonTree(list));
			json.addProperty("count", count);

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.getOutputStream().print(gson.toJson(json));

	}
	
	public void listPaymentPlan(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		try {
			user = User.getCurrentUser();

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (!user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}
			
			String sort = "date";
			if(!request.getParameter("sort").equalsIgnoreCase("status")){
				sort = request.getParameter("sort");
			}
			
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("payment_plan");
			qb.field("payment_plan_id")
			  .field("citation_id")
			  .field("amount")
			  .field("frequency")
			  .field("date")
			  .field("paid")
			  .field("type")
			  .field("number_payment")
			  .field("CONCAT('Amount: $',amount, ', Date: ',date) as description");
			 

			DBFilterList filter = new DBFilterList();
			String value = "";

			value = request.getParameter("citation_id");
			filter.add(new DBFilter("citation_id", "=", value));

			value = request.getParameter("paid");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("paid", "=", value));
			}
			// for filter
			value = request.getParameter("filter");
			if (value != null && value.length() > 0) {
				DBFilter nameFilter = new DBFilter("frequency", "LIKE", value);
			}
			
			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir).where(filter).start(start).max(limit)
					.select();
			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("payment_plan", gson.toJsonTree(list));
			json.addProperty("count", count);

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.getOutputStream().print(gson.toJson(json));

	}

	public void listAppeal(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;

		try {
			user = User.getCurrentUser();

			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");

			QueryBuilder qb = new QueryBuilder("citation_appeal");
			qb.field("citation_appeal.citation_appeal_id")
					.field("citation_appeal.citation_id")
					.field("citation_appeal.appeal_date")
					.field("citation_appeal.status")
					.field("citation_appeal.name")
					.field("citation_appeal.email")
					.field("citation_appeal.phone")
					.field("citation_appeal.address")
					.field("citation_appeal.city")
					.field("citation_appeal.state_id")
					.field("citation_appeal.zip")
					.field("citation_appeal.reason")
					.field("citation_appeal.decision_date")
					.field("citation_appeal.officer_notes")
					.field("citation_appeal.decision_reason");

			qb.join("citation citation",
					"citation.citation_id=citation_appeal.citation_id").field(
					"citation.citation_number citation_number");

			DBFilterList filter = new DBFilterList();
			String value = "";

			// for filter
			value = request.getParameter("filter");
			if (value != null && value.length() > 0) {
				DBFilter nameFilter = new DBFilter("citation_appeal.status",
						"LIKE", value);
				filter.add(nameFilter);
				filter.addOr(nameFilter, new DBFilter(
						"citation.citation_number", "LIKE", value));
			}

			value = request.getParameter("filter_name");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("name", "LIKE", value));
			}

			value = request.getParameter("filter_citation_appeal_status");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("citation_appeal.status", "=", value));
			}

			value = request.getParameter("filter_email");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("email", "LIKE", value));
			}

			value = request.getParameter("filter_city");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("city", "LIKE", value));
			}

			value = request.getParameter("filter_address");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("address", "LIKE", value));
			}

			value = request.getParameter("filter_zip");
			if (value != null && value.length() > 0) {
				filter.add(new DBFilter("zip", "=", value));
			}

			value = request.getParameter("filter_date");
			if (value != null && value.length() > 0) {
				DateParser dp = new DateParser("yyyy-MM-dd'T'HH:mm:ss")
						.parse(value);
				Timestamp start = dp.firstHour().getTimestamp();
				Timestamp end = dp.lastHour().getTimestamp();
				// DBFilter(String fieldName, String operator, Object value,
				// Object betweenValue)
				filter.add(new DBFilter("appeal_date", "between", start, end));

			}

			value = request.getParameter("owner_id");
			if (value != null && value.length() > 0) {
				try {
					int oid = Integer.parseInt(value);
					if (oid > 0) {
						filter.add(new DBFilter("owner_id", "=", oid));
					}
				} catch (NumberFormatException nfe) {
				}
			}

			int start = 0;
			int limit = 0;

			if (request.getParameter("start") != null) {
				try {
					start = Integer.parseInt(request.getParameter("start"));
				} catch (NumberFormatException nfe) {
				}
			}

			if (request.getParameter("limit") != null) {
				try {
					limit = Integer.parseInt(request.getParameter("limit"));
				} catch (NumberFormatException nfe) {
				}
			}

			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir).where(filter).start(start).max(limit)
					.select();
			int count = list.size();
			if (limit > 0) {
				count = qb.count();
			}

			json.addProperty("success", true);
			json.add("appeals", gson.toJsonTree(list));
			json.addProperty("count", count);

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.getOutputStream().print(gson.toJson(json));

	}
	
	public void deletePaymentPlan(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");
		User user = User.getCurrentUser();
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();

		if (user == null || !user.hasPermission(User.PL_ADMIN)) {
			json.addProperty("msg",
					"You don\'t have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}

		int paymentPlanID = 0;
		try {
			paymentPlanID = Integer.parseInt(request.getParameter("payment_plan_id"));
			CitationPaymentPlan  paymentPlan  = new CitationPaymentPlan(paymentPlanID);
			
			if (!paymentPlan.delete()) {
				response.getOutputStream().print("{success: false, msg: 'Error removing Payment Plan.'}");
				return;
			}
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid pyment plan id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid pyment plan id'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}
	
	public void deleteAppeal(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");
		User user = User.getCurrentUser();
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();

		if (user == null || !user.hasPermission(User.PL_ADMIN)) {
			json.addProperty("msg",
					"You don\'t have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}

		int citationID = 0;
		try {
			citationID = Integer.parseInt(request.getParameter("citation_id"));

			Citation citation = new Citation(citationID);
			CitationAppeal appeal = citation.getAppeal();

			if (!appeal.delete()) {
				response.getOutputStream().print(
						"{success: false, msg: 'Error removing appeal.'}");
				return;
			}
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}

	public void delete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");
		User user = User.getCurrentUser();
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();

		if (user == null || !user.hasPermission(User.PL_ADMIN)) {
			json.addProperty("msg",
					"You don\'t have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}

		int citationID = 0;
		try {
			citationID = Integer.parseInt(request.getParameter("citation_id"));

			int ownerId = 0;
			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			Citation citation = new Citation(citationID, ownerId);
			if (!citation.delete()) {
				response.getOutputStream().print(
						"{success: false, msg: 'Error removing citation.'}");
				return;
			}
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}

	public void verifyOfficer(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		
		User user = User.getCurrentUser();
		if (user == null || !user.isAdmin()) {
			response.getOutputStream().print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}

		try {

			String value = request.getParameter("citation_id");
			if (value != null && value.length() > 0) {
				Citation citation = new Citation(Integer.parseInt(value));
				//citation.officer_id;
				if(citation.officer_id.equalsIgnoreCase(user.officer_id)){
					response.getOutputStream().print("{success: true}");
				}else {
					response.getOutputStream().print("{success: false}");
				}
				
			}else {
				response.getOutputStream().print("{success: false, msg: 'Invalid citatin id.'}");
			}

			

		} catch (Exception e) {
			response.getOutputStream().print("{success: false, msg: 'Error in the aplication'}");
			return;
		}

	}
	
	public void clear(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");
		User user = User.getCurrentUser();
		if (user == null || !user.isAdmin()) {
			response.getOutputStream()
					.print("{success: false, msg: 'You don\'t have permission to perform this action.'}");
			return;
		}

		try {
			DateParser dp = new DateParser("MM/dd/yyyy");
			Timestamp start = null;
			Timestamp end = null;

			String value = request.getParameter("citation_clear_start");
			if (value != null && value.length() > 0) {
				start = dp.parse(value).firstHour().getTimestamp();
			}

			value = request.getParameter("citation_clear_end");
			if (value != null && value.length() > 0) {
				end = dp.parse(value).lastHour().getTimestamp();
			}

			Citation citation = new Citation();
			if (!citation.clear(start, end)) {
				response.getOutputStream().print(
						"{success: false, msg: 'Error deleting citations.'}");
				return;
			}

		} catch (Exception e) {
			response.getOutputStream().print(
					"{success: false, msg: 'Error deleting citations: "
							+ e.getMessage() + "'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}

	public ModelAndView ownerDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
			
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_OWNER_MANAGE|User.PL_OWNER_VIEW)) && !user.hasPermission(User.PL_CITATION_MANAGE|User.PL_CITATION_VIEW))
		{
			response.getOutputStream().print("You don't have permission to perform this action.");
			return null;
		}
		
		try
		{
			//int owner_id = Integer.parseInt(request.getParameter("owner_id"));
			String strOwnerid = request.getParameter("owner_id");
			String strCitationid = request.getParameter("citation_id");
			Owner owner = new Owner();
			if(strOwnerid != null && strOwnerid.length() > 0)
			{
				int owner_id = Integer.parseInt(strOwnerid);
				if(owner_id > 0)
				{
					Owner editOwner = new Owner(owner_id);
					editOwner.loadExtra();
					owner=editOwner;
				}else{
					if(strCitationid != null && strCitationid.length() > 0)
					{
						int citation_id = Integer.parseInt(strCitationid);
						Citation citation = new Citation(citation_id);
						Owner addOwner = new Owner(citation.owner_id);
						if(citation.owner_id > 0)
						{
							addOwner.loadExtra();
						}
						owner=addOwner;
					}else{
						owner = new Owner(0);
					}
					
				}
				
			}else{
				owner = new Owner(0);
			}
			
			
			OwnerFields fields = new OwnerFields();
			ArrayList<OwnerField> ownerFields = fields.getFields();
			
			if(request.getParameter("xaction") != null)
			{
			
				json.addProperty("success", true);
				
				json.add("owner", gson.toJsonTree(owner));
				json.add("fields", gson.toJsonTree(ownerFields));
				
				OwnerType types = new OwnerType();
				json.add("types",  gson.toJsonTree(types.get(0, 0, "name ASC", null)));	
			
				response.setContentType("text/json");
				response.getOutputStream().print(gson.toJson(json));
				return null;
			}
			else
			{
				ModelAndView mv =  new ModelAndView("owner_citation");
				
				mv.addObject("fields", ownerFields);
				mv.addObject("owner", owner);
				
				return mv;
			}
		}
		catch(Exception e)
		{
			
		}
		
		response.setContentType("text/json");
		response.getOutputStream().print("{success: false, msg: 'Owner not found.'}");
		return null;
	}
	
	public void saveOwner(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);
		
		User user = User.getCurrentUser();
		if(user == null || !user.hasPermission(User.PL_OWNER_MANAGE))
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		String strOwnerId = request.getParameter("owner_id");
		
		if(strOwnerId != null && strOwnerId.length() > 0)
		{
			try
			{
				int id = Integer.parseInt(strOwnerId);
				Owner owner = new Owner(id);
				owner.username = request.getParameter("username");
				
				Owner test = Owner.getByUsername(owner.username);
				if(test == null || test.owner_id == owner.owner_id)
				{
					String password = request.getParameter("password");
					if(password != null && password.length() > 0)
					{
						owner.setPassword(password);
					}
					else if(owner.owner_id == 0)
					{
						owner.password = "";
					}
					owner.first_name = request.getParameter("first_name");
					if(owner.first_name == null)
					{
						owner.first_name = "";
					}
					owner.last_name = request.getParameter("last_name");
					if(owner.last_name == null)
					{
						owner.last_name = "";
					}
					owner.email = request.getParameter("email");
					if(owner.email == null)
					{
						owner.email = "";
					}
					owner.status = request.getParameter("status");
					if(owner.status == null)
					{
						owner.status = "";
					}
					owner.type_id = Integer.parseInt(request.getParameter("type_id"));
					owner.home_phone = request.getParameter("home_phone");
					if(owner.home_phone == null)
					{
						owner.home_phone = "";
					}
					owner.mobile_phone = request.getParameter("mobile_phone");
					if(owner.mobile_phone == null)
					{
						owner.mobile_phone = "";
					}
					owner.address = request.getParameter("address");
					if(owner.address == null)
					{
						owner.address = "";
					}
					owner.city = request.getParameter("city");
					if(owner.city == null)
					{
						owner.city = "";
					}
					owner.state_id = request.getParameter("state");
					owner.zip = request.getParameter("zip");
					if(owner.zip == null)
					{
						owner.zip = "";
					}
												
					if(owner.commit())
					{
						int citation_id = Integer.parseInt(request.getParameter("citation_id"));
						Citation citation = new Citation(citation_id);
						if (owner.owner_id==0){
							citation.owner_id= owner.getMaxOwnerId();
						}else{
							citation.owner_id= owner.owner_id;
						}
						
						if(citation.commit())
						{
							json.addProperty("success", true);
							json.add("owner", gson.toJsonTree(owner));
						}else
						{
							json.addProperty("msg", "Failed to assign this owner to citation.");
						}
						json.addProperty("success", true);
						json.add("owner", gson.toJsonTree(owner));
						
					}
					else
					{
						json.addProperty("msg", "Unknown error saving owner.");
					}
				}
				else
				{
					json.addProperty("msg", "Username is already in use. Please select a different Username.");
				}
				
			}
			catch(Exception e)
			{
				json.addProperty("msg", "Error saving owner: " +e.getMessage());
			}
		}
		else
		{
			json.addProperty("msg", "Error saving owner.");
		}
				
		response.getOutputStream().print(gson.toJson(json));

	}
	
	public void selectOwner(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/json");
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false); 
		
		User user = User.getCurrentUser();
		if(user == null || !user.hasPermission(User.PL_OWNER_MANAGE))
		{
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		String strOwnerId = request.getParameter("owner_id");
		String strCitationId = request.getParameter("citation_id");
		
		if((strOwnerId != null && strOwnerId.length() > 0)&&(strCitationId != null && strCitationId.length() > 0))
		{
			try
			{
				Citation citation = new Citation(Integer.parseInt(strCitationId));
				citation.owner_id= Integer.parseInt(strOwnerId);				
				if(citation.commit())
				{
					json.addProperty("success", true);
					json.add("owner", gson.toJsonTree(citation));
				}else
				{
					json.addProperty("msg", "Failed to assign this owner to citation.");
				}
				json.addProperty("success", true);
				json.add("owner", gson.toJsonTree(citation));
			}
			catch(Exception e)
			{
				json.addProperty("msg", "Error saving owner: " +e.getMessage());
			}
		}
		else
		{
			json.addProperty("msg", "Error saving owner.");
		}
				
		response.getOutputStream().print(gson.toJson(json));

	}
	
	public void ownerManage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		response.setContentType("text/json");
		
		User user = User.getCurrentUser();
		if(user == null || (!user.hasPermission(User.PL_OWNER_VIEW|User.PL_OWNER_MANAGE)))
		{
			JsonObject json = new JsonObject();
			json.addProperty("success", false);
			json.addProperty("msg", "You don't have permission to perform this action.");
			response.getOutputStream().print(gson.toJson(json));
			return;
		}
		
		try
		{
			QueryBuilder qb = new QueryBuilder("owner");
			qb.field("DISTINCT owner.owner_id").field("owner.address").field("owner.zip");
			
			
			if(qb.isSQLServer())
			{
				qb.field(" last_name + ', ' + first_name as name");
			}
			else if(qb.isOracle())
			{
				qb.field("CONCAT(CONCAT(last_name,', '),first_name) name");
			}
			else //mysql
			{
				qb.field("CONCAT(last_name,', ',first_name) name");
			}
			
			
			qb.join("owner_type", "owner_type.owner_type_id=owner.type_id").field("owner_type.name ownertype");
			
			DBFilterList filter = new DBFilterList();
						
			String value = request.getParameter("filter");
			if(value != null && value.length() > 0)
			{

				qb.join("mpermit", "mpermit.owner_id=owner.owner_id");
				qb.join("vehicle", "vehicle.owner_id=owner.owner_id");
				
				DBFilter nameFilter = new DBFilter("first_name", "LIKE", value);
				filter.add(nameFilter);
				filter.addOr(nameFilter, new DBFilter("owner.owner_id", "=", value));
				filter.addOr(nameFilter, new DBFilter("last_name", "LIKE", value));
				filter.addOr(nameFilter, new DBFilter("owner.status", "=", value));
				filter.addOr(nameFilter, new DBFilter("owner_type.name", "=", value));
				filter.addOr(nameFilter, new DBFilter("mpermit.permit_number", "LIKE", value));
				filter.addOr(nameFilter, new DBFilter("vehicle.license", "LIKE", value));
			}
			
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			if(sort.equals("status"))
			{
				sort = "owner.status";
			}
			else if(sort.equals("last_name") && qb.isOracle())
			{
				sort = "name";
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
			
			ArrayList<Hashtable<String, String>> list = qb.orderBy(sort)
					.orderDir(dir)
					.where(filter)
					.start(start)
					.max(limit)
					.select();

			String json = gson.toJson(list);
			
			int count = list.size();
			if(limit > 0)
			{
				count = qb.count();
			}
			
			response.getOutputStream().print("{count: "+count+", owners: " + json + "}");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void savePaymetPlan(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/json");
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = User.getCurrentUser();

		int paymentPlanId = 0;
		try {

			int ownerId = 0;
			String strOwnerId = request.getParameter("owner_id");
			if (user == null
					|| !user.hasPermission(User.PL_CITATION_MANAGE)
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE))) {
				json.addProperty("msg",
						"You don\'t have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			if (request.getParameter("payment_plan_id") != null) {
				paymentPlanId = Integer.parseInt(request.getParameter("payment_plan_id"));
			}
			
			if(paymentPlanId > 0){
				CitationPaymentPlan paymentPlan = new CitationPaymentPlan(paymentPlanId);
				
				if (request.getParameter("amount") != null)
					paymentPlan.amount = Float.parseFloat(request.getParameter("amount"));
				if (request.getParameter("date") != null)
					paymentPlan.date = request.getParameter("date");
				if (request.getParameter("frequency") != null)
					paymentPlan.frequency = request.getParameter("frequency");
				if (request.getParameter("paid") != null)
					paymentPlan.paid = Integer.parseInt(request.getParameter("paid"));
				if(paymentPlan.commit()){
					json.addProperty("success", true);
				} else {
					json.addProperty("msg", "Error saving payment plan.");
				}
				
			}else {
				float total_amount = 0;
				float total_paid = 0;
				float amount = 0;
				int num_payment = 0;
				int sumDays = 7;
				boolean flag = false;
				Timestamp date = Timestamp.valueOf(request.getParameter("date")+" 00:00:00");
								    
				
				Citation citation = new Citation(Integer.parseInt(request.getParameter("citation_id")));
				Invoice invoice = new Invoice();
				invoice = citation.getInvoice();
				LateFee fee = new LateFee();
				fee = citation.getLateFee();
				

				if(invoice != null){
					for(InvoiceItem item : invoice.getItems()){
						if(item.type == InvoiceItem.TYPE_PAYMENT){
							total_paid += item.amount;
						}else if(item.status.equalsIgnoreCase("Refunded") && item.type == InvoiceItem.TYPE_REFUND){
							total_paid -= item.amount;
						} else if(item.status.equalsIgnoreCase("Voided") && item.type == InvoiceItem.TYPE_VOID){
							total_paid -= item.amount;
						}
			        }
				}
								 
				total_amount = citation.violation_amount;
				if(fee != null){
					total_amount = total_amount + fee.fee_amount;  	
				}
				total_amount = (total_amount - total_paid);
				if(request.getParameter("amount") != null)
					amount = Integer.parseInt(request.getParameter("amount"));
				if(request.getParameter("number_payment") != null)
					num_payment = Integer.parseInt(request.getParameter("number_payment"));
				if(request.getParameter("frequency").equalsIgnoreCase("monthly")){
					sumDays = 30;
				}
				
				if(request.getParameter("type").equalsIgnoreCase("payment_amount")){
					
					while (total_amount > 0 && flag == false) {         
						if(total_amount - amount < 0){
							flag = true;
							amount = total_amount;
						}else {
							total_amount = total_amount - amount;
						}
					
						//now.setDate(now.getDate() + 7);
						CitationPaymentPlan paymentPlan = new CitationPaymentPlan();
						paymentPlan.amount = amount;
						paymentPlan.date = date.toString();
						paymentPlan.citation_id = Integer.parseInt(request.getParameter("citation_id"));
						paymentPlan.frequency = request.getParameter("frequency");
						paymentPlan.type = request.getParameter("type");
						paymentPlan.commit();
						
						long miliseconds = 0;
						miliseconds = TimeUnit.DAYS.toMillis(sumDays);
						long newDate = new Timestamp(date.getTime() + miliseconds).getTime();					
						date = new Timestamp(newDate);
					} 
					
				}else {
					
					float payments_amount =  Math.round((total_amount / num_payment));
					
					while (total_amount > 0 && flag == false) {
						
						if(total_amount - payments_amount < 0){
							flag = true;
							payments_amount = total_amount;
						}else {
							total_amount = total_amount - payments_amount;
						}
				
						CitationPaymentPlan paymentPlan = new CitationPaymentPlan();
						paymentPlan.amount = payments_amount;
						paymentPlan.date = date.toString();
						paymentPlan.citation_id = Integer.parseInt(request.getParameter("citation_id"));
						paymentPlan.frequency = request.getParameter("frequency");
						paymentPlan.type = request.getParameter("type");
						paymentPlan.commit();
						
						
						long miliseconds = 0;
						miliseconds = TimeUnit.DAYS.toMillis(sumDays);
						long newDate = new Timestamp(date.getTime() + miliseconds).getTime();					
						date = new Timestamp(newDate);
					}
				}
				
			
				/*
				CitationPaymentPlan paymentPlan = new CitationPaymentPlan(paymentPlanId);
				
				if (request.getParameter("type") != null)
					paymentPlan.type = request.getParameter("type");
				if (request.getParameter("amount") != null)
					paymentPlan.amount = Float.parseFloat(request
							.getParameter("amount"));
				if (request.getParameter("citation_id") != null)
					paymentPlan.citation_id = Integer.parseInt(request
							.getParameter("citation_id"));
				if (request.getParameter("date") != null)
					paymentPlan.date = request.getParameter("date");
				if (request.getParameter("frequency") != null)
					paymentPlan.frequency = request.getParameter("frequency");
				if (request.getParameter("status") != null) {
					paymentPlan.status = request.getParameter("status");
				} else {
					paymentPlan.status = "No Paid";
				}
				if (request.getParameter("number_payment") != null) {
					paymentPlan.number_payment =  Integer.parseInt(request.getParameter("number_payment"));
				}*/
				
				json.addProperty("success", true);
			}
			
			
			
		} catch (UnknownObjectException uoe) {
			json.addProperty("msg", "Citation not found.");
		} catch (NumberFormatException nfe) {
			json.addProperty("msg", "Invalid citation id.");
		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error saving citation: " + e.getMessage());
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void saveNotes(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/json");
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = User.getCurrentUser();

		int citationID = 0;
		try {

			int ownerId = 0;
			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_CITATION_MANAGE) && !user
							.hasPermission(User.PL_OWNER_MANAGE))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE))) {
				json.addProperty("msg",
						"You don\'t have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			citationID = Integer.parseInt(request.getParameter("citation_id"));
			Citation citation = new Citation(citationID, ownerId);

			// update message
			// citation.notes = request.getParameter("citation_notes");

			if (citation.commit()) {
				json.addProperty("success", true);
			}
		} catch (UnknownObjectException uoe) {
			json.addProperty("msg", "Citation not found.");
		} catch (NumberFormatException nfe) {
			json.addProperty("msg", "Invalid citation id.");
		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error saving citation: " + e.getMessage());
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void save(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// citation-violation-amount
		response.setContentType("text/json");
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();

		json.addProperty("success", false);

		User user = User.getCurrentUser();

		int citationID = 0;
		try {

			int ownerId = 0;
			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_CITATION_MANAGE) && !user
							.hasPermission(User.PL_OWNER_MANAGE))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE))) {
				json.addProperty("msg",
						"You don\'t have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			if (request.getParameter("citation_id") != null) {
				citationID = Integer.parseInt(request
						.getParameter("citation_id"));
			} else {
				citationID = 0;
			}

			
			
			boolean historyFlag = false;
			Citation citation = new Citation(citationID, ownerId);
			citation.loadExtra();

			if(citation.pin == null)
	    	{
				citation.pin = citation.generatePIN();
	    	}
			
			// first update status
			if(citation.status_id != Integer.parseInt(request.getParameter("citation-status"))){
				citation.status_id = Integer.parseInt(request.getParameter("citation-status"));
				historyFlag = true;
			}
			
			if (request.getParameter("citation-number") != null) {
				citation.citation_number = request
						.getParameter("citation-number");
			}
			
			
			if (citation.status_id==4) {
				citation.community_service_end = Timestamp.valueOf(request
						.getParameter("citation-community-service-end"));
			}

			if (request.getParameter("updateGeneral") != null) {
				if (citation.commit()) {
					if(historyFlag){
						StatusHistory statusHistory = new StatusHistory();
						statusHistory.citation_id = citation.citation_id;
						statusHistory.status_id = citation.status_id;
						statusHistory.date = new Timestamp(System.currentTimeMillis());
						statusHistory.commit();
						
					}
					
					json.addProperty("success", true);
					response.getOutputStream().print(gson.toJson(json));
					return;					
				}
			}
			// make sure citation number is still unique
			if (!Citation
					.unique(citation.citation_number, citation.citation_id)) {
				throw new Exception(
						"Citation number already exists. Please change it.");
			}

			if (citationID == 0
					&& (LicenseManager.isManagedPermitsEnabled() || LicenseManager
							.isManagedPermitsEnabled())) {

				DBConnection conn = null;

				conn = new DBConnection();

				if (conn.query("SELECT o_owner_id from permit WHERE (license = '"
						+ request.getParameter("citation-license")
						+ "' OR vin = '"
						+ request.getParameter("citation-vin")
						+ "') limit 1")) {
					ResultSet rs = conn.getResultSet();
					if (rs.next()) {
						citation.owner_id = rs.getInt(1);
					}

				}

			}

			if (citationID == 0) {
				citation.permit_number = request
						.getParameter("citation-permit_number");
			}

			CiteFields cfields = new CiteFields();
			ArrayList<CiteField> fields = cfields.getFields(false, true);
			for (CiteField field : fields) {
				if (field.type.equals(CiteField.TYPE_TEXT)) {
					CitationAttribute attr = citation.getAttribute(field.name);
					if (attr == null) {
						attr = new CitationAttribute(field.name);
						citation.addAttribute(attr);
					}

					attr.setValue(request
							.getParameter("citation-" + field.name));
				} else if (field.type.equals(CiteField.TYPE_LIST)
						|| field.type.equals(CiteField.TYPE_DB)) {
					CitationAttribute attr = citation.getAttribute(field.name);
					if (attr == null) {
						attr = new CitationAttribute(field.name);
						citation.addAttribute(attr);
					}

					String id = request.getParameter("citation-" + field.name);
					if (id != null && id.length() > 0) {
						FieldOption option = field.getFieldOption(id);
						if (option != null) {
							attr.setValue(option);
						}
					}
				} else if (field.type.equals(CiteField.TYPE_CODES)) {
					String id = request.getParameter("citation-" + field.name);
					if (id != null && id.length() > 0) {
						FieldOption option = field.getFieldOption(id);
						if (option != null) {
							if (field.name.equals("violation")) {
								citation.violation_id = option.id;
								citation.violation_description = option.name;
								if (citationID == 0) {
									Code code = new Code("violation", option.id);
									citation.violation_type = code
											.getFineType();
								}

								// save chalk times
								String date = request
										.getParameter("citation-violation-start-date");
								String time = request
										.getParameter("citation-violation-start-time");
								if (date != null && date.length() > 0) {
									citation.violation_start = DateParser.toTimestamp(date + " " + time,"MM/dd/yyyy h:mm a");
								}

								date = request
										.getParameter("citation-violation-end-date");
								time = request
										.getParameter("citation-violation-end-time");
								if (date != null && date.length() > 0) {
									citation.violation_end = DateParser
											.toTimestamp(date + " " + time,
													"MM/dd/yyyy h:mm a");
								}

								try {
									citation.violation_amount = Float
											.valueOf(request
													.getParameter("citation-violation-amount"));
								} catch (NumberFormatException nfe) {
									throw new Exception(
											"Invalid violation amount.");
								}
							}
							if (field.name.equals("state")) {
								citation.state_id = option.id;
								if (option.isOther()) {
									citation.state_description = request
											.getParameter("citation-"
													+ field.name + "-other");
								} else {
									citation.state_description = option.name;
								}
							} else if (field.name.equals("make")) {
								citation.make_id = option.id;
								if (option.isOther()) {
									citation.make_description = request
											.getParameter("citation-"
													+ field.name + "-other");
								} else {
									citation.make_description = option.name;
								}
							} else if (field.name.equals("color")) {
								citation.color_id = option.id;
								if (option.isOther()) {
									citation.color_description = request
											.getParameter("citation-"
													+ field.name + "-other");
								} else {
									citation.color_description = option.name;
								}
							} else if (field.name.equals("location")) {
								citation.location_id = option.id;
								if (option.isOther()) {
									citation.location_description = request
											.getParameter("citation-"
													+ field.name + "-other");
								} else {
									citation.location_description = option.name;
								}
							} else if (field.name.equals("comment")) {
								citation.comment_id = option.id;
								if (option.isOther()) {
									citation.comments = request
											.getParameter("citation-"
													+ field.name + "-other");
								} else {
									citation.comments = option.name;
								}
							}
						}
					}
				} else // standard
				{
					if (field.name.equals("date_time")) {
						String date = request.getParameter("citation-date")
								+ " " + request.getParameter("citation-time");
						citation.citation_date = DateParser.toTimestamp(date,
								"MM/dd/yyyy h:mm a");
					} else if (field.name.equals("license")) {
						citation.license = request
								.getParameter("citation-license");
					} else if (field.name.equals("vin")) {
						citation.vin = request.getParameter("citation-vin");
					} else if (field.name.equals("officer_id")) {
						citation.officer_id = request
								.getParameter("citation-officer_id");
					}
				}
			}

			if (citation.commit()) {
				json.addProperty("success", true);
			}
		} catch (UnknownObjectException uoe) {
			json.addProperty("msg", "Citation not found.");
		} catch (NumberFormatException nfe) {
			json.addProperty("msg", "Invalid citation id.");
		} catch (Exception e) {
			e.printStackTrace();
			json.addProperty("msg", "Error saving citation: " + e.getMessage());
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void exported(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");
		User user = User.getCurrentUser();
		Gson gson = new GsonBuilder().create();
		JsonObject json = new JsonObject();

		int citationID = 0;
		try {

			if (user == null || !user.hasPermission(User.PL_CITATION_MANAGE)) {
				json.addProperty("msg",
						"You don\'t have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			citationID = Integer.parseInt(request.getParameter("citation_id"));

			int ownerId = 0;
			String strOwnerId = request.getParameter("owner_id");
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			Citation citation = new Citation(citationID, ownerId);
			if (!citation.setExported(false)) {
				response.getOutputStream().print(
						"{success: false, msg: 'Error updating citation.'}");
				return;
			}
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}

	public void photos(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");
		int citationID = 0;
		try {
			citationID = Integer.parseInt(request.getParameter("citation_id"));
			Citation citation = new Citation(citationID);

			Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			response.setContentType("text/json");
			String json = gson.toJson(citation.getPhotos());

			response.getOutputStream().print(
					"{success: true, photos: " + json + "}");
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		}
	}

	public void photo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("image/jpg");
		int photoID = 0;
		try {
			String pid = request.getParameter("pid");
			photoID = Integer.parseInt(pid);
			CitationPhoto photo = new CitationPhoto(photoID);
			IOUtils.copy(photo.getFileInputStreamPhoto(),
					response.getOutputStream());
			// response.getOutputStream().write(photo.getPhoto());
		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		}

		response.getOutputStream().print("{success: true}");
	}

	public void pdfExport(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			
			int citation_id = 0;
			String id = request.getParameter("citation_id");
			citation_id = Integer.parseInt(id);
			Citation citation = new Citation(citation_id);
			
			String img = "";
			String htmlFile = "";			
			
			if(request.getParameter("notification").equalsIgnoreCase("first")){
				img = "img_first_notice.png";
			}else {
				img = "img_final_notice.png";
			}
			
			// Owner owner = new Owner(citation.owner_id);
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");

			ServletContext context = this.getServletContext();
			String filePath = context.getRealPath("") + "/static/images/";
			String total = "";
			float paid = 0;
			Invoice list = citation.getInvoice();
			
			for (InvoiceItem data : list.getItems(Invoice.TYPE_CITATION)) {
				paid += data.amount;
			}
			
			LateFee fee = new LateFee();
			fee = citation.getLateFee();
			
			HashMap<String, Object> model = new HashMap<String, Object>();
			model.put("citation", citation);
			model.put("path", filePath+img);
			model.put("fee", false);
			if(fee == null){
				model.put("fee", false);
				//total = String.format("$%.02f", citation.violation_amount);
			}else {
				//model.put("fee", fee);
				model.put("fee", false);
				//total =  String.format("$%.02f", citation.violation_amount + fee.fee_amount);
			}
			
			model.put("total", String.format("$%.02f",((citation.violation_amount + fee.fee_amount)-paid)));		
			model.put("paid", String.format("$%.02f",paid));
			String filename = citation.citation_number;
			
			if(request.getParameter("notification").equalsIgnoreCase("first")){
				htmlFile = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, first_template_path, model);
				filename+= " - first notice.pdf";
			}else {
				htmlFile = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, final_template_path, model);
				filename+= " - final notice.pdf";
			}
						
			OutputStream file = new FileOutputStream(new File(filePath + "temp.pdf"));

			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, file);
			document.open();
			InputStream is = new ByteArrayInputStream(htmlFile.getBytes());
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);

			document.close();
			file.close();

			
			String agent = request.getHeader("USER-AGENT");
			if (agent != null && agent.indexOf("MSIE") != -1) {
				filename = URLEncoder.encode(filename, "UTF8");
				response.setContentType("application/x-download");
				response.setHeader("Content-Disposition", "attachment;filename=" + filename);
			} else if (agent != null && agent.indexOf("Mozilla") != -1) {
				response.setCharacterEncoding("UTF-8");
				filename = MimeUtility.encodeText(filename, "UTF8", "B");
				response.setContentType("application/force-download");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			}

			InputStream in = new FileInputStream(filePath + "temp.pdf");

			BufferedOutputStream out = new BufferedOutputStream(
					response.getOutputStream());
			byte by[] = new byte[32768];
			int index = in.read(by, 0, 32768);
			while (index != -1) {
				out.write(by, 0, index);
				index = in.read(by, 0, 32768);
			}
			out.flush();
			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void details(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy HH:mm:ss")
				.excludeFieldsWithoutExposeAnnotation().create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;
		int citationID = 0;

		try {

			user = User.getCurrentUser();
			citationID = Integer.parseInt(request.getParameter("citation_id"));
			
			String strOwnerId = request.getParameter("owner_id");
			String prefix = "";
			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}
			
			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			Citation citation = new Citation(citationID, ownerId);
			citation.status = new Status(citation.status_id);
			citation.loadExtra();
			if (citationID==0){
				prefix = citation.createCitationNumber();
			}
			CiteFields cfields = new CiteFields();
			ConfigItem itemDaysDispute = ConfigItem.lookup("DAYS_TO_DISPUTE");
			ConfigItem itemFirstNotification = ConfigItem.lookup("CITE_FIRST_NOTIFICATION");
			ConfigItem itemSecondNotification = ConfigItem.lookup("CITE_SECOND_NOTIFICATION");
			int firstNote = 30;
			int finalNote = 45;
			if (itemFirstNotification.text_value.length()>0){
				try{
					firstNote = Integer.parseInt(itemFirstNotification.text_value);
				}catch(Exception e){
					
				}
			}
			if (itemSecondNotification.text_value.length()>0){
				try{
					finalNote = Integer.parseInt(itemSecondNotification.text_value);
				}catch(Exception e){
					
				}
			}
			
			ArrayList<CiteField> fields = cfields.getFields();
			fields.add(0, new CiteField("Status", CiteField.TYPE_STANDARD));// add
																			// non
																			// standard/custom
																			// fields
			Calendar maxDayDispute = Calendar.getInstance();

			if (citation.getCitation_date() != null) {

				maxDayDispute.setTimeInMillis(citation.getCitation_date()
						.getTime());
				try {
					maxDayDispute.add(Calendar.DAY_OF_MONTH,
							Integer.parseInt(itemDaysDispute.text_value));
				} catch (NumberFormatException nfe) {
					// not configured so they can't dispute
				}

				if ((Calendar.getInstance()).compareTo(maxDayDispute) <= 0) {
					itemDaysDispute.int_value = 1;
				}
			}

			//Gson mygson = new GsonBuilder().create();
			//JsonObject jsonPrefix = (JsonObject) gson.toJson(prefix);
			//response.getOutputStream().print("{success: true, timeMax: "+mygson.toJson(prefix)+"}");


			JsonObject jsonCite = (JsonObject) gson.toJsonTree(citation);
			jsonCite.add("extra", gson.toJsonTree(citation.getExtras()));

			if (citation.hasOverride()) {
				jsonCite.add("hasOverride", gson.toJsonTree(true));
			}

			String xaction = request.getParameter("xaction");
			if (xaction != null && xaction.equals("edit")) {
				for (CiteField field : fields) {
					if (field.type.equals(CiteField.TYPE_DB)
							|| field.type.equals(CiteField.TYPE_CODES)) {
						field.loadDatabaseOptions();
					}
				}
			} else {
				jsonCite.add("photos", gson.toJsonTree(citation.getPhotos()));
				jsonCite.add("appeal", gson.toJsonTree(citation.getAppeal()));
				jsonCite.add("invoice", gson.toJsonTree(citation.getInvoice()));
				jsonCite.add("late_fee", gson.toJsonTree(citation.getLateFee()));
				jsonCite.add("first_export", gson.toJsonTree(citation.sendNitification(firstNote)));
				jsonCite.add("final_export", gson.toJsonTree(citation.sendNitification(finalNote)));
			}

			json.addProperty("success", true);
			json.addProperty("prefix", prefix);
			json.add("citation", jsonCite);
			json.add("fields", gson.toJsonTree(fields));
			json.add("daysDispute", gson.toJsonTree(itemDaysDispute));
			json.add("maxDayDispute", gson.toJsonTree(maxDayDispute.getTime()));

		} catch (UnknownObjectException uoe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Citation not found.'}");
			return;
		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (Exception e) {
			json.addProperty("msg", "Error in the application");
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void appealDetails(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;
		int citationApeealID = 0;

		try {

			user = User.getCurrentUser();
			citationApeealID = Integer.parseInt(request
					.getParameter("citation_appeal_id"));
			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			CitationAppeal citationAppeal = new CitationAppeal();
			JsonObject jsonCitationAppeal = (JsonObject) gson
					.toJsonTree(citationAppeal
							.getCitationAppeal(citationApeealID));
			/*
			 * Calendar maxDayDispute = Calendar.getInstance();
			 * maxDayDispute.setTimeInMillis
			 * (citationAppeal.getAppealDate().getTime()); try {
			 * maxDayDispute.add(Calendar.DAY_OF_MONTH,
			 * Integer.parseInt(itemDaysDispute.text_value)); } catch
			 * (NumberFormatException nfe) { // not configured so they can't
			 * dispute }
			 * 
			 * String xaction = request.getParameter("xaction"); if (xaction !=
			 * null && xaction.equals("edit")) { for (CiteField field : fields)
			 * { if (field.type.equals(CiteField.TYPE_DB) ||
			 * field.type.equals(CiteField.TYPE_CODES)) {
			 * field.loadDatabaseOptions(); } } } else { jsonCite.add("photos",
			 * gson.toJsonTree(citation.getPhotos())); jsonCite.add("appeal",
			 * gson.toJsonTree(citation.getAppeal())); jsonCite.add("invoice",
			 * gson.toJsonTree(citation.getInvoice())); jsonCite.add("late_fee",
			 * gson.toJsonTree(citation.getLateFee())); }
			 */

			json.addProperty("success", true);
			json.add("citation_appeal", jsonCitationAppeal);

		} catch (NumberFormatException nfe) {
			response.getOutputStream().print(
					"{success: false, msg: 'Invalid citation id'}");
			return;
		} catch (Exception e) {
			json.addProperty("msg", "Error in the application");
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void expiration(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;

		try {
			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			user = User.getCurrentUser();
			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			Citation citation = new Citation(
					request.getParameter("citationNumber"));

			citation.setOverride_fine_amount(Float.parseFloat(request
					.getParameter("overrideFineAmount")));
			citation.setOverride_late_fee(Float.parseFloat(((request
					.getParameter("overrideLateFee") == null)
					|| (request.getParameter("overrideLateFee").trim()
							.equals("")) ? "0" : request
					.getParameter("overrideLateFee"))));

			if (request.getParameter("expiration") != null
					&& !request.getParameter("expiration").equals("")) {
				DateParser dp = new DateParser("MM/dd/yyyy").parse(request
						.getParameter("expiration"));
				Timestamp end = dp.lastHour().getTimestamp();
				citation.setOverride_expiration(end);
			}

			citation.commit();

			if (citation.hasOverride() && citation.owner_id != 0) {
				OwnerNote note = new OwnerNote(0, citation.owner_id);
				note.setUpdated(user);
				note.note = "Citation Fee: "
						+ citation.override_fine_amount
						+ "\n Late Fee: "
						+ citation.override_late_fee
						+ "\n Must be paid by: "
						+ DateParser.toString(
								citation.getOverride_expiration(),
								"MM/dd/yyyy HH:mm:ss");
				note.commit();
			}

			json.addProperty("success", true);
			json.addProperty("fineAmount", citation.violation_amount);
			json.addProperty("lateFee", (citation.getLateFee() == null ? 0.0
					: citation.getLateFee().fee_amount));
			json.add("overrideExpiration", gson.toJsonTree(citation
					.getOverride_expiration() != null ? DateParser.toString(
					citation.getOverride_expiration(), "MM/dd/yyyy") : ""));
			json.addProperty("hasOverride", citation.hasOverride());
		} catch (NumberFormatException ne) {
			json.addProperty("msg", "The fine amount must be numeric");
		} catch (Exception e) {
			json.addProperty("msg", "Error saving expiration");
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void cleanOverride(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;

		try {
			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			user = User.getCurrentUser();
			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE
							| User.PL_OWNER_VIEW) && !user
								.hasPermission(User.PL_CITATION_MANAGE
										| User.PL_CITATION_VIEW))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE
									| User.PL_CITATION_VIEW
									| User.PL_CITATION_RETRIEVE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			Citation citation = new Citation(
					request.getParameter("citationNumber"));

			citation.setOverride_fine_amount(0);
			citation.setOverride_late_fee(0);
			citation.setOverride_expiration(null);

			citation.commit();

			json.addProperty("success", true);
			json.addProperty("fineAmount", citation.violation_amount);
			json.addProperty("lateFee", (citation.getLateFee() == null ? 0.0
					: citation.getLateFee().fee_amount));
		} catch (NumberFormatException ne) {
			json.addProperty("msg", "Error deleting the override citation");
		} catch (Exception e) {
			json.addProperty("msg", "Error deleting the override citation");
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void payment(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		User user = null;
		int ownerId = 0;
		int citationID = 0;

		try {

			user = User.getCurrentUser();
			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE) && (!user
							.hasPermission(User.PL_CITATION_MANAGE)))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			Citation citation = null;
			JsonObject jsonCite = null;

			citationID = Integer.parseInt(request.getParameter("citation_id"));
			citation = new Citation(citationID, ownerId);

			Cart cart = new Cart(citation);

			cart.setBilling(request).setCitationAmount(request)
					.validateBilling().payCitation(request);

			jsonCite = (JsonObject) gson.toJsonTree(citation);
			jsonCite.add("invoice", gson.toJsonTree(citation.getInvoice()));
			jsonCite.add("late_fee", gson.toJsonTree(citation.getLateFee()));

			json.add("citation", jsonCite);
			json.addProperty("success", true);
		} catch (UnknownObjectException uoe) {
			json.addProperty("msg", "Citation not found.");
		} catch (CartException ce) {
			json.addProperty("msg", ce.getMessage());
		}

		response.getOutputStream().print(gson.toJson(json));
	}

	public void codes(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String orderBy = "description ASC";
			String sort = request.getParameter("sort");
			String dir = request.getParameter("dir");
			if (sort != null) {
				orderBy = sort + " " + dir;
			}

			ArrayList<Code> list = Codes.getCodes(request.getParameter("type"),
					orderBy);

			Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			response.setContentType("text/json");
			String json = gson.toJson(list);

			int count = list.size();

			response.getOutputStream().print(
					"{count: " + count + ", codes: " + json + "}");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Save or update citation appeal
	public void appeal(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonObject json = new JsonObject();
		json.addProperty("success", false);

		int ownerId = 0;
		User user = User.getCurrentUser();

		try {

			user = User.getCurrentUser();

			String strOwnerId = request.getParameter("owner_id");

			if (strOwnerId != null && strOwnerId.length() > 0) {
				ownerId = Integer.parseInt(strOwnerId);
			}

			if (user == null
					|| (!user.hasPermission(User.PL_OWNER_MANAGE) && (!user
							.hasPermission(User.PL_CITATION_MANAGE)))
					|| (ownerId == 0 && !user
							.hasPermission(User.PL_CITATION_MANAGE))) {
				json.addProperty("msg",
						"You don't have permission to perform this action.");
				response.getOutputStream().print(gson.toJson(json));
				return;
			}

			Citation citation = new Citation(Integer.parseInt(request
					.getParameter("citation_id")));
			CitationAppeal appeal = citation.getAppeal();

			String xaction = request.getParameter("xaction");
			if (xaction.equals("start")) {
				if (appeal == null) {
					appeal = new CitationAppeal();
					appeal.citation_id = citation.citation_id;
				}
				appeal.name = request.getParameter("appeal_name");
				appeal.email = request.getParameter("appeal_email");
				appeal.phone = request.getParameter("appeal_phone");
				appeal.address = request.getParameter("appeal_address");
				appeal.city = request.getParameter("appeal_city");
				appeal.state_id = request.getParameter("appeal_state_id");
				appeal.zip = request.getParameter("appeal_zip");
				appeal.reason = request.getParameter("appeal_reason");
				appeal.status = request.getParameter("appeal_status");
				appeal.decision_reason = request
						.getParameter("appeal_decision_reason");

				// all data required
				if (appeal.name == null || appeal.name.length() == 0
						|| appeal.email == null || appeal.email.length() == 0
						|| appeal.address == null
						|| appeal.address.length() == 0 || appeal.city == null
						|| appeal.city.length() == 0 || appeal.state_id == null
						|| appeal.state_id.length() == 0 || appeal.zip == null
						|| appeal.zip.length() == 0 || appeal.reason == null
						|| appeal.reason.length() == 0) {
					json.addProperty("msg",
							"All fields are required to create an appeal.");
				} else {
					if (appeal.commit()) {

						MailerTask task = new MailerTask(); // send message
															// (email) when
															// appeal is update
						task.setMessagePreparator(new AppealUpdate(citation,
								appeal));
						Executor.getIntance().addTask(task);
						// change citation status to disputed and update
						citation.status_id = Citation.CS_DISPUTED;
						citation.commit();

						json.addProperty("success", true);
						json.add("appeal", gson.toJsonTree(appeal));
						json.add("citation", gson.toJsonTree(citation));
					}
				}
			} else if (appeal != null) {
				if (xaction.equals("decision")) {
					appeal.status = request
							.getParameter("appeal_decision_status");

					appeal.decision_date = new Timestamp(
							System.currentTimeMillis());
					appeal.decision_reason = request
							.getParameter("appeal_decision_reason");

					if (appeal.commit()) {
						json.addProperty("success", true);
						json.add("appeal", gson.toJsonTree(appeal));
					}
				}
			} else {
				json.addProperty("msg", "No appeal found for this citation.");
			}
		} catch (Exception e) {
			json.addProperty("msg", "Error with appeal: " + e.getMessage());
		}

		response.getOutputStream().print(gson.toJson(json));
	}
}
