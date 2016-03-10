function sendFormBilling(form_id)
{
	var form = $('#'+form_id);
	
	 $.ajax({type: 'post', url: _contextPath + '/cart/saveBilling', data: form.serialize(), success: sendFormCB,dataType: 'json' });
	
}

function sendFormCB(data){
	
	if(data.success)
	{
	
		if (data.redirect != undefined) {
			window.location.href=data.redirect;
		}
		
	}else
	{
		alert(data.msg);
		if (data.redirect != undefined) {
			window.location.href=data.redirect;
		}
	}
	
	
}


function sameAsShipping(cb)
{
	if(cb.checked)
	{
		$('#billing_first_name').val($('#shipping_first_name').val());
		$('#billing_last_name').val($('#shipping_last_name').val());
		$('#billing_email').val($('#owner_email').val());
		$('#billing_address').val($('#shipping_address').val());
		$('#billing_city').val($('#shipping_city').val());
		$('#billing_state_id').val($('#shipping_state_id').val());
		$('#billing_zip').val($('#shipping_zip').val());
	}
	else
	{
		$('#billing_first_name').val('');
		$('#billing_last_name').val('');
		$('#billing_email').val('');
		$('#billing_address').val('');
		$('#billing_city').val('');
		$('#billing_state_id').val('');
		$('#billing_zip').val('');
	}
}

function pickup(cb)
{
	if(cb.checked)
	{
		$('#shipping_first_name').prop('disabled', true);
		$('#shipping_last_name').prop('disabled', true);
		$('#owner_email').prop('disabled', true);
		$('#shipping_address').prop('disabled', true);
		$('#shipping_city').prop('disabled', true);
		$('#shipping_state_id').prop('disabled', true);
		$('#shipping_zip').prop('disabled', true);
	}
	else
	{
		$('#shipping_first_name').prop('disabled', false);
		$('#shipping_last_name').prop('disabled', false);
		$('#owner_email').prop('disabled', false);
		$('#shipping_address').prop('disabled', false);
		$('#shipping_city').prop('disabled', false);
		$('#shipping_state_id').prop('disabled', false);
		$('#shipping_zip').prop('disabled', false);
	}
}

function cancelOrder()
{
	if (confirm("Do you want to cancel your order?")) 
	{
		 window.location.href='../cart/cancel';
	}
}


	
	
	
	