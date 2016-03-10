package com.cambiolabs.citewrite.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;

import com.cambiolabs.citewrite.db.QueryBuilderTable.JoinType;


public class QueryBuilder
{
	private ArrayList<QueryBuilderTable> tables = new ArrayList<QueryBuilderTable>();
	private ArrayList<String> fields = new ArrayList<String>();
	private ArrayList<String> sumFields = new ArrayList<String>();
	private ArrayList<String> groupBy = new ArrayList<String>();
	private DBFilterList where = null;
	private DBFilterList having = null;
	private String strWhere = null;
	private int start = 0;
	private int max = 0;
	private String orderBy = null;
	private String orderDir = null;

	
	public QueryBuilder(String table, ArrayList<String> fields)
	{
		this.join(table, "");
		if(fields != null)
		{
			this.fields = fields;
		}
	}
	
	public QueryBuilder(String table)
	{
		this(table, null);
	}
	
	
	public QueryBuilder join(String table, String criteria)
	{
		this.tables.add(new QueryBuilderTable(table, criteria, JoinType.LEFT_JOIN));
		return this;
	}
	
	public QueryBuilder join(String table, String criteria, JoinType joinType)
	{
		this.tables.add(new QueryBuilderTable(table, criteria, joinType.getType() != null ? joinType : JoinType.LEFT_JOIN));
		return this;
	}
	
	public QueryBuilder field(String field)
	{
		this.fields.add(field);
		
		return this;
	}
	
	public QueryBuilder sumField(String sumField)
	{
		if(sumField.split(" ").length > 1 ){
			ArrayList<String> params = new ArrayList<String>();
			
			for(int i=0; (i<sumField.split(" ").length) && (params.size())<2; i++){
				if(!sumField.split(" ")[i].isEmpty()){
					params.add(sumField.split(" ")[i]);
				}
			}
			
			sumField = "(CASE WHEN %s" + params.get(0) + "%s IS NULL THEN 0 ELSE  %s"+ params.get(0) +" END)%s "+ params.get(1);
		}
		
		this.sumFields.add(sumField);
		return this;
	}
	
	public QueryBuilder start(int start)
	{
		this.start = start;
		
		return this;
	}
	
	public QueryBuilder max(int max)
	{
		this.max = max;
		
		return this;
	}
	
	public QueryBuilder orderBy(String orderBy)
	{
		this.orderBy = orderBy;
		
		return this;
	}
	
	public QueryBuilder orderDir(String dir)
	{
		this.orderDir = dir;
		
		return this;
	}
	
	public QueryBuilder groupBy(String groupBy)
	{
		groupBy(groupBy, false);
		return this;
	}
	
	public QueryBuilder groupBy(String groupBy, boolean firstGroup)
	{
		
		if((isMySQL()) && firstGroup){
			this.groupBy.add(groupBy);
		}else{
			if(firstGroup){
				this.groupBy.add(0, groupBy);
			}else{
				this.groupBy.add(groupBy);
			}
			
		}

		return this;
	}
	
	public QueryBuilder where(DBFilterList where)
	{
		this.where = where;
		
		return this;
	}
	
	public QueryBuilder having(DBFilterList having)
	{
		this.having = having;
		
		return this;
	}
	
	public QueryBuilder where(String where)
	{
		this.strWhere = where;
		
		return this;
	}
	
	public boolean isMySQL()
	{
		return (DBConnection.getDatabaseType() == DBConnection.DatabaseType.MYSQL);
	}
	
	public boolean isOracle()
	{
		return (DBConnection.getDatabaseType() == DBConnection.DatabaseType.ORACLE);
	}
	
	public boolean isSQLServer()
	{
		return (DBConnection.getDatabaseType() == DBConnection.DatabaseType.SQLSERVER);
	}
	
	public DBConnection query()
	{
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			String filter = "";
			String havingFilter = "";
			String order = "";
			String group = "";
			String limit = "";
			String sql = "SELECT ";
			String fieldList = "";
			String tableList = "";
			
			int size = this.fields.size();
			if(size == 0)
			{
				fieldList = "* ";
			}
			else
			{
				for(int i = 0; i < size; i++)
				{
					String field = this.fields.get(i);
					fieldList += field;
					
					if(i < size-1)
					{
						fieldList += ", ";
					}
				}
			}
			
			size = this.groupBy.size();
			for(int i = 0; i < size; i++)
			{
				String field = this.groupBy.get(i);
				group += field;
				
				if(i < size-1)
				{
					group += ", ";
				}
			}
			
			//add tables to select
			size = this.tables.size();
			tableList = this.tables.get(0).getTable();
			
			for(int i = 1; i < size; i++)
			{
				String crit = this.tables.get(i).getCriteria();
				if(crit.length() > 0)
				{
					crit = " on "+crit;
				}
					
				tableList += String.format(" %s %s %s", this.tables.get(i).getType().getType(), this.tables.get(i).getTable(), crit);
			}
			
			if(this.where != null && !this.where.isEmpty())
			{
				filter = " WHERE " + this.where.toString();
			}
			
			if(this.having != null && !this.having.isEmpty())
			{
				havingFilter = " HAVING  " + this.having.toString();
			}
			
			if(this.strWhere != null && this.strWhere.length() > 0)
			{
				if(this.where != null && !this.where.isEmpty())
				{
					filter += " AND " + this.strWhere;
				}
				else
				{
					filter = " WHERE " + this.strWhere;
				}
			}
			
			if(this.orderBy != null && this.orderDir != null)
			{
				order = String.format(" order by %s %s", this.orderBy, this.orderDir);
			}
			
			if((this.groupBy != null) && (!this.groupBy.isEmpty()))
			{
				group = String.format(" group by %s", group);
			}
			
			if(connection.isMySQL())
			{
				
				if(this.max > 0)
				{
					limit = " limit " + this.start + ", " + this.max;
				}
								
				sql = String.format("SELECT %s from %s %s %s %s %s %s",
						fieldList,
						tableList,
						filter,
						group,
						havingFilter,
						order,
						limit);
			}
			else if(connection.isOracle())
			{
				String sqlFormat = "SELECT "+fieldList+" from %s %s %s %s %s %s";
				if(max > 0)
				{
					String outerlist = "";
					size = this.fields.size();
					for(int i = 0; i < size; i++)
					{
						String field = this.fields.get(i);
						
						int start = field.lastIndexOf(' '); //check for aliases
						if(start > -1) //remove any outer lists
						{
							field = field.substring(start+1);
						}
						
						start = field.indexOf('.');
						if(start > -1)
						{
							outerlist += field.substring(start+1);
						}
						else
						{
							outerlist += field;
						}
						
						if(i < size-1)
						{
							outerlist += ", ";
						}
					}
					
					sqlFormat = "SELECT "+outerlist+", rn from (SELECT "+fieldList+", ROWNUM-1 rn from %s %s %s %s %s) %s";
					limit = " WHERE rn BETWEEN " + start + " AND " + (start+max);
				}
				
				sql =  String.format(sqlFormat,
						tableList, 
						filter,
						group != "" ? group +", ROWNUM ": "",
						havingFilter,
						order,
						limit);
			}
			else if(connection.isSQLServer())
			{
				if(max > 0)
				{
					String sqlFormat = "SELECT * from ("+
											"SELECT "+fieldList+", row_number() OVER(%s) as rownum from %s %s %s %s" +
										") as temptable %s";
					limit = " WHERE temptable.rownum BETWEEN " + start + " AND " + (start+max);
					
					sql =  String.format(sqlFormat, 
							order,
							tableList, 
							filter,	
							group,
							havingFilter,
							limit);
				}
				else
				{
				
					sql = String.format("SELECT %s from %s %s %s %s %s",
							fieldList,
							tableList, 
							filter,
							order,
							group,
							havingFilter);
				}
			}
			
			PreparedStatement pst = connection.prepare(sql);
			
			if(this.where != null && !this.where.isEmpty())
			{
				this.where.set(pst);
			}
			
			if(this.having != null && !this.having.isEmpty())
			{
				this.having.set(pst);
			}
			
			if(connection.query(pst))
			{
				return connection;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ArrayList<Hashtable<String, String>> select()
	{
		ArrayList<Hashtable<String, String>> rv = new ArrayList<Hashtable<String, String>>();
		
		DBConnection connection = null;
		try
		{
			connection = this.query();
			if(connection != null)
			{
				Hashtable<String, String> row = null;
				while((row = connection.fetchAssoc()) != null)
				{
					rv.add(row);
				}
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
		
		return rv;
	}
	
	public int count(){
		return this.count(null);
	}
	
	public int count(String countField)
	{
		int rv = 0;
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			String filter = "";
			String sql = "";
			String tableList = "";
			String groupBy = "";
			String having  = "";
			//add tables to select
			int size = this.tables.size();
			tableList = this.tables.get(0).getTable();
			
			if(countField == null){
				countField = "*";
			}
			
			
			for(int i = 1; i < size; i++)
			{
				String crit = this.tables.get(i).getCriteria();
				if(crit.length() > 0)
				{
					crit = " on "+crit;
				}
					
				tableList +=  String.format(" %s %s %s", this.tables.get(i).getType().getType(), this.tables.get(i).getTable(), crit);
			}
			
			if(this.where != null && !this.where.isEmpty())
			{
				filter = " WHERE " + this.where.toString();
			}
			
			if(this.groupBy != null && !this.groupBy.isEmpty())
			{
				groupBy = String.format(" group by %s", this.groupBy.get(0));
			}	
			
			if(this.having != null && !this.having.isEmpty())
			{
				having = " HAVING  " + this.having.toString();
			}

			sql = String.format("SELECT count(%s) from %s %s %s %s ",
					countField,
					tableList,
					filter,
					groupBy,
					having);
			
			PreparedStatement pst = connection.prepare(sql);
			
			if(this.where != null && !this.where.isEmpty())
			{
				this.where.set(pst);
			}
			
			if(this.having != null && !this.having.isEmpty())
			{
				this.having.set(pst);
			}
			
			if(connection.query(pst))
			{
				ResultSet rs = connection.getResultSet();
				if(rs != null)
				{
					if(rs.next())
					{
						rv = rs.getInt(1);
					}
				}
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
		
		return rv;
	}
	
	public ArrayList<Hashtable<String, String>> sum()
	{
		ArrayList<Hashtable<String, String>> rv = new ArrayList<Hashtable<String, String>>();
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			String filter = "";
			String sql = "";
			String tableList = "";
			String fieldList = "";
			String groupBy = "";
			String having  = "";
			int size;
			
			//add tables to select
			size = this.sumFields.size();
			
			ArrayList<String> sumCurrentFields =  new ArrayList<String>();
			for(int i = 0; i < size; i++)
			{
				String field = this.sumFields.get(i);
				String opSum = "Sum(";
				String closeParentheses = ")";
				if(this.groupBy != null && !this.groupBy.isEmpty()){
					opSum = "";
					closeParentheses = "";
				}
				
				field = String.format(field, opSum, closeParentheses, opSum, closeParentheses);
				if(!sumCurrentFields.contains(field)){
					sumCurrentFields.add(field);
					fieldList += String.format(" %s%s", field, sumFields.size() - 1 == i ? "" : ",");	
				}
				
				
			}
			
			//add tables to select
			size = this.tables.size();
			tableList = this.tables.get(0).getTable();
			
			for(int i = 1; i < size; i++)
			{
				String crit = this.tables.get(i).getCriteria();
				if(crit.length() > 0)
				{
					crit = " on "+crit;
				}
					
				tableList +=  String.format(" %s %s %s", this.tables.get(i).getType().getType(), this.tables.get(i).getTable(), crit);
			}
			
			size = this.groupBy.size();
			for(int i = 0; i < size; i++)
			{
				String field = this.groupBy.get(i);
				groupBy += field;
				
				if(i < size-1)
				{
					groupBy += ", ";
				}
			}
			
			if(this.where != null && !this.where.isEmpty())
			{
				filter = " WHERE " + this.where.toString();
			}
			
			if(this.groupBy != null && !this.groupBy.isEmpty())
			{
				groupBy = String.format(" group by %s", groupBy);
			}	
			
			if(this.having != null && !this.having.isEmpty())
			{
				having = " HAVING  " + this.having.toString();
			}
					
			if(fieldList.isEmpty()){
				return null;
			}else{
				sql = String.format("SELECT %s from %s %s %s %s",
						fieldList,
						tableList,
						filter,
						groupBy,
						having);
			}
			
			if(this.groupBy != null && !this.groupBy.isEmpty()){
				
				size = this.sumFields.size();
				fieldList = "";
				sumCurrentFields =  new ArrayList<String>();
				for(int i = 0; i < size; i++)
				{
					String field = this.sumFields.get(i);
					String alias = field.split("%s")[field.split("%s").length-1];
					field = alias;
					field = "Sum(T."+field.trim().replace("'", "")+") "+field.trim();
					if(!sumCurrentFields.contains(field)){
						sumCurrentFields.add(field);
						fieldList += String.format(" %s%s", field, sumFields.size() - 1 == i ? "" : ",");	
					}
				}
				
				sql = String.format("SELECT %s FROM (" + sql + ") T",fieldList);
			
			}
			
			PreparedStatement pst = connection.prepare(sql);
			
			if(this.where != null && !this.where.isEmpty())
			{
				this.where.set(pst);
			}
			
			if(this.having != null && !this.having.isEmpty())
			{
				this.having.set(pst);
			}
			
			if(connection.query(pst))
			{

				Hashtable<String, String> row = null;
				while((row = connection.fetchAssoc()) != null)
				{
					rv.add(row);
				}
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
		
		return rv;
	}
}
