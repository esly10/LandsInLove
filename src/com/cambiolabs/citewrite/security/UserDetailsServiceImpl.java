package com.cambiolabs.citewrite.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cambiolabs.citewrite.data.*;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService 
{
	private String type = "";
	public void setType(String type)
	{
		this.type = type;
	}
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException 
	{
		try 
		{
			/*if(this.type.equals("owner"))
			{
				return Owner.getByUsername(username);
			}
			else */
			if(this.type.equals("admin"))
			{
				return User.getByUsername(username);
			}
			else if(this.type.equals("guest"))
			{
				//return Guest.getByCitationNumber(username);
			}
			
			return null;
			
		} 
		catch (Exception e) 
		{
			throw new UsernameNotFoundException("Invalid User Name and/or password.");
		}
	}
}
