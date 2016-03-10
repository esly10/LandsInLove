package com.cambiolabs.citewrite.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Vector;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Util;
import com.google.gson.annotations.Expose;

public class User extends DBObject implements org.springframework.security.core.userdetails.UserDetails, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public final static int PL_ADMIN = 1; //full access
	public final static int PL_RESERVATION_VIEW = 2; // 
	public final static int PL_OCUPANCY_LIST = 4; //
	public final static int PL_CHARGES_VIEW = 8; //
	public final static int PL_REPORT_VIEW = 16; //
	public final static int PL_PAYMENT_REPORT_VIEW = 32; // 
	public final static int PL_RESERVATION_MANAGE = 64; //
	public final static int PL_AGENCY_MANAGE  = 128; //
	public final static int PL_GUEST_MANAGE = 256; //
	public final static int PL_SERVICE_MANAGE = 512; //
	public final static int PL_ROOM_MANAGE = 1024; //
	public final static int PL_SETTINGS_MANAGE = 2048; //
	
	@Expose public int user_id = 0;
	//@Expose public String officer_id = null;
	@Expose public String first_name = null;
	@Expose public String last_name = null;
	@Expose public String username = null;
	public String password = null;
	@Expose public int permissions = 0;
	@Expose public Timestamp password_updated = null;
	
	public User()
	{
		super("users", "user_id");
	}
	
	public User(int userID) throws UnknownObjectException
	{
		super("users", "user_id");
		
		if(userID > 0)
		{
			this.user_id = userID;
			this.populate();
		}
	}
	
	public int getUserID()
	{
		return this.user_id;
	}
	public void setFirstName(String firstName) {
		this.first_name = firstName;
	}
	public String getFirstName() {
		return first_name;
	}
	public void setLastName(String lastName) {
		this.last_name = lastName;
	}
	public String getLastName() {
		return last_name;
	}
	public String getName() {
		return first_name + " " + last_name;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public void setPassword(String password) 
	{
		this.password_updated = new Timestamp(System.currentTimeMillis());
		this.password = Util.getPasswordHash(password);
	}
	public String getPassword() {
		return password;
	}
	/*public void setOfficerID(String officer_id)
	{
		this.officer_id = officer_id;
	}
	public String getOfficerID()
	{
		return this.officer_id;
	}*/
	public int getPermissions() {
		return permissions;
	}
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
	
	public boolean clear(Timestamp start, Timestamp end)
	{
		return false;
	}
	
	public boolean hasPermission(int level)
	{
		if(this.isAdmin())
		{
			return true;
		}
		
		return ((this.permissions & level) > 0);
	}
	
	public boolean isAdmin()
	{
		return ((this.permissions & PL_ADMIN) > 0);
	}
	
	public static User getByUsername(String username)
	{
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			User user = new User();
			user.setUsername(username);
			
			if(conn.lookup(user))
			{
				return user;
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
	
	public static User getCurrentUser() 
	{
		try
		{
		    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	
		    if (principal instanceof User)
		    {
		    	return (User) principal;
		    }
	
		    // principal object is either null or represents anonymous user -
		    // neither of which our domain User object can represent - so return null
		    return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static User authenticate(String username, String password)
	{
		User user = null;
		
		try
		{
			user = User.getByUsername(username);
			if(user == null || (!user.password.equals(password) &&  !Util.getPasswordHash(password).equals(user.password)))
			{
				return null; //not a valid user or username and password
			}
			
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
		
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		Collection<GrantedAuthority> authorities = new Vector<GrantedAuthority>();
	    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	    
	    return authorities;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		try 
		{
			PasswordConfig passwordConfig = PasswordConfig.get(PasswordConfig.AuthorizationType.USER);
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
		// TODO Auto-generated method stub
		return true;
	}

}
