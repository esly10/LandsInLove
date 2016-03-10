package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Comparator;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

import com.cambiolabs.citewrite.db.DBObject;

public class Code extends DBObject implements Comparator<Code>
{
	private static Object _lock = new Object();
	
	public static final String CT_STATE = "state";
	public static final String CT_COLOR = "color";
	public static final String CT_COMMENT = "comment";
	public static final String CT_LOCATION = "location";
	public static final String CT_MAKE = "make";
	public static final String CT_VIOLATION = "violation";
	
	public static int SORT_ANY = 0;
    public static int SORT_ID = 1;
    public static int SORT_INT_ID = 2;
    public static int SORT_DESCRIPTION = 3;
    
    @Expose public String type = null;
    @Expose public String codeid = null;
    @Expose public String description = null;
    @Expose public String fine_amount = null;
    @Expose public String fine_type = null;
	@Expose public int is_overtime = 0;
	@Expose public int is_other = 0;
	
	public int hashcode = 0;
	
	private int sortType = SORT_ANY;
	
	public Code()
	{
		super("codes", "codeid");
	}
	
	public Code(String type, String codeid)  throws UnknownObjectException
	{
		super("codes", "codeid");
		
		this.type = type;
		this.codeid = codeid;
		this.populate();
	}
	
	public Code(String type, String codeid, String description)
	{
		this(type, codeid, description, null, null, 0, 0);
	}
	
	public Code(String type, String codeid, String description, int is_other)
	{
		this(type, codeid, description, null, null, 0, is_other);
	}
	
	public Code(String type, String codeid, String description, String fine_amount, String fine_type, int is_overtime, int is_other)
	{
		super("codes", "codeid");
		
		this.type = type;
		this.codeid = codeid;
		this.description = description;
		this.fine_amount = fine_amount;
		this.fine_type = fine_type;
		this.is_overtime = is_overtime;
		this.is_other = is_other;
	}
		
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setCodeId(String codeid) {
		this.codeid = codeid;
	}
	public String getCodeId() {
		return codeid;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setFineAmount(String fineAmount) {
		this.fine_amount = fineAmount;
	}
	public String getFineAmount() {
		return fine_amount;
	}
	public void setFineType(String fineType) {
		this.fine_type = fineType;
	}
	public String getFineType() {
		return this.fine_type;
	}
	public boolean isOvertime()
	{
		return (this.is_overtime == 1);
	}
	public int compare(Code c1, Code c2)
    {
        if(this.sortType == Code.SORT_ID)
        {
            if(c1.codeid != null && c1.codeid.length() > 0)
            {
                return c1.codeid.compareTo(c2.codeid);
            }
        }
        else if(this.sortType == Code.SORT_INT_ID)
        {
            try
            {
                int id1 = Integer.parseInt(c1.codeid, 10);
                int id2 = Integer.parseInt(c2.codeid, 10);
                
                return (id1-id2);
            }
            catch(Exception e)
            {
                return 0;
            }
        }
        else if(this.sortType == Code.SORT_DESCRIPTION)
        {
            return c1.description.compareTo(c2.description);
        }
        
        if(c1.codeid != null && c1.codeid.length() > 0)
        {
            return c1.codeid.compareTo(c2.codeid);
        }
         
        return c1.description.compareTo(c2.description);
    }
    
    public boolean isOther()
    {
    	return (this.is_other == 1);
    }
  
    public boolean commit()
	{
		synchronized(_lock) // need to make sure we are getting a unique id and only adding it
		{
			DBConnection connection = null;
			try
			{
				Integer ts = new Integer((int)(System.currentTimeMillis()/1000));
				
				connection = new DBConnection();
				
				String sql = "INSERT INTO "+this.tableName + "(type, codeid, description, fine_amount, fine_type, is_overtime, is_other, added, updated, hashcode) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
				PreparedStatement pst = connection.prepare(sql);
				if(pst != null)
				{
					
					int hashcode = 0;
					if(this.type.equalsIgnoreCase("violation"))
					{
						String[] hashes = {
								this.type,
								this.codeid,
								this.description,
								this.fine_type,
								this.fine_amount,
								String.valueOf(this.is_overtime)};
						
						hashcode = Arrays.hashCode(hashes);
					}
					else
					{
						String[] hashes = {
								this.type,
								this.codeid,
								this.description,
								String.valueOf(this.is_other)};
						
						hashcode = Arrays.hashCode(hashes);
					}

					Object params[] = new Object[] 
					        {
								this.type,
								this.codeid,
								this.description,
								this.fine_amount,
								this.fine_type,
								this.is_overtime,
								this.is_other,
								ts,
								ts,
								String.valueOf(hashcode)
							};
					
					
					return connection.bindExec(pst, params, params.length);
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
	}
    
    public boolean delete()
	{
    	Integer ts = new Integer((int)(System.currentTimeMillis()/1000));
    	
		String sql = "UPDATE "+this.tableName+" set type='', codeid='', description='', fine_type='', fine_amount='', added=0, updated=? where codeid=? AND type=? AND hashcode=?";
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement ps = null;
			if ((ps = connection.prepare(sql)) != null)
			{
				Object[] updateBindings = {ts, this.codeid, this.type, this.hashcode };
				return connection.bindExec(ps, updateBindings, updateBindings.length);
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
}
