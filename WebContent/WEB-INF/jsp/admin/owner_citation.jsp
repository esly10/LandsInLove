<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>


<div style="width: 300px; float: left;">
	<dl class="list">
		<dt>Name:</dt>
		<dd><c:out value="${owner.firstName}" /> <c:out value="${owner.lastName}" /></dd>
		<dt>Type:</dt>
		<dd><c:out value="${owner.type}" /></dd>
		<dt>Status:</dt>
		<dd><c:out value="${owner.status}" /></dd>
		<dt>Username:</dt>
		<dd><c:out value="${owner.username}" /></dd>
		<dt>Email:</dt>
		<dd><c:out value="${owner.email}" /></dd>
		<dt class="spacer"></dt>
		
		<dt>Home Phone:</dt>
		<dd><c:out value="${owner.homePhone}" /></dd>
		<dt>Mobile Phone:</dt>
		<dd><c:out value="${owner.mobilePhone}" /></dd>
		<dt class="spacer"></dt>
		
		<dt>Address:</dt>
		<dd><c:out value="${owner.address}" /></dd>
		<dt>City:</dt>
		<dd><c:out value="${owner.city}" /></dd>
		<dt>State:</dt>
		<dd><c:out value="${owner.state}" /></dd>
		<dt>Zip:</dt>
		<dd><c:out value="${owner.zip}" /></dd>
		<dt class="spacer"></dt>
		
	</dl>
</div>
