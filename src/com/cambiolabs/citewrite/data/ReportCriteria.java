package com.cambiolabs.citewrite.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ReportCriteria implements Serializable {

	private static final long serialVersionUID = 4042796433722449007L;

	@Expose
	public String name = null;
	@Expose
	public String operator = null;
	@Expose
	public String value = null;
	@Expose
	public String betweenValue = null;
	@Expose
	public String logicOperator = null;
	@Expose
	public int order = 0;

	public ReportCriteria() {
		super();
	}
	

	public ReportCriteria(String name, String operator, String value, String betweenValue,
			String logicOperator, int order) {
		this.name = name;
		this.operator = operator;
		this.value = value;
		this.betweenValue = betweenValue;
		this.logicOperator = logicOperator;
		this.order = order;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getBetweenValue() {
		return betweenValue;
	}

	public void setBetweenValue(String betweenValue) {
		this.betweenValue = betweenValue;
	}

	public String getLogicOperator() {
		return logicOperator;
	}

	public void setLogicOperator(String logicOperator) {
		this.logicOperator = logicOperator;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
