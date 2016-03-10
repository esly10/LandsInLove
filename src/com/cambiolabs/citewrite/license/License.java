/*
 * @(#)License.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 * created:  2-10-2001
 */
package com.cambiolabs.citewrite.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.cambiolabs.citewrite.util.FileUtil;
import com.cambiolabs.citewrite.util.UnicodeEncoder;

/**
 * The class read and write to the license file.
 * it should be used by both the SignManager and the LicenseManager
 * so that they are signing and verifying the same data.
 *
 * @author Samuel Chen
 * @version $Revision: 1.5 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class License {

  public static final String NEVER = "never";
  private static final String LICENSE_FILE = "license.lic";
  private static final String EXPIRATION = "Expiration";
  public static final String PERMIT = "Permit";
  public static final String CITATION_PAYMENT = "Payment";
  private static final String SIGNATURE = "Signature";
  private List<String> names;
  private Properties prop;  // all features are stored in prop.
  private String data = null; // the data content of the license

  private License() {
    names = new ArrayList<String>();
    prop = new Properties();    
  }

  /**
   * This method is used by the LicenseTool to create a License.
   * LicenseTool uses the set methods to set the features and then use create()
   * to create the license.
   * @return a new empty license to be created.
   */
  public static License newLicense() {
    return new License();
  }

  /**
   * The method is used by the LicenseManager to load an existing License.
   * LicenseManager uses the get method to validate the license.
   * @return an existing license to be validated.
   */
  public static License loadLicense() throws LicenseNotFoundException {
    License lic = new License();
    lic.readFile();
    return lic;
  }

  /**
   * This is called by LicenseTool to create the license.
   * Before create, data must have been formated so that
   * data and signature exist.
   */
  public void create() throws Exception {
    if (data == null || getSignature() == null) {
      throw new Exception("License is not formated ....");
    }
    java.io.FileWriter out = new java.io.FileWriter(LICENSE_FILE);
    out.write(data);
    out.write('\n');
    out.write(SIGNATURE);
    out.write('=');
    out.write(getSignature());
    out.flush();
    out.close();
  }

  /**
   * The license data that is used to to create the license file and well as used for 
   * validate the license along with the signature.
   * @return the license data that is used to to create the license file and well as used for 
   * validate the license along with the signature.
   */
  public String format() {
    StringBuffer buf = new StringBuffer();
    Iterator<String> it = names.iterator();
    while(it.hasNext()) {
      String key = (String)it.next();
      if (key.length() != 0 && !key.equals(SIGNATURE)) {
        String value = UnicodeEncoder.encode(prop.getProperty(key));
        buf.append(key).append('=').append(value).append('\n');
      }
    }
    data = buf.toString();
    return data;
  }

  /**
   * This method is called by the license client (or the LicenseManager)
   * to validate the license.
   * @param name a feature in the license.
   * @return the value corresponding to this feature.
   */
  public String getFeature(String name) {
    return prop.getProperty(name);
  }

  /**
   * This method is called by the LicenseTool to set the features for the license.
   * @param name a feature of the license.
   * @param value the value of the feature.
   */
  public void setFeature(String name, String value) {
    if (!names.contains(name)) {
      names.add(name);
    }
    prop.setProperty(name, value);
  }

  /**
   * This method is called by the license client (or the LicenseManager)
   * to validate the license.
   * @return the expiration date.
   */
  public String getExpiration() {
    return prop.getProperty(EXPIRATION);
  }

  /**
   * This method is called by the LicenseTool before creating the license.
   * @param date the license expiration date.
   */
  public void setExpiration(String date) {
    setFeature(EXPIRATION, date);
  }
  

  /**
   * This method is called by the license client (or the LicenseManager)
   * to validate the license.
   * @return the signature of the license.
   */
  public String getSignature() {
    return prop.getProperty(SIGNATURE);
  }

  /**
   * This method is called by the LicenseTool before creating the license.
   * @param signature the license signature.
   */
  public void setSignature(String signature) {
    setFeature(SIGNATURE, signature);
  }

  /**
   * This method was called when loading up an existing license.
   */
  private void readFile() throws LicenseNotFoundException {
    try 
    {
    	String path = License.class.getResource("License.class").toURI().toString();
    	
    	//look in the WEB-INF directory for a file that ends with .lic
    	int index = path.lastIndexOf("WEB-INF");
    	if(index > -1)
    	{
    		path = path.substring(0, index+8);
    		if(path.startsWith("jar:"))
    		{
    			path = path.substring(4);
    		}
    		
    		File web = new File(new URI(path));
    		File[] files = web.listFiles(new FilenameFilter(){
    			public boolean accept(File dir, String name)
    			{
    				return name.endsWith(".lic");
    			}
    		});
    		
    		if(files.length > 0)
    		{
    			System.out.println("****Using license "+files[0]);
    			new FileUtil(new FileInputStream(files[0])).read(names, prop);
    			return;
    		}
    	}
    	
    	new FileUtil(LICENSE_FILE).read(names, prop);
    } catch (Exception e) {
    	e.printStackTrace();
      throw new LicenseNotFoundException();
    }
  }

}
