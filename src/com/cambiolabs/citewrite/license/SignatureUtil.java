/*
 * @(#)SignatureUtil.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.license;

import java.security.*;
import com.cambiolabs.citewrite.util.ByteHex;
import com.cambiolabs.citewrite.util.security.KeyUtil;

/**
 * This is a utility class for sign and verify the signature.
 *
 * @author Samuel Chen
 * @version $Revision: 1.1 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class SignatureUtil {

  private static final String algorithm = "DSA";
  private SignatureUtil() {
  }

  /**
   * This method creates the signature based on the license data and the private key.
   * It is called by the LicenseTool to create the license.
   * @param data the license data.
   * @param encodedPrivateKey the private key.
   * @return the encoded signature.
   */
  public static String sign(String data, byte[] encodedPrivateKey)
				    throws GeneralSecurityException {
    Signature sig = Signature.getInstance(algorithm);
    PrivateKey key = KeyUtil.getPrivate(encodedPrivateKey);
    sig.initSign(key); 
    sig.update(data.getBytes());
    byte[] result = sig.sign();
    return ByteHex.convert(result);
  }
  
  /**
   * This method validate license data based on the signature and the public key.
   * It is called by the LicenseManager to validate the license.
   * @param data the license data.
   * @param encodedPublicKey the public key.
   * @return a boolean whether or the license is valid.
   */
  public static boolean verify(String data, byte[] signature, byte[] encodedPublicKey)
                                                       throws GeneralSecurityException {
    Signature sig = Signature.getInstance(algorithm);
    PublicKey key = KeyUtil.getPublic(encodedPublicKey);
    sig.initVerify(key); 
    sig.update(data.getBytes());  
    return sig.verify(signature);
  }

}
