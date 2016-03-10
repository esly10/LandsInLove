<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermit" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitField" %>


<div style="width: 300px;">
	<dl class="list">
		<dt>Permit Number:</dt>
		<dd><c:out value="${permit.permitNumber}" /></dd>
		<dt>Status:</dt>
		<dd><c:out value="${permit.status}" /></dd>
		<dt>Type:</dt>
		<dd><c:out value="${permit.type.name}" /> - <c:out value="${permit.type.description}" /></dd>
		<dt>Valid Start:</dt>
		<dd><c:out value="${permit.validStart}" /></dd>
		<dt>Valid End:</dt>
		<dd><c:out value="${permit.validEnd}" /></dd>
		<dt class="spacer"></dt>
						
		<c:forEach items="${fields}" var="field">
			<dt><c:out value="${field.label}" />:</dt>
			<dd><%= ((ManagedPermit)request.getAttribute("permit")).getAttributeValue((ManagedPermitField)pageContext.getAttribute("field")) %></dd>
		</c:forEach>
		<dt class="spacer"></dt>
		
		<dt>Vehicle(s)</dt>
		<c:forEach items="${vehicles}" var="vehicle">
			<dd><c:out value="${vehicle.license}" /></dd>
			<dt>&nbsp;</dt>
		</c:forEach>
		<dd></dd>
		
		<dt>Added</dt>
		<dd><c:out value="${permit.created}" /></dd>
		<dt>Updated:</dt>
		<dd><c:out value="${permit.updated}" /></dd>
	</dl>
</div>
