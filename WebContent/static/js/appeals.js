Ext.onReady(function(){
	var appeals = Ext.get('nav-ocupancy');
	if(appeals != null)
	{
		
		appeals.on('click',function(){
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			
			content.add(new AppealsCitationPanel());
			content.doLayout();
			return;
		});//end onclick 
	}
});
