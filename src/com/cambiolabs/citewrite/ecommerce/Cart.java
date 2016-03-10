package com.cambiolabs.citewrite.ecommerce;

import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

import com.cambiolabs.citewrite.data.Citation;
import com.cambiolabs.citewrite.data.CitationPaymentPlan;
import com.cambiolabs.citewrite.data.ManagedPermit;
import com.cambiolabs.citewrite.data.ManagedPermitAttribute;
import com.cambiolabs.citewrite.data.ManagedPermitField;
import com.cambiolabs.citewrite.data.ManagedPermitFields;
import com.cambiolabs.citewrite.data.ManagedPermitType;
import com.cambiolabs.citewrite.data.Owner;
import com.cambiolabs.citewrite.data.OwnerType;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.data.Vehicle;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.cambiolabs.citewrite.ecommerce.merchant.Merchant;
import com.cambiolabs.citewrite.ecommerce.merchant.MerchantException;
import com.cambiolabs.citewrite.email.MailerTask;
import com.cambiolabs.citewrite.email.preparator.PaymentMessage;
import com.cambiolabs.citewrite.email.preparator.PurchaseMessage;
import com.cambiolabs.citewrite.task.Executor;
import com.cambiolabs.citewrite.util.Util;

public class Cart 
{
	private static final int CCTYPE_AMEX = 3;
	
	public String paymentMethod = Invoice.PAYMENT_METHOD_CC;
	
	public String checkNumber = "";
	
	public String billingFirstName = "";
	public String billingLastName = "";
	public String billingEmail = "";
	public String billingAddress = "";
	public String billingCity = "";
	public String billingStateId = "";
	public String billingZip = "";
	
	public String ccNumberMask = "";
	private String ccNumber = "";
	public int ccType = 0;
	public String ccCvv = "";
	public int ccExpMonth = 0;
	public int ccExpYear = 0;
	
	public String shippingFirstName = "";
	public String shippingLastName = "";
	public String shippingAddress = "";
	public String shippingCity = "";
	public String shippingStateId = "";
	public String shippingZip = "";
	public int will_pickup = 0;
		
	private ManagedPermit permit = null;
	private ArrayList<Vehicle> vehicles = null;
	
	private Owner owner = null;
	private User user = null;
	private Citation citation = null;
	private float citationAmount = -1;
	private float citationLateFee = -1;
	
	public Cart()
	{
		this.owner = Owner.getCurrentOwner();
	}
	
	public Cart(Owner owner)
	{
		this.owner = owner;
		this.user = User.getCurrentUser();
	}
	
	public Cart(Citation citation)
	{
		this.citation = citation;
		this.user = User.getCurrentUser();
	}
	
	public Cart setPermit(ManagedPermit permit)
	{
		this.permit = permit;
		
		return this;
	}
	
	public Cart setPermit(HttpServletRequest request) throws CartException 
	{
		ManagedPermitFields pfields = new ManagedPermitFields();
		
		this.permit = new ManagedPermit();
		this.permit.owner_id = this.owner.owner_id;
		for(ManagedPermitField field: pfields.getFields())
		{
			ManagedPermitAttribute attr = new ManagedPermitAttribute(field.name);
			String param = request.getParameter("mpermit-extra-" + field.name);
			if(param != null && param.length() > 0)
			{
				attr.setValue(param);
			}
			else
			{
				attr.setValue("");
			}
			
			this.permit.addAttribute(attr);
		}
		
		try
		{
			Integer idPermitType = Integer.parseInt(request.getParameter("mpermit-type-id"));
			this.permit.mpermit_type_id = idPermitType;
		}
		catch(NumberFormatException nfe)
		{
			throw new CartException("Invalid permit type.");
		}
		
		
		this.vehicles = new ArrayList<Vehicle>(); //reset the list
		String[] vehicleList = request.getParameterValues("mpermit-vehicle-list");		
		if(vehicleList != null)
		{
			for(String vehicleId: vehicleList)
			{
				try
				{
					this.vehicles.add(new Vehicle(Integer.parseInt(vehicleId), this.owner.owner_id));
				}
				catch(NumberFormatException nfe)
				{
					throw new CartException("Invalid vehicle ID: " + vehicleId);
				} 
				catch (UnknownObjectException e)
				{
					throw new CartException("Vehicle not found: " + vehicleId);
				}
			}
		}
		
		//administration usage
		ArrayList<Vehicle> ownerVehicles = this.owner.getVehicles();
		for(Vehicle vehicle: ownerVehicles)
		{
			String on = request.getParameter("mpermit-vehicle-"+vehicle.vehicle_id);
			if(on != null && on.equals("on"))
			{
				this.vehicles.add(vehicle);
			}
		}
		
		return this;
		
	}
	
	public Cart validateCitation() throws CartException
	{
		if(this.citation == null)
		{
			throw new CartException("No citation provided.");
		}
		
		if(this.citation.status.equals(Citation.CS_PAID))
		{
			throw new CartException("This citation has already been paid for.");
		}
		
		if(this.citation.violation_amount == 0)
		{
			throw new CartException("This citation has no violation amount with it.");
		}
		
		return this;
	}
	
	public Cart validatePermit() throws CartException
	{
		ManagedPermitType type = this.permit.getType();
		
		boolean found = false;
		ArrayList<OwnerType> ownerTypeList = type.getOwnerTypes();
		for(OwnerType ownerType: ownerTypeList)
		{
			if (ownerType.owner_type_id == this.owner.type_id) 
			{
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			throw new CartException("You cannot purchase this permit.");
		}
		
		if (type.max_vehicles < this.vehicles.size()) 
		{
			String msg = "Only " + type.max_vehicles + " vehicle";
			if(type.max_vehicles > 1)
			{
				msg += "s ";
			}
			msg += " can be associated with this permit.";
			
			throw new CartException(msg);
		}
		
		ManagedPermitFields pfields = new ManagedPermitFields();
		for(ManagedPermitField field: pfields.getFields())
		{
			ManagedPermitAttribute attr = this.permit.getAttribute(field.name);
			if(field.required && attr.value.length() == 0)
			{
				throw new CartException(field.label + " is required.");
			}
			if(field.validation.length()!= 0 && !Util.isValid(attr.value, field.validation))
			{
				throw new CartException(field.label + " malformat.");
			}
		}
		
		return this;
	}
		
	public ManagedPermit getPermit()
	{
		return permit;
	}
	
	
	
	public Citation getCitation() {
		return citation;
	}

	public ArrayList<ManagedPermitAttribute> getPermitExtra()
	{
		return permit.getExtra();
	}
	
	public ArrayList<Vehicle> getVehicles()
	{
		return vehicles;
	}
	
	
		
	public Cart validateCreditCard() throws CartException
	{
		if(this.setCreditCardType() < 1)
		{
			throw new CartException("Unknown credit card type.");
		}
		
		CreditCardType type = this.getCardType();
		if(type == null || !type.isAccepted())
		{
			throw new CartException(type.name + " credit cards are not accepted.");
		}

		int sum = 0;
	    int digit = 0;
	    int addend = 0;
	    boolean timesTwo = false;

	    for (int i = this.ccNumber.length() - 1; i >= 0; i--) 
	    {
	        digit = Integer.parseInt(this.ccNumber.substring(i, i + 1));
	        if (timesTwo) 
	        {
	            addend = digit * 2;
	            if (addend > 9) 
	            {
	                addend -= 9;
	            }
	        } 
	        else 
	        {
	            addend = digit;
	        }
	        sum += addend;
	        timesTwo = !timesTwo;
	    }

	    if((sum % 10) != 0)
	    {
	    	throw new CartException("Invalid credit card number");
	    }
	    
	    return this;
	}
	
	public Cart validateExpiration() throws CartException
	{
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		
		if (this.ccExpYear == year) //if we are on this year, check expiration
		{
			if (this.ccExpMonth < month)
			{
				throw new CartException("Your credit card has expired");
				
			}
		}
		
		return this;
	}
	
	public Cart validateCvv() throws CartException
	{
		int len = this.ccCvv.length();
		if(len < 3)
		{
			throw new CartException("Invalid CVC number");
		}
		else if(len == 3 && this.ccType == CCTYPE_AMEX)
		{
			throw new CartException("Invalid CVC number");
		}
		else if(len == 4 && this.ccType != CCTYPE_AMEX)
		{
			throw new CartException("Invalid CVC number");
		}
				
		try 
		{
			Integer.parseInt(this.ccCvv);
		} 
		catch (NumberFormatException nfe) 
		{
			throw new CartException("Invalid CVC number");
			
		}
		
		return this;
	}
	
	public Cart validateShippingAddress() throws CartException
	{
		ManagedPermitType type = this.permit.getType();
		if(type.requiresShipping())
		{
			if(this.will_pickup == 1){
				return this;
			}
			if(this.shippingFirstName == null || this.shippingFirstName.length() == 0 || 
						this.shippingLastName == null || this.shippingLastName.length() == 0 || 
							this.shippingAddress == null || this.shippingAddress.length() == 0 ||
								this.shippingCity == null || this.shippingCity.length() == 0 || 
									this.shippingStateId == null || this.shippingStateId.length() == 0 || 
										this.shippingZip == null || this.shippingZip.length() == 0)
			{
				throw new CartException("All shipping fields are required");
			}
		}
		
		return this;
		
	}
	
	public Cart setShipping(HttpServletRequest request)
	{
		
		this.shippingFirstName = request.getParameter("shipping_first_name");
		this.shippingLastName = request.getParameter("shipping_last_name");
		this.shippingAddress = request.getParameter("shipping_address");
		this.shippingCity = request.getParameter("shipping_city");
		this.shippingStateId = request.getParameter("shipping_state_id");
		this.shippingZip = request.getParameter("shipping_zip");
		
		return this;
	}
	public Cart setPickup(HttpServletRequest request)
	{
		String pickup = request.getParameter("will_pickup");
		if(pickup != null)
		{
			this.will_pickup = 1;
		}
				
		return this;
	}
	public boolean hasVehicle(Vehicle vehicle)
	{
		if(this.vehicles != null && this.vehicles.size() > 0)
		{
			return this.vehicles.contains(vehicle);
		}
		
		return false;
	}
	
	public Cart setVehicles(ArrayList<Vehicle> vehicles)
	{
		this.vehicles = vehicles;
		
		return this;
	}
	
	public boolean requiresCreditCardBilling()
	{
		return this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CC);
	}
	
	public Cart validateBilling() throws CartException
	{
		if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CC))
		{
			this.validateBillingAddress()
			.validateCreditCard()
			.validateCvv()
			.validateExpiration();
		}
		else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CHECK))
		{
			if(this.checkNumber == null || this.checkNumber.length() == 0)
			{
				throw new CartException("Please enter a check number.");
			}
			else
			{
				try
				{
					Integer.parseInt(this.checkNumber);
				}
				catch(NumberFormatException nfe)
				{
					throw new CartException("Please enter a valid check number.");
				}
			}
			
			if(this.billingEmail.length() == 0 || !Util.isEmail(this.billingEmail))
			{
				throw new CartException("Please enter a valid email address.");
			}
		}
		else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CASH))
		{
			if(this.billingEmail.length() == 0 || !Util.isEmail(this.billingEmail))
			{
				throw new CartException("Please enter a valid email address.");
			}
		}
		
		return this;
	}
	
	public Cart validateBillingAddress() throws CartException
	{
		if(this.billingFirstName == null || this.billingFirstName.length() == 0 || 
					this.billingLastName == null || this.billingLastName.length() == 0 || 
						this.billingAddress == null || this.billingAddress.length() == 0 ||
							this.billingEmail == null || this.billingEmail.length() == 0 ||
								this.billingCity == null || this.billingCity.length() == 0 ||
									this.billingStateId == null || this.billingStateId.length() == 0 || 
										this.billingZip == null || this.billingZip.length() == 0 || 
											this.ccNumber == null || this.ccNumber.length() == 0 || 
												this.ccCvv == null || this.ccCvv.length() == 0 )
		{
			throw new CartException("All billing fields are required");
		}
		else
		{
			if(!Util.isZip(this.billingZip))
			{
				throw new CartException("Please enter a valid zip code.");
			}
			
			if(!Util.isEmail(this.billingEmail))
			{
				throw new CartException("Please enter a valid email address.");
			}
		}
		
		return this;
	}
	
	public Cart setCitationAmount(HttpServletRequest request)
	{
		this.citationAmount = -1;
		String strAmount = request.getParameter("pay_amount");
		
		if(strAmount != null && strAmount.length() > 0)
		{
			try
			{
				this.citationAmount = Float.parseFloat(strAmount);
			}
			catch(NumberFormatException nfe){}
		}
				
		return this;
	}
	
	public Cart setBilling(HttpServletRequest request)
	{
		ManagedPermitType type = null;
		if(this.permit != null)
		{
			type = this.permit.getType();
		}
			
		if((type != null && type.cost > 0) || this.citation != null)
		{
			String method = request.getParameter("payment_method");
			if(method != null && method.length() > 0)
			{
				if(method.equals(Invoice.PAYMENT_METHOD_CC) || method.equals(Invoice.PAYMENT_METHOD_CHECK) 
						|| method.equals(Invoice.PAYMENT_METHOD_CASH) || method.equals(Invoice.PAYMENT_METHOD_NONE))
				{
					this.paymentMethod = method;
				}
				else
				{
					this.paymentMethod = Invoice.PAYMENT_METHOD_CC;
				}
			}
			else
			{
				this.paymentMethod = Invoice.PAYMENT_METHOD_CC;
			}
			
			if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CC))
			{
				this.billingFirstName = request.getParameter("billing_first_name");
				this.billingLastName = request.getParameter("billing_last_name");
				this.billingEmail = request.getParameter("billing_email");
				this.billingAddress = request.getParameter("billing_address");
				this.billingCity = request.getParameter("billing_city");
				this.billingStateId = request.getParameter("billing_state_id");
				this.billingZip = request.getParameter("billing_zip");
				
				this.setCCNumber(request.getParameter("cc_number"));
				this.ccCvv = request.getParameter("cc_cvv");
				this.ccCvv = (this.ccCvv == null)?"":this.ccCvv;
				String strNum = request.getParameter("cc_exp_month");
				this.ccExpMonth = (strNum == null || strNum.length() == 0)?0:Integer.parseInt(strNum);
				strNum = request.getParameter("cc_exp_year");
				this.ccExpYear = (strNum == null || strNum.length() == 0)?0:Integer.parseInt(strNum);
			}
			else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CHECK))
			{
				this.checkNumber = request.getParameter("check_number");
				this.billingEmail = request.getParameter("check_billing_email");
			}
			else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CASH))
			{
				this.billingEmail = request.getParameter("cash_billing_email");
			}
		}
		else
		{
			this.paymentMethod = Invoice.PAYMENT_METHOD_NONE;
		}
		
		return this;
	}
	
	private int setCreditCardType()
	{
		int type = 0;
		
		ArrayList<CreditCardType> types = CreditCardType.get();
		for(CreditCardType t: types)
		{
			if(t.is(this.ccNumber))
			{
				type = t.cc_type_id;
				break;
			}
		}
		
		this.ccType = type;
		
		return this.ccType;
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
	
	public String getCardTypeImage()
	{
		CreditCardType type = this.getCardType();
		if(type != null)
		{
			return type.image_name;
		}
		
		return "";
	}

	
	public CreditCardType getCardType()
	{
		if(this.ccType > 0)
		{
			try
			{
				return new CreditCardType(this.ccType);
			} 
			catch (UnknownObjectException e){}
		}
		
		return null;
	}

	public String getBillingFirstName() {
		return billingFirstName;
	}

	public String getBillingLastName() {
		return billingLastName;
	}
	
	public String getBillingEmail() {
		return billingEmail;
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public String getBillingCity() {
		return billingCity;
	}

	public String getBillingState_id() {
		return billingStateId;
	}

	public String getBillingZip() {
		return billingZip;
	}


	public String getCcNumber() {
		return ccNumber;
	}
	
	

	public String getCcNumberMask() {
		return ccNumberMask;
	}

	public int getCctype() {
		return ccType;
	}

	public String getCcCvv() {
		return ccCvv;
	}

	public int getCcExpMonth() {
		return ccExpMonth;
	}

	public int getCcExpYear() {
		return ccExpYear;
	}
	
	
	public String getBillingStateId() {
		return billingStateId;
	}

	public String getShippingFirstName() {
		return shippingFirstName;
	}

	public String getShippingLastName() {
		return shippingLastName;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public String getShippingCity() {
		return shippingCity;
	}

	public String getShippingStateId() {
		return shippingStateId;
	}

	public String getShippingZip() {
		return shippingZip;
	}
	
	public boolean getPickup()
	{
		return(will_pickup == 1);
	}



	private void setCCNumber(String ccNumber)
	{
		if(ccNumber == null || ccNumber.length() == 0) //anything less than 13 is not valid
		{
			this.ccNumberMask = "";
			this.ccNumber = "";
		}
		else
		{
			this.ccNumber = ccNumber.replaceAll("[^0-9]", "");
			
			//need to mask the credit card and remove the cvv
		    int end = this.ccNumber.length() - 4;
		    if(end > 0)
		    {
		    	this.ccNumberMask = this.ccNumber.substring(0, 4) + StringUtils.repeat("*", end - 4) + this.ccNumber.substring(end) ;
		    }
		    else
		    {
		    	this.ccNumberMask = this.ccNumber;
		    }
		}
	}
	
	public void setInvoiceShipping(Invoice invoice)
	{
		invoice.shipping_first_name = this.shippingFirstName;
		invoice.shipping_last_name = this.shippingLastName;
		invoice.shipping_address = this.shippingAddress;
		invoice.shipping_city = this.shippingCity;
		invoice.shipping_state_id = this.shippingStateId;
		invoice.shipping_zip = this.shippingZip;
		invoice.will_pickup = this.will_pickup;
	}
	
	public void setInvoiceBilling(Invoice invoice)
	{
		if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CC))
		{
			invoice.billing_first_name = this.billingFirstName;
			invoice.billing_last_name = this.billingLastName;
			invoice.billing_email = this.billingEmail;
			invoice.billing_address = this.billingAddress;
			invoice.billing_city = this.billingCity;
			invoice.billing_state_id = this.billingStateId;
			invoice.billing_zip = this.billingZip;
						
			invoice.cc_exp_month = this.ccExpMonth;
			invoice.cc_exp_year = this.ccExpYear;
			invoice.cc_type = this.ccType;
			invoice.cc_number = this.ccNumberMask;
		}
		else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CHECK))
		{
			invoice.check_number = this.checkNumber;
			invoice.billing_email = this.billingEmail;
		}
		else if(this.paymentMethod.equals(Invoice.PAYMENT_METHOD_CASH))
		{
			invoice.billing_email = this.billingEmail;
		}
	}
	
	private static Object _singlePurchase = new Object();
	public void purchasePermit() throws CartException
	{
		synchronized (_singlePurchase)
		{
			Invoice invoice = new Invoice();
			invoice.owner_id = this.owner.owner_id;
			invoice.payment_method = this.paymentMethod;
			
			invoice.setUser(this.user);
			
			int nextNumber = ManagedPermit.getNextPermitNumber();

			this.setInvoiceShipping(invoice);
			this.setInvoiceBilling(invoice);
			
			this.permit.setValidity();
			
			if(this.permit.commit())
			{
				invoice.setPermit(this.permit);

				this.permit.setVehicles(this.vehicles);
								
				if(invoice.commit()) //need to save to get a transaction id
				{
					if(this.requiresCreditCardBilling())
					{
						Merchant merchant;
						try
						{
							merchant = Merchant.factory();
						} 
						catch (ClassNotFoundException e)
						{
							throw new CartException(e.getMessage());
						}
						
						if(merchant != null)
						{
							try
							{
								merchant.chargeCC(invoice, this.ccNumber, this.ccCvv, true);
							}
							catch(MerchantException me)
							{
								System.out.println(me.getMessage());
								invoice.delete();
								this.permit.delete();
								this.permit.mpermit_id = 0;
								ManagedPermit.setNextPermitNumber(nextNumber);
								
								throw new CartException("Error billing merchant: " + me.getMessage());
							}
						}
						else
						{
							invoice.delete();
							this.permit.delete();
							this.permit.mpermit_id = 0;
							ManagedPermit.setNextPermitNumber(nextNumber);
							
							throw new CartException("Merchant has not been setup.");
						}
					}
					
					MailerTask task = new MailerTask();
					task.setMessagePreparator(new PurchaseMessage(owner, invoice));
					Executor.getIntance().addTask(task);
				}
				else
				{
					this.permit.delete();
					this.permit.mpermit_id = 0;
					ManagedPermit.setNextPermitNumber(nextNumber);
					
					throw new CartException("Error saving invoice data.");
				}
				
			}
			else
			{
				throw new CartException("Error saving permit information");
			}
		}
	}
	
	private static Object _singlePurchaseCitation = new Object();
	public void payCitation(HttpServletRequest request) throws CartException, UnknownObjectException
	{
		synchronized (_singlePurchaseCitation)
		{
		
			Citation citation = new Citation(this.citation.citation_id);
			if(!citation.status.equals(Citation.CS_PAID))
			{
				
				Invoice hasInvoice = new Invoice();
				hasInvoice = citation.getInvoice();
				 
				Invoice invoice = new Invoice();
				invoice.owner_id = this.citation.owner_id;
				invoice.payment_method = this.paymentMethod;
				
				invoice.setUser(this.user);
				this.setInvoiceBilling(invoice);
				
				//invoice.setCitationForPayment(this.citation, this.citationAmount, invoice);
				// see if we need to update the amounts
				if(this.citationAmount > -1)
				{
					InvoiceItem item = invoice.getItem(InvoiceItem.TYPE_PAYMENT);
					if(item != null)
					{
						item.amount = this.citationAmount;
					}
				}
								
				if(this.citationAmount > -1 || this.citationLateFee > -1)
				{
					invoice.setAmount();
				}
						
				if(hasInvoice == null){
				
					invoice.setCitationForPayment(this.citation, this.citationAmount, invoice);
					if(invoice.commit()) //need to save to get a transaction id
					{
						if(this.requiresCreditCardBilling())
						{
							Merchant merchant;
							try
							{
								merchant = Merchant.factory();
							} 
							catch (ClassNotFoundException e)
							{
								throw new CartException(e.getMessage());
							}
							
							if(merchant != null)
							{
								try
								{
									merchant.chargeCC(invoice, this.ccNumber, this.ccCvv, true);
									
									float amount = invoice.getItemsAmount();
									float citeAmount = 0;
									if( this.citation.getLateFee() != null){
										citeAmount = this.citation.violation_amount + this.citation.getLateFee().fee_amount;
									}else {
										citeAmount = this.citation.violation_amount;
									}
									
									if(amount >= citeAmount){
										this.citation.status_id = Citation.CS_PAID;
									}
									
									this.citation.commit(); 
									
									if(request.getParameter("payment_plan_id") != null){
										CitationPaymentPlan citationPaymentPlan = new CitationPaymentPlan(Integer.parseInt(request.getParameter("payment_plan_id")));
										citationPaymentPlan.paid = 1;
										/*if(citationPaymentPlan.paid == citationPaymentPlan.number_payment){
											citationPaymentPlan.status = "Paid";
										}*/										
										citationPaymentPlan.commit();
									}	
									
									
									//request.getParameter("billing_first_name")
									request.getParameter("billing_first_name");
									MailerTask task = new MailerTask();
									task.setMessagePreparator(new PaymentMessage(this.citation, invoice));
									Executor.getIntance().addTask(task);
								}
								catch(MerchantException me)
								{	
									System.out.println(me.getMessage());
									invoice.delete();
									throw new CartException("Error billing merchant: " + me.getMessage());
								}
							}
							else
							{
								invoice.delete();				
								throw new CartException("Merchant has not been setup.");
							}
						}
						else
						{
							float amount = invoice.getItemsAmount();
							float citeAmount = 0;
							if( this.citation.getLateFee() != null){
								citeAmount = this.citation.violation_amount + this.citation.getLateFee().fee_amount;
							}else {
								citeAmount = this.citation.violation_amount;
							}
							
							if(amount >= citeAmount){
								this.citation.status_id = Citation.CS_PAID;
							}
							
							
							this.citation.commit();
							
							if(request.getParameter("payment_plan_id") != null){
								CitationPaymentPlan citationPaymentPlan = new CitationPaymentPlan(Integer.parseInt(request.getParameter("payment_plan_id")));
								citationPaymentPlan.paid = 1;
								/*if(citationPaymentPlan.paid == citationPaymentPlan.number_payment){
									citationPaymentPlan.status = "Paid";
								}*/
								citationPaymentPlan.commit();
							}
							
							MailerTask task = new MailerTask();
							task.setMessagePreparator(new PaymentMessage(this.citation, invoice));
							Executor.getIntance().addTask(task);
						}
					}
					else
					{
						throw new CartException("Error saving invoice data.");
					}
				} else { // if invoice exist insert only item
					
					invoice.invoice_id = hasInvoice.invoice_id;
					InvoiceItem invoiceItem = new InvoiceItem(InvoiceItem.TYPE_PAYMENT, this.citationAmount, "Citation Payment" ,invoice);
					
					if(invoiceItem.commit()) //need to save to get a transaction id
					{
						if(this.requiresCreditCardBilling())
						{
							Merchant merchant;
							try
							{
								merchant = Merchant.factory();
							} 
							catch (ClassNotFoundException e)
							{
								throw new CartException(e.getMessage());
							}
							
							if(merchant != null)
							{
								try
								{							
									merchant.chargeCCItem(invoiceItem, this.ccNumber, this.ccCvv, true);
									hasInvoice = citation.getInvoice();
									float amount = hasInvoice.getItemsAmount();
									float citeAmount = 0;
																		
									if( this.citation.getLateFee() != null){
										citeAmount = this.citation.violation_amount + this.citation.getLateFee().fee_amount;
									}else {
										citeAmount = this.citation.violation_amount;
									}
									
									if(amount >= citeAmount){
										this.citation.status_id = Citation.CS_PAID;
									}
									
									this.citation.commit();
									
									if(request.getParameter("payment_plan_id") != null){
										CitationPaymentPlan citationPaymentPlan = new CitationPaymentPlan(Integer.parseInt(request.getParameter("payment_plan_id")));
										citationPaymentPlan.paid = 1;
										/*if(citationPaymentPlan.paid == citationPaymentPlan.number_payment){
											citationPaymentPlan.status = "Paid";
										}*/
										citationPaymentPlan.commit();
									}
									MailerTask task = new MailerTask();
									task.setMessagePreparator(new PaymentMessage(this.citation, invoice));
									Executor.getIntance().addTask(task);
								}
								catch(MerchantException me)
								{	
									System.out.println(me.getMessage());
									invoice.deleteItem();
									throw new CartException("Error billing merchant: " + me.getMessage());
								}
							}
							else
							{
								invoice.deleteItem();			
								throw new CartException("Merchant has not been setup.");
							}
						}
						else
						{
							hasInvoice = citation.getInvoice();
							float amount = hasInvoice.getItemsAmount();
							float citeAmount = 0;
																				
							if( this.citation.getLateFee() != null){
								citeAmount = this.citation.violation_amount + this.citation.getLateFee().fee_amount;
							}else {
								citeAmount = this.citation.violation_amount;
							}
													
							if(amount >= citeAmount){
								this.citation.status_id = Citation.CS_PAID;
							}
							
							this.citation.commit();
							
							if(request.getParameter("payment_plan_id") != null){
								CitationPaymentPlan citationPaymentPlan = new CitationPaymentPlan(Integer.parseInt(request.getParameter("payment_plan_id")));
								citationPaymentPlan.paid = 1;
								/*if(citationPaymentPlan.paid == citationPaymentPlan.number_payment){
									citationPaymentPlan.status = "Paid";
								}*/
								citationPaymentPlan.commit();
							}
							
							
							MailerTask task = new MailerTask();
							task.setMessagePreparator(new PaymentMessage(this.citation, invoice));
							Executor.getIntance().addTask(task);
						}
					}else {
						throw new CartException("Error saving invoice data.");
					}
					
				}
				
				
			}
			else
			{
				throw new CartException("Error pay citation.");
			}
		}
	}
}