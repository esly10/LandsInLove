package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class ReservationStatus extends DBObject{
	@Expose public int reservation_status_id = 0;
	@Expose public String reservation_status_description = null;
	
	public ReservationStatus() throws UnknownObjectException
	{
		this(0);
	}
	
	public ReservationStatus(int reservation_status_id) throws UnknownObjectException
	{
		super("reservation_status", "reservation_status_id");
		if(reservation_status_id > 0)
		{
			this.reservation_status_id = reservation_status_id;
			this.populate();
		}
	}
	
	public int getReservation_status_id() {
		return reservation_status_id;
	}
	public void setReservation_status_id(int reservation_status_id) {
		this.reservation_status_id = reservation_status_id;
	}
	public String getReservation_status_description() {
		return reservation_status_description;
	}
	public void setReservation_status_description(String reservation_status_description) {
		this.reservation_status_description = reservation_status_description;
	}
	
}
