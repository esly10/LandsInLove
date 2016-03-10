function view(index)
{
	if($('#invoice_details-'+index).is (':visible'))
	{
		$('#invoice_details-'+index).slideUp(1000);
		$('#button-icon-'+index).removeClass('expand');
	}
	else
	{
		$('#invoice_details-'+index).slideDown(1000);
		$('#button-icon-'+index).addClass('expand');
	}
	
}

function printInvoice(id)
{
	$(document.body).append('<iframe name="print-frame" id="print-frame" width="0" height="0" src= "'+_contextPath+'/owner/printInvoice?invoiceId='+id+'"></iframe>');
}