<%@ page session="true" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@ page import="com.cambiolabs.citewrite.data.User" %>
<%@ page import="com.cambiolabs.citewrite.data.Config" %>
<%@ page import="com.cambiolabs.citewrite.license.LicenseManager" %>
<%
	User cwUser = User.getCurrentUser();
	pageContext.setAttribute("user", cwUser);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="Content-Language" content="en-us" />
	<meta http-equiv="Expires" content="Tue, 01 Jan 1980 00:00:00 GMT" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Content-Style-Type" content="text/css" />
	<title>Lands in Love</title>
	<script type="text/javascript" src="<c:url value="/static/js/library/prototype.js" />" ></script>
	<script type="text/javascript" src="<c:url value="/static/js/library/scriptaculous/scriptaculous.js?load=effects" /> " ></script>
	
	<link href="<c:url value="/static/js/library/ext/css/ext-all.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/static/js/library/ext/css/citewrite.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/static/css/global.css" />" rel="stylesheet" type="text/css" />
	
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/adapter/ext-prototype-adapter.js" />" ></script>
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/ext-all-debug.js" />" ></script>
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/status-bar.js" />" ></script>
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/clear-trigger.js" />" ></script>
	
	
	<script type="text/javascript">
		var _isAdmin = <%= (cwUser.isAdmin())?"true":"false" %>;
		var _version = '<%= Config.VERSION %>';
		var _contextPath = '<%= request.getContextPath() %>/admin';
		var _rootContextPath = '<%= request.getContextPath() %>';
	</script>
		
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/js/library/ext/ux/fileuploadfield/css/fileuploadfield.css" />"/>
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/ux/fileuploadfield/FileUploadField.js" />"></script>
	
    <link rel="stylesheet" type="text/css" href="<c:url value="/static/js/library/ext/ux/css/Ext.ux.GridTotals.css" />"/>
	<script type="text/javascript" src="<c:url value="/static/js/library/ext/ux/Ext.ux.GridTotals.js" />"></script>
	
	<script type="text/javascript" src="<c:url value="/static/js/portal.js" />"></script>
	<script type="text/javascript" src="<c:url value="/static/js/utils.js" />"></script>
	<script type="text/javascript" src="<c:url value="/static/js/growl.js" />"></script>
	<script type="text/javascript" src="<c:url value="/static/js/user-account.js" />"></script>
	
	<link rel="SHORTCUT ICON" HREF="<c:url value="/favicon.ico" />" />
	<%@include file="manage-user-permissions.jsp" %>
</head>
<body id="body">
	
	<div id="headerPanel" class="x-hide-display">
		<div style="position: relative;">
			<div id="header"><div></div></div>
	        <div id="header-user"><div id="header-user-text">
	        <c:out value="${user.firstName} ${user.lastName}" />&nbsp;&nbsp;&nbsp;&nbsp; </div>
	        <div id="header-user-icons">
	        <a href="#" id="my-account-link"><img src=<c:url value="/static/images/user.png" /> ></a>&nbsp;&nbsp;&nbsp;&nbsp;
	        <a href="<c:url value="/admin/logout" />"><img src=<c:url value="/static/images/exit.png" /> ></a></div>
	       </div>
	    </div>
    </div>
    
	<div id="toolsPanel" class="x-hide-display">
        <ul class="navigation" id="tool-options">
        	<% if(cwUser.hasPermission(User.PL_RESERVATION_MANAGE)){ %>
        		<li id="nav-reservation">New Reservation
		    		<script type="text/javascript" src="<c:url value="/static/js/search.js" />"></script>
		    		<script type="text/javascript" src="<c:url value="/static/js/search-panel.js" />"></script>
		    		<script type="text/javascript" src="<c:url value="/static/js/search-panel-result.js" />"></script>
		    	</li>
		    <% } if(cwUser.hasPermission(User.PL_RESERVATION_VIEW)){ %>
		    	<li id="nav-list">Reservation List</li>
		     	<li style="display:none;">
		    		<script type="text/javascript" src="<c:url value="/static/js/reservation-list.js" />"></script>
		    		<script type="text/javascript" src="<c:url value="/static/js/reservation-list-panel.js" />"></script>
		    	</li>
		    	
		    <% } if(cwUser.hasPermission(User.PL_OCUPANCY_LIST)){ %>
		    	<li id="nav-ocupancy">Ocupancy</li>
		   
	    	
		    <% } if(cwUser.hasPermission(User.PL_CHARGES_VIEW)){ %>
		    	<li id="nav-payment">Charges
		    		<script type="text/javascript" src="<c:url value="/static/js/charges.js" />"></script>
		    		<script type="text/javascript" src="<c:url value="/static/js/charge-general.js" />"></script>
		    	</li>
		  
		    <% } if(cwUser.hasPermission(User.PL_GUEST_MANAGE)){ %>
		    	<li id="nav-contacts">Guests
			    	<script type="text/javascript" src="<c:url value="/static/js/guests.js" />"></script>
			    </li>
			    </li>
    		<% } if(cwUser.hasPermission(User.PL_AGENCY_MANAGE)){ %>
				<li id="nav-agencies">Agencies
		 			<script type="text/javascript" src="<c:url value="/static/js/agencies.js" />"></script>
		 		</li>
			<% } if(cwUser.hasPermission(User.PL_SERVICE_MANAGE)){ %>
				<li id="nav-services">Services
					<script type="text/javascript" src="<c:url value="/static/js/services.js" />"></script>
	   	 		</li>
	   	 		</li>
    		<% } if(cwUser.hasPermission(User.PL_ROOM_MANAGE)){ %>
				<li id="nav-rooms">Rooms
		 			<script type="text/javascript" src="<c:url value="/static/js/rooms.js" />"></script>
		 			<script type="text/javascript" src="<c:url value="/static/js/rooms-grid.js" />"></script>
		 		</li>
			<% } if(cwUser.hasPermission(User.PL_ADMIN)){ %>
				<li id="nav-users">Users
	   	 			<script type="text/javascript" src="<c:url value="/static/js/users.js" />"></script>
	   	 		</li>
    		<% } if(cwUser.hasPermission(User.PL_REPORT_VIEW)){ %>
				<li id="nav-reports">Reports
		 			<script type="text/javascript" src="<c:url value="/static/js/report-list.js" />"></script>
		 			<script type="text/javascript" src="<c:url value="/static/js/report-viewer.js" />"></script>
		 		</li>
			<% } if(cwUser.isAdmin()){ %>
				<li id="nav-settings">Settings		
					<%
					if(LicenseManager.isManagedPermitsEnabled()){
				%>
					<script type="text/javascript" src="<c:url value="/static/js/config-field-class.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-type.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-field.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-view.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-owner.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-managed.js" />"></script>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-general.js" />"></script>
					<% } else { %>
					<script type="text/javascript" src="<c:url value="/static/js/config-permit-file.js" />"></script>
					<% } %>	
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-citation.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-citation-pages.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-citation-fields.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-codes.js" />"></script>
	   	 			<%
					if(LicenseManager.isCitationPaymentEnabled() || LicenseManager.isManagedPermitsEnabled()){
					%>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-ecommerce.js" />"></script>
	   	 			<% }  %>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-general.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-print-panel.js" />"></script>
	   	 			<script type="text/javascript" src="<c:url value="/static/js/config-print-form.js" />"></script>
	   	 		</li>
    		<% } %>
        </ul>
    </div>
        
    <script type="text/javascript">
    Ext.onReady(function(){
    	var nav = Ext.getCmp('navigation-panel');
    	nav.doLayout();    	
    });
    </script>
</body>
</html>