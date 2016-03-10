package com.cambiolabs.citewrite.data;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.QueryBuilder;
import com.cambiolabs.citewrite.db.QueryBuilderTable.JoinType;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.InvoiceItem;
import com.cambiolabs.citewrite.util.DateParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

public class Report extends DBObject
{
	
	public static final int CITATION_TYPE = 0;
	public static final int MPERMIT_TYPE = 1;
	public static final int FINANCIA_CITATION__TYPE = 2;
	public static final int FINANCIA_PERMIT__TYPE = 3;
	public static final String EXPORT = "export";
	public static final String RUN = "run";
	
	@Expose public int report_id = 0;
    @Expose public String name = null;
    @Expose public int report_type = 0;
    public String query_fields = null;
    public String query_criterias = null;
    
    @Expose public LinkedList<ReportField> reportFields = new LinkedList<ReportField>();
    @Expose public LinkedList<ReportCriteria> reportCriterias = new LinkedList<ReportCriteria>();
    
	public Report()
	{
		super("report", "report_id", new String[]{"reportFields","reportCriterias"});
	}
	
	public Report(int id) throws UnknownObjectException
	{
		super("report", "report_id", new String[]{"reportFields","reportCriterias"});
		this.report_id = id;
		
		if(this.report_id > 0)
		{
			this.populate();
			this.populateFields();
		}
	}
	
	public void populateFields()
	{
		if(this.query_fields != null && this.query_fields.length() > 0)
		{
			Type listType = new TypeToken<List<ReportField>>() {}.getType();
			Gson gson = new Gson();
			this.reportFields = gson.fromJson(this.query_fields, listType);
			
		}
		
		if(this.query_criterias != null && this.query_criterias.length() > 0)
		{
			Type listType = new TypeToken<List<ReportCriteria>>() {}.getType();
			Gson gson = new Gson();
			this.reportCriterias = gson.fromJson(this.query_criterias, listType);
			
		}
	}
	
	public ArrayList<? extends DBObject> get(int start, int max, String orderBy, DBFilterList filterList)
	{
		@SuppressWarnings("unchecked")
		ArrayList<Report> list = (ArrayList<Report>)super.get(start, max, orderBy, filterList);
		
		for(Report r: list)
		{
			r.populateFields();
		}
		
		return list;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	public void setQuery_fields(String query_fields) {
		this.query_fields = query_fields;
	}
	public String getQuery_fields() {
		return this.query_fields;
	}
	
	public void clearFields()
	{
		this.reportFields.clear();
	}

	public void clearCriterias()
	{
		this.reportCriterias.clear();
	}
	
	public void addField(ReportField field)
	{
		this.reportFields.add(field);
	}
	
	public void addCriteria(ReportCriteria field)
	{
		this.reportCriterias.add(field);
	}
	
	public int getReport_type() {
		return report_type;
	}

	public void setReport_type(int report_type) {
		this.report_type = report_type;
	}

	public boolean commit()
	{       
		
		Type fieldListType = new TypeToken<List<ReportField>>() {}.getType();
		Type criteriaListType = new TypeToken<List<ReportCriteria>>() {}.getType();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create(); // Or use new GsonBuilder().create();
	    this.query_fields = gson.toJson(this.reportFields, fieldListType);
	    this.query_criterias = gson.toJson(this.reportCriterias, criteriaListType);

	    if(this.query_criterias.intern() == "[null]") 
	    { 
	    	this.query_criterias = null;
	    }
	    	
		return super.commit();
		
	}
	
	public QueryBuilder buildQuery(String sort, String dir, int start, int limit, String typeBuildQuery) throws Exception{
		
		String reportTable = null;
		
		if(report_type == CITATION_TYPE)
		{
			reportTable = "citation cite";
			
		}else if (report_type == MPERMIT_TYPE)
		{
			reportTable = "mpermit mper";
			
		} else if ((report_type == FINANCIA_CITATION__TYPE) || (report_type == FINANCIA_PERMIT__TYPE)){
			reportTable = "invoice";
		}
		
		QueryBuilder qb = new QueryBuilder(reportTable);
		DBFilterList filter = new DBFilterList();
		DBFilterList havingFilter = new DBFilterList();
		List <String> attrTablesList = new ArrayList<String>();
		List <String> attrTypeTablesList = new ArrayList<String>();
		List <String> attrFinaTablesList = new ArrayList<String>();
		boolean addField = false;
		
		for(ReportField field: this.reportFields)
		{	
			if(typeBuildQuery.equals(EXPORT) && !field.export)
			{
				continue;
			}else if (typeBuildQuery.equals(RUN) && !field.view)
			{
				continue;
			}
			
			addField = addField | true;
			if(field.name.startsWith("attr-"))
			{
				String reffield = field.name.substring(5);
				
				if(reffield.endsWith("_id"))
				{
					reffield = reffield.substring(0, reffield.length()-3);
					
				}
				
				if(!attrTablesList.contains(reffield)){
					attrTablesList.add(reffield);
				}
				
				String column = "";
				
				if(field.name.endsWith("_id"))
				{
					column = "attr"+attrTablesList.lastIndexOf(reffield)+".value_id "+reffield+"_id";
					qb.groupBy("attr"+attrTablesList.lastIndexOf(reffield)+".value_id");
				}
				else
				{
					column = "attr"+attrTablesList.lastIndexOf(reffield)+".value "+reffield+"";
					qb.groupBy("attr"+attrTablesList.lastIndexOf(reffield)+".value");
				}
				
				qb.field(column);
				
			}else if(field.name.startsWith("attrType-"))
			{
				String reffield = field.name.substring(9);
				
				if(reffield.endsWith("_id"))
				{
					reffield = reffield.substring(0, reffield.length()-3);
					
				}
				
				if(!attrTypeTablesList.contains(reffield)){
					attrTypeTablesList.add(reffield);
				}
				
				String column = "";
				
				if(field.name.endsWith("_id"))
				{
					column = "attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value_id "+reffield+"_id";
					qb.groupBy("attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value_id");
				}
				else
				{
					column = "attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value "+reffield+"";
					qb.groupBy("attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value");
				}
				
				qb.field(column);
			}else if (field.name.startsWith("attrFina-"))
			{
				String reffield = field.name.substring(9);
				
				if(!attrFinaTablesList.contains(reffield)){
					attrFinaTablesList.add(reffield);
				}
				
				String column = "";
			
				if(reffield.equals("n"+InvoiceItem.TYPE_REFUND+"rec")){
					
					column = "attrFina"+attrFinaTablesList.lastIndexOf(reffield)+".amount";
					qb.field("(CASE WHEN SUM("+column+") IS NULL THEN 0 ELSE SUM("+column+") END) "+reffield+"");	
					qb.sumField("SUM("+column+") "+reffield+"");	
					
				}else{
					
					column = "attrFina"+attrFinaTablesList.lastIndexOf(reffield)+".amount";
					qb.field("(CASE WHEN "+column+" IS NULL THEN 0 ELSE "+column+" END) "+reffield+"");	
					qb.groupBy(column);
					
					if((reffield.equals("n"+InvoiceItem.TYPE_LATE_FEE+"rec"))||(reffield.equals("n"+InvoiceItem.TYPE_VOID+"rec"))){
						column = "attrFina"+attrFinaTablesList.lastIndexOf(reffield)+".amount";
						qb.sumField(column+" "+reffield+"");	
					}

				}

			}else
			{
				String column  = field.name.replace('-', '.');
				
				if (column.equals("cite.date_time")){
					column = "cite.citation_date";
					qb.field("(CASE WHEN "+column+" IS NULL THEN 0 ELSE "+column+" END) citation_date");	
					qb.groupBy(column);
					
				}else if(column.equals("cite.violation_amount")){
					qb.field("(CASE WHEN "+column+" IS NULL THEN 0 ELSE "+column+" END) violation_amount");	
					qb.groupBy(column);
					
				}else if(column.equals("invoice.amount")){
					qb.field("(CASE WHEN "+column+" IS NULL THEN 0 ELSE "+column+" END) amount");	
					qb.groupBy(column);
					
				}else{
					qb.field(column);	
					qb.groupBy(column);
					
				}

				if((report_type == FINANCIA_CITATION__TYPE || report_type == FINANCIA_PERMIT__TYPE) && (column.equals("cite.violation_amount") || column.equals("invoice.amount"))){
					qb.sumField(column+" "+column.replaceAll("cite.|mper.|invoice.", "")+"");	
				}
			
			}
		}
		
		if(!addField){return null;}
		
		String logicOperator = null;	

		for(ReportCriteria reportCriteria: this.reportCriterias)
		{			
						
			if(reportCriteria.value != null ||  ((reportCriteria.value == null) && reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.IS_NULL)||reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.NOT_NULL)))
			{
				
				if(reportCriteria.name.startsWith("attr-"))
				{  
					String reffield = reportCriteria.name.substring(5);
					
					if(reffield.endsWith("_id"))
					{
						reffield = reffield.substring(0, reffield.length()-3);
						
					}
					
					if(!attrTablesList.contains(reffield)){
						attrTablesList.add(reffield);
					}
					
					if(reportCriteria.name.endsWith("_id"))
					{
						addFilter(filter,new DBFilter("attr"+attrTablesList.lastIndexOf(reffield)+".value_id", reportCriteria.operator, reportCriteria.value), logicOperator);

					}
					else
					{
						addFilter(filter,new DBFilter("attr"+attrTablesList.lastIndexOf(reffield)+".value", reportCriteria.operator, reportCriteria.value), logicOperator);
						
					}
					
				}else if(reportCriteria.name.startsWith("attrType-"))
				{
					String reffield = reportCriteria.name.substring(9);
					
					if(reffield.endsWith("_id"))
					{
						reffield = reffield.substring(0, reffield.length()-3);
						
					}
					
					if(!attrTypeTablesList.contains(reffield)){
						attrTypeTablesList.add(reffield);
					}
					
					if(reportCriteria.name.endsWith("_id"))
					{
						addFilter(filter,new DBFilter("attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value_id", reportCriteria.operator, reportCriteria.value), logicOperator);

					}
					else
					{
						addFilter(filter,new DBFilter("attrType"+attrTypeTablesList.lastIndexOf(reffield)+".value", reportCriteria.operator, reportCriteria.value), logicOperator);
						
					}
					
				}else if(reportCriteria.name.startsWith("attrFina-"))
				{
					/****/
					String reffield = reportCriteria.name.substring(9);
					
					if(!attrFinaTablesList.contains(reffield)){
						attrFinaTablesList.add(reffield);
					}
					
					if(!("n"+InvoiceItem.TYPE_REFUND+"rec").equals(reffield)){/*Hay que conciderar los operadores que no aplican*/
						addFilter(filter,new DBFilter("attrFina"+attrFinaTablesList.lastIndexOf(reffield)+".amount", reportCriteria.operator, reportCriteria.value), logicOperator);
						
					}else{
						havingFilter.add(new DBFilter("SUM("+"attrFina"+attrFinaTablesList.lastIndexOf(reffield)+".amount"+")", reportCriteria.operator, reportCriteria.value), filter.getIndex());
						
					}
					
				}else
				{
					String column  = reportCriteria.name.replace('-', '.');
					Object value = reportCriteria.value;
					String betweenValue = reportCriteria.betweenValue;
					Timestamp end = null;
					
					if (column.equals("cite.date_time")){
						column = "cite.citation_date";
					}
					
					if(reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.IS_NULL)||reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.NOT_NULL)){
						addFilter(filter,new DBFilter(column, reportCriteria.operator, null), logicOperator);
						
					}
					else if(value != null)
					{
						
						if(column.equals("cite.citation_date") || column.equals("cite-violation_start") || column.equals("cite-violation_end") || column.equals("cite.violation_end") || column.equals("cite.violation_start"))
						{
							
							DateParser dp = new DateParser("MM/dd/yyyy").parse((String)value);
							value = dp.firstHour().getTimestamp();
							
							if(reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.BETWEEN)||reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.NOT_BETWEEN))
							{
								end = dp.parse(betweenValue).lastHour().getTimestamp();
								
							}else{
								
								if(reportCriteria.operator.trim().equalsIgnoreCase(DBFilter.EQ)){
									reportCriteria.operator = DBFilter.BETWEEN;
									end = dp.parse((String)reportCriteria.value).lastHour().getTimestamp();	
									
								}
								
							}
						}
						
						addFilter(filter,new DBFilter(column, reportCriteria.operator, value, end), logicOperator);
					}
				}
			}
		
			logicOperator = reportCriteria.logicOperator;
			
		}
		
		
		switch (report_type) {
		
		case CITATION_TYPE:	
			qb.join("users", "cite.officer_id=users.officer_id");
			for(String table : attrTablesList){
				if(table != null){
					qb.join("citation_attribute" +" attr"+attrTablesList.indexOf(table), "attr"+attrTablesList.indexOf(table)+".citation_id=cite.citation_id AND attr"+attrTablesList.indexOf(table)+".field_ref='"+table+"'");
				}
			}
			break;
			
		case MPERMIT_TYPE:
			qb.join("owner", "mper.owner_id=owner.owner_id");
			qb.join("mpermit_type", "mper.mpermit_type_id=mpermit_type.mpermit_type_id");
			for(String table : attrTablesList){
				if(table != null){
					qb.join("mpermit_attribute" +" attr"+attrTablesList.indexOf(table), "attr"+attrTablesList.indexOf(table)+".mpermit_id=mper.mpermit_id AND attr"+attrTablesList.indexOf(table)+".name='"+table+"'");
				}
			}
			for(String table : attrTypeTablesList){
				if(table != null){
					qb.join("mpermit_type_attribute" +" attrType"+attrTypeTablesList.indexOf(table), "attrType"+attrTypeTablesList.indexOf(table)+".mpermit_type_id=mpermit_type.mpermit_type_id AND attrType"+attrTypeTablesList.indexOf(table)+".name='"+table+"'");
				}
			}
			break;
			
		case FINANCIA_CITATION__TYPE:
			qb.join("owner", "invoice.owner_id=owner.owner_id");
			qb.join("citation cite", "invoice.reference_id=cite.citation_id AND invoice.type = 2", JoinType.JOIN);
			qb.join("users", "cite.officer_id=users.officer_id");

			for(String table : attrTablesList){
				if(table != null){
					qb.join("citation_attribute" +" attr"+attrTablesList.indexOf(table), "attr"+attrTablesList.indexOf(table)+".citation_id=cite.citation_id AND attr"+attrTablesList.indexOf(table)+".field_ref='"+table+"'");
				}
			}
			
			for(String table : attrFinaTablesList){
				if(table != null){
					qb.join("invoice_item" +" attrFina"+attrFinaTablesList.indexOf(table), "attrFina"+attrFinaTablesList.indexOf(table)+".invoice_id=invoice.invoice_id AND attrFina"+attrFinaTablesList.indexOf(table)+".type='"+table.replace("rec", "").replace("n", "")+"'");
				}
			}
			qb.groupBy("invoice.invoice_id", true);
			break;
			
		case FINANCIA_PERMIT__TYPE:
			qb.join("owner", "invoice.owner_id=owner.owner_id");
			qb.join("mpermit mper", "invoice.reference_id=mper.mpermit_id AND invoice.type = 1", JoinType.JOIN);
			qb.join("mpermit_type", "mper.mpermit_type_id=mpermit_type.mpermit_type_id");
	
			for(String table : attrTablesList){
				if(table != null){
					qb.join("mpermit_attribute" +" attr"+attrTablesList.indexOf(table), "attr"+attrTablesList.indexOf(table)+".mpermit_id=mper.mpermit_id AND attr"+attrTablesList.indexOf(table)+".name='"+table+"'");
				}
			}
			
			for(String table : attrTypeTablesList){
				if(table != null){
					qb.join("mpermit_type_attribute" +" attrType"+attrTypeTablesList.indexOf(table), "attrType"+attrTypeTablesList.indexOf(table)+".mpermit_type_id=mpermit_type.mpermit_type_id AND attrType"+attrTypeTablesList.indexOf(table)+".name='"+table+"'");
				}
			}
			
			for(String table : attrFinaTablesList){
				if(table != null){
					qb.join("invoice_item" +" attrFina"+attrFinaTablesList.indexOf(table), "attrFina"+attrFinaTablesList.indexOf(table)+".invoice_id=invoice.invoice_id AND attrFina"+attrFinaTablesList.indexOf(table)+".type='"+table.replace("rec", "").replace("n", "")+"'");
				}
			}
			
			qb.groupBy("invoice.invoice_id", true);
			break;
			
		}
		
		qb.orderBy(sort).orderDir(dir).where(filter).having(havingFilter).start(start);
		
		if(limit != 0){
			qb.max(limit);
		}

		return qb;
	}
	
	private void addFilter (DBFilterList filters, DBFilter filter, String logicOperator)
	{
		
		if((logicOperator == null) || logicOperator.equals("and") || (logicOperator.equals("done") )){
			filters.add(filter);
			
		}else if (logicOperator.equals("or")){
			filters.addOr(filters.get(filters.size()-1), filter);
			
		}
	
	}
	
}
