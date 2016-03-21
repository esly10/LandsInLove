package com.cambiolabs.citewrite.data;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;


public class DateFormater {

		@Expose public int dateday = 0;
		@Expose public int datemonth = 0;
		@Expose public int dateyear = 0;
		@Expose public int dayweek = 0;
		@Expose public Timestamp datecomplete = null;
		@Expose public GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		
		
		
		public DateFormater() throws UnknownObjectException
		{
			this(0);
		}
		
		public DateFormater(int day) throws UnknownObjectException
		{
			
		}
		
		public DateFormater(Timestamp date) throws UnknownObjectException
		{
			this.datecomplete = date;
			this.cal.setTime(this.datecomplete); 
			this.dayweek= cal.get(Calendar.DAY_OF_WEEK);
			this.dateyear = cal.get(Calendar.YEAR);
			this.dateday = cal.get(Calendar.DAY_OF_MONTH) ; // Note: zero based!
			this.datemonth = cal.get(Calendar.MONTH)+ 1;
		}
		
		public int getDateday() {
			return dateday;
		}
		public void setDateday(int dateday) {
			this.dateday = dateday;
		}
		public int getDatemonth() {
			return datemonth;
		}
		public void setDatemonth(int datemonth) {
			this.datemonth = datemonth;
		}
		public int getDateyear() {
			return dateyear;
		}
		public void setDateyear(int dateyear) {
			this.dateyear = dateyear;
		}
		public Timestamp getDatecomplete() {
			return datecomplete;
		}
		public void setDatecomplete(Timestamp datecomplete) {
			this.datecomplete = datecomplete;
		}
		public GregorianCalendar getCal() {
			return cal;
		}
		public void setCal(GregorianCalendar cal) {
			this.cal = cal;
		}
		public String getFormatdate() {
			String weekday = getDayWeekName(this.dayweek);
			String month = getMonthName(this.datemonth);
			String LongFormat =weekday+", "+month+" "+this.dateday+", "+this.dateyear;
			return LongFormat;
		}
		
		public Timestamp getTimestampDate(GregorianCalendar cal) {
			long millis = cal.getTimeInMillis();
			this.datecomplete = new Timestamp(millis);
			return this.datecomplete;
		}
		
		public String getMonthName(int month) {
			switch (month){
				case 1: return "January";
				case 2: return "February";
				case 3: return "March";
				case 4: return "April";
				case 5: return "May";
				case 6: return "June";
				case 7: return "July";
				case 8: return "August";
				case 9: return "September";
				case 10: return "October";
				case 11: return "November";
				case 12: return "December";
			}
			return null;
		}
		public String getDayWeekName(int day) {
			switch (day){
				case 1: return "Sunday ";
				case 2: return "Monday ";
				case 3: return "Tuesday ";
				case 4: return "Wednesday ";
				case 5: return "Thursday ";
				case 6: return "Friday ";
				case 7: return "Saturday ";
			}
			return null;
		}
		
}
