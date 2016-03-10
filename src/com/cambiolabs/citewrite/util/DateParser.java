package com.cambiolabs.citewrite.util;

import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateParser 
{
	private Calendar calendar;
	private SimpleDateFormat format = null;

	public DateParser(Date date) 
	{
		calendar = Calendar.getInstance();
		calendar.setTime(date);
		this.format = new SimpleDateFormat("yyyy-MM-dd");
	}

	public DateParser(String format)
	{
		this.format = new SimpleDateFormat(format); 
		this.calendar = Calendar.getInstance();
	}

	public DateParser parse(String dateStr) throws ParseException
	{
		this.calendar.setTime(this.format.parse(dateStr));

		return this;
	}

	public DateParser firstHour()
	{
		this.calendar.set(Calendar.HOUR_OF_DAY, 0);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		return this;
	}

	public DateParser lastHour()
	{
		this.calendar.set(Calendar.HOUR_OF_DAY, 23);
		this.calendar.set(Calendar.MINUTE, 59);
		this.calendar.set(Calendar.SECOND, 59);
		return this;
	}

	public Timestamp getTimestamp()
	{
		return new Timestamp(this.calendar.getTimeInMillis());
	}

	public static String toString(Calendar cal)
	{
		return toString(cal, null);
	}

	public static String toString(Calendar cal, String format)
	{
		if(format == null)
		{
			format = "MM/dd/yyyy HH:mm:ss";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		return sdf.format(new Date(cal.getTimeInMillis()));		
	}

	public static String toString(Timestamp ts)
	{
		return toString(ts, null);
	}

	public static String toString(Timestamp ts, String format)
	{
		if(format == null)
		{
			format = "MM/dd/yyyy hh:mm a";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		return sdf.format(new Date(ts.getTime()));		
	}

	public static java.util.Date toUtilDate(String dateStr) {
		Date date = null;
		DateFormat formater;
		try {
			formater = new java.text.SimpleDateFormat("yyyy-MM-dd"); 
			date = formater.parse(dateStr);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				formater = new java.text.SimpleDateFormat("MM/dd/yyyy"); 
				date = formater.parse(dateStr);
			} catch (Exception e2) {
				e2.printStackTrace();
				//give up!
			}
		}
		return date;
	}

	public static Timestamp toTimestamp(String date, String format) 
	{
		DateParser parser = new DateParser(format);
		try
		{
			parser.parse(date);
			return parser.getTimestamp();
		} 
		catch (ParseException e)
		{
		}
		return null;
	}

	/**
	 * @param date is java.util.Date
	 * @return java.sql.Date
	 * Convert a java.util.Date to java.sql.Date
	 */
	public static java.sql.Date toSqlDate(java.util.Date date) {
		if (date==null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	public static java.sql.Date toSqlDate(String dateStr) {
		return DateParser.toSqlDate(DateParser.toUtilDate(dateStr));
	}

	/**
	 * @return string year
	 */
	public String getYear() {
		int year = calendar.get(Calendar.YEAR);
		return Integer.toString(year);
	}

	/**
	 * @return last two digital part of the year
	 */
	public String getXXYear() {
		return getYear().substring(2,4);
	}

	/**
	 * @return the month of the year
	 */
	public int getMonth(){
		return calendar.get(Calendar.MONTH)+1;
	}

	/**
	 * @return the day of the month
	 */
	public int getDayOfMonth() {
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @return the day of the month
	 */
	public int getDayOfWeek() {
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static void main (String[] args) {
		DateParser parser = new DateParser(args[0]);
		System.out.println("year="+parser.getYear());
		System.out.println("year2="+parser.getXXYear());
		System.out.println("month="+parser.getMonth());
		System.out.println("day="+parser.getDayOfMonth());
		System.out.println("week="+parser.getDayOfWeek());
		//System.out.println("SUNDAY="+Calendar.SUNDAY);
		//System.out.println("JANUARY="+Calendar.JANUARY);
	}
}


