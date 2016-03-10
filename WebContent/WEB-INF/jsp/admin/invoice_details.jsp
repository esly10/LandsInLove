<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermit" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitField" %>


<div style="width: 100%;">
	<h2>General</h2>
	<dl class="list">
		<dt>ID:</dt>
		<dd><c:out value="${invoice.invoiceId}" /></dd>
		<dt>Type:</dt>
		<dd><c:out value="${invoice.typeName}" /></dd>
		<dt>Payment Method:</dt>
		<dd><c:out value="${invoice.paymentMethod}" /></dd>
		<dt>Purchased:</dt>
		<dd><c:out value="${invoice.created}" /></dd>
	<c:if test="${invoice.status == 'Refunded'}">
		<dt>Refunded:</dt>
		<dd><c:out value="${invoice.refundDate}" /></dd>	
	</c:if>
		<dt>status:</dt>
		<dd><c:out value="${invoice.status}" /></dd>
		<dt class="spacer"></dt>
	</dl>
	<h2>Details</h2>
	<dl class="list invoice-items">
		<c:forEach var="item" items="${invoice.allItems}" >
			<dt><c:out value="${item.typeLabel}" /></dt>
			<dd class="description"><c:out value="${item.description}" /></dd>
			<dd class="amount"><fmt:formatNumber type="currency" value="${item.amount}" /></dd>
			<c:if test="${item.userID != 0}">
			<dd class="clerk"><c:out value="${item.issuedBy}" /></dd>
			</c:if>
		</c:forEach>
		<dt style="font-weight: bold;">Total:</dt>
		<dd class="description"></dd>
		<dd style="font-weight: bold;" class="amount"><fmt:formatNumber type="currency" value="${invoice.total}" /></dd>
		<dt class="spacer"></dt>
	</dl>
	<h2>Shipping</h2>
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
		<dt class="spacer"></dt>
	</dl>
	
	<c:choose>
		<c:when test="${invoice.paymentMethod == 'Credit Card' }">
			<h2>Billing</h2>
			<dl class="list">
				<dt>Name:</dt>
				<dd><c:out value="${invoice.billingFirstName}" /> <c:out value="${invoice.billingLastName}" /></dd>
				<dt>Email:</dt>
				<dd><c:out value="${invoice.billingEmail}" /></dd>
				<dt>Address:</dt>
				<dd><c:out value="${invoice.billingAddress}" /></dd>
				<dt>City:</dt>
				<dd><c:out value="${invoice.billingCity}" /></dd>
				<dt>State:</dt>
				<dd><c:out value="${invoice.billingState}" /></dd>
				<dt>Zip:</dt>
				<dd><c:out value="${invoice.billingZip}" /></dd>
				<dt class="spacer"></dt>
				
				<dt>Credit Card:</dt>
				<dd><c:out value="${invoice.ccNumber}" /></dd>
				<dt>Type</dt>
				<dd><c:out value="${invoice.cardTypeName}" /></dd>
				<dt>Expiration:</dt>
				<dd><c:out value="${invoice.ccExpMonth}" />/<c:out value="${invoice.ccExpYear}" /></dd>
				<dt class="spacer"></dt>
			</dl>
		</c:when>
		<c:when test="${invoice.paymentMethod == 'Check' }">
			<h2>Billing</h2>
			<dl class="list">
				<dt>Check Number:</dt>
				<dd><c:out value="${invoice.checkNumber}" /></dd>
				<dt>Receipt Email:</dt>
				<dd><c:out value="${invoice.billingEmail}" /></dd>
			</dl>
		</c:when>
		<c:when test="${invoice.paymentMethod == 'Cash' }">
			<h2>Billing</h2>
			<dl class="list">
				<dt>Receipt Email:</dt>
				<dd><c:out value="${invoice.billingEmail}" /></dd>
			</dl>
		</c:when>
	</c:choose>
	<c:choose>
		<c:when test="${permit != null}">
			<h2>Permit</h2>
			<dl class="list">
				<dt>Number:</dt>
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
			</dl>
		</c:when>
		<c:when test="${citation != null}">
			<h2>Citation</h2>
			<dl class="list">
				<dt>Number:</dt>
				<dd><c:out value="${citation.citationNumber}" /></dd>
				<dt>Date/Time:</dt>
				<dd><c:out value="${citation.citationDate}" /></dd>
				<dt>Violation:</dt>
				<dd><c:out value="${citation.violationId}" /> - <c:out value="${citation.violationDescription}" /></dd>
			</dl>
		</c:when>
	</c:choose>
</div>
