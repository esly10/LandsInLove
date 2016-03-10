<%@page import="com.cambiolabs.citewrite.data.PermitColumnMetaData"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.PermitColumnMetaData" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitFields" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitField" %>
<%@ page import="com.cambiolabs.citewrite.data.OwnerFields" %>
<%@ page import="com.cambiolabs.citewrite.data.OwnerField" %>
<%@ page import="com.cambiolabs.citewrite.data.VehicleFields" %>
<%@ page import="com.cambiolabs.citewrite.data.VehicleField" %>
<%@ page import="com.cambiolabs.citewrite.data.CiteFields" %>
<%@ page import="com.cambiolabs.citewrite.data.CiteField" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilter" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilterList" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.GsonBuilder" %>

<div class="form-header">
	<dl class="config-form">
		<dt>Field Name</dt>
		<dt style="margin: 0px 0px 5px 10px;">Citation Mapping</dt>
		<dt style="width: 60px;margin: 0px 0px 5px 10px;">Searchable</dt>
		<dd><div class="x-tool-btn btn-add" title="Add Column" id="permitViewAddColumn"></div></dd>
	</dl>
</div>
<div id="column-container">
	<%
		ArrayList<ManagedPermitField> defaultPfields = (new ManagedPermitFields()).getDefaultFields();
		ArrayList<ManagedPermitField> pfields = (new ManagedPermitFields()).getFields();
		ArrayList<OwnerField> defaultOfields = (new OwnerFields()).getDefaultFields();
		ArrayList<OwnerField> ofields = (new OwnerFields()).getFields();
		ArrayList<VehicleField> defaultVfields = (new VehicleFields()).getDefaultFields();
		ArrayList<VehicleField> vfields = (new VehicleFields()).getFields();
		ArrayList<CiteField> citeFields = (new CiteFields()).getFields();
		
		ArrayList<CiteField> citeFieldTempList = new ArrayList<CiteField>();
		for(CiteField citeField : citeFields){
			if((citeField.name.equals("date_time")) || (citeField.name.equals("officer_id")) || (citeField.name.equals("violation")) || (citeField.name.equals("location")) || (citeField.name.equals("comment"))){
				citeFieldTempList.add(citeField);
			}	
		}
		
		citeFields.removeAll(citeFieldTempList);
		
		Gson gson = new GsonBuilder().create();
	%>
	<script type="text/javascript">
		var _defaultPermitFields = <%= gson.toJson(defaultPfields) %>;
		var _permitFields = <%= gson.toJson(pfields) %>;
		var _defaultOwnerFields = <%= gson.toJson(defaultOfields) %>;
		var _ownerFields = <%= gson.toJson(ofields) %>;
		var _defaultVehicleFields = <%= gson.toJson(defaultVfields) %>;
		var _vehicleFields = <%= gson.toJson(vfields) %>;
		var _citeFields = <%= gson.toJson(citeFields) %>;
	</script>
	<%
	
		ArrayList<PermitColumnMetaData> list = PermitColumnMetaData.get();
		for(PermitColumnMetaData item : list)
		{
	%>
	<div class="form-row">
		<dl class="config-form">
			<dt>
				<select class="x-form-select" id="column_name" name="column_name" >
					<optgroup label="Permit">
						<% for(ManagedPermitField field: defaultPfields){ String fieldName = "mpermit."+field.name;
						if(field.name.equals("permit_type")){
							fieldName = "mpermit_type.name";
						}
						if(field.name.equals("valid_end_date")){
							fieldName = "mpermit.valid_end_date";
						}
						%>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
						<% for(ManagedPermitField field: pfields){ String fieldName = "mpermit_attribute."+field.name; %>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
					</optgroup>
					<optgroup label="Vehicle">
						<% for(VehicleField field: defaultVfields){ String fieldName = "vehicle."+field.name; %>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
						<% for(VehicleField field: vfields){ String fieldName = "vehicle_attribute."+field.name; %>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
					</optgroup>
					<optgroup label="Owner">
						<% for(OwnerField field: defaultOfields){ String fieldName = "owner."+field.name; if(field.name.equals("password")){ continue; } %>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
						<% for(OwnerField field: ofields){ String fieldName = "owner_attribute."+field.name;%>
						<option value="<%= fieldName %>" <%= (item.queryName.equals(fieldName))?"selected":"" %>><%= field.label %></option>
						<% } %>
					</optgroup>
				</select>
			</dt>
			<dt>&nbsp;
				<select class="x-form-select" id="mapping" name="mapping" >
					<option value="">-- None --</option>
				<% for(CiteField field: citeFields){ %>
					<option value="<%= field.name %>" <%= (item.mapping.equals(field.name))?"selected":"" %>><%= field.label %></option>
				<% } %>
				</select>
			</dt>
			<dd class="searchable"><input type="checkbox" class="x-form-checkbox" id="column_searchable" name="column_searchable" value="1" <%= (item.searchable)?"checked=\"checked\"":"" %>/></dd>
			<dd>
				<div class="x-tool-btn btn-up" title="Move Column Up"></div>
				<div class="x-tool-btn btn-down" title="Move Column Down"></div>
				<div class="x-tool-btn btn-remove" title="Remove Column"></div>
			</dd>
		</dl>
	</div>
	<% } %>
	<div style="clear: left;"></div>
</div>
<div style="clear: left;"></div>
<script type="text/javascript">
	var deviceView = new PermitDeviceView({url: '<%= request.getContextPath() %>/admin/managedpermit/deviceView', container: $('column-container'), btnAdd: $('permitViewAddColumn'), rowCount: 3});
</script>
<div style="padding-top: 10px;">
	<div id="saveDeviceView" style="float: left"></div>
</div>
<script type="text/javascript">
	var saveBtn = new Ext.Button({renderTo:'saveDeviceView', text:'Save', width: '70px'});
	saveBtn.handler = deviceView.save.bind(deviceView);
</script>