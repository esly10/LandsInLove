/*
 * $Id: UnicodeEncoder.java,v 1.2 2006/01/19 06:41:45 xingming Exp $
 *
 * Copyright (c) 2002-2006 WEBsina, Inc. All Rights Reserved.
 *
 */
package com.cambiolabs.citewrite.util;

/**
 * Utility class for writing out NO-ISO string in escaped unicode format &#92;uxxxx
 *
 * @author Samuel Chen
 * @version $Revision: 1.2 $
 * created:  1-8-2006
 */
public class UnicodeEncoder {

  private static StringBuffer charAccumulator = new StringBuffer(2);

  public static String encode(String s) {
    StringBuffer sb = new StringBuffer();
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] <= 127) {
        sb.append(chars[i]);
        continue;
      }
      int codePoint = Integer.MIN_VALUE;
      if (isLowSurrogate(chars[i]) || isHighSurrogate(chars[i])) {
        charAccumulator.append(chars[i]);
      } else {
        codePoint = chars[i];
      }
      if (charAccumulator.length() == 2) {
        codePoint = toCodePoint(charAccumulator.charAt(0), charAccumulator.charAt(1));
        charAccumulator.setLength(0);
      }
      if (charAccumulator.length() == 0) {
        sb.append(encode(codePoint));
      }
    }
    return sb.toString();
  }

  public static String encode(int c) {
    StringBuffer sb = new StringBuffer(10);
    sb.append(Integer.toHexString(c));
    while (sb.length() < 4) {
      sb.insert(0, '0');
    }
    sb.insert(0, "\\u");
    return sb.toString();
  }

  // the following are copied from Java 5

  private static boolean isHighSurrogate(char ch) {
    return ch >= MIN_HIGH_SURROGATE && ch <= MAX_HIGH_SURROGATE;
  }

  private static boolean isLowSurrogate(char ch) {
    return ch >= MIN_LOW_SURROGATE && ch <= MAX_LOW_SURROGATE;
  }

  private static int toCodePoint(char high, char low) {
    return ((high - MIN_HIGH_SURROGATE) << 10)
        + (low - MIN_LOW_SURROGATE) + MIN_SUPPLEMENTARY_CODE_POINT;
  }

  private static final char MIN_HIGH_SURROGATE = '\uD800';
  private static final char MAX_HIGH_SURROGATE = '\uDBFF';
  private static final char MIN_LOW_SURROGATE  = '\uDC00';
  private static final char MAX_LOW_SURROGATE  = '\uDFFF';
  private static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

}
