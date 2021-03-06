<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>
<script type="text/javascript">
	function myFunction(){
		var ua = window.navigator.userAgent; 
		var msie = ua.indexOf("MSIE "); 
		if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./)){
			 window.document.execCommand("print", false, null);
		}
		 else{
			window.print();
		}
	}
</script>


<html>
	<head>
	<title>Reservation</title>
	</head>
		
	<style>
	.demo {
		border:1px solid #C0C0C0;
		border-collapse:collapse;
		padding:5px;
		width:900px;
	}
	.demo th {
		border:1px solid #C0C0C0;
		padding:5px;
		background:#F0F0F0;
	}
	.demo td {
		border:1px solid #C0C0C0;
		padding:5px;
	}
	
	#footer {    
	    clear: both;
	 	 position:fixed;
	    bottom:0;
		text-align:center;
		width:900px;
	}
	
	@page {
	        size:a4;
	        margin: 0;
	    }
    @media print {
        html, body {
            width: auto;
            height: auto;
            -webkit-print-color-adjust: exact;       
        }
    } 
	</style>
	
	<body onload="myFunction();">
		<br><br>
		<div id="content">
			<div id="title" style="width:900px; text-align:center">
				<span style="font-size:20px">Lands in love - Tierras Enamoradas </span><br>
				<span style="font-size:20px">HOTEL & RESORT</span><br>
				<span style="font-size:23px"><b><u>RESERVATION CONFIRMATION</u></b></span><br>
			</div>
			<br><br>
			<div style="width:900px; text-align:left;">
				<c:if test="${reservation.reservation_type == 2}">
	    			<span><b>To: </b></span><c:out value="${agency.agency_name}" /> <br>
					<table style="width:900px;">
						<tr>
							<td><b>Address: </b><c:out value="${agency.address}" /></td>
							<td><b>Tel: </b><c:out value="${agency.agency_phone}" /></td>
						</tr>
						<tr>
							<td></td>
							<td><b>Website: </b><c:out value="${agency.web}" /></td>
						</tr>
						<tr>
							<td></td>
							<td><b>Fax: </b><c:out value="${agency.fax}" /></td>
						</tr>
						<tr>
							<td></td>
							<td><b>Email: </b><c:out value="${agency.agency_email}" /></td>
						</tr>
						<tr>
							<td><b>Reservation Coordinator: </b><c:out value="${user.firstName}" /> <c:out value="${user.lastName}" /></td>
							<td><b>Reservation ID: </b><c:out value="${reservation.reservation_number}" /></td>
						</tr>
					</table>
				</c:if>
				
				<c:if test="${reservation.reservation_type != 2}">
				<span><b>To: </b></span><c:out value="${guest.name}" /> <br>
				<table style="width:900px;">
					<tr>
						<td><b>Address: </b><c:out value="${guest.address}" /></td>
						<td><b>Tel: </b><c:out value="${guest.phone}" /></td>
					</tr>
					<tr>
						<td></td>
						<td><b>Cel: </b><c:out value="${guest.mobile}" /></td>
					</tr>
					<tr>
						<td></td>
						<td><b>Fax: </b><c:out value="${guest.fax}" /></td>
					</tr>
					<tr>
						<td></td>
						<td><b>Email: </b><c:out value="${guest.email}" /></td>
					</tr>
					<tr>
						<td><b>Reservation Coordinator: </b><c:out value="${user.firstName}" /> <c:out value="${user.lastName}" /></td>
						<td><b>Reservation ID: </b><c:out value="${reservation.reservation_number}" /></td>
					</tr>
				</table>
				
				</c:if>
				
				
			</div>
			<br>
			<span style="font-size:18px"><b><u>STAY DETAILS:</u></b></span>
			
			<table style="width:900px;">
				<tr>
					<td style="width:230px;"><b>NAME:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><span style="background-color: yellow;"><c:out value="${guest.name}" /></span></td>
				</tr>
				<tr>
					<td style="width:230px;"><b>QUANTITY:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${reservation.reservation_rooms_qty}" /></td>
				</tr>
				<tr>
					<td style="width:230px;"><b>STAY DATES:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><span style="background-color: yellow;"><c:out value="${reservation.reservation_check_in_format}" /> &nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp; <c:out value="${reservation.reservation_check_out_format}" /> &nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp; <c:out value="${reservation.reservation_nights}" /> Night(s)</span></td>
				</tr>
				<tr>
					<td style="width:230px;"><b>PLAN:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${mealPlan.meal_plan_description}" /></td>
				</tr>
				<tr>
					<td style="width:230px;"><b>ROOM(S) NO:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><span style="background-color: yellow;"><c:out value="${reservation.reservation_rooms_comma_separate}" /></span></td>
				</tr>
				<tr>
					<td style="width:230px;"><b>OCCUPANCY:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${reservation.reservation_adults}" /> Adults, <c:out value="${reservation.reservation_children}" /> Children, <c:out value="${reservation.reservation_guides}" /> Guide/Driver </td>
				</tr>
				<tr>
					<td style="width:230px;"><b>NOTES:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid;">
						Service Notes:  <c:out value="${reservation.reservation_service_notes}" />,<br>
						Transport Notes:  <c:out value="${reservation.reservation_transport_notes}" />
					</td>
				</tr>
			</table>
		
			<br>
			<span style="font-size:18px"><b><u>PRICING:</u></b></span>
			<table class="demo">
				<thead>
					<tr>
						<th style="width:480px; text-align:left;">ITEM</th>
						<th style="">QTY</th>
						<th style="">RATE</th>
						<th style="">TOTAL</th>
					</tr>
					</thead>
					<tbody>
					
					<c:forEach items="${reservation.charges}" var="charge">
						<tr>
							<td><c:out value="${charge.charge_item_name}" /></td>
							<td style="text-align: center;"><c:out value="${charge.charge_qty}" /></td>
							<td style="text-align: center;"><c:out value="${charge.charge_rate}" /></td>
							<td style="text-align: center;"><c:out value="${charge.charge_total_format}" /></td>
						</tr>
					</c:forEach>				
				</tbody>
			</table>
			<div style="width:900px; height:80px;"></div>
			<hr>
			<table style="width:900px; text-align: right;">
				<tr>
					<td style="width:800px;"><b>Subtotal:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid; text-align: right;"><c:out value="${subtotal}" /></td>
				</tr>
				<tr>
					<td style="width:800px;"><b>Tax:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid; text-align: right;"><c:out value="${tax}" /></td>
				</tr>
				<tr>
					<td style="width:800px;"><b>Total:</b></td>
					<td style="border-bottom: 1px #000000;border-bottom-style: solid; text-align: right;"><b><c:out value="${total}" /></b></td>
				</tr>
			</table>
			<br><br>
			<div style="width:900px; height:80px; position:relative;">
				<div style="float:left; width:425px">
					<span style="font-size:18px"><b><u>PAYMENT DETAILS:</u></b></span>	<br>
						<label><c:out value="${lastPeyment.back_account}" /> </label>	<br>
						<label>Cuenta #: <c:out value="${lastPeyment.transaction_no}" /> </label>	<br>
						<label>Beneficiry: HOTELERA LA GRAN VISTA S.A. </label>	<br>
				</div>
				<div style="float: left;   width: 425px;    border-left-style: solid;    border-left-color: #000000;    border-left-width: 2px;    padding-left: 10px;">
					<span style="font-size:18px"><b><u>CREDIT CARD DETAILS:</u></b></span>	
					<table style="width:425px;">
						<tr>
							<td  style="width:80px;"><b>No:</b></td>
							<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${reservation.card_no}" /></td>
						</tr>
						<tr>
							<td  style="width:80px;"><b>Name:</b></td>
							<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${reservation.card_name}" /></td>
						</tr>
						<tr>
							<td  style="width:80px;"><b>Type:</b></td>
							<td style="border-bottom: 1px #000000;border-bottom-style: solid;"><c:out value="${reservation.card_type_name}" /></td>
						</tr>
					</table>
				</div>
			</div>
			<br><br>	
			<div>
				<label><b>Payment method: </b><input type="checkbox"> Credit Card &nbsp;&nbsp;&nbsp;<input type="checkbox"> Wire &nbsp;&nbsp;&nbsp;<input type="checkbox"> Cash &nbsp;&nbsp;&nbsp;<input type="checkbox"> Other: _______________</label><br>
				<label><b>Payment Terms: </b><c:out value="${reservation.reservation_payment_terms_name}" /></label><br>
				<label><b>For Hotel Reception Usage: </b>Payment confirm Yes / No</label><br>
			</div>
			<br><br><br>
			<table style="width:900px;">
				<tr>
					<td style="font-size:16px"><b><u>Confirmation is subject to payment </b></u></td>
					<td style="border-top: 1px #000000;border-top-style: solid; text-align: center;">Customer Signature</td>
				</tr>
			</table>
		
			<br>
			<div id="footer">
				<span>Reservations: (506) 475-1081, Fax: +506-475-1084, Email: reservations@landsinlove.com</span><br>
				<span>San Ram�n, Alajuela, Costa Rica. www.landsinlove.com</span><br>
			</div>
		</div>
	</body>

</html>