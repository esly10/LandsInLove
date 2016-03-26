package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class PaymentMethod  extends DBObject{
	@Expose public int payment_method_id = 0;
	@Expose public String payment_method_description = null;
	
	public PaymentMethod() throws UnknownObjectException
	{
		this(0);
	}
	
	public PaymentMethod(int payment_method_id) throws UnknownObjectException
	{
		super("payment_method", "payment_method_id");
		if(payment_method_id > 0)
		{
			this.payment_method_id = payment_method_id;
			this.populate();
		}
	}
	
	public int getPayment_method_id() {
		return payment_method_id;
	}
	public void setPayment_method_id(int payment_method_id) {
		this.payment_method_id = payment_method_id;
	}
	public String getPayment_method_description() {
		return payment_method_description;
	}
	public void setPayment_method_description(String payment_method_description) {
		this.payment_method_description = payment_method_description;
	}
}
