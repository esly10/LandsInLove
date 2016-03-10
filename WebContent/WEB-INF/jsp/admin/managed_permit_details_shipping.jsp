<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermit" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitField" %>


<div style="width: 300px;">
	<c:choose>
		<c:when test="${invoice != null && invoice.will_pickup == 1}">
			<p>Will pick-up in office</p>
		</c:when>
		<c:when test="${invoice != null && invoice.will_pickup == 0}">
			<dl class="list">
				<dt>Name:</dt>
				<dd><c:out value="${invoice.shippingFirstName}" /> <c:out value="${invoice.shippingLastName}" /></dd>
				<dt>Address:</dt>
				<dd><c:out value="${invoice.shippingAddress}" /></dd>
				<dt>City:</dt>
				<dd><c:out value="${invoice.shippingCity}" /></dd>
				<dt>State:</dt>
				<dd><c:out value="${invoice.shippingState}" /></dd>
				<dt>Zip:</dt>
				<dd><c:out value="${invoice.shippingZip}" /></dd>
			</dl>
		</c:when>
		<c:otherwise>
			<p>No shipping information</p>
		</c:otherwise>
	</c:choose>
</div>
