package com.cambiolabs.citewrite.interceptor;

import java.util.Collection;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import com.cambiolabs.citewrite.data.ConfigItem;
import com.cambiolabs.citewrite.data.Owner;

@SuppressWarnings("serial")
public abstract class OwnerAuthentication implements Authentication {
	
	private boolean isAuthenticated = false;
	private Owner owner = null;
	private String password = null; 
	
	public static OwnerAuthentication factory (){
		
		Class<?> ownerAuthenticationClass = null;
		OwnerAuthentication ownerAuthentication = null;
		
		try {
			
			ConfigItem item = ConfigItem.lookup("AUTHENTICATION_CLASS"); 
			if(item.getTextValue() != null && !item.getTextValue().equals("")){
				ownerAuthenticationClass = Class.forName(item.getTextValue());
				
				if((ownerAuthenticationClass.newInstance()) instanceof OwnerAuthentication){
					ownerAuthentication = (OwnerAuthentication) ownerAuthenticationClass.newInstance();
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ownerAuthentication;
	}
	
	
	@Override
	public String getName() {
		return owner.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return owner.getAuthorities();
	}

	@Override
	public Object getCredentials() {
		return password;
	}

	@Override
	public Object getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPrincipal() {
		return owner;
	}

	@Override
	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.isAuthenticated = isAuthenticated;
	}
	
	public void processAuthentication (String password, Owner owner) throws AuthenticationException{
		this.password = password;
		this.owner = owner;
		setAuthenticated (authenticate());
	}

	abstract public boolean authenticate() throws AccountStatusException, BadCredentialsException, NonceExpiredException, UsernameNotFoundException;
}
