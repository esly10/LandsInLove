<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.ConfigItem" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>


<div style="width: 300px;">
	<dl class="list">
		<dt>License:</dt>
		<dd><c:out value="${vehicle.license}" /></dd>
		<dt>VIN:</dt>
		<dd><c:out value="${vehicle.vin}" /></dd>
		<dt>State:</dt>
		<dd><c:out value="${vehicle.state}" /></dd>
		<dt>Make:</dt>
		<dd><c:out value="${vehicle.make}" /></dd>
		<dt>Color:</dt>
		<dd><c:out value="${vehicle.color}" /></dd>
		<dt class="spacer"></dt>
				
		<c:forEach items="${fields}" var="field">
			<dt><c:out value="${field.label}" />:</dt>
			<dd><%= ((Vehicle)request.getAttribute("vehicle")).getAttributeValue((VehicleField)pageContext.getAttribute("field")) %></dd>
		</c:forEach>
		<dt class="spacer"></dt>
		
		<dt>Added:</dt>
		<dd><c:out value="${vehicle.created}" /></dd>
		<dt>Updated:</dt>
		<dd><c:out value="${vehicle.updated}" /></dd>
	</dl>
</div>
