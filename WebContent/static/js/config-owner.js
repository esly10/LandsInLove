OwnerTypeGrid = Ext.extend(Ext.grid.GridPanel,
{
	initComponent : function()
	{
		var columns = [{
				dataIndex: 'owner_type_id',
				header: 'ID',
				sortable: true,
				width: 85
			},{
				dataIndex: 'name',
				header: 'Name',
				sortable: true,
				width: 85
			},{
				dataIndex: 'requires_auth',
				header: 'Requires Auth',
				sortable: true,
				renderer: function(v){ if(v == 1){ return "Yes"; } return "No"; }
			},{
				dataIndex: 'active',
				header: 'Active',
				sortable: true,
				renderer: function(v){ if(v == 1){ return "Yes"; } return "No"; }
			}];
		
		var store = new Ext.data.JsonStore({
			root: 'types',
			url: _contextPath + '/owner/types',
			totalProperty: 'count',
			fields: [
			         'owner_type_id', 
			         'name',
			         {name: 'requires_auth', type:'number'},
			         {name: 'active', type:'number'}
			         ],
			remoteSort: true,
			sortInfo: {
					field: 'owner_type_id',
					direction: 'ASC'
	        },
	        autoLoad: {start: 0, limit: 50}
		});
		
		var grid = this;
		var config = 
		{
			stripeRows: true,
	        loadMask: true,
	        layout: 'fit',
			frame: false,
			border:false,
			bodyBorder: false,
			store: store,
			bbar:
			{
				xtype:'paging',
				store: store,
	            displayInfo: true,
	            displayMsg: 'Displaying {0} - {1} of {2}',
	            emptyMsg: "No owner types to display",
	            pageSize: 50,
	            items: ['-', 
	                    {
			                text: 'Add',
			                cls: 'x-btn-text details',
			                handler: function(btn, event)
			                { 
			                	grid.addOwnerType(null); 
			                	
			                }
	                    }] 
			},	
			viewConfig:
			{
				forceFit:true
			},
			columns: columns,
			listeners: 
			{
				'rowcontextmenu' : function(grid, index, event) 
				{
					grid.ownerTypeGridMenu(grid,index,event);
				},
				'rowdblclick' : function(grid, index, event) 
				{
					grid.addOwnerType(grid.getStore().getAt(index).data);
				}
			}
				
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		OwnerTypeGrid.superclass.initComponent.apply(this, arguments);
	},
	getFilterPanel: function()
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
				    	   fieldLabel: 'ID',
				    		name: 'filter_owner_type_id'
				       },
				       {
				    	   fieldLabel: 'Name',
				    	   name: 'filter_owner_type_name'
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
		            	grid.store.load({params: {start: 0, limit: 50}});
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
	            		grid.store.baseParams = {};
		            	grid.store.load({params: {start: 0, limit: 50}});
	            	}
	            }
	        }]
		}; //filterForm
		
		return new Ext.FormPanel(filterForm);
	},
	addOwnerType: function(record)
	{
		var grid = this;
		var fp = {
				bodyBorder: false,
				border: false,
				frame: false,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px; ',
				bodyCssClass: 'x-citewrite-panel-body',
				autoWidth: true,
				defaults: { width: '95%' },
				items: [{
						xtype: 'hidden',
						id: 'otype-type-id',
						value: 0
					},{
						id: 'otype-name',
			    	   fieldLabel: 'Name',
			    	   maskRe: /^[a-zA-Z0-9-\s]*$/,
		               allowBlank: false
			       },{
						id: 'otype-requires-auth',
						xtype: 'checkbox',
						boxLabel: 'Requires Authorization'
				       },{
							id: 'otype-active',
							xtype: 'checkbox',
							boxLabel: 'Active'
					       }]
			};
		
		var formPanel = new Ext.FormPanel(fp);
	  
		var ajaxParams = {xaction: 'save'};
		var title = "Add ";
		if(record != null)
		{
			title = "Edit ";
		}
		
		title += " Owner Type";
		
			var ownerWindow = new Ext.Window({
	            renderTo: document.body,
	            title: title,
	            width:325,
	            height: 200,
	            plain: true,
	            resizable: false,
	            modal: true,
	            id: 'ownerTypeFormWindow',
	            items: formPanel,
				autoScroll: true,

	            buttons: [{
	                text:'Save',
	                handler: function()
	                {                	
	                		                	
	                	//validate form
	                	formPanel.getForm().submit({
	                	    url: _contextPath + '/owner/types',
	                	    scope: this,
	                	    params: ajaxParams,
	                	    success: function(form, action) {
	                	    	grid.store.reload();
	                	    	
	                	    	var parent = action.options.scope.findParentByType('window'); 
	                	    	parent.close();
	                	       
	                	    	Ext.growl.message('Success', 'Owner type has been saved.');
	                	    },
	                	    failure: function(form, action) {
	                	        switch (action.failureType) {
	                	            case Ext.form.Action.CLIENT_INVALID:
	                	            	Ext.Msg.show({
                	            		   title:'Error!',
                	            		   msg: 'All fields are required.',
                	            		   buttons: Ext.Msg.OK,
                	            		   icon: Ext.MessageBox.ERROR
                	            		});
	                	                break;
	                	            case Ext.form.Action.CONNECT_FAILURE:
	                	            	Ext.Msg.show({
                	            		   title:'Failure',
                	            		   msg: 'Ajax communication failed.',
                	            		   buttons: Ext.Msg.OK,
                	            		   icon: Ext.MessageBox.ERROR
                	            		});
	                	                break;
	                	            case Ext.form.Action.SERVER_INVALID:
	                	            	Ext.Msg.show({
	                	            		   title:'Failure',
	                	            		   msg: action.result.msg,
	                	            		   buttons: Ext.Msg.OK,
	                	            		   icon: Ext.MessageBox.ERROR
	                	            		});
	                	       }
	                	    }
	                	});
	                }
	            },{
	                text: 'Close',
	                handler: function(){
	                	this.findParentByType('window').close();
	                }
	            }]
	        });
			
			ownerWindow.show();
			if(record != null)
			{
				Ext.getCmp('otype-type-id').setValue(record.owner_type_id);
				Ext.getCmp('otype-name').setValue(record.name);
				Ext.getCmp('otype-requires-auth').setValue(record.requires_auth);
				Ext.getCmp('otype-active').setValue(record.active);
			}
			else
			{
				Ext.getCmp('otype-type-id').setValue(0);
				Ext.getCmp('otype-default').setValue(0);
				Ext.getCmp('otype-active').setValue(1);
			}
	},
	ownerTypeGridMenu: function(grid, index, event)
	{
		event.stopEvent();
		var record = grid.getStore().getAt(index);

		var edit_item = 
		{
			text: 'Edit',
			handler: function() 
			{
				menu.hide();

				grid.addOwnerType(record.data);
				
			}
		};
		
		var delete_item = 
		{
			text: 'Delete',
			handler: function(p1, p2, p3) 
			{
				menu.hide();
							
				Ext.Msg.show({
					   title:'Delete Owner Type?',
					   msg: 'Delete '+record.data.name+'?',
					   buttons:
					   {
							yes:'Yes',
							cancel:'Cancel'
					   },
					   fn: function(button)
					   {
						   var params = {'owner-type-id': record.data.owner_type_id, xaction: 'delete'};
					   
						   switch(button)
						   {
							   	case 'yes':
							   	{
									Ext.Ajax.request({
										url:_contextPath + '/owner/types',
										success:function(p1, p2)
										{ 
											var response = Ext.util.JSON.decode(p1.responseText);
											if(response.success)
											{
												grid.store.reload();
												Ext.growl.message('Success', 'Owner Type has been deleted.');
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
										failure:function(){},
										params: params
									});
								}
						   }
					   },
					   animEl: 'elId',
					   icon: Ext.MessageBox.QUESTION
					});
			}
		};
		
		var menu = new Ext.menu.Menu(
		{
			items: [edit_item, delete_item]
		});
		menu.showAt(event.xy);
	}
});