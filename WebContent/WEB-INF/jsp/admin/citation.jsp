<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.*" %>

<p></p>
<div style="margin: -20px 0px 20px 0px;">
	<div id="saveCitation" style="float: left"></div>
	<div style="clear: left;"></div>
</div>
<div class="form-header">
	<dl class="config-form">
		<dt><b>Field Name</b></dt>
		<dd><div class="x-tool-btn btn-add" title="Add Column" id="citeAddField"></div></dd>
	</dl>
</div>
<div id="field-container">
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

<div style="clear: left;"></div>
<script type="text/javascript">
	var citeFields = new CiteConfiguration({url: '<%= request.getContextPath() %>/admin/administration/citation', container: $('field-container'), btnAdd: $('citeAddField')});
</script>