package com.cambiolabs.citewrite.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.Invoice;
import com.cambiolabs.citewrite.util.PermitWatcher;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.annotations.Expose;

public class Owner extends DBObject implements org.springframework.security.core.userdetails.UserDetails
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String STATUS_ACTIVE = "Active";
	public static String STATUS_INACTIVE = "Inactive";
	public static String STATUS_PENDING = "Pending";
	
	@Expose public int owner_id = 0;
	@Expose public String status = "Inactive"; //Active, Inactive, Pending
	@Expose public int type_id = 0;
	@Expose public String first_name = null;
	@Expose public String last_name = null;
	@Expose public String email = null;
	@Expose public String username = null;
	@Expose public String password = null; //MD5
	@Expose public String home_phone = null;
	@Expose public String mobile_phone = null;
	@Expose public String address = null;
	@Expose public String city = null;
	@Expose public String state_id = null;
	@Expose public String zip = null;
	@Expose public Timestamp create_date = null;
	@Expose public Timestamp update_date = null;
	@Expose public Timestamp password_updated = null;
	
	
	
	@Expose protected ArrayList<OwnerAttribute> extra = new ArrayList<OwnerAttribute>();
	
	public Owner()
	{
		super("owner", "owner_id");
	}
	
	public Owner(int id) throws UnknownObjectException
	{
		this();
		if(id > 0)
		{
			this.owner_id = id;
			this.status = null;
			this.populate();
		}
	}
	
	
	public Owner(HttpServletRequest request) throws UnknownObjectException
	{
		this();
		this.set(request);
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtra()
	{
		//check if there are any options
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("owner_id", "=", this.owner_id));
		
		OwnerAttribute attr = new OwnerAttribute();

		this.extra = (ArrayList<OwnerAttribute>)attr.get(0, 0, "", filter);
	}
	
	public ArrayList<OwnerAttribute> getExtras()
	{
		return this.extra;
	}
	
	public OwnerAttribute getAttribute(String name)
	{
		for(OwnerAttribute va: this.extra)
		{
			if(va.name.equals(name))
			{
				return va;
			}
		}
		
		return null;
	}
	
	public String getAttributeValue(OwnerField field)
	{
		OwnerAttribute va = this.getAttribute(field.name);
		
		if(va != null && va.value.length() > 0)
		{
			if(field.type.equals(OwnerField.TYPE_LIST))
			{
				for(FieldOption option: field.options)
				{
					if(option.id.equals(va.value))
					{
						return option.name;
					}
				}
			}
			else
			{
				return va.value;
			}
		}
		
		return "";
	}
	
	public void addAttribute(OwnerAttribute attr)
	{
		if(this.extra == null)
		{
			this.loadExtra();
		}
	
		if(attr.owner_attr_id == 0) //its a new attribute
		{
			this.extra.add(attr);
			return;
		}
		
		int size = this.extra.size();
		for(int i = 0; i < size; i++)
		{
			OwnerAttribute a = this.extra.get(i);
			if(a.owner_attr_id == attr.owner_attr_id)
			{
				this.extra.set(i, attr);
				return;
			}
		}
	}
	
	public static Owner getByUsername(String username)
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			Owner owner = new Owner();
			owner.status = null;
			owner.setUsername(username);
			
			if(conn.lookup(owner))
			{
				return owner;
			}
			
		}
		catch(Exception e)
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
		return null;
	}
	
	public void setUsername(String username) 
	{
		this.username = username;
	}
	
	public void set( HttpServletRequest request) 
	{
		this.first_name = request.getParameter("first_name");
		this.last_name = request.getParameter("last_name");
		this.address = request.getParameter("address");
		this.email = request.getParameter("email");
		this.home_phone = request.getParameter("home_phone");
		this.mobile_phone = request.getParameter("mobile_phone");
		this.city = request.getParameter("city");
		this.state_id = request.getParameter("state");
		this.zip = request.getParameter("zip");
		String password = request.getParameter("password");
		if(!password.equals("__Not_Changed__"))
		{
			this.setPassword(request.getParameter("password"));
		}
		this.username = request.getParameter("username");
		
		OwnerFields ofields = new OwnerFields();
		ArrayList<OwnerField> extrafieldsowner = ofields.getFields(false);
		
		for(OwnerField field: extrafieldsowner)
		{
			
			String value = request.getParameter(field.name);
			this.addAttribute(new OwnerAttribute(field.name,value));
			
		}
	}
	public String getUsername()
	{
		return this.username;
	}
	
	public String getPassword()
	{
		return this.password;
	}
	
	public String getNewPassword ()
	{
		String password = "";
		long milis = new java.util.GregorianCalendar().getTimeInMillis();
		Random r = new Random(milis);
		int i = 0;
		while ( i < 6)
		{
			char c = (char)r.nextInt(255);
			if ( (c >= '0' && c <='9') || (c >='A' && c <='Z') )
			{
				password += c;
				i ++;
			}
		}
		this.setPassword(password);
		if(this.commit())
		{
			return password;
		}
		return null;
	}
	
	public void setPassword(String password) 
	{
		this.password_updated = new Timestamp(System.currentTimeMillis());
		this.password = Util.getPasswordHash(password);
	}
		
	public int getOwnerId()
	{
		return owner_id;
	}

	public String getStatus()
	{
		return status;
	}

	public int getTypeId()
	{
		return type_id;
	}
	
	public OwnerType getOwnerType()
	{
		try {
			return new OwnerType(this.type_id);
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getType()
	{
		OwnerType type = this.getOwnerType();
		if(type != null)
		{
			return type.name;
		}
		return "Unknown";
	}

	public String getFirstName()
	{
		return first_name;
	}

	public String getLastName()
	{
		return last_name;
	}

	public String getEmail()
	{
		return email;
	}

	public String getHomePhone()
	{
		return home_phone;
	}

	public String getMobilePhone()
	{
		return mobile_phone;
	}

	public String getAddress()
	{
		return address;
	}

	public String getCity()
	{
		return city;
	}

	public String getStateId()
	{
		return state_id;
	}
	
	
	public String getState()
	{
		Code code = Codes.getCode(Code.CT_STATE, this.state_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;		
	}
	

	public String getZip()
	{
		return zip;
	}

	public Timestamp getCreateDate()
	{
		return create_date;
	}

	public Timestamp getUpdateDate()
	{
		return update_date;
	}

	public String getCreated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.create_date);
		}
		
		return "";
	}
	
	public String getUpdated()
	{
		if(this.update_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.update_date);
		}
		
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Vehicle> getVehicles()
	{
		DBFilterList filter = new DBFilterList();
		DBFilter filterlist = new DBFilter("owner_id","=",this.owner_id);
		filter.add(filterlist);
		
		Vehicle v = new Vehicle();
		
		return (ArrayList<Vehicle>)v.get(0, 0, "create_date DESC", filter);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Invoice> getInvoices()
	{
		DBFilterList filter = new DBFilterList();
		DBFilter filterlist = new DBFilter("owner_id","=",this.owner_id);
		filter.add(filterlist);
		
		Invoice i = new Invoice();
		
		return (ArrayList<Invoice>)i.get(0, 0, "create_date DESC", filter);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ManagedPermit> getManagedPermits()
	{
		DBFilterList filter = new DBFilterList();
		DBFilter filterlist = new DBFilter("owner_id","=", this.owner_id);
		filter.add(filterlist);
		
		ManagedPermit p = new ManagedPermit();
		
		return (ArrayList<ManagedPermit>)p.get(0, 0, "create_date DESC", filter);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<OwnerNote> getNotes()
	{
		OwnerNote note = new OwnerNote();
		
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("owner_id", "=", this.owner_id));
		return (ArrayList<OwnerNote>)note.get(0, 0, "create_date DESC", filter);
	}
	
	public int getMaxOwnerId()
	{
		int max = 0;
		
		DBConnection connection = null;
		try
		{
			connection = new DBConnection();
			PreparedStatement pst = connection.prepare("select max(owner_id) from owner");
			//pst.setInt(1, this.mpermit_type_id);
			
			if(connection.query(pst))
			{
				ResultSet rs = connection.getResultSet();
				while(rs.next())
				{
					try
					{
						max = rs.getInt(1);
						//OwnerType ot = new OwnerType(rs.getInt(1));
						//rv.add(ot);
					}
					catch(Exception e)
					{
						e.printStackTrace();
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
		
		return max;
	}
	
	
	public ArrayList<OwnerAttribute> getExtra()
	{
		return extra;
	}

	public boolean isActive()
	{
		return this.status.equals(STATUS_ACTIVE);
	}
	
	@Override
	public boolean commit()
	{
		if(this.owner_id == 0) //new
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
			this.create_date = this.update_date;
			this.password_updated = this.update_date;
		}
		else//update
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
		}
		boolean rv = super.commit();
		
		if(rv){
			PermitWatcher.update();
		}
		
		if(rv && this.extra != null)
		{
			for(OwnerAttribute attr: this.extra)
			{
				if(attr.dirty)//only save dirty 
				{
					attr.owner_id = this.owner_id;
					attr.commit();
				}
				else
				{
					attr.delete();
				}
			}
		}
		
		this.extra = null; //reload this
		this.loadExtra();
		
		return rv;
	}
	
	@Override
	public boolean delete()
	{
		boolean rv = super.delete();
		
		if(rv)
		{
			DBConnection conn = null;
			try
			{
				conn = new DBConnection();
				
				conn.execute("DELETE from owner_attribute where owner_id="+this.owner_id);
				conn.execute("DELETE from vehicle_attribute where vehicle_id IN (select vehicle_id from vehicle where owner_id ="+this.owner_id+")");
				conn.execute("DELETE from vehicle where owner_id="+this.owner_id);
			}
			catch(Exception e)
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
		}
		
		return rv;
	}
	

	public static Owner getCurrentOwner() 
	{
		try
		{
		    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	
		    if (principal instanceof Owner)
		    {
		    	return (Owner) principal;
		    }
	
		    // principal object is either null or represents anonymous user -
		    // neither of which our domain User object can represent - so return null
		    return null;
		}
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	
	public static void setCurrentOwner(Owner owner)
	{
		SecurityContextHolder
			.getContext()
			.setAuthentication(new PreAuthenticatedAuthenticationToken(owner, null, owner.getAuthorities()));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		Collection<GrantedAuthority> authorities = new Vector<GrantedAuthority>();
	    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
	    
	    return authorities;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		PasswordConfig passwordConfig;
		try 
		{
			passwordConfig = PasswordConfig.get(PasswordConfig.AuthorizationType.OWNER);
			return !passwordConfig.isExpired(this.password_updated);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	@Override
	public boolean isEnabled()
	{
		return this.isActive();
	}
		
	public Owner validate() throws Exception
	{
		if (this.first_name == null || this.first_name.length() == 0 ||
				this.last_name == null || this.last_name.length() == 0 ||
					this.address == null || this.address.length() == 0 ||
						this.email == null || this.email.length() == 0 ||
							this.city == null || this.city.length() == 0 ||
								this.state_id == null || this.state_id.length() == 0 ||
									this.zip == null || this.zip.length() == 0 ||
										this.home_phone == null || this.home_phone.length() == 0 ||
											this.mobile_phone == null || this.mobile_phone.length() == 0 ||
											this.username == null || this.username.length() == 0 ||
												this.password == null || this.password.length() == 0 ||
													this.type_id == 0 ) 
		{
			throw new Exception("All Fields are required.");
		}
		else 
		{
			if (!Util.isZip(this.zip )) 
			{
				throw new Exception(" Please enter a valid Zip.");
			}
			
			if (!Util.isEmail(this.email)) 
			{
				throw new Exception("Please enter a valid Email.");
			}
			
			if (!Util.isAlphaNumeric(this.username)) 
			{
				throw new Exception("Please enter a valid User Name.");
			}
			
			
			
			
			Owner test = Owner.getByUsername(this.username);
			if(test != null && this.owner_id != test.owner_id)
			{
				throw new Exception("User name is already in use. Please select a different user name.");
			}
			
		
			OwnerFields ofields = new OwnerFields();
			ArrayList<OwnerField> extrafieldsowner = ofields.getFields(false);
			
			for(OwnerField field: extrafieldsowner)
			{
				OwnerAttribute attr = this.getAttribute(field.name);
				
				if(field.required && attr.value.length() == 0)
				{
					throw new Exception(field.label + " is required.");
				}
				
				if(field.validation.length()!= 0 && !Util.isValid(attr.value, field.validation))
				{
					throw new Exception("Please enter a valid " + field.label);
				}
			}
		}
		return this;
	}
}


