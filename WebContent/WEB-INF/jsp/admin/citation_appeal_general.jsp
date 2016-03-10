<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>


<div style="width: 300px; float: left;">
	<dl class="list">
		<dt>Citation Number:</dt>
		<dd><c:out value="${citation.citationNumber}" /></dd>
		<dt>Status:</dt>
		<dd><c:out value="${appeal.status}" /></dd>
		<dt>Date:</dt>
		<dd><c:out value="${appeal.appealDate}" /></dd>
		<dt>Name:</dt>
		<dd><c:out value="${appeal.name}" /></dd>
		<dt>Email:</dt>
		<dd><c:out value="${appeal.email}" /></dd>
		<dt>Phone:</dt>
		<dd><c:out value="${appeal.phone}" /></dd>
		<dt>Address:</dt>
		<dd><c:out value="${appeal.address}" /></dd>
		<dt>City:</dt>
		<dd><c:out value="${appeal.city}" /></dd>
		<dt>State:</dt>
		<dd><c:out value="${appeal.state}" /></dd>
		<dt>Zip:</dt>
		<dd><c:out value="${appeal.zip}" /></dd>
		<dt class="spacer"></dt>
		
		<dt>Reason:</dt>
		<dd><c:out value="${appeal.reason}" /></dd>
		<dt>Decision Date:</dt>
		<dd><c:out value="${appeal.decisionDateFormat}" /></dd>
		<dt>Decision Reason:</dt>
		<dd><c:out value="${appeal.decisionReason}" /></dd>
	</dl>
</div>
