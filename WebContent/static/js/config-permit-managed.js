PermitPanel = Ext.extend(Ext.TabPanel,{
	initComponent : function()
	{
		
		var filterForm = {
			bodyBorder: false,
			border: false,
			frame: false,
			defaultType:'textfield',
			labelAlign: 'top',
			buttonAlign:'center',
			bodyStyle: 'padding: 10px; ',
			autoWidth: true,
			defaults: { width: '95%' },
			bodyCssClass: 'x-citewrite-panel-body',
			items:[{
			    	   fieldLabel: 'Code ID',
			    		name: 'codeid'
			       },
			       {
			    	   fieldLabel: 'Description',
			    	   name: 'description'
			       }],
        buttons: [{
            text: 'Apply',
            width: 60,
            handler: function(){
            	
            	var form = this.findParentByType('form');
            	var params = form.getForm().getFieldValues();
               
            	var parent = form.findParentBy(function(c){
            		if(c.layout.type == 'border')
            		{
            			return true;
            		}
            		
            		return false;
            	});
            	var grids = parent.findBy(function(c) 
            			{
            				if(c.store != undefined && c.store != null)
            				{
            					return true;
            				}
            				return false;
            			});
            	if(grids.length > 0)
            	{
            		var grid = grids[0];
	            	Ext.apply(grid.store.baseParams, params);
	            	grid.store.load({params: {start: 0, limit: codePageLimit}});
            	}
            }
        },{
            text: 'Reset',
            width: 60,
            handler: function(){
            	var form = this.findParentByType('form');
            	form.getForm().reset();
            	
            	var parent = form.findParentBy(function(c){
            		if(c.layout.type == 'border')
            		{
            			return true;
            		}
            		
            		return false;
            	});
            	var grids = parent.findBy(function(c) 
            			{
            				if(c.store != undefined && c.store != null)
            				{
            					return true;
            				}
            				return false;
            			});
            	if(grids.length > 0)
            	{
            		var grid = grids[0];
            	
	            	var type = grid.store.baseParams.type;
	            	grid.store.baseParams = {type: type};
	            	grid.store.load({params: {start: 0, limit: codePageLimit}});
            	}
            }
        }]
	}; //filterForm
		
		var violationFilterForm = Ext.apply({}, {}, filterForm);
		violationFilterForm.items = [
				{
					   fieldLabel: 'Code ID',
						name: 'codeid'
				},
				{
					   fieldLabel: 'Description',
					   name: 'description'
				},
				{
					fieldLabel: 'Amount',
			    	name: 'fine_amount'
			    },
				{
					fieldLabel: 'Type',
			    	name: 'fine_type'
			    },
				{
			    	xtype: 'checkbox',
					boxLabel: 'Overtime Only',
			    	name: 'is_overtime',
			    	hideLabel: true
			    }];
		
		
		var permitTypeGrid = new PermitTypeGrid();
		var ownerTypeGrid = new OwnerTypeGrid();
		var config = 
		{
			title: 'Manage Permits',
			activeTab: 0,
			tabPosition: 'bottom',
			border: false,
			frame: false,
			items:[
			       {			       
					title: 'General',
					id: 'permit-general-config-panel',
					bodyCssClass: 'x-citewrite-panel-body',
					padding: '10px',
					bodyStyle: 'margins:20px 0px 10px 0px',
					layout: 'fit',
					items: [new PermitGeneralPanel()]
				   },			   
			       {
					xtype: 'panel',
					title: 'Permit Fields',
					id: 'permit-fields-config-panel',
					bodyCssClass: 'x-citewrite-panel-body',
					padding: '10px',
					autoScroll: true,
					autoLoad : { url : _contextPath + '/managedpermit/admin', scripts : true } 
			       },
				   {
						xtype: 'panel',
						title: 'Permit Type Fields',
						id: 'permit-type-fields-config-panel',
						bodyCssClass: 'x-citewrite-panel-body',
						padding: '10px',
						autoScroll: true,
						autoLoad : { url : _contextPath + '/managedpermittype/admin', scripts : true } 
					},
					{
						xtype: 'panel',
						title: 'Permit Types',
						layout:'border',
						border: false,
						bodyCssClass: 'x-citewrite-border-ct',
						defaults: {
						    collapsible: true,
						    split: true,
						    layout: 'fit'
						},
						items: [{
								collapsible: false,
							    region:'center',
							    margins: '5 0 5 5',
								items: [permitTypeGrid]
							},{
								title: 'Filter',
								region:'east',
								margins: '5 5 5 0',
								width: 200,
								items: [permitTypeGrid.getFilterPanel()]
							}]
					},
					
					
					{
						xtype: 'panel',
						title: 'Device - Permit View',
						id: 'permit-device-view-config-panel',
						bodyCssClass: 'x-citewrite-panel-body',
						padding: '10px',
						autoScroll: true,
						autoLoad : { url : _contextPath + '/managedpermit/deviceView', scripts : true } 
					},
					
					{
						xtype: 'panel',
						title: 'Vehicle Fields',
						id: 'vehicle-fields-config-panel',
						bodyCssClass: 'x-citewrite-panel-body',
						padding: '10px',
						autoScroll: true,
						autoLoad : { url : _contextPath + '/vehicle/admin', scripts : true } 
					},
					
					{
						xtype: 'panel',
						title: 'Owner Fields',
						id: 'owner-fields-config-panel',
						bodyCssClass: 'x-citewrite-panel-body',
						padding: '10px',
						autoScroll: true,
						autoLoad : { url : _contextPath + '/owner/admin', scripts : true } 
					},
					
					{
						xtype: 'panel',
						title: 'Owner Types',
						layout:'border',
						border: false,
						bodyCssClass: 'x-citewrite-border-ct',
						defaults: {
						    collapsible: true,
						    split: true,
						    layout: 'fit'
						},
						items: [{
								collapsible: false,
							    region:'center',
							    margins: '5 0 5 5',
								items: [ownerTypeGrid]
							},{
								title: 'Filter',
								region:'east',
								margins: '5 5 5 0',
								width: 200,
								items: [ownerTypeGrid.getFilterPanel()]
							}]
					}
			]//end items
		
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		PermitPanel.superclass.initComponent.apply(this, arguments);
	}
});