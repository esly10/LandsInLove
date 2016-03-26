package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class ReservationType extends DBObject{
	@Expose public int reservation_type_id = 0;
	@Expose public String reservation_type_description = null;
	
	public ReservationType() throws UnknownObjectException
	{
		this(0);
	}
	
	public ReservationType(int reservation_type_id) throws UnknownObjectException
	{
		super("reservation_type", "reservation_type_id");
		if(reservation_type_id > 0)
		{
			this.reservation_type_id = reservation_type_id;
			this.populate();
		}
	}
	
	public int getReservation_type_id() {
		return reservation_type_id;
	}
	public void setReservation_type_id(int reservation_type_id) {
		this.reservation_type_id = reservation_type_id;
	}
	public String getReservation_type_description() {
		return reservation_type_description;
	}
	public void setReservation_type_description(String reservation_type_description) {
		this.reservation_type_description = reservation_type_description;
	}
	
}