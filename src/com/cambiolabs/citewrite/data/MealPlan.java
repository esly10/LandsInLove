package com.cambiolabs.citewrite.data;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class MealPlan extends DBObject{
	@Expose public int meal_plan_id = 0;
	@Expose public String meal_plan_description = null;
	
	public MealPlan() throws UnknownObjectException
	{
		this(0);
	}
	
	public MealPlan(int meal_plan_id) throws UnknownObjectException
	{
		super("meal_plan", "meal_plan_id");
		if(meal_plan_id > 0)
		{
			this.meal_plan_id = meal_plan_id;
			this.populate();
		}
	}
	
	public int getMeal_plan_id() {
		return meal_plan_id;
	}
	public void setMeal_plan_id(int meal_plan_id) {
		this.meal_plan_id = meal_plan_id;
	}
	public String getMeal_plan_description() {
		return meal_plan_description;
	}
	public void setMeal_plan_description(String meal_plan_description) {
		this.meal_plan_description = meal_plan_description;
	}
	
}
