package com.cambiolabs.citewrite.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ReportField implements Serializable {

	private static final long serialVersionUID = 248563549316682941L;

	@Expose
	public String name = null;
	@Expose
	public String label = null;
	@Expose
	public boolean view = false;
	@Expose
	public boolean export = false;
	@Expose
	public int order = 0;

	public ReportField() {
		super();
	}

	public ReportField(String name, String label, boolean view, boolean export, int order) {
		this.name = name;
		this.view = view;
		this.export = export;
		this.order= order;
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isView() {
		return view;
	}

	public void setView(boolean view) {
		this.view = view;
	}

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
