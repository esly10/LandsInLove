<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermit" %>
<%@ page import="com.cambiolabs.citewrite.data.ManagedPermitField" %>


<div style="width: 300px;">
	<c:choose>
		<c:when test="${invoice != null}">
			<dl class="list">
				<dt>Purchased:</dt>
				<dd><c:out value="${invoice.created}" /></dd>
				<dt>Status:</dt>
				<dd><c:out value="${invoice.status}" /></dd>
			<c:if test="${invoice.status == 'Refunded'}">
				<dt>Refunded:</dt>
				<dd><c:out value="${invoice.refundDate}" /></dd>	
			</c:if>
				<dt>Payment Method:</dt>
				<dd><c:out value="${invoice.paymentMethod}" /></dd>
				<dt>Amount:</dt>
				<dd><c:out value="${invoice.amount}" /></dd>
				<dt class="spacer"></dt>
				
		<c:choose>
			<c:when test="${invoice.paymentMethod == 'Credit Card' }">
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
				<dt>Expiration:</dt>
				<dd><c:out value="${invoice.ccExpMonth}" />/<c:out value="${invoice.ccExpYear}" /></dd>
			</c:when>
			<c:when test="${invoice.paymentMethod == 'Check' }">
				<dt>Check Number:</dt>
				<dd><c:out value="${invoice.checkNumber}" /></dd>
				<dt>Receipt Email:</dt>
				<dd><c:out value="${invoice.billingEmail}" /></dd>
			</c:when>
			<c:when test="${invoice.paymentMethod == 'Cash' }">
				<dt>Receipt Email:</dt>
				<dd><c:out value="${invoice.billingEmail}" /></dd>
			</c:when>
		</c:choose>
		<c:if test="${invoice.userID != 0}" >			
				<dt class="spacer"></dt>
				<dt>Issued By:</dt>
				<dd><c:out value="${invoice.issuedBy}" /></dd>
		</c:if>
			</dl>
		</c:when>
		<c:otherwise>
			<p>No billing information</p>
		</c:otherwise>
	</c:choose>
</div>
