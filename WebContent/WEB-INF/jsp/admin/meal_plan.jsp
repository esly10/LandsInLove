<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%! private int Contador = 0; %>


	<div style="width: 100%; float: left;">
		<div style="width: 100%; float: left;">
			<dl class="list" style="font-size:14px;">
				<dt style="background:#FFFFFF; color:#ff7f00; text-align:center; padding:5px; font-size:24px;">Meal Plan</dt>	
				<dd style="background:#FFFFFF; color:#ff7f00; text-align:center; padding:5px; font-size:24px;"></dd> 	
				<dt style="background:#FFFFFF; color:#A65B1A; text-align:left; padding:5px; font-size:14px;"><c:out value="${date.formatdate}" /></dt>	
				<dd style="background:#FFFFFF; color:#A65B1A; text-align:center; padding:5px; font-size:14px;"></dd> 	
			</dl>
		</div>	
	</div>	
		<div class="mealplan" style="width: 100%; float: left;">
		<table>
			  <thead>
				  <tr>
				    <th>Room No</th><th>Guest Name</th><th>Qty</th><th>Plan</th><th>Breakfast</th> <th>Launch</th><th>Dinner</th>
				  </tr>
			  </thead>
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
			<tfoot>
				<tr>
					<td colspan="7"><div id="paging">
							<ul>
								<li><a href="#"><span>Previous</span></a></li>
								<li><a href="#" class="active"><span>1</span></a></li>
								<li><a href="#"><span>2</span></a></li>
								<li><a href="#"><span>3</span></a></li>
								<li><a href="#"><span>4</span></a></li>
								<li><a href="#"><span>5</span></a></li>
								<li><a href="#"><span>Next</span></a></li>
							</ul>
						</div>
				</tr>
			</tfoot>
			<tbody></tbody>
		</table>
	</div>		