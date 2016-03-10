/*
 * $Id: LicenseManagerImpl.java,v 1.6 2006/01/19 03:55:20 xingming Exp $
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.license;

import java.security.GeneralSecurityException;
import java.util.Date;

import com.cambiolabs.citewrite.util.ByteHex;
import com.cambiolabs.citewrite.util.DateParser;

/**
 * This class is shipped along with the product to the consumer.
 * It is used to verify the license.
 * The license is a text file also shipped to the comsumer.
 *
 * @author Samuel Chen
 * @version $Revision: 1.6 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class LicenseManagerImpl extends LicenseManager {

  // User need replace this public key with the one created by the KeyManager.
  
  private static final String PUBLIC_KEY ="308201B83082012C06072A8648CE3804013082011F02818100FD7F53811D75122952DF4A9C2EECE4E7F611B7523CEF4400C31E3F80B6512669455D402251FB593D8D58FABFC5F5BA30F6CB9B556CD7813B801D346FF26660B76B9950A5A49F9FE8047B1022C24FBBA9D7FEB7C61BF83B57E7C6A8A6150F04FB83F6D3C51EC3023554135A169132F675F3AE2B61D72AEFF22203199DD14801C70215009760508F15230BCCB292B982A2EB840BF0581CF502818100F7E1A085D69B3DDECBBCAB5C36B857B97994AFBBFA3AEA82F9574C0B3D0782675159578EBAD4594FE67107108180B449167123E84C281613B7CF09328CC8A6E13C167A8B547C8D28E0A3AE1E2BB3A675916EA37F0BFA213562F1FB627A01243BCCA4F1BEA8519089A883DFE15AE59F06928B665E807B552564014C3BFECF492A03818500028181008D9BD4095FAD1F9898927791FD0AE90F02C2B3D524F33A42BFDBF691739F806A0B620B8B55C8F5BF0E09D40C8BA55C99274F074F0BC2BD5AFEF31F0DF351FBD36B374C19CF31DDF4FFCC2E1AB78B09EB8FFCDAD161C87C0FD9E81DFFD4B0EDB734619980915EF1CC47539EA0112064CE51D48C350124DBF22EDCE04074E2B267";

  private static final int MS_A_DAY = 24*3600*1000;
  private byte[] key;
  private License lic;
  
  protected LicenseManagerImpl() {
    try {
      lic = License.loadLicense();
    } catch (LicenseNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
    try {
      key = ByteHex.convert(PUBLIC_KEY);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * It first checks the license itself is not tampered and is valid,
   * and then checks the license is not expired.
   *
   * User should use getFeature(feature) for additional validation.
   * @return whether or not the license is valid.
   */
  public boolean isValid() throws GeneralSecurityException {
    String signature =lic.getSignature();
    if(signature == null || signature.trim().length() == 0)
      return false;
    boolean valid = SignatureUtil.verify(lic.format(), ByteHex.convert(signature), key);
    if (valid == false) 
      return false;
    if (daysLeft() < 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * It returns how many days are left for the license.
   * Here, 0 is valid, it may indicate a never expire license.
   * @return how many days are left for the license.
   */
  public int daysLeft() {
    String expiration = lic.getExpiration();
    if(expiration == null) {
      return -1;
    } else if (expiration.trim().length() == 0
               || expiration.indexOf(License.NEVER) != -1) {
      return 0;
    }
    Date licDate = DateParser.toUtilDate(expiration);
    long time = licDate.getTime() - System.currentTimeMillis();
    int days = 1 + (int)(time/MS_A_DAY);
    return days;
  }

  /**
   * This method is called by the license client to get the feature of the license.
   *
   * For example, user may use IP_Address as a feature, and encode a particular
   * IP address inside the license. This IP is then checked against the machine
   * when the user's software is running.
   *
   * One should first call isValid() before calling this method.
   *
   * @return the feature value of the license.
   */
  public String getFeature(String name) {
    return lic.getFeature(name);
  }

}

