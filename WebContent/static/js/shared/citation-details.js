$(document).ready(function(){
	
	
	$(document.body).append($('#popup-dispute').detach());
	$(document.body).append($('#dispute-details-popup').detach());
	
	
	$('#close-popup').click(function(){
		$('#popup-dispute').hide();
		$('#popup-mask').remove();
		$('#form-dispute').reset();
	});
	
	$('#close-popup-details').click(function(){
		$('#dispute-details-popup').hide();
		$('#popup-mask').remove();
	});

});

jQuery.fn.reset = function ()
{
	$(this).each (function() { this.reset(); });
};

function disputeDetails()
{
	var pdispute_details = jQuery('#dispute-details-popup');
	
	var center_x = $(document).width()/2;
	var center_y = $(document).height()/2;
			
	var vpopup_x = pdispute_details.width()/2;
	var vpopup_y = pdispute_details.height()/2;
		

	$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
	$('#dispute-details-popup').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
	$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
	
}

function dispute(citation_id)
{
	var pdispute = $('#popup-dispute');
	
	var center_x = $(document).width()/2;
	var center_y = $(document).height()/2;
			
	var vpopup_x = pdispute.width()/2;
	var vpopup_y = pdispute.height()/2;
	
	
	
	
	$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
	$('#popup-dispute').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
	$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
	
	$('#citation_id').val(citation_id);
}

function sendDispute(form_id)
{
	var form = $('#'+form_id);
	
	 $.ajax({type: 'post', url: '../citation/appeal', data: form.serialize(), success: sendDisputeCB,dataType: 'json' });
	
}
		
function sendDisputeCB(data)
{
	
	if(data.success)
	{
	
		alert('Your appeal has been received. We will be in contact with you shortly.');
		cancel();
		window.location.href = window.location.href;
		
	}else
	{
		alert(data.msg);
	}
	
	
}

function cancel()
{
	$('#popup-dispute').hide();
	$('#popup-mask').remove();
	$('#form-dispute').reset();
	$('#dispute-details-popup').hide();
	
}

function sendFormBilling(form_id)
{
	var form = $('#'+form_id);
	
	$.ajax({type: 'post', url: '../cart/saveBilling', data:  form.serialize() , success: sendFormBillingCB,dataType: 'json'});
	
}
		
function sendFormBillingCB(data)
{
	
	if(data.success)
	{
	
		if (data.redirect != undefined) {
			window.location.href=data.redirect;
		}
		
	}else
	{
		alert(data.msg);
	}
	
	
}


function sendBilling(citation_id)
{
	var data = { citation_id: citation_id};
	$.ajax({type: 'post', url: '../cart/citation', data: data , success: BillingCB,dataType: 'json'});

}

function BillingCB(data)
{
	
	if(data.success)
	{
	
		if (data.redirect != undefined) {
			window.location.href=data.redirect;
		}
		
	}else
	{
		alert(data.msg);
	}
	
	
}

function sendPay(form_id)
{
	
	var form = $('#'+form_id);
	
	 $.ajax({type: 'post', url: '../cart/payCitation', data: form.serialize(), success: sendPayCB,dataType: 'json' });

}

function sendPayCB(data)
{
	
	if(data.success)
	{
	
		if (data.redirect != undefined) 
		{
			window.location.href=data.redirect;
		}
		
	}else
	{
		alert(data.msg);
		if (data.redirect != undefined)
		{
			window.location.href=data.redirect;
		}
	}
	
	
}

function loadMap(lat,lng)
{

	var cloudmade = new CM.Tiles.CloudMade.Web({key:'578a0efef86b451dbfaceb920797c3ab',tileSize:256,styleId: 75214});
	var map = new CM.Map('map', cloudmade);
	var myMarkerLatLng = new CM.LatLng(lat,lng);
	var myMarker = new CM.Marker(myMarkerLatLng);
	    
	var zoom = 18;
	map.setCenter(myMarkerLatLng, zoom);
	map.addOverlay(myMarker);
	
}


function validateDispute(form_dispute)
{
	var name = $('#appeal_name').val();
	var phone = $('#appeal_phone').val();
	var email = $('#appeal_email').val();
	var address = $('#appeal_address').val();
	var city = $('#appeal_city').val();
	var state = $('#appeal_state_id').val();
	var zip = $('#appeal_zip').val();
	var reason = $('#appeal_reason').val();
	
	if(name.length == 0 || phone.length == 0 ||
			email.length == 0 || address.length == 0 ||
				city.length == 0 || state.length == 0 ||
					zip.length == 0 || reason.length == 0 )
	{
		alert('All fields are required.');
	}
	else
	{
		sendDispute(form_dispute);
	}
}

function cancelOrder()
{
	if (confirm("Do you want to cancel paying your citation?")) 
	{
		 window.location.href='../cart/cancel';
	}
}

function cancelPay()
{
	if (confirm("Do you want to cancel paying your citation?")) 
	{
		 window.location.href='../cart/cancel';
	}
}


