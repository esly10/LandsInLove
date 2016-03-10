package com.cambiolabs.citewrite.interceptor;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.cambiolabs.citewrite.data.Owner;

public class OwnerAuthenticationProvider implements  AuthenticationProvider {

	@Override
	
	public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
		
		OwnerAuthentication ownerAuthentication = OwnerAuthentication.factory();
		if(ownerAuthentication!= null){
			Owner owner = Owner.getByUsername(authentication.getName());
			ownerAuthentication.processAuthentication((String)authentication.getCredentials(), owner);
			return ownerAuthentication;
		} 
		
		return null;
    }
 
    public boolean supports(Class<? extends Object> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
