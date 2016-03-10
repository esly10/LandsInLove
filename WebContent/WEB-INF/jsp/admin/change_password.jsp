<%@page import="com.cambiolabs.citewrite.data.Config"%>
<%@page import="com.cambiolabs.citewrite.license.LicenseManager"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="date" class="java.util.Date" />
<% 
	if(!LicenseManager.isManagedPermitsEnabled())
	{ 
		response.sendRedirect(request.getContextPath() + Config.URL_GUEST_LOGIN); 
		return; 
	} 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="Content-Language" content="en-us" />
		<meta http-equiv="Expires" content="Tue, 01 Jan 1980 00:00:00 GMT" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		
		<title>CiteWrite</title>
		
		<link rel="stylesheet" type="text/css" href="<c:url value="/static/css/public.css" />" media="all" />
		
		<script type="text/javascript" src="<c:url value="/static/js/library/jquery/jquery-1.8.3.min.js" />"></script>
		<script type="text/javascript">
			var _contextPath = '<%= request.getContextPath() %>';
		</script>
		<script type="text/javascript">
	function validate (form_id)
	{
		var username = $('#username').val();
		var password = $('#password').val();
		var new_password = $('#new_password').val();
		var retype_password = $('#retype_password').val();
		
		if(username.length > 0 && password.length > 0 && new_password.length > 0 && retype_password.length > 0)
		{
			if(new_password == retype_password)
			{
				changePassword(form_id);
			}
			else
			{
				alert('The passwords do not match.');
			}
		}
		else
		{
			alert('All fields are required');
		}
	}
	
	function changePassword(form_id)
	{
		var form = $('#'+form_id);
			
		 $.ajax({type: 'post', url: _contextPath + '/admin/user/password', data: form.serialize(), success: changePasswordCB,dataType: 'json'
		 });
	}


	function changePasswordCB(data)
	{
		
		
		if(data.success)
		{
			alert(data.msg);
			if (data.redirect != undefined) 
			{
				window.location.href=data.redirect;
			}
		}
		else
		{
			alert(data.msg);
		}
		
			
	}
</script>
	
		
	</head>

	<body class="login">
		<div id="body-frame">	
			<div id="site-frame" style="width: 334px;">
				<div id="header">
					<div id="logo" style="margin-left: 40px;"></div>  
					
				</div>
				
				<div id="content">
	<div id="login-body">
		<div id="top"></div>
		<div id="middle">
			<div id="content-login">
			<p>Your password has expired. Please change your password.</p>
				<form id="changePassword">
					<dl class="form login-index" >
						<dt>User Name</dt>
						<dd ><input type="text" name="username" id="username" value=""></dd>
						
						<dt>Password</dt>
						<dd ><input type="password" name="password" id="password" value=""></dd>
						
						<dt>New Password</dt>
						<dd ><input type="password" name="new_password" id="new_password" value=""></dd>
						
						<dt>Confirm New Password</dt>
						<dd ><input type="password" name="retype_password" id="retype_password"></dd>
					</dl>
					<div id="btn-container">
						<button  class="medium" style="float: left;" onclick="validate('changePassword'); return false;"><div>Change</div></button>
						<div style="clear: left;"></div>
					</div>
				</form>
			</div>
		</div>
		<div id="bottom"></div>
			
	</div>
</div>
				
			</div>
			<div id="footer" class="public"><div>&copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Cambio CiteWrite. All Rights Reserved</div><div><a href="<c:url value="/index/policy" />" target="_blank">Privacy Policy</a>&nbsp;<label>|</label>&nbsp;<a href="<c:url value="/index/terms" />" target="_blank">Terms &amp; Condition</a></div></div>
		</div>
	
	
	</body>
</html>