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
		
		<dt>Added:</dt>
		<dd><c:out value="${owner.created}" /></dd>
		<dt>Updated:</dt>
		<dd><c:out value="${owner.updated}" /></dd>
	</dl>
</div>
<div style="width: 300px; float: left;">
	<dl class="list">
		<c:forEach items="${fields}" var="field">
			<dt><c:out value="${field.label}" />:</dt>
			<dd><%= ((Owner)request.getAttribute("owner")).getAttributeValue((OwnerField)pageContext.getAttribute("field")) %></dd>
		</c:forEach>
	</dl>
</div>
