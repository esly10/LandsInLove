<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%! private int Contador = 0; %>


	<div style="width: 100%; float: left;">
		<div style="width: 100%; float: left;">
			<dl class="list" style="font-size:14px;">
				<dt style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;">Meal Plan:</dt>	
				<dd style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dd> 	
				<dt></dt><dd></dd>	 
			</dl>
		</div>	
	</div>	
		<div style="width: 100%; float: left;">
		<table  class="tableCharge"  style="width:100%; float: right;">
			 <tr><td colspan=7>Date Meal List: </td></tr>
			  <tr>
			    <td>Room No</td><td>Guest Name</td><td>Qty</td><td>Plan</td><td>Breakfast</td> <td>Launch</td><td>Dinner</td>
			  </tr>
			<c:forEach items="${reservations}" var="item">	
			 <tr>
			 		<td><c:out value="${item.rooms}" /></td>
			 		<td><c:out value="${item.guestName}" /></td>
					<td><c:out value="${item.reservation_occupancy}" /></td> 
					<td><c:choose>
					    <c:when test="${item.reservation_meal_plan == '1'}">Breakfast.</c:when>
					    <c:when test="${item.reservation_meal_plan == '2'}">Half Board.</c:when>  
					    <c:when test="${item.reservation_meal_plan == '3'}">Special Full Board.</c:when>  
					    <c:when test="${item.reservation_meal_plan == '4'}">None.</c:when>  
					    <c:otherwise>No show.</c:otherwise>
					</c:choose></td>
					<td><input type="checkbox"></td>
					<td><input type="checkbox"></td>
					<td><input type="checkbox"></td>								
			 </tr>		
			</c:forEach>
		</table>
		
	</div>			