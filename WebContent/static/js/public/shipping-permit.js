	function sendFormShipping(form_id)
		{
			var form = $('#'+form_id);
			
			 $.ajax({type: 'post', url: _contextPath + '/permit/saveShipping', data: form.serialize(), success: sendFormCB,dataType: 'json' });
			
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