package com.cambiolabs.citewrite.util;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class Util
{
	public static int indexOf(String[] array, String key)
	{
		int size = array.length;
		
		for(int i = 0; i < size; i++)
		{
			String k = array[i];
			if(k != null && k.equals(key))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	public static boolean isEmail(String field)
	{
		return isValid(field, "^([0-9a-zA-Z]([_.w]*[0-9a-zA-Z])*@([0-9a-zA-Z][-w]*[0-9a-zA-Z].)+([a-zA-Z]{2,9}.)+[a-zA-Z]{2,3})$");
	}
	
	public static boolean isValid(String field, String regex)
	{
		Pattern pat = null;
		Matcher mat = null;
		pat = Pattern.compile(regex);
		mat = pat.matcher(field);
		if (mat.find()) {
		   
	        return true;
	    }else{
	        return false;
	    }
	}
	 
	public static boolean isPhone(String field)
	{
		return isValid(field, "^(?=.{7,32}$)(\\(?\\+?[0-9]*\\)?)?[0-9_\\- \\(\\)]*((\\s?x\\s?|ext\\s?|extension\\s?)\\d{1,5}){0,1}$");
	}
	 
	public static boolean isZip(String field)
	{
		return isValid(field, "(^\\d{5}(-\\d{4})?$)|(^[ABCEGHJKLMNPRSTVXY]{1}\\d{1}[A-Z]{1} *\\d{1}[A-Z]{1}\\d{1}$)");
	}
		
	public static boolean isAlphaNumeric(String field)
	{
		return isValid(field, "^[a-z0-9_-]{3,15}$");
	}
		
	public static String getPasswordHash(String password){
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(password.getBytes("UTF-8"));
			
			
			return Base64.encodeBase64String(digest);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "";
	}
		
	public static boolean isLicense(String field)
	{
		return isValid(field, "^[0-9a-zA-Z]{1,12}$");
	}
		
	public static boolean isVin(String field)
	{
		return isValid(field, "^(([a-h,A-H,j-n,J-N,p-z,P-Z,0-9]{9})([a-h,A-H,j-n,J-N,p,P,r-t,R-T,v-z,V-Z,0-9])([a-h,A-H,j-n,J-N,p-z,P-Z,0-9])(\\d{6}))$");
		//^[^\\Wioq]{17}$
	}
		

	public static String numberFormat(String format, int number)
	{
		String numFormat = format;
		//need find the number format
    	int start = numFormat.indexOf("%X%"); //just replace with whatever the number is
    	if(start > -1)
    	{
    		numFormat = numFormat.replace("%X%", String.valueOf(number));
    	}
    	else
    	{
    		start = numFormat.indexOf("%X");
	    	if(start > -1)
	    	{
	    	
	    		int end = numFormat.indexOf("X%", start);
	    		if(end > -1)
	    		{
	    			String xs = numFormat.substring(start, end+2);
	    			String formatted = addPadding(xs.substring(1, xs.length()-1), number); //only send the X's
	    			numFormat = numFormat.replace(xs, formatted);
	    		}        
	    	}
    	}
    	
    	//see if we have a date format in the citation number
    	start = numFormat.indexOf("%date-format:");
    	if(start > -1)
    	{
    		int end = numFormat.indexOf('%', start+1);
    		if(end > -1)
    		{
	    		String replace = numFormat.substring(start, end+1);
	    		String df = replace.substring(13, replace.length()-1);
	    		SimpleDateFormat sdf = new SimpleDateFormat(df);
	    		numFormat = numFormat.replace(replace, sdf.format(new Date()));
    		}
    	}
    	
        return numFormat;
	}
		
	private static String addPadding(String format, int number)
    {
        String rv = String.valueOf(number);
        
        int size = rv.length();
        int max = format.length();
        for(int i = size; i < max; i++)
        {
            rv = "0"+rv;
        }
        
        return rv;
    }
}
