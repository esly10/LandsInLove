function recoverPassword()
{
	var username = $('#j_username').val();
	if(username != "")
	{
		var data = { username: username};
		
		 $.ajax({type: 'post', url: _contextPath + '/owner/recover', data: data, success: recoverPasswordCB ,dataType: 'json'});
	
	}
	else
	{
		alert("Please enter your user name.");
	}
}

function recoverPasswordCB(data)
{
	
	alert(data.msg);
	
}