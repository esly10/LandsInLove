package com.cambiolabs.citewrite.data;

import java.util.Collection;
import java.util.Vector;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.util.Util;

public class Guest implements UserDetails
{
	private static final long serialVersionUID = 1L;
	
	private Citation citation = null;
	public Guest(Citation citation)
	{
		this.citation = citation;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		Collection<GrantedAuthority> authorities = new Vector<GrantedAuthority>();
	    authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
	    
	    return authorities;
	}

	@Override
	public String getPassword()
	{
		return Util.getPasswordHash(this.citation.license);
	}

	@Override
	public String getUsername()
	{
		return this.citation.citation_number;
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
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
	
	public static Guest getCurrentGuest() 
	{
		try
		{
		    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	
		    if (principal instanceof Guest)
		    {
		    	return (Guest) principal;
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

	public static Guest getByCitationNumber(String number)
	{
		try
		{
			return new Guest(new Citation(number));
		}
		catch(UnknownObjectException e)
		{
		}
		return null;
	}

	public Citation getCitation()
	{
		return this.citation;
	}
	

}
