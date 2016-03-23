<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%! private int Contador = 0; %>


<div style="width: 100%; float: left;">
		<div style="width: 33%; float: left;">
			<dl class="list"; style="font-size:14px;">
				<dt style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;">Reservation Info:</dt>	
				<dd style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dd> 	
				<dt></dt><dd></dd>	 
			<dt>Reservation #:</dt>	<dd><c:out value="${reservation.reservation_number}" />		
				<c:choose>
				    <c:when test="${reservation.reservation_type == '1'}"> ||  Fit.</c:when>
				    <c:when test="${reservation.reservation_type == '2'}"> ||  Group.</c:when>    
				    <c:otherwise> ||  Event.</c:otherwise>
				</c:choose>
			</dd>	
			<dt>Room #:</dt><dd><c:out value="${room.ROOM_NO}" />
				<c:choose>
				    <c:when test="${room.ROOM_TYPE == '1'}"> ||  Double.</c:when>
				    <c:when test="${room.ROOM_TYPE == '2'}"> ||  Single.</c:when>    
				    <c:when test="${room.ROOM_TYPE == '3'}"> ||  Superior.</c:when>        
				    <c:otherwise>Family Room.</c:otherwise>
				</c:choose>
				</dd>
				<dt>Status:</dt><dd>
					<c:choose>
					    <c:when test="${reservation.reservation_status == '1'}">Confirmed.</c:when>
					    <c:when test="${reservation.reservation_status == '2'}">Canceled.</c:when>  
					    <c:when test="${reservation.reservation_status == '3'}">Check in.</c:when>  
					    <c:when test="${reservation.reservation_status == '4'}">Check out.</c:when>  
					    <c:otherwise>No show.</c:otherwise>
					</c:choose>
				</dd>
			</dl>
		</div>
		
		<div style="width: 34%; float: left;">
			<dl class="list"; style="font-size:14px;">
				<dt style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dt>	
				<dd style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dd> 	
				<dt></dt><dd></dd>	
				<dt>Check in:</dt><dd><c:out value="${reservation.reservation_check_in}" /></dd>
				<dt>Check out:</dt><dd><c:out value="${reservation.reservation_check_out}"/> (<c:out value="${reservation.reservation_nights}" /> Nights)</dd>
				<dt>Occupancy:</dt><dd><c:out value="${reservation.reservation_adults}" /> ( <c:out value="${reservation.reservation_adults}"/> Adults, 
				<c:out value="${reservation.reservation_children}"/> Children, <c:out value="${reservation.reservation_guides}"/> Guides.)</dd>
				<dt>Meal Plan:</dt><dd><c:choose>
					    <c:when test="${reservation.reservation_meal_plan == '1'}">Breakfast.</c:when>
					    <c:when test="${reservation.reservation_meal_plan == '2'}">Half Board.</c:when>  
					    <c:when test="${reservation.reservation_meal_plan == '3'}">Special Full Board.</c:when>  
					    <c:when test="${reservation.reservation_meal_plan == '4'}">None.</c:when>  
					    <c:otherwise>No show.</c:otherwise>
					</c:choose></dd>
				<dt></dt><dd></dd>
			</dl>
		</div>
		<div style="width: 33%; float: right;">
			<dl class="list"; style="font-size:14px;">
				<dt style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dt>	
				<dd style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;"></dd> 	
				<dt></dt><dd></dd>	
				<dt>Guest:</dt><dd><c:out value="${guest.name}"/></dd>
				<dt>Contact:</dt><dd><c:out value="${guest.phone} || ${guest.email}"/></dd>		
				<dt>Agency:</dt><dd><c:out value="${agency.agency_name}"/></dd>
				<dt>Contact:</dt><dd><c:out value="${agency.agency_phone} || ${agency.agency_email}"/></dd>	
				
			</dl>	
		</div>	
	</div>
	<div style="width: 100%; float: left;">
		<div style="width: 100%; font-size:14px;">
			<dl class="list">
			<dt style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:14px;">Rooms Related:</dt>
			<dd  style="background:#00BBFF; color:#FFFFFF; text-align:center; padding:5px; font-size:18px;"></dd>
			</dl>
				<dl><dt></dt><dd></dd></dl>	
			<ul>
				
				<dl><dt></dt><dd></dd></dl>	
				 <c:forEach items="${roomsrelated}" var="rooms">	
				  			<li>Room #: <c:out value="${rooms.ROOM_NO}" /></li>
				  </c:forEach>
			</ul>
		</div>
	</div>
	<div style="width: 100%; float: left;">
		<table  class="tableCharge"  style="width:49%; float: right;";>
			 <tr><td colspan=6>Agency Charges: </td></tr>
			  <tr>
			    <th>Item</th><th>Description</th><th>Qty</th><th>Rate</th><th>Tax</th> <th>Total</th>
			  </tr>
			<c:forEach items="${agencyCharges}" var="item">	
			 <tr>
					<td><c:out value="${item.charge_item_name}" /></td> 
					<td><c:out value="${item.charge_item_desc}" /></td>
					<td><c:out value="${item.charge_qty}" /></td>
					<td>$ <c:out value="${item.charge_rate}" /></td>
					<td>$ <c:out value="${item.charge_tax}" /></td>
					<td>$ <c:out  value="${item.charge_total}" /></td>								
			 </tr>		
			</c:forEach>
			<tr><td colspan=4>Tax:</td><td colspan=2>$ <c:out value="${reservation.reservation_guest_tax}" /></td></tr>
			<tr><td colspan=4>Sub Total:</td><td colspan=2>$ <c:out value="${reservation.reservation_guest_amount}" /></td></tr>	
			<tr><td colspan=4>Total:</td><td colspan=2>$ <c:out value="${reservation.reservation_guest_amount+reservation.reservation_guest_tax}" /></td></tr>
		
		</table>
		
		<table class="tableCharge" style="width:49%;  float: left;";>
		<tr><td colspan=6>Guest Charges: </td></tr>
			  <tr>
			    <th>Item</th><th>Description</th><th>Qty</th><th>Rate</th><th>Tax</th> <th>Total</th>
			  </tr>
			<c:forEach items="${guestCharges}" var="item">	
			 <tr>
					<td><c:out value="${item.charge_item_name}" /></td> 
					<td><c:out value="${item.charge_item_desc}" /></td>
					<td><c:out value="${item.charge_qty}" /></td>
					<td>$ <c:out value="${item.charge_rate}" /></td>
					<td>$ <c:out value="${item.charge_tax}" /></td>
					<td>$ <c:out  value="${item.charge_total}" /></td>							
			 </tr>		
			</c:forEach>
			<tr><td colspan=4>Tax:</td><td colspan=2>$ <c:out value="${reservation.reservation_agency_tax}" /></td></tr>
			<tr><td colspan=4>Sub Total:</td><td colspan=2>$ <c:out value="${reservation.reservation_agency_amount}" /></td></tr>	
			<tr><td colspan=4>Total:</td><td colspan=2>$ <c:out value="${reservation.reservation_agency_amount+reservation.reservation_agency_tax}" /></td></tr>
		</table>
		
		<table class="tableCharge" style="width:100%;";>
		<tr><td colspan=6>Totals: </td></tr>
			  <tr>
			    <th>Folio</th><th>Amount</th>
			  </tr>
			
			 <tr>
					<td>Agency</td> 
					<td>$ <c:out value="${reservation.reservation_agency_tax+reservation.reservation_agency_amount}" /></td>
			 </tr>	
			  <tr>
					<td>Guest</td> 
					<td>$ <c:out value="${reservation.reservation_guest_amount+reservation.reservation_guest_tax}" /></td>
			 </tr>	
			 <tr style="font-size:18px;">
					<td>Total</td> 
					<td>$ <c:out value="${reservation.reservation_guest_amount+reservation.reservation_guest_tax+reservation.reservation_agency_tax+reservation.reservation_agency_amount}" /></td>
			 </tr>		
		</table>
	
</div>
	
	
	

												
												