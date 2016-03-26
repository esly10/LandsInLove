<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%! private int Contador = 0; %>


	<div style="width: 100%; float: left;">
		<div style="width: 100%; float: left;">
			<dl class="list" style="font-size:14px;">
				<dt style="background:#FFFFFF; color:#16df9d; text-align:center; padding:5px; font-size:24px;">Unhandled Statuses Report</dt>	
				<dd style="background:#FFFFFF; color:#16df9d; text-align:center; padding:5px; font-size:24px;"></dd> 	
				<dt style="background:#FFFFFF; color:#49e34c; text-align:left; padding:5px; font-size:14px;">Created: <c:out value="${date.formatdate}" /></dt>	
				<dd style="background:#FFFFFF; color:#49e34c; text-align:center; padding:5px; font-size:14px;"></dd> 	
				<dt></dt><dd></dd>	
			</dl>
		</div>	
	</div>	
		<div class="mealplan" style="width: 100%; float: left;">
		 <hr>  
		<table>
			  <thead>
				  <tr>
				    <th>Res No</th><th>Type</th><th>Check In</th><th>Check Out</th><th>Guest</th> <th>Agency</th><th>Rooms</th><th>Created</th>
				  </tr>
			  </thead>
			  <tr style="background:#E1E1E1;"><td colspan=8; style="color:#19BF89; text-align:left; font-size:18px; font-style:bold;">Confirmed </td> </tr>
			<c:forEach items="${reservations}" var="item">	
			 <c:choose>
				<c:when test="${item.reservation_status == '1'}">
				 	<tr>
						<td><c:out value="${item.reservation_number}" /></td>
						<td><c:out value="${item.typeName}" /></td>
						<td><c:out value="${item.checkInFormated}" /></td> 
						<td><c:out value="${item.checkOutFormated}" /></td>
						<td><c:out value="${item.guestName}" /></td>
						<td><c:out value="${item.agencyName}" /></td>
						<td><c:out value="${item.reservation_rooms}" /></td>
						<td><c:out value="${item.reservation_creation_date}" /></td> 
				 	</tr>	
			 	</c:when>
			</c:choose>
			 	
			</c:forEach>
			<tr></tr>
			<tr style="background:#E1E1E1;"><td colspan=8; style="color:#19BF89; text-align:left; font-size:18px;">Open </td> </tr>
			<c:forEach items="${reservations}" var="item">	
				 <c:choose>
					<c:when test="${item.reservation_status == '0'}">
					 	<tr>
							<td><c:out value="${item.reservation_number}" /></td>
							<td><c:out value="${item.typeName}" /></td>
							<td><c:out value="${item.checkInFormated}" /></td> 
							<td><c:out value="${item.checkOutFormated}" /></td>
							<td><c:out value="${item.guestName}" /></td>
							<td><c:out value="${item.agencyName}" /></td>
							<td><c:out value="${item.reservation_rooms_qty}" /></td>
							<td><c:out value="${item.reservation_creation_date}" /></td> 
					 	</tr>
					 	
				 	</c:when>	
				</c:choose>
			</c:forEach>
			<tr></tr>
			<tbody></tbody>
		</table>
		<hr>
	</div>		