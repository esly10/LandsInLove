<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.cambiolabs.citewrite.data.PermitColumnMetaData" %>
<%@ page import="com.cambiolabs.citewrite.data.CiteFields" %>
<%@ page import="com.cambiolabs.citewrite.data.CiteField" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.GsonBuilder" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilter" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilterList" %>

<% 
	ConfigItem item = null;
	try
	{
		item = new ConfigItem("PERMIT_PATH");
	}
	catch(Exception e)
	{
		item = new ConfigItem();
		item.name = "PERMIT_PATH";
		item.text_value = "";
	}
%>
<p>Use the form below to define your permit file. If you are updating the permit file, the permit table will be recreated and the mobile devices will require a full sync.</p>
<dl style="margin-bottom: 10px;">
	<dt>Permit File Path</dt>
	<dd><input type="text" name="permit_path" id="permit_path" value="<%= item.text_value %>" class="x-form-text" style="width: 270px;"/>
</dl>

<div class="form-header">
	<dl class="config-form">
		<dt>Field Name</dt>
		<dt style="margin: 0px 0px 5px 10px;">Citation Mapping</dt>
		<dt style="width: 50px;margin: 0px 0px 5px 10px;">Display</dt>
		<dt style="width: 60px;margin: 0px 0px 5px 10px;">Searchable</dt>
		<dd><div class="x-tool-btn btn-add" title="Add Column" id="permitAddColumn"></div></dd>
	</dl>
</div>
<div id="column-container">

	<%
		ArrayList<CiteField> citeFields = (new CiteFields()).getFields();
		ArrayList<PermitColumnMetaData> lists = PermitColumnMetaData.get();
		Gson gson = new GsonBuilder().create();
	%>
	
	<script type="text/javascript">
		var _citeColumnFields = <%= gson.toJson(citeFields) %>;
	</script>

	<%

			for(PermitColumnMetaData permitColumnMetaData : lists)
			{

	%>
	<div class="form-row">
		<dl class="config-form">
			<dt><input type="text" class="x-form-text" style="width: 80%"  id="column_name" name="column_name" value="<%= permitColumnMetaData.label %>" maxlength="29"/></dt>
			<dd>
				<select class="x-form-select" id="mapping" name="mapping" >
					<option value="">-- None --</option>
				<% for(CiteField field: citeFields){ %>
					<option value="<%= field.name %>" <%= (permitColumnMetaData.mapping.equals(field.name))?"selected":"" %>><%= field.label %></option>
				<% } %>
				</select>
			</dd>
			<dd class="display-order"><input type="text" class="x-form-text" id="column_display_order" name="column_display_order" value="<%= permitColumnMetaData.displayOrder %>" style="width: 40px;"/></dd>
			<dd class="searchable"><input type="checkbox" class="x-form-checkbox" id="column_searchable" name="column_searchable" value="1" <%= (permitColumnMetaData.searchable)?"checked=\"checked\"":"" %>/></dd>
			<dd>
				<div class="x-tool-btn btn-up" title="Move Column Up"></div>
				<div class="x-tool-btn btn-down" title="Move Column Down"></div>
				<div class="x-tool-btn btn-remove" title="Remove Column"></div>
			</dd>
		</dl>
	</div>
	<% }  %>
	<div style="clear: left;"></div>
</div>
<div style="clear: left;"></div>
<script type="text/javascript">
	var permitFile = new FileConfiguration({type: 'permit', mappingSource: _citeColumnFields, url: '<%= request.getContextPath() %>/admin/administration/permit', container: $('column-container'), btnAdd: $('permitAddColumn'),  rowCount: <%=  lists != null? lists.size() : 0 %>, path: $('permit_path')});
</script>
<div style="padding-top: 10px;">
	<div id="savePermit" style="float: left"></div>
</div>
<script type="text/javascript">
	var saveBtn = new Ext.Button({renderTo:'savePermit', text:'Save', width: '70px'});
	saveBtn.handler = permitFile.save.bind(permitFile);
</script>