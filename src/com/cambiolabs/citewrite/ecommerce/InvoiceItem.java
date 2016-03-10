package com.cambiolabs.citewrite.ecommerce;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class InvoiceItem extends DBObject
{
	public static final int TYPE_PURCHASE 	= 0;
	public static final int TYPE_PAYMENT 	= 1;
	public static final int TYPE_LATE_FEE 	= 2;
	public static final int TYPE_DISCOUNT 	= 3;
	public static final int TYPE_CC_CHARGE	= 4;//deprecation
	public static final int TYPE_REFUND 	= 5;
	public static final int TYPE_VOID 		= 6;
	public static final int TYPE_CANCELED   = 7;
	public static final int TYPE_EXPIRED    = 8;
	public static final int TYPE_PENDING    = 9;
	public static final int TYPE_REVOKED    = 10;
	
	
	public static final String STATUS_APPROVED = "Approved";
	public static final String STATUS_VOIDED = "Voided";
	public static final String STATUS_REFUNDED = "Refunded";
	
	public static final String STATUS_CANCELED   = "Canceled";
	public static final String STATUS_EXPIRED    = "Expired";
	public static final String STATUS_PENDING    = "Pending";
	public static final String STATUS_REVOKED    = "Revoked";
		
	public static final String PAYMENT_METHOD_CC = "Credit Card";
	public static final String PAYMENT_METHOD_CHECK = "Check";
	public static final String PAYMENT_METHOD_CASH = "Cash";
	public static final String PAYMENT_METHOD_NONE = "None";
	
	@Expose public int invoice_item_id = 0;
	@Expose public int invoice_id = 0;
	@Expose public int user_id = 0;
	@Expose public String user_full_name = null;
	@Expose public int type = 0;
	@Expose public Timestamp item_date = null;
	@Expose public float amount = 0;
	@Expose public int refund_or_void_id = 0;
	@Expose public String description = null;
	
	// same data from item
	
	//for check payment type
	@Expose public String check_number = null;
	@Expose public String payment_method = null;
	
	
	@Expose public String billing_first_name = null;
	@Expose public String billing_last_name = null;
	@Expose public String billing_email = null;
	@Expose public String billing_address = null;
	@Expose public String billing_city = null;
	@Expose public String billing_state_id = null;
	@Expose public String billing_zip = null;
	
	@Expose public String cc_number = null;
	@Expose public int cc_type = 0;
	@Expose public int cc_exp_month = 0;
	@Expose public int cc_exp_year = 0;
	
	@Expose public String shipping_first_name = null;
	@Expose public String shipping_last_name = null;
	@Expose public String shipping_address = null;
	@Expose public String shipping_city = null;
	@Expose public String shipping_state_id = null;
	@Expose public String shipping_zip = null;
	
	@Expose public String auth_code = null;
	@Expose public String trans_id = null;
	@Expose public String status = STATUS_APPROVED;

	@Expose public Timestamp refund_date = null;
	@Expose public Timestamp create_date = null;
	
	public InvoiceItem()
	{
		super("invoice_item", "invoice_item_id");
	}

	public InvoiceItem(int itemId) throws UnknownObjectException
	{
		this();
		if(itemId > 0)
		{
			this.invoice_item_id = itemId;
			this.populate();
		}
	}
	
	public InvoiceItem(int type, float amount, String description)
	{
		this(0, type, amount, description);
	}
	
	public InvoiceItem(int type, float amount, String description, Invoice invoice)
	{
		this(0, type, amount, description);
		this.invoice_id = invoice.invoice_id;
		this.shipping_address = invoice.shipping_address;
		this.shipping_city = invoice.shipping_city;
		this.shipping_first_name = invoice.shipping_first_name;
		this.shipping_last_name = invoice.shipping_last_name;
		this.shipping_state_id = invoice.shipping_state_id;
		this.shipping_zip = invoice.shipping_zip;
		this.billing_address = invoice.billing_address;
		this.billing_city = invoice.billing_city;
		this.billing_email = invoice.billing_email;
		this.billing_first_name = invoice.billing_first_name;
		this.billing_last_name = invoice.billing_last_name;
		this.billing_state_id = invoice.billing_state_id;
		this.user_full_name= invoice.user_full_name; 
		this.billing_zip = invoice.billing_zip;
		this.cc_exp_month = invoice.cc_exp_month;
		this.cc_exp_year = invoice.cc_exp_year;
		this.cc_number = invoice.cc_number;
		this.cc_type = invoice.cc_type;
		this.item_date = new Timestamp(System.currentTimeMillis());
		this.create_date = new Timestamp(System.currentTimeMillis());
		this.check_number = invoice.check_number;
		this.payment_method = invoice.payment_method;
		
	}
	
	
	
	public InvoiceItem(int invoice_id, int type, float amount, String description)
	{
		this();
		this.type = type;
		this.amount = amount;
		this.description = description;
		this.item_date = new Timestamp(System.currentTimeMillis());
		
		//only need to store user for refunds/voids
		if(this.type == TYPE_REFUND || this.type == TYPE_VOID)
		{
			this.setUser(User.getCurrentUser());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<InvoiceItem> getAll(int invoiceId)
	{
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("invoice_id", "=", invoiceId));
		
		InvoiceItem items = new InvoiceItem();
		return (ArrayList<InvoiceItem>)items.get(0, 0, "item_date ASC, type ASC", filter);
	}
	
	public void setUser(User user)
	{
		if(user != null)
		{
			this.user_id = user.user_id;
			this.user_full_name = user.getName();
		}
	}
	
	public String getAuth_code() {
		return auth_code;
	}

	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
	}

	public String getTrans_id() {
		return trans_id;
	}

	public void setTrans_id(String trans_id) {
		this.trans_id = trans_id;
	}
	
	public String getCheck_number() {
		return check_number;
	}

	public void setCheck_number(String check_number) {
		this.check_number = check_number;
	}

	public String getPayment_method() {
		return payment_method;
	}

	public void setPayment_method(String payment_method) {
		this.payment_method = payment_method;
	}

	public String getBilling_first_name() {
		return billing_first_name;
	}

	public void setBilling_first_name(String billing_first_name) {
		this.billing_first_name = billing_first_name;
	}

	public String getBilling_last_name() {
		return billing_last_name;
	}

	public void setBilling_last_name(String billing_last_name) {
		this.billing_last_name = billing_last_name;
	}

	public String getBilling_email() {
		return billing_email;
	}

	public void setBilling_email(String billing_email) {
		this.billing_email = billing_email;
	}

	public String getBilling_address() {
		return billing_address;
	}

	public void setBilling_address(String billing_address) {
		this.billing_address = billing_address;
	}

	public String getBilling_city() {
		return billing_city;
	}

	public void setBilling_city(String billing_city) {
		this.billing_city = billing_city;
	}

	public String getBilling_state_id() {
		return billing_state_id;
	}

	public void setBilling_state_id(String billing_state_id) {
		this.billing_state_id = billing_state_id;
	}

	public String getBilling_zip() {
		return billing_zip;
	}

	public void setBilling_zip(String billing_zip) {
		this.billing_zip = billing_zip;
	}

	public String getCc_number() {
		return cc_number;
	}

	public void setCc_number(String cc_number) {
		this.cc_number = cc_number;
	}

	public int getCc_type() {
		return cc_type;
	}

	public void setCc_type(int cc_type) {
		this.cc_type = cc_type;
	}

	public int getCc_exp_month() {
		return cc_exp_month;
	}

	public String getCcNumber()
	{
		return cc_number;
	}

	public int getCcExpMonth()
	{
		return cc_exp_month;
	}

	public int getCcExpYear()
	{
		return cc_exp_year;
	}
	
	public void setCc_exp_month(int cc_exp_month) {
		this.cc_exp_month = cc_exp_month;
	}

	public int getCc_exp_year() {
		return cc_exp_year;
	}

	public void setCc_exp_year(int cc_exp_year) {
		this.cc_exp_year = cc_exp_year;
	}

	public String getShipping_first_name() {
		return shipping_first_name;
	}

	public void setShipping_first_name(String shipping_first_name) {
		this.shipping_first_name = shipping_first_name;
	}

	public String getShipping_last_name() {
		return shipping_last_name;
	}

	public void setShipping_last_name(String shipping_last_name) {
		this.shipping_last_name = shipping_last_name;
	}

	public String getShipping_address() {
		return shipping_address;
	}

	public void setShipping_address(String shipping_address) {
		this.shipping_address = shipping_address;
	}

	public String getShipping_city() {
		return shipping_city;
	}

	public void setShipping_city(String shipping_city) {
		this.shipping_city = shipping_city;
	}

	public String getShipping_state_id() {
		return shipping_state_id;
	}

	public void setShipping_state_id(String shipping_state_id) {
		this.shipping_state_id = shipping_state_id;
	}

	public String getShipping_zip() {
		return shipping_zip;
	}

	public void setShipping_zip(String shipping_zip) {
		this.shipping_zip = shipping_zip;
	}

	public Timestamp getRefund_date() {
		return refund_date;
	}

	public void setRefund_date(Timestamp refund_date) {
		this.refund_date = refund_date;
	}

	public Timestamp getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Timestamp create_date) {
		this.create_date = create_date;
	}

	public int getUserID()
	{
		return this.user_id;
	}
	
	public String getIssuedBy()
	{
		if(this.user_id > 0)
		{
			try
			{
				User user = new User(this.user_id);
				return user.getName();
			}
			catch(UnknownObjectException uoe)
			{
				return this.user_full_name;
			}
		}
		
		return "";
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public String getTypeLabel()
	{
		switch(this.type)
		{
			case TYPE_PURCHASE:
				return "Purchase";
			case TYPE_PAYMENT:
				return "Payment";
			case TYPE_LATE_FEE:
				return "Late Fee";
			case TYPE_DISCOUNT:
				return "Discount";
			case TYPE_CC_CHARGE:
				return "Charge";
			case TYPE_REFUND:
				return "Refund";
			case TYPE_VOID:
				return "Void";
			case TYPE_CANCELED:
				return "Canceled";
			case TYPE_EXPIRED:
				return "Expired";
			case TYPE_PENDING:
				return "Pending";
			case TYPE_REVOKED:
				return "Revoked";
		}
		
		return "Unknown";
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public float getAmount()
	{
		return this.amount;
	}
	
	public String getFormatAmount()
	{
		
		return String.format("$%.02f", this.amount);
	}
}
