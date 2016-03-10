/*
 * @(#)LicenseNotFoundException.java 1.0
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.license;

/**
 * License Exception
 *
 * @author Samuel Chen
 * @version $Revision: 1.1 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class LicenseNotFoundException extends Exception {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MSG = "License Not Found";
  /**
   * @param msg
   */
  public LicenseNotFoundException(String msg) {
    super(msg);
    }

  public LicenseNotFoundException() {
    super(MSG);
  }

}
