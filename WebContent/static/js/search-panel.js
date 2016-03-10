SearchPanel = Ext.extend(Ext.Panel, {
	stateStore: null,
	initComponent: function()
    {

		 var panel = this;
		
	    this.stateStore = new Ext.data.JsonStore({
	    	root: 'states',
			url: _contextPath + '/permit/permitSearchableList',
			totalProperty: 'count',
			fields: ['label', 'queryName', 'columnName'],
			autoLoad: true
	    });
		
		var config = 
		{
			xtype: 'panel',
			title: 'Search Type',
			layout: 'fit',
			bodyBorder: false,
			border: false,
			autoScroll: true,
			bodyCssClass: 'x-citewrite-panel-body',
			tbar: [
		            {
						xtype: 'combo',
						id: 'searchType',
						valueField: 'columnName',
						displayField: 'label',
						lazyRender: false,
					 	store: this.stateStore,
						typeAhead: false,
						width: 100,
						triggerAction: 'all',
						forceSelection: true,
						mode: 'local',
						editable:false,
					}, ' ',
					{
						xtype: 'textfield',
						id: 'lookup-value',
		                width:150,
		                allowblank: false,
		                enableKeyEvents: true,
		                listeners: {
							keydown: function(f,e)
							{
								var flag = false;
								var charCode = e.keyCode;
								var regex = /^([a-z,A-Z,0-9,%, ])*$/;
								var content = Ext.getCmp('lookup-value').getValue();
								
								if(Ext.getCmp('searchType').getValue() != undefined && Ext.getCmp('searchType').getValue() != "" && Ext.getCmp('searchType').getValue() == "v_license"){
								 regex = /^([a-z,A-Z,0-9])*$/;
								 flag = true;
								}
								
								
								
								if ( e.keyCode == 13 || e.keyCode > 105) {
									 e.stopEvent();
									 return false;
								}
	 
	 
							 	if (!charCode || (content == undefined)) {
							 		e.stopEvent();
									return false;
								}
								 
	
								 
							 	try{
									var code = e.keyCode ? e.keyCode : e.keyCode;
									if(code!==46 && code!==8 && code!==37 && code!==39){
									
										if(charCode>95 && charCode<106){
											charCode = charCode - 48;
										}
									
										content = content + String.fromCharCode(charCode).toLowerCase();
										
										if(content.substr(0,content.length).match(regex) == undefined){
			 								e.stopEvent();
											return false;
										}else{
											if(e.shiftKey && e.keyCode != 53){
											    e.stopEvent();
												return false;
										    }else if (e.shiftKey && e.keyCode  == 53){
										    	if(flag){
										    		e.stopEvent();
													return false;
										    	}
										    }
										   
										}
	
									}
									
									
									
								}catch(e){}
							}	
						}

		            }, ' ',
					{
		            	xtype: 'tbbutton',
		            	text: 'GO',
		            	handler: function()
		            	{
		            		panel.doLookup();
		            	}
		            },
		            {
		            	xtype: 'tbbutton',
		            	text: 'RESET',
		            	handler: function()
		            	{
		            		$('searchType').setValue('');
		            		$('lookup-value').value = '';
		            		$('lookup-value').focus();
		            		panel.removeAll();
		            	}
	            	}
		        ],
			items: [

				]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CitationPanel.superclass.initComponent.apply(this, arguments);

    },
    doLookup: function()
	{
    	var value = Ext.getCmp('lookup-value').getValue();
    	var searchType = Ext.getCmp('searchType').getValue();
    	
    	var data = {
				value: value,
				searchType: searchType
		};
    	
    	if(value.length == 0)
		{
    		Ext.Msg.show({
			   title:'Missing Field',
			   msg: 'Please enter a value.',
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});
    		
			return false;
		}
    	
    	if(searchType.length == 0)
		{
    		Ext.Msg.show({
 			   title:'Missing Field',
 			   msg: 'Please enter a search type.',
 			   buttons: Ext.Msg.OK,
 			   icon: Ext.MessageBox.ERROR
 			});

			return false;
		}
    	
    	 var tabs = new Ext.TabPanel({
             border: false,
             bodyBorder: false,
             activeTab: 0,
             frame:false,
             plain: false,
             tabPosition: 'top',
             id: 'searchTabPanel'
         });
    	 
    	Ext.getCmp('statusbar').showBusy('Searching...');
    	this.removeAll();
    	var panel = this;
    	this.load({
		    url: _contextPath + '/search/search',
		    scripts: true,
		    params: data, // or a URL encoded string
		    callback: function()
		    {
		    	//alert('Hello');
		    	Ext.getCmp('statusbar').clearStatus();
		    	panel.add(tabs);
		    	panel.doLayout();
		    }});
	}
});




