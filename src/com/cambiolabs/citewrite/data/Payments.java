package com.cambiolabs.citewrite.data;

import java.sql.Timestamp;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class Payments extends DBObject
{	

	@Expose public int payment_id = 0;
	@Expose public int reservation_id = 0;	
	@Expose public Timestamp payment_date = null;	
	@Expose public String payment_method = null;
	@Expose public Timestamp receive_date = null;
	@Expose public int transaction_no = 0;
	@Expose public String back_account = null;
	@Expose public float amount = 0;
	@Expose public String bill_to = null;
	@Expose public String payment_notes = null;
	

	//private static final String UTF_8 = "UTF-8";
	
	public Payments() throws UnknownObjectException
	{
		this(0);
	}
	
	public Payments(int payment_id) throws UnknownObjectException
	{
		super("payments", "payment_id");
		if(payment_id > 0)
		{
			this.payment_id = payment_id;
			this.populate();
		}
	}

	public int getPayment_id() {
		return payment_id;
	}

	public void setPayment_id(int payment_id) {
		this.payment_id = payment_id;
	}

	public Timestamp getPayment_date() {
		return payment_date;
	}

	public void setPayment_date(Timestamp payment_date) {
		this.payment_date = payment_date;
	}

	public String getPayment_method() {
		return payment_method;
	}

	public void setPayment_method(String payment_method) {
		this.payment_method = payment_method;
	}

	public Timestamp getReceive_date() {
		return receive_date;
	}

	public void setReceive_date(Timestamp receive_date) {
		this.receive_date = receive_date;
	}

	public int getTransaction_no() {
		return transaction_no;
	}

	public void setTransaction_no(int transaction_no) {
		this.transaction_no = transaction_no;
	}

	public String getBack_account() {
		return back_account;
	}

	public void setBack_account(String back_account) {
		this.back_account = back_account;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public String getBill_to() {
		return bill_to;
	}

	public void setBill_to(String bill_to) {
		this.bill_to = bill_to;
	}

	public String getPayment_notes() {
		return payment_notes;
	}

	public void setPayment_notes(String payment_notes) {
		this.payment_notes = payment_notes;
	}

	public int getReservation_id() {
		return reservation_id;
	}

	public void setReservation_id(int reservation_id) {
		this.reservation_id = reservation_id;
	}
	
	
	

		
}