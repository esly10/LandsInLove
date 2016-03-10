Ext.onReady(function(){
	var citations = Ext.get('nav-listsa');
	if(citations != null)
	{
		
		citations.on('click',function(){
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			
			content.add(new CitationPanel());
			content.doLayout();
			return;
		});//end onclick 
	}
});
