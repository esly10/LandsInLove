package com.cambiolabs.citewrite.ecommerce;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.cambiolabs.citewrite.data.Citation;
import com.cambiolabs.citewrite.data.Code;
import com.cambiolabs.citewrite.data.Codes;
import com.cambiolabs.citewrite.data.LateFee;
import com.cambiolabs.citewrite.data.ManagedPermit;
import com.cambiolabs.citewrite.data.Owner;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Invoice extends DBObject
{
	public static final int TYPE_PERMIT = 1;
	public static final int TYPE_CITATION = 2;
	
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
	
	@Expose public int invoice_id = 0;
	@Expose public int type = 0;
	@Expose public String payment_method = PAYMENT_METHOD_CC;
	@Expose public String status = STATUS_APPROVED;
	@Expose public int owner_id = 0;
	@Expose public int user_id = 0; //this is the cler that took payment
	@Expose public String user_full_name = null;
	
	//for check payment type
	@Expose public String check_number = null;
	
	//credit card information
	@Expose public int reference_id = 0;
	@Expose public String auth_code = null;
	@Expose public String trans_id = null;
	@Expose public float amount = 0;
			
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
	@Expose public int will_pickup = 0;
	
	@Expose public Timestamp refund_date = null;
	@Expose public Timestamp create_date = null;
	@Expose public Timestamp update_date = null;
	
	@Expose private ArrayList<InvoiceItem> items = null;

	public Invoice()
	{
		super("invoice", "invoice_id", new String[]{"items"});
		this.items = new ArrayList<InvoiceItem>();
	}

	public Invoice(int invoiceId) throws UnknownObjectException
	{
		this();
		if(invoiceId > 0)
		{
			this.invoice_id = invoiceId;
			this.status = null;
			this.payment_method = null;
			this.populate();
			
			this.loadItems();
		}
	}
	
	public Invoice(int invoiceId, int ownerId) throws UnknownObjectException
	{
		this();
		if(invoiceId > 0)
		{
			this.invoice_id = invoiceId;
			this.owner_id = ownerId;
			this.status = null;
			this.payment_method = null;
			this.populate();
			
			this.loadItems();
		}
	}
	
	public void loadItems()
	{
		this.items = InvoiceItem.getAll(this.invoice_id);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Invoice> getByOwner(int owner_id)
	{
		DBFilterList filter = new DBFilterList();
		filter.add(new DBFilter("owner_id", "=", owner_id));
		
		Invoice invoice = new Invoice();
		return (ArrayList<Invoice>)invoice.get(0, 0, "create_date DESC", filter);
	}
		
	public int getInvoiceId()
	{
		return invoice_id;
	}
	
	public String getPaymentMethod()
	{
		return this.payment_method;
	}
	
	public boolean getRequiresBilling()
	{
		return !this.payment_method.equals(PAYMENT_METHOD_NONE);
	}

	public int getType()
	{
		return type;
	}
	
	public String getTypeName()
	{
		if(this.type == TYPE_CITATION)
		{
			return "Citation";
		}
		
		return "Permit";
	}

	public String getStatus()
	{
		return status;
	}

	public Owner getOwner()
	{
		try
		{
			return new Owner(owner_id);
		} 
		catch (UnknownObjectException e){}
		
		return null;
	}

	public int getReferenceId()
	{
		return reference_id;
	}

	public String getAuthCode()
	{
		return auth_code;
	}

	public String getTransId()
	{
		return trans_id;
	}

	public String getAmount()
	{
		return NumberFormat.getInstance().format((double)this.amount);
	}
	
	public String getFormatAmount()
	{
		
		return String.format("$%.02f", this.amount);
	}

	public String getBillingFirstName()
	{
		return billing_first_name;
	}
	
	public String getBillingEmail()
	{
		return billing_email;
	}

	public String getBillingLastName()
	{
		return billing_last_name;
	}

	public String getBillingAddress()
	{
		return billing_address;
	}

	public String getBillingCity()
	{
		return billing_city;
	}

	public String getBillingState()
	{
		Code code = Codes.getCode(Code.CT_STATE, this.billing_state_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;
	}
	public String getBillingStateID()
	{
		return billing_state_id;	
	}

	public String getBillingZip()
	{
		return billing_zip;
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

	public String getShippingFirstName()
	{
		return shipping_first_name;
	}

	public String getShippingLastName()
	{
		return shipping_last_name;
	}

	public String getShippingAddress()
	{
		return shipping_address;
	}

	public String getShippingCity()
	{
		return shipping_city;
	}

	public String getShippingState()
	{
		Code code = Codes.getCode(Code.CT_STATE, this.shipping_state_id);
		if (code == null) 
		{
			return "";	
		}
		
		return code.description;
	}
	
	public String getShippingStateID()
	{
		return shipping_state_id;	
	}

	public String getShippingZip()
	{
		return shipping_zip;
	}

	public Timestamp getCreateDate()
	{
		return create_date;
	}

	public Timestamp getUpdateDate()
	{
		return update_date;
	}
	
	public boolean getWillPickup()
	{
		return(will_pickup == 1);
	}
		
	public void setCitationId(int citationId)
	{
		this.reference_id = citationId;
		this.type = TYPE_CITATION;
	}
	
	public void setPermitId(int permitId)
	{
		this.reference_id = permitId;
		this.type = TYPE_PERMIT;
	}
	
	public void setUser(User user)
	{
		if(user != null)
		{
			this.user_id = user.user_id;;
			this.user_full_name = user.getName();
		}
		else
		{
			this.user_id = 0;
			this.user_full_name = "";
		}
	}
	
	public int getUserID()
	{
		return this.user_id;
	}
	
	public String getIssuedBy()
	{
		return this.user_full_name;
	}
	
	public void setPermit(ManagedPermit permit)
	{
		this.type = Invoice.TYPE_PERMIT;
		this.reference_id = permit.mpermit_id;				
		
		this.items.add(new InvoiceItem(InvoiceItem.TYPE_PURCHASE, permit.getType().cost, "New Permit"));
		
		this.setAmount();
	}
	
	public void setCitation(Citation citation)
	{
		this.type = Invoice.TYPE_CITATION;
		this.reference_id = citation.citation_id;
		
		this.items.add(new InvoiceItem(InvoiceItem.TYPE_PAYMENT, citation.violation_amount, "Citation Payment"));
		
		LateFee fee = citation.getLateFee();
		if(fee != null)
		{
			if(citation.status_id != Citation.CS_PAID){
				this.items.add(new InvoiceItem(InvoiceItem.TYPE_LATE_FEE, fee.fee_amount, "Late Fee Charge: late fee accessed after "+fee.days_late+" days late."));
			}
			
		}
		
		this.setAmount();
	}
	
	public void setCitationForPayment(Citation citation, float amount, Invoice invoice)
	{
		this.type = Invoice.TYPE_CITATION;
		this.reference_id = citation.citation_id;
		
		this.items.add(new InvoiceItem(InvoiceItem.TYPE_PAYMENT, amount, "Citation Payment", invoice));
		
		this.setAmount();
	}
	
	public void addItem(InvoiceItem item)
	{
		this.items.add(item);
	}
	
	public String getCheckNumber()
	{
		return this.check_number;
	}
	
	public InvoiceItem getItem(int type)
	{
		for(InvoiceItem item: this.items)
		{
			if(item.type == type)
			{
				return item;
			}
		}
		
		return null;
	}
	
	public ArrayList<InvoiceItem> getAllItems()
	{
		return this.getItems(-1);
	}
	
	public ArrayList<InvoiceItem> getItems()
	{
		return this.getItems(-1);
	}
	
	public ArrayList<InvoiceItem> getItems(int type)
	{
		if(this.items == null || this.items.size() == 0)
		{
			this.loadItems();
		}
		
		ArrayList<InvoiceItem> rv = new ArrayList<InvoiceItem>();
		
		for(InvoiceItem item: this.items)
		{
			if(type == -1 || item.type == type)
			{
				rv.add(item);
			}
		}
		
		return rv;
	}
	
	public void setAmount()
	{
		this.amount = 0;
		for(InvoiceItem item: this.items)
		{
			switch(item.type)
			{
				case InvoiceItem.TYPE_PURCHASE:
				case InvoiceItem.TYPE_PAYMENT:
				case InvoiceItem.TYPE_LATE_FEE:
					this.amount += item.amount;
					break;
				case InvoiceItem.TYPE_CC_CHARGE:
				case InvoiceItem.TYPE_DISCOUNT:
				case InvoiceItem.TYPE_REFUND:
				case InvoiceItem.TYPE_VOID:
					this.amount -= item.amount;
					break;
			}
		}
	}

	public int getWill_pickup() {
		return will_pickup;
	}

	public static Invoice getByReference(int reference_id, int type)
	{
		DBConnection conn = null;
		DBFilterList filter = new DBFilterList();
		
		try
		{
			conn = new DBConnection();
			Invoice invoice = new Invoice();
			
			filter.add(new DBFilter("type",type));
			filter.add(new DBFilter("reference_id",reference_id));

			@SuppressWarnings("unchecked")
			ArrayList<Invoice> list = (ArrayList<Invoice>)invoice.get(0, 1, "update_date" + " " + "DESC", filter);
			
			if(!list.isEmpty())
			{
				invoice = list.get(0);
				invoice.loadItems();
				return invoice;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();//invoice not found
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		return null;
	}
	
	public Citation getCitation()
	{
		if(this.reference_id > 0 && this.type == TYPE_CITATION)
		{
			try
			{
				return new Citation(this.reference_id);
			}
			catch(UnknownObjectException uoe){}
		}
		
		return null;
	}
	
	public ManagedPermit getPermit()
	{
		if(this.reference_id > 0 && this.type == TYPE_PERMIT)
		{
			try
			{
				return new ManagedPermit(this.reference_id);
			}
			catch(UnknownObjectException uoe){}
		}
		
		return null;
	}
	
	public String getCreated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy";
			return new SimpleDateFormat(dateFormat).format(this.create_date);
		}
		
		return "";
	}
	
	public String getUpdated()
	{
		if(this.create_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.update_date);
		}
		
		return "";
	}
	
	public String getRefundDate()
	{
		if(this.refund_date != null)
		{
			String dateFormat = "MMMMM dd, yyyy h:mm a";
			return new SimpleDateFormat(dateFormat).format(this.refund_date);
		}
		
		return "";
	}
	
	@Override
	public boolean commit()
	{
		if(this.invoice_id == 0) //new
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
			this.create_date = this.update_date;
			this.refund_date = this.update_date;
		}
		else//update
		{
			this.update_date = new Timestamp(System.currentTimeMillis());
		}
		
		//see what is null
		if(this.check_number == null){ this.check_number = ""; }
		if(this.auth_code == null){ this.auth_code = ""; }
		if(this.trans_id == null){ this.trans_id = ""; }
		if(this.cc_number == null){ this.cc_number = ""; }
		if(this.billing_first_name == null){ this.billing_first_name = ""; }
		if(this.billing_last_name == null){ this.billing_last_name = ""; }
		if(this.billing_email == null){ this.billing_email = ""; }
		if(this.billing_address == null){ this.billing_address = ""; }
		if(this.billing_city == null){ this.billing_city = ""; }
		if(this.billing_state_id == null){ this.billing_state_id = ""; }
		if(this.billing_zip == null){ this.billing_zip = ""; }
		if(this.shipping_first_name == null){ this.shipping_first_name = ""; }
		if(this.shipping_last_name == null){ this.shipping_last_name = ""; }
		if(this.shipping_address == null){ this.shipping_address = ""; }
		if(this.shipping_city == null){ this.shipping_city = ""; }
		if(this.shipping_state_id == null){ this.shipping_state_id = ""; }
		if(this.shipping_zip == null){ this.shipping_zip = ""; }

		if(super.commit())
		{
			for(InvoiceItem item: this.items)
			{
				item.create_date = this.create_date;
				item.refund_date = this.refund_date;
				item.invoice_id = this.invoice_id;				
				item.trans_id = this.trans_id;
				item.auth_code = this.auth_code;
				
				item.commit();
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean commitItem()
	{
		
		for(InvoiceItem item: this.items)
		{
			item.create_date = new Timestamp(System.currentTimeMillis());
			item.refund_date = this.refund_date;
			item.invoice_id = this.invoice_id;
			item.auth_code = this.auth_code;
			item.trans_id = this.trans_id;
			item.commit();
		}
		
		
		return true;
	}
	
	public float getItemsAmount()
	{
		float allAmount = 0;
		for(InvoiceItem item: this.items)
		{
			//item.create_date = new Timestamp(System.currentTimeMillis());
			if(item.type == InvoiceItem.TYPE_REFUND || item.type == InvoiceItem.TYPE_VOID){
				allAmount = allAmount - item.amount;
			}else {
				allAmount = allAmount + item.amount;
			}
			 
		}
		
		
		return allAmount;
	}
	
	@Override
	public boolean delete()
	{
		boolean rv = super.delete();
		
		if(rv)
		{
			DBConnection conn = null;
			try
			{
				conn = new DBConnection();
				
				conn.execute("DELETE from invoice_item where invoice_id="+this.invoice_id);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
		}
		
		return rv;
	}
	
	public boolean deleteItem()
	{
		
		DBConnection conn = null;
		try
		{
			conn = new DBConnection();
			
			for(InvoiceItem item: this.items)
			{
				conn.execute("DELETE from invoice_item where invoice_item_id="+item.invoice_item_id);
			}
			
						
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		
		return true;
	
	}
	
	public String getCardTypeName()
	{
		CreditCardType type = this.getCardType();
		if(type != null)
		{
			return type.name;
		}
		
		return "Unknown";
	}
	
	public CreditCardType getCardType()
	{
		if(this.cc_type > 0)
		{
			try
			{
				return new CreditCardType(this.cc_type);
			} 
			catch (UnknownObjectException e){}
		}
		
		return null;
	}
	
	public float getTotal (){
		float total = this.amount;
		for(InvoiceItem invoiceItem : this.getAllItems()){
			if(invoiceItem.type == InvoiceItem.TYPE_REFUND){
				total = total - invoiceItem.amount;
			}else{
				if(invoiceItem.type == InvoiceItem.TYPE_VOID){
					total = 0;
				}
			}
		}
		return total;
	}
}
