package com.cambiolabs.citewrite.data;
import java.sql.Timestamp;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Guests extends DBObject
{	

	@Expose public int guest_id = 0;
	@Expose public String name = null;
	@Expose public String dni = null;
	@Expose public int title = 0;
	@Expose public String address = null;
	@Expose public String zip = null;
	@Expose public String phone = null;
	@Expose public int country = 0;
	@Expose public String email = null;
	@Expose public String mobile = null;
	@Expose public String fax = null;
	@Expose public String notes = null;
	@Expose public int market = 0;
	@Expose public Timestamp creation_date = null;
	@Expose public int type = 0;
	
	//private static final String UTF_8 = "UTF-8";
	
	public Guests() throws UnknownObjectException
	{
		this(0);
	}
	
	public Guests(int guest_id) throws UnknownObjectException
	{
		super("guests", "guest_id");
		if(guest_id > 0)
		{
			this.guest_id = guest_id;
			this.populate();
		}
	}
	
	public int getGuestID() {
		return guest_id;
	}
	public void setGuestID(int guestID) {
		this.guest_id = guestID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDni() {
		return dni;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
	public int getTitle() {
		return title;
	}
	public void setTitle(int title) {
		this.title = title;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	
	public int getCountry() {
		return country;
	}
	public void setCountry(int country) {
		this.country = country;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getMarket() {
		return market;
	}
	public void setMarket(int market) {
		this.market = market;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Timestamp getCreationDate() {
		return creation_date;
	}
	public void setCreationDate(Timestamp creation_date) {
		this.creation_date = creation_date;
	}
		
}
