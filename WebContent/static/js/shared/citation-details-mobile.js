/*$(document).ready(function()
	{
		$('#cc_number').limitkeypress({rexp:/^[0-9,-]*$/});
		$('#billing_zip').limitkeypress({rexp:/^[0-9AaBbCcEeGgHhJjKkLlMmNnPpRrSsTtVvXxYy-]*$/});
		$('#cc_number').limitkeypress({rexp:/^[0-9,-]*$/});
		$('#cc_cvv').limitkeypress({rexp:/^[0-9]*$/});
	});
jQuery.fn.reset = function ()
{
	$(this).each (function() { this.reset(); });
};*/

function disputeDetails()
{
	$('#dispute-details-popup').popup('open');
}

function dispute(citation_id)
{
	window.location.href = '../citation/dispute';
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
		
		
	}else
	{
		alert(data.msg);
	}
	
	
}

function cancel()
{
	window.location.href = '../citation/details';
	
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
	
		if (data.redirect != undefined) {
			window.location.href=data.redirect;
		}
		
	}else
	{
		alert(data.msg);
	}
	
	
}


function loadMap(lat, lng)
{
	var map = L.map('map',{
		center:[lat,lng],
		zoom:16
		
	});
	L.tileLayer('http://{s}.tile.cloudmade.com/578a0efef86b451dbfaceb920797c3ab/997/256/{z}/{x}/{y}.png', {maxZoom: 18}).addTo(map);
	var marker = L.marker([lat, lng]).addTo(map);
}

function loadIframe(lat,lng)
{
	$('#map-content').append('<iframe name="iframe_map" id="iframe_map" width="95%" height="300px" frameBorder="0" src= "../citation/map?lat='+lat+'&lng='+lng+'"></iframe>');
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
		alert('All fields are Required.');
	}
	else
	{
		sendDispute(form_dispute);
	}
}
