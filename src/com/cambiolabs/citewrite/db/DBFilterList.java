package com.cambiolabs.citewrite.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class DBFilterList extends ArrayList<DBFilter> 
{

	private static final long serialVersionUID = 1L;
	private int index = 1;
	
	public boolean add(DBFilter filter, int index)
	{
		this.index = index;
		return add(filter);
	}
	
	public boolean add(DBFilter filter)
	{
		super.add(filter);
		
		if(!filter.getOperator().trim().equalsIgnoreCase(DBFilter.NOT_NULL) && !filter.getOperator().trim().equalsIgnoreCase(DBFilter.IS_NULL)){
			filter.setIndex(index);
			if(filter.getOperator().trim().equalsIgnoreCase(DBFilter.BETWEEN) || filter.getOperator().trim().equalsIgnoreCase(DBFilter.NOT_BETWEEN)){
				++index; 
			}
			index++;
		}
		
		return true;
	}
	
	public void addAnd(DBFilter filter, DBFilter andFilter)
	{
		andFilter.setIndex(index);
		index++;
		filter.and(andFilter);
	}
	
	public void addOr(DBFilter filter, DBFilter orFilter)
	{
		orFilter.setIndex(index);
		index++;
		filter.or(orFilter);
	}

	
	public String toString()
	{
		String sql = "";
		for (Iterator<DBFilter> iter = this.iterator(); iter.hasNext(); ) {
		    DBFilter filter = iter.next();
		    
		    if(sql.length()>0){ sql += " AND "; }
		    
		    sql += filter.toString();
		}
		
		return sql;
	}
	
	public void set(PreparedStatement pst) throws SQLException
	{
		for (Iterator<DBFilter> iter = this.iterator(); iter.hasNext(); ) {
		    DBFilter filter = iter.next();
		    filter.set(pst);
		}
	}

	public int getIndex() {
		return index;
	}

}
