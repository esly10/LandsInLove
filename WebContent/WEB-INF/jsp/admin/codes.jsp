<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilter" %>
<%@ page import="com.cambiolabs.citewrite.db.DBFilterList" %>
<%@ page import="java.util.ArrayList" %>

<% ConfigItem item = ConfigItem.lookup("CODES_PATH"); %>
<p>Use the form below to define your codes XML file. If you are updating the codes file, the codes table will be recreated and the mobile devices will require a full sync.</p>
<dl style="margin-bottom: 10px;">
	<dt>Codes XML File Path</dt>
	<dd><input type="text" name="codes_path" id="codes_path" value="<%= item.text_value %>" class="x-form-text" style="width: 270px;"/>
</dl>
<form name="codes-form" id="codes-form">
	<div id="codes-container" style="width: 600px;">
		<div class="form-row" style="float: left; width: 300px;">
			<h2>States</h2>
			<dl class="config-form codes-form">
				<% item = ConfigItem.lookup("CODES_STATES_ENABLED"); %>
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="STATES_ENABLED" name="STATES_ENABLED" value="y" style="width: auto;" <%= (item != null)?"checked=\"checked\"":"" %>/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_STATES_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="STATES_XPATH" name="STATES_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_STATES_ID"); %>
				<dd><input type="text" class="x-form-text" id="STATES_ID" name="STATES_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_STATES_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="STATES_DESCRIPTION" name="STATES_DESCRIPTION" value="<%= item.text_value %>" /></dd>
			</dl>
		</div>	
		<div class="form-row" style="float: left; clear: none; width: 300px;">
			<h2>Makes</h2>
			<dl class="config-form codes-form">
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="MAKES_ENABLED" name="MAKES_ENABLED" value="y" style="width: auto;"/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_MAKES_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="MAKES_XPATH" name="MAKES_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_MAKES_ID"); %>
				<dd><input type="text" class="x-form-text" id="MAKES_ID" name="MAKES_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_MAKES_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="MAKES_DESCRIPTION" name="MAKES_DESCRIPTION" value="<%= item.text_value %>" /></dd>
			</dl>
		</div>
		<div class="form-row" style="float: left; clear: none; width: 300px;">
			<h2>Colors</h2>
			<dl class="config-form codes-form">
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="COLORS_ENABLED" name="COLORS_ENABLED" value="y" style="width: auto;"/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_COLORS_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="COLORS_XPATH" name="COLORS_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_COLORS_ID"); %>
				<dd><input type="text" class="x-form-text" id="COLORS_ID" name="COLORS_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_COLORS_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="COLORS_DESCRIPTION" name="COLORS_DESCRIPTION" value="<%= item.text_value %>" /></dd>
			</dl>
		</div>
		<div class="form-row" style="float: left; clear: none; width: 300px">
			<h2>Locations</h2>
			<dl class="config-form codes-form">
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="LOCATIONS_ENABLED" name="LOCATIONS_ENABLED" value="y" style="width: auto;"/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_LOCATIONS_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="LOCATIONS_XPATH" name="LOCATIONS_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_LOCATIONS_ID"); %>
				<dd><input type="text" class="x-form-text" id="LOCATIONS_ID" name="LOCATIONS_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_LOCATIONS_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="LOCATIONS_DESCRIPTION" name="LOCATIONS_DESCRIPTION" value="<%= item.text_value %>" /></dd>
			</dl>
		</div>
		<div class="form-row" style="float: left; clear: none; width: 300px">
			<h2>Comments</h2>
			<dl class="config-form codes-form">
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="COMMENTS_ENABLED" name="COMMENTS_ENABLED" value="y" style="width: auto;"/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_COMMENTS_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="COMMENTS_XPATH" name="COMMENTS_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_COMMENTS_ID"); %>
				<dd><input type="text" class="x-form-text" id="COMMENTS_ID" name="COMMENTS_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_COMMENTS_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="COMMENTS_DESCRIPTION" name="COMMENTS_DESCRIPTION" value="<%= item.text_value %>" /></dd>
			</dl>
		</div>
		<div class="form-row" style="float: left; clear: none; width: 300px;">
			<h2>Violations</h2>
			<dl class="config-form codes-form">
				<dd style="margin: 0 0 5px 00px;"><input type="checkbox" class="x-form-checkbox" id="VIOLATIONS_ENABLED" name="VIOLATIONS_ENABLED" value="y" style="width: auto;"/>&nbsp;&nbsp;Enabled</dd>
				<dt>XPath</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_XPATH"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_XPATH" name="VIOLATIONS_XPATH" value="<%= item.text_value %>" /></dd>
				<dt>ID</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_ID"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_ID" name="VIOLATIONS_ID" value="<%= item.text_value %>" /></dd>
				<dt>Descrption</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_DESCRIPTION"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_DESCRIPTION" name="VIOLATIONS_DESCRIPTION" value="<%= item.text_value %>" /></dd>
				<dt>Is Overtime</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_OVERTIME"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_OVERTIME" name="VIOLATIONS_OVERTIME" value="<%= item.text_value %>" /></dd>
				<dt>Amount</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_AMOUNT"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_AMOUNT" name="VIOLATIONS_AMOUNT" value="<%= item.text_value %>" /></dd>
				<dt>Type</dt>
				<% item = ConfigItem.lookup("CODES_VIOLATIONS_TYPE"); %>
				<dd><input type="text" class="x-form-text" id="VIOLATIONS_TYPE" name="VIOLATIONS_TYPE" value="<%= item.text_value %>" /></dd>
			</dl>
			<div style="clear: left;"></div>
		</div>
	</div>
</form>
<div style="clear: left;"></div>
<script type="text/javascript">
	var codesConfig = new CodesConfiguration();
</script>
<div style="padding-top: 10px;">
	<div id="saveCodes" style="float: left"></div>
</div>
<script type="text/javascript">
	var saveBtn = new Ext.Button({renderTo:'saveCodes', text:'Save', width: '70px'});
	saveBtn.handler = codesConfig.save.bind(codesConfig);
</script>