	function sendFormReview(form_id)
		{
			var form = $('#'+form_id);
			
			 $.ajax({type: 'post', url: _contextPath + '/cart/payPermit', data: form.serialize(), success: sendFormCB,dataType: 'json' });
			
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
		
		function cancelPay()
		{
			if (confirm("Do you want to cancel your order?")) 
			{
				 window.location.href='../cart/cancel';
			}
		}