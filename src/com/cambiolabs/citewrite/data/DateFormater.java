package com.cambiolabs.citewrite.data;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.cambiolabs.citewrite.db.DBConnection;
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
		
		public Timestamp getAddDays(Timestamp date) {
			GregorianCalendar incrementday = (GregorianCalendar) Calendar.getInstance();
			incrementday.setTime(date); 
			incrementday.add(Calendar.DATE, 1);
			long millis = incrementday.getTimeInMillis();
			Timestamp dateAdd = new Timestamp(millis);
			return dateAdd;
		}
		
		public Timestamp getAddMonths(Timestamp date) {
			GregorianCalendar incrementday = (GregorianCalendar) Calendar.getInstance();
			incrementday.setTime(date); 
			incrementday.add(Calendar.MONTH, 1);
			long millis = incrementday.getTimeInMillis();
			Timestamp dateAdd = new Timestamp(millis);
			return dateAdd;
		}
		
		public Timestamp getYearPerMonth(int Year, int Month) {
			GregorianCalendar incrementmonth = (GregorianCalendar) Calendar.getInstance();
			incrementmonth.set(Year, Month, 1);
			long millis = incrementmonth.getTimeInMillis();
			Timestamp dateAdd = new Timestamp(millis);
			return dateAdd;
		}
		
		public String getCheckinsRes(){
			int checkins = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT COUNT(*) from reservations where DATE('"+ this.datecomplete +"') = DATE(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkins = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Reserv: "+checkins;
			}
		
		public String getCheckoutsRes(){
		
			int checkouts = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT COUNT(*) from reservations where DATE('"+ this.datecomplete +"') = DATE(reservation_check_out)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkouts = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Reserv: "+checkouts;
			}
		
		public String getCheckinsRooms(){
			int checkins = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_qty) from reservations where DATE('"+this.datecomplete+"') = DATE(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkins = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Rooms: "+checkins;
			}
		
		public String getCheckoutsRooms(){
			int checkouts = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_qty) from reservations where DATE('"+this.datecomplete+"') = DATE(reservation_check_out)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkouts = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Rooms: "+checkouts;
			}
		
		public String getMorningRooms(){
			int morning = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_qty) as rooms from reservations "
							+ "where (DATE('"+this.datecomplete+"') BETWEEN reservation_check_in and  reservation_check_out) "
							+ "and DATE('"+this.datecomplete+"') <> DATE(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							morning = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Rooms: "+morning;
			}
		
		public String getEveningRooms(){
			int evening = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_qty) as rooms from reservations "
							+ "where (DATE('"+this.datecomplete+"') BETWEEN reservation_check_in and  reservation_check_out) "
							+ "and DATE('"+this.datecomplete+"') <> DATE(reservation_check_out)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							evening = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Rooms: "+evening;
			}
		
		public String getMorningOccupancy(){
			int morning = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_occupancy) as rooms from reservations "
							+ "where (DATE('"+this.datecomplete+"') BETWEEN reservation_check_in and  reservation_check_out) "
							+ "and DATE('"+this.datecomplete+"') <> DATE(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							morning = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Guest : "+morning;
			}
		
		public String getEveningOccupancy(){
			int evening = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_occupancy) as rooms from reservations "
							+ "where (DATE('"+this.datecomplete+"') BETWEEN reservation_check_in and  reservation_check_out) "
							+ "and DATE('"+this.datecomplete+"') <> DATE(reservation_check_out)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							evening = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return "Guest : "+evening;
			}
		
		public ArrayList <String> getGroups(){
			String Name = null;
			int MealPlan = 0;
			String Plan = null;
			int Qty =0;
			ArrayList <String> Group = new ArrayList<String>();
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT reservations.reservation_guest_id, guests.name, reservation_type, reservation_meal_plan, reservation_rooms_occupancy "
							+ "from reservations "
							+ "inner join guests on reservations.reservation_guest_id = guests.guest_id"
							+ " where (DATE('"+this.datecomplete+"') BETWEEN DATE(reservation_check_in) and  DATE(reservation_check_out)) and reservations.reservation_type = 2;";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						while (rs.next()){
						
							Name = rs.getString(2);
							MealPlan = rs.getInt(4);
							Qty = rs.getInt(5);
							switch (MealPlan){
								case 1:{
									Plan = "Breackfast";
								}break;
								case 2:{
									Plan = "Half Board.";							
								}break;
								case 3:{
									Plan = "Special Full Board.";
								}break;
								case 4:{
									Plan = "All Included";
								}break;
								default: Plan = "No show";
								
							}
							
							Group.add(Name+",  "+Qty+" Guests, Plan: "+Plan);
						}
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return Group;
			}
		
		public ArrayList <String> getEvents(){
			String Name = null;
			int MealPlan = 0;
			String Plan = null;
			int Qty =0;
			ArrayList <String> Group = new ArrayList<String>();
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT reservations.reservation_guest_id, guests.name, reservation_type, reservation_meal_plan, reservation_rooms_occupancy "
							+ "from reservations "
							+ "inner join guests on reservations.reservation_guest_id = guests.guest_id"
							+ " where (DATE('"+this.datecomplete+"') BETWEEN DATE(reservation_check_in) and  DATE(reservation_check_out)) and reservations.reservation_type = 3;";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						while (rs.next()){
						
							Name = rs.getString(2);
							MealPlan = rs.getInt(4);
							Qty = rs.getInt(5);
							switch (MealPlan){
								case 1:{
									Plan = "Breackfast";
								}break;
								case 2:{
									Plan = "Half Board.";							
								}break;
								case 3:{
									Plan = "Special Full Board.";
								}break;
								case 4:{
									Plan = "All Included";
								}break;
								default: Plan = "No show";
								
							}
							
							Group.add(Name+",  "+Qty+" Guests, Plan: "+Plan);
						}
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return Group;
			}
		
		public int getMonthNights(){
			int checkouts = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_nights) from reservations where Month('"+this.datecomplete+"') = Month(reservation_check_in) and YEAR('"+this.datecomplete+"') = YEAR(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkouts = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return checkouts;
			}
		
		
		public int getMonthGuests(){
			int checkouts = 0;
			DBConnection conn = null;
				try 
				{
					conn = new DBConnection();
					String sql = "SELECT SUM(reservation_rooms_occupancy) from reservations where Month('"+this.datecomplete+"') = Month(reservation_check_in) and YEAR('"+this.datecomplete+"') = YEAR(reservation_check_in)";
					if(conn.query(sql))
					{
						ResultSet rs = conn.getResultSet();
						if (rs.next())
							checkouts = rs.getInt(1);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					if(conn != null)
					{
						conn.close();
					}
				}
				return checkouts;
			}
		public String getMonth() {
			GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
			calendar.setTime(this.datecomplete); 
			
			int month = calendar.get(Calendar.MONTH);
			
			switch (month){
				case 0: return "January";
				case 1: return "February";
				case 2: return "March";
				case 3: return "April";
				case 4: return "May";
				case 5: return "June";
				case 6: return "July";
				case 7: return "August";
				case 8: return "September";
				case 9: return "October";
				case 10: return "November";
				case 11: return "December";
			}
			return null;
		}
		
		public String getMonthYear() {
			GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
			calendar.setTime(this.datecomplete); 
			
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			
			switch (month){
				case 0: return "January, "+year;
				case 1: return "February, "+year;
				case 2: return "March, "+year;
				case 3: return "April, "+year;
				case 4: return "May, "+year;
				case 5: return "June, "+year;
				case 6: return "July, "+year;
				case 7: return "August, "+year;
				case 8: return "September, "+year;
				case 9: return "October, "+year;
				case 10: return "November, "+year;
				case 11: return "December, "+year;
			}
			return null;
		}
		public int getMonthNumber() {
			GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
			calendar.setTime(this.datecomplete); 
			
			int month = calendar.get(Calendar.MONTH);
			
			return month;
		}
		public int getYear() {
			GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
			calendar.setTime(this.datecomplete); 
			
			int year = calendar.get(Calendar.YEAR);
			
			return year;
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
