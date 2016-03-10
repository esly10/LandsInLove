<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.*" %>

<dl>
	<dt>Permit Number Counter</dt>
	<dd><input type="text" name="permit_number" id="permit_number" value="<c:out value="${permit_number}" />" class="x-form-text" style="width: 200px;"/>
	<dt>Permit Number Format</dt>
	<dd><input type="text" name="permit_number_format" id="permit_number_format" value="<c:out value="${permit_number_format}" />" class="x-form-text" style="width: 200px;"/>
</dl>
<div style="margin: 10px 0px 20px 0px;">
	<div id="savePermitFormat" style="float: left"></div>
	<div style="clear: left;"></div>
</div>
<div style="width: 150px; float: left;">
	<div class="form-header">
		<dl class="config-form">
			<dt><b>Default Fields</b></dt>
		</dl>
	</div>
	<div>
		<dl class="config-form">
			<dt>Permit Number</dt>
			<dt style="clear: left;">Status</dt>
			<dt style="clear: left;">Validity</dt>
		</dl>
	</div>
</div>

<div style="width: 400px; float: left;">
	<div class="form-header">
		<dl class="config-form">
			<dt><b>Additional Fields</b></dt>
			<dd><div class="x-tool-btn btn-add" title="Add Column" id="permitAddField"></div></dd>
		</dl>
	</div>
	<div id="permit-field-container">
		<c:forEach items="${fields}" var="field">
			<div class="form-row">
				<dl class="config-form">
					<dt><c:out value="${field.label}" /><input type="hidden" id="field_name" name="field_name" value="<c:out value="${field.name}" />" /></dt>
					<dd>
						<div class="x-tool-btn btn-up" title="Move Field Up"></div>
						<div class="x-tool-btn btn-down" title="Move Field Down"></div>
						<div class="x-tool-btn btn-edit" title="Field Settings"></div>
						<div class="x-tool-btn btn-remove" title="Remove Field"></div>
					</dd>
				</dl>
			</div>
		</c:forEach>
		<div style="clear: left;"></div>
	</div>
</div>
<div style="clear: left;"></div>
<script type="text/javascript">
	var permitFields = new PermitFieldConfiguration({url: '<%= request.getContextPath() %>/admin/managedpermit/field', container: $('permit-field-container'), btnAdd: $('permitAddField'), numCounter: $('permit_number'), numFormat: $('permit_number_format')});
</script>
<script type="text/javascript">
	var saveBtn = new Ext.Button({renderTo:'savePermitFormat', text:'Save', width: '70px'});
	saveBtn.handler = permitFields.save.bind(permitFields);
</script>