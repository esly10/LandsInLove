<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilter" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilterList" %>
<%@ page import="com.cambiolabs.citewrite.data.HotListColumnMetaData" %>
<%@ page import="com.cambiolabs.citewrite.data.PermitColumnMetaData" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.GsonBuilder" %>

<% 
	ConfigItem item = null;
	try
	{
		item = new ConfigItem("HOTLIST_PATH");
	}
	catch(Exception e)
	{
		item = new ConfigItem();
		item.name = "HOTLIST_PATH";
		item.text_value = "";
	}
%>
<p>Use the form below to define your hotlist file. If you are updating the hotlist file, the hotlist table will be recreated and the mobile devices will require a full sync.</p>
<dl style="margin-bottom: 10px;">
	<dt>Permit File Path</dt>
	<dd><input type="text" name="hotlist_path" id="hotlist_path" value="<%= item.text_value %>" class="x-form-text" style="width: 270px;"/>
</dl>

<div class="form-header">
	<dl class="config-form">
		<dt>Field Name</dt>
		<dt style="margin: 0px 0px 5px 10px;">Permit Mapping</dt>
		<dt style="width: 50px;margin: 0px 0px 5px 10px;">Display</dt>
		<dd><div class="x-tool-btn btn-add" title="Add Column" id="hotlistAddColumn"></div></dd>
	</dl>
</div>
<div id="hotlist-column-container">
	<% 
		ArrayList<PermitColumnMetaData> permitColumnMetaDataList = PermitColumnMetaData.get(true);
		ArrayList<HotListColumnMetaData> hotListColumnMetaDataList = HotListColumnMetaData.get(false);
		Gson gson = new GsonBuilder().create();
	%>
	
	<script type="text/javascript">
		var _permitColumnFields = <%= gson.toJson(permitColumnMetaDataList) %>;
	</script>

	<%
		
		if(hotListColumnMetaDataList.size() == 0)
		{
	%>	
		Mensaje de no hay hostlist field configurados
	<% 
		} 
		else 
		{
			
			for(HotListColumnMetaData hotListColumnMetaData : hotListColumnMetaDataList)
			{
	%>
	<div class="form-row">
		<dl class="config-form">
			<dt><input type="text" class="x-form-text" style="width: 80%" id="column_name" name="column_name" value="<%= hotListColumnMetaData.label%>" maxlength="29"/></dt>
			<dd>
				<select class="x-form-select" id="mapping" name="mapping" >
					<option value="">-- None --</option>
				<% for(PermitColumnMetaData permitColumnMetaData: permitColumnMetaDataList){ %>
					<option value="<%= permitColumnMetaData.columnName %>" <%= (permitColumnMetaData.columnName.equals(hotListColumnMetaData.mapping))?"selected":"" %>><%= permitColumnMetaData.label %></option>
				<% } %>
				</select>
			</dd>
			<dd class="display-order"><input type="text" class="x-form-text" id="column_display_order" name="column_display_order" value="<%= hotListColumnMetaData.displayOrder%>" style="width: 40px;"/></dd>
			<dd>
				<div class="x-tool-btn btn-up" title="Move Column Up"></div>
				<div class="x-tool-btn btn-down" title="Move Column Down"></div>
				<div class="x-tool-btn btn-remove" title="Remove Column"></div>
			</dd>
		</dl>
	</div>
	<% } } %>
	<div style="clear: left;"></div>
</div>
<div style="clear: left;"></div>
<script type="text/javascript">
	var hotlistFile = new FileConfiguration({type: 'hotlist', mappingSource: _permitColumnFields, url: '<%= request.getContextPath() %>/admin/hotlist/list', container: $('hotlist-column-container'), btnAdd: $('hotlistAddColumn'), rowCount: <%=  hotListColumnMetaDataList != null? hotListColumnMetaDataList.size() : 0 %>, path: $('hotlist_path')});
</script>
<div style="padding-top: 10px;">
	<div id="saveHotlist" style="float: left"></div>
</div>
<script type="text/javascript">
	var saveBtn = new Ext.Button({renderTo:'saveHotlist', text:'Save', width: '70px'});
	saveBtn.handler = hotlistFile.save.bind(hotlistFile);
</script>