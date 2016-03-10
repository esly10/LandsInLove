PermitFieldConfiguration = Class.create(FieldConfiguration, {
	currentRow: null,
	permitType: false,
	initialize: function(options) 
	{
		this.options = {
			container: null,
			btnAdd: null,
			numFormat: null,
			numCounter: null,
			url: null
		};
		
		Object.extend(this.options, options || {});
		
		if(this.options.btnAdd != null)
		{
			this.options.btnAdd.onclick = this.add.bind(this);
		}
		this.doBind();
	},
	save: function(button, event)
	{
		var object = this;
		var format = this.options.numFormat.value;
		
		if(format.length == 0)
		{
			Ext.Msg.show({
				   title:'Error',
				   msg: 'Please enter a Citation Number Format.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
			return;
		}
		
		var counter = this.options.numCounter.value;
		if(counter.legnth == 0 || isNaN(parseInt(counter)))
		{
			Ext.Msg.show({
				   title:'Error',
				   msg: 'Please enter a valid integer for the Citation Number Counter.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
			return;
		}
		
		var data = {xaction: 'permit-number', format: format, counter: counter};
		
		Ext.Ajax.request({
				url: object.options.url,
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   Ext.growl.message('Success', 'Permit Counter and Format have been saved.');
				   }
				   else
				   {
					   Ext.Msg.show({
	    				   title:'Error',
	    				   msg: response.msg,
	    				   buttons: Ext.Msg.OK,
	    				   icon: Ext.MessageBox.ERROR
	    				});
				   }
			   },
			   failure: function()
			   {
				   Ext.Msg.show({
    				   title:'Error',
    				   msg: 'Error saving configuration.',
    				   buttons: Ext.Msg.OK,
    				   icon: Ext.MessageBox.ERROR
    				});
			   },
			   params: data
			});
	}
});