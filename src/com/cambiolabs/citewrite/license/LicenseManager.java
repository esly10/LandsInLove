/*
 * $Id: LicenseManager.java,v 1.4 2005/04/24 02:31:44 xingming Exp $
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.license;

import java.security.GeneralSecurityException;

/**
 * The class is shipped along with the product to the consumer.
 * It is an abstract class and the getInstance() method returns a LicenseManager
 * implemented by LicenseManagerImpl. 
 *
 * @author Samuel Chen
 * @version $Revision: 1.4 $
 * created:  2-10-2001
 * @since 1.0 
 */
public abstract class LicenseManager {
  
  private static LicenseManager instance = new LicenseManagerImpl();

  public static final LicenseManager getInstance() {
    return instance;
  }
    
  /**
   * It first checks the license itself is not tampered and is valid,
   * and then checks the license is not expired.
   *
   * User should use getFeature(feature) for additional validation.
   * @return whether or not the license is valid.
   */
  public abstract boolean isValid() throws GeneralSecurityException;

  /**
   * It returns how many days are left for the license.
   * Here, 0 should be valid, it may indicate the license never expires.
   * @return how many days are left for the license.
   */
  public abstract int daysLeft();

  /**
   * This method is called by the license client to get the feature of the license.
   *
   * For example, user may use IP_Address as a feature, and encode a particular
   * IP address inside the license. This IP is then checked against the machine
   * when the user's software is running.
   * In fact, by just checking whether or not a feature exist one case validate
   * the proper use of the license.
   * @return the feature value of the license.
   */
  public abstract String getFeature(String name);
  
  
  /**
   * This method is used to determine if managed permitting is supported
   * @return the expiration date.
   */
  	public static boolean isManagedPermitsEnabled()
  	{
  		LicenseManager lm = getInstance();
	  
	    String value =  lm.getFeature(License.PERMIT);
	    
	    try
	    {
	    	return Boolean.parseBoolean(value);
	    }
	    catch(Exception e){}
	    
	    return false;
  	}
  	
  	public static boolean isCitationPaymentEnabled()
  	{
  		LicenseManager lm = getInstance();
	  
	    String value =  lm.getFeature(License.CITATION_PAYMENT);
	    
	    try
	    {
	    	return Boolean.parseBoolean(value);
	    }
	    catch(Exception e){}
	    
	    return false;
  	}

}
