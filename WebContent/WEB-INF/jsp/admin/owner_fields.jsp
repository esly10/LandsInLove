<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.*" %>

<div style="width: 150px; float: left;">
	<div class="form-header">
		<dl class="config-form">
			<dt><b>Default Fields</b></dt>
		</dl>
	</div>
	<div>
		<dl class="config-form">
			<% 
				ArrayList<OwnerField> ofields = (new OwnerFields()).getDefaultFields();
				for(OwnerField field: ofields)
				{
			%>
			<dt style="clear: left;"><%= field.label %></dt>
			<% } %>
		</dl>
	</div>
</div>
<div style="width: 400px; float: left;">
	<div class="form-header">
		<dl class="config-form">
			<dt><b>Additional Fields</b></dt>
			<dd><div class="x-tool-btn btn-add" title="Add Column" id="ownerAddField"></div></dd>
		</dl>
	</div>
	<div id="owner-field-container">
		<%
			OwnerFields mpFields = new OwnerFields();
			ArrayList<OwnerField> fields = mpFields.getFields();
			
			for(OwnerField field: fields) 
			{
				
		%>
		<div class="form-row">
			<dl class="config-form">
				<dt><%= field.label %><input type="hidden" id="field_name" name="field_name" value="<%= field.name %>" /></dt>
				<dd>
					<div class="x-tool-btn btn-up" title="Move Field Up"></div>
					<div class="x-tool-btn btn-down" title="Move Field Down"></div>
					<div class="x-tool-btn btn-edit" title="Field Settings"></div>
					<div class="x-tool-btn btn-remove" title="Remove Field"></div>
				</dd>
			</dl>
		</div>
		<% }  %>
		<div style="clear: left;"></div>
	</div>
</div>
<div style="clear: left;"></div>
<script type="text/javascript">
	new FieldConfiguration({url: '<%= request.getContextPath() %>/admin/owner/field', container: $('owner-field-container'), btnAdd: $('ownerAddField')});
</script>