Ext.onReady(function(){
	var invoice = Ext.get('nav-vehicles');
	if(invoice != null)
	{
		
		invoice.on('click',function(){
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			
			content.add(new SearchPanel());
			content.doLayout();
			return;
		});//end onclick 
	}
});
