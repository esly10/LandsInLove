/*
 * @(#)FileUtil.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * This is utility class to load a file.
 *
 * @author Samuel Chen
 * @version $Revision: 1.3 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class FileUtil {

  private InputStream is;

  /**
   * @param filename the file name, it must be in the classpath.
   */
  public FileUtil(String filename) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    is = cl.getResourceAsStream(filename);    
  }

  /**
   * @param is InputStream to be read.
   */
  public FileUtil(InputStream is) {
    this.is = is;
  }

  /**
   * The method loads the file (properties file) into a property names List object
   * and a Properties object.
   * Both objects have to be initialized.
   * This is used when one wants to keep the properties names in order.
   * @param names the properties name list.
   * @param prop the Properties itself.
   */
  public void read(List<String> names, Properties prop) throws IOException {

    if (is == null) {
      throw new IOException("There is nothing to read from ...");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    String name;
    String value;
    while ( (line=reader.readLine()) != null ) {      
      if ((line.trim()).indexOf('#') == 0 || (line.trim()).indexOf('!') == 0)
        continue;
      int index = line.indexOf('=');
      if (index > 0) {
        name = line.substring(0, index).trim();
        value = line.substring(++index).trim();
      } else {
        name = line.trim();
        value = "";
      }
      names.add(name);
      prop.setProperty(name, loadConvert(value));
    }

  }

  /*
   * Copied from JDK 1.4 Properties
   * Converts encoded &#92;uxxxx to unicode chars
   * and changes special saved chars to their original forms
   */
  private String loadConvert(String theString) {
    char aChar;
    int len = theString.length();
    StringBuffer outBuffer = new StringBuffer(len);

    for (int x=0; x<len; ) {
      aChar = theString.charAt(x++);
      if (aChar == '\\') {
        aChar = theString.charAt(x++);
        if (aChar == 'u') {
          // Read the xxxx
          int value=0;
          for (int i=0; i<4; i++) {
            aChar = theString.charAt(x++);
            switch (aChar) {
              case '0': case '1': case '2': case '3': case '4':
              case '5': case '6': case '7': case '8': case '9':
                value = (value << 4) + aChar - '0';
                break;
              case 'a': case 'b': case 'c':
              case 'd': case 'e': case 'f':
                value = (value << 4) + 10 + aChar - 'a';
                break;
              case 'A': case 'B': case 'C':
              case 'D': case 'E': case 'F':
                value = (value << 4) + 10 + aChar - 'A';
                break;
              default:
                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
            }
          }
          outBuffer.append((char)value);
        } else {
          if (aChar == 't') aChar = '\t';
          else if (aChar == 'r') aChar = '\r';
          else if (aChar == 'n') aChar = '\n';
          else if (aChar == 'f') aChar = '\f';
          outBuffer.append(aChar);
        }
      } else {
        outBuffer.append(aChar);
      }
    }
    return outBuffer.toString();
  }
  
}


