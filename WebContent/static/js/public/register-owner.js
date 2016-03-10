$(document).ready(function(){
	
	$('#zip').limitkeypress({rexp:/^[0-9AaBbCcEeGgHhJjKkLlMmNnPpRrSsTtVvXxYy-]*$/});
	
});


function validate (form_id)
{
	var password = $('#password').val();
	var retype = $('#retype_password').val();
	
	if(password.length != 0)
		{
			if(retype.length != 0)
			{
				if(password== retype)
					{
						sendForm(form_id);
					}
					else
					{
						alert('The passwords do not match.');
						
					}
			}
			else
			{
				alert('Please re-enter your password.');
			}
		}
		else
		{
			alert('Please enter a password.');
		}
		
}

function sendForm(form_id)
{
	var form = $('#'+form_id);
		
	 $.ajax({type: 'post', url: _contextPath + '/register/save', data: form.serialize(), success: sendFormCB,dataType: 'json'
	 });
}


function sendFormCB(data)
{
	
	
	if(!data.success)
		{
			alert(data.msg);
		}
	
	if (data.redirect != undefined) 
	{
		window.location.href=data.redirect;
	}
	
}