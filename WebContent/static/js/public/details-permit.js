function sendFormPermit(form_id)//send permit cart
{
	var form = $('#'+form_id);
	
	if($("#mpermit-type-id").val() == "" || $("#mpermit-type-id").val() == null){
		alert('Select the Permit Type');
		return false;
	}
	
	 $.ajax({type: 'post', url: _contextPath + '/cart/permit', data: form.serialize(), success: sendFormPCB,dataType: 'json' });
	
}
		
function sendFormPCB(data)
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
$(document).ready(function(){
	$('#mpermit-type-id').change (function (){
		
		var option = this.options[this.selectedIndex];
		var cost = option.getAttribute("cost");
		$('#permit-amount').html(cost);
});	
	
	
		
});	