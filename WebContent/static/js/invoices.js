Ext.onReady(function(){
	var invoice = Ext.get('nav-payment');
	if(invoice != null)
	{
		
		invoice.on('click',function(){
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			
			content.add(new InvoicePanel());
			content.doLayout();
			return;
		});//end onclick 
	}
});
