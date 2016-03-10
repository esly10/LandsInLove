PermitTypeGrid = Ext.extend(Ext.grid.GridPanel,
{
	initComponent : function()
	{
		var columns = [{
				dataIndex: 'mpermit_type_id',
				header: 'ID',
				sortable: true,
				width: 85
			},{
				dataIndex: 'name',
				header: 'Name',
				sortable: true,
				width: 85
			},{
				dataIndex: 'description',
				header: 'Description',
				sortable: true
			},{
				dataIndex: 'period_type',
				header: 'Period',
				renderer: function(value, p1, record)
				{
					if(value == 'relative')
					{
						return record.data.period_days + " days";
					}
					var start = new Date(Date.parse(record.data.period_start_date, 'M d, Y g:i:s A'));
					var end = new Date(Date.parse(record.data.period_end_date, 'M d, Y g:i:s A'));
					
					return start.format('m/d/y') + ' - ' + end.format('m/d/y');
				},
				sortable: false
			},{
				dataIndex: 'cost',
				header: 'Cost',
				renderer: Ext.util.Format.usMoney,
				sortable: true
			}];
		
		var store = new Ext.data.JsonStore({
			root: 'types',
			url: _contextPath + '/managedpermittype/list',
			totalProperty: 'count',
			fields: [
			         'mpermit_type_id', 
			         'name', 
			         'description', 
			         'cost', 
			         'extra', 
			         'period_type', 
			         {name: 'period_days', type: 'int'}, 
			         {name: 'period_start_date', type: 'Date', dateFormat:'Y-m-dTH:i:s'},
			         {name: 'period_end_date', type: 'Date', dateFormat:'Y-m-dTH:i:s'},
			         ],
			remoteSort: true,
			sortInfo: {
					field: 'mpermit_type_id',
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
	            emptyMsg: "No permit types to display",
	            pageSize: 50,
	            items: ['-', 
	                    {
			                text: 'Add',
			                cls: 'x-btn-text details',
			                handler: function(btn, event)
			                { 
			                	Ext.Ajax.request({
									url:_contextPath + '/managedpermittype/fields',
									success:function(p1, p2)
									{ 
										var response = Ext.util.JSON.decode(p1.responseText);
										if(response.success)
										{
											grid.addPermitType(null, response.fields, response.ownerTypes); 
										}
										else
										{
											Ext.Msg.show({
											   title:'Failure',
											   msg: response.msg,
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
										}
									},
									failure:function(){}
								});
			                	
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
					grid.permitTypeGridMenu(index,event);
				},
				'rowdblclick' : function(grid, index, event) 
				{
					grid.editPermitType(grid, grid.getStore().getAt(index));
				}
			}
				
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		PermitTypeGrid.superclass.initComponent.apply(this, arguments);
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
				    		name: 'mpermit_type_id'
				       },
				       {
				    	   fieldLabel: 'Name',
				    	   name: 'name'
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
	addPermitType: function(record, fields, ownerTypes)
	{
		grid = this;
		var general = {
				title: 'Default',
				bodyBorder: false,
				border: false,
				layout: 'form',
				frame: false,
				autoHeight: true,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px; ',
				bodyCssClass: 'x-citewrite-panel-body',
                defaults: { width: '95%' },
				items: [{
						xtype: 'hidden',
						id: 'ptype-type-id',
						value: 0
					},{
						id: 'ptype-name',
			    	   fieldLabel: 'Name',
			    	   maskRe: /^[ a-zA-Z0-9-]*$/,
		               allowBlank: false
			       },{
			    	   id: 'ptype-description',
			    	   fieldLabel: 'Description',
		               allowBlank: false
			       },{
			    	   id: 'ptype-max-vehicles',
			    	   fieldLabel: 'Max Vehicles',
			    	   maskRe: /^[0-9]*$/,
			    	   value: 1,
		               allowBlank: false
			       },{
			    	   xtype: 'combo',
			    	   id: 'ptype_period-type',
			    	   hiddenName: 'ptype-period-type',
			    	   fieldLabel: 'Period',
			    	   submitValue: true,
		               width: 165,
					 	lazyRender: false,
					 	store: new Ext.data.ArrayStore({
					        autoDestroy: true,
					        fields: ['id', 'description'],
					        data : [
					            ['absolute', 'Absolute'],
					            ['relative', 'Relative']
					        ]
					    }),
					    displayField: 'description',
					    valueField: 'id',
					    value: 'absolute',
						triggerAction: 'all',
						forceSelection: true,
						mode: 'local'
						,listeners: {
							select: function(combo, record, index)
							{
								var absolutePanel = Ext.getCmp('ptype-absolute-panel');
								var relativePanel = Ext.getCmp('ptype-relative-panel');
								if(record.data.id == 'absolute')
								{
									absolutePanel.show();
									relativePanel.hide();
								}
								else
								{
									absolutePanel.hide();
									relativePanel.show();
								}
							}
						}
			       },{
			    	   xtype: 'panel',
			    	   layout: 'form',
			    	   id: 'ptype-absolute-panel',
			    	   border: false,
			    	   padding: 0,
						bodyCssClass: 'x-citewrite-panel-body',
			    	   items:[{
			    		   		xtype: 'datefield',
			    		   		id: 'ptype-start-date',
			    		   		fieldLabel: 'Start Date',
								format: 'm/d/Y'
			    	   		},{
			    		   		xtype: 'datefield',
			    		   		id: 'ptype-end-date',
			    		   		fieldLabel: 'End Date',
								format: 'm/d/Y'
			    	   		}]
			       },{
			    	   xtype: 'panel',
			    	   layout: 'form',
			    	   id: 'ptype-relative-panel',
						bodyCssClass: 'x-citewrite-panel-body',
			    	   hidden: true,
			    	   border: false,
			    	   padding: 0,
			    	   items:[{
			    		   		xtype: 'textfield',
			    		   		id: 'ptype-days',
			    		   		fieldLabel: 'Days',
			    		   		maskRe: /^[0-9]*$/,
			    		   		allowBlank: true,
			    		   		width: 100
			    	   		}]
			       },{
			    	   id: 'ptype-cost',
			    	   fieldLabel: 'Cost',
		               allowBlank: false,
		               value: '0.00',
		               width: 100
			       },		       
			       {
			    	   xtype: 'checkbox',
			    	   id: 'ptype-requires-shipping',
			    	   fieldLabel: '',
			    	   boxLabel: 'Requires Shipping',
			    	   listeners: {
			        	    check: function(checkbox, checked) {
			        	    	if(checked){
			        	    		Ext.getCmp('ptype-pick-up').enable(); 	
			        	    	}else{
			        	    		Ext.getCmp('ptype-pick-up').disable();
			        	    	}
			        	    }
			        	}
			       },
			       {
			    	   xtype: 'checkbox',
			    	   id: 'ptype-pick-up',
			    	   fieldLabel: '',
			    	   style:{
				            marginLeft: '15px'
				        },
			    	   boxLabel: 'Pick-up',
			    	   disabled:true
			       },
			       {
			    	   xtype: 'checkbox',
			    	   id: 'ptype-print',
			    	   fieldLabel: '',
			    	   boxLabel: 'Print'
			       }]
			};
		
		var additional = {
				title: 'Additional',
				bodyBorder: false,
				border: false,
				frame: false,
	                        layout: 'form',
				autoHeight: true,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px; ',
				bodyCssClass: 'x-citewrite-panel-body',
				autoWidth: true,
				defaults: { width: '95%' },
				items: []
		};
		
		if(fields != undefined && fields.length > 0)
		{
			for(var i = 0; i < fields.length; i++)
			{
				var field = fields[i];
				if(field.type == 'list' || field.type == 'database')
				{
					additional.items.push({
				    	   xtype: 'combo',
				    	   id: 'ptype_'+field.name,
				    	   hiddenName: 'ptype-'+field.name,
				    	   fieldLabel: field.label,
				    	   width: 165,
						 	lazyRender: false,
						 	store: new Ext.data.JsonStore({
						        autoDestroy: true,
						        fields: ['id', 'name'],
						        data : field.options
						    }),
						    displayField: 'name',
						    valueField: 'id',
							triggerAction: 'all',
							forceSelection: true,
							mode: 'local',
							allowBlank: (field.required != true),
							submitValue: true
				       });
				}
				else //text
				{
					var options = {
					    	   id: 'ptype_'+field.name,
					    	   name: 'ptype-'+field.name,
					    	   fieldLabel: field.label,
				               allowBlank: (field.required != true)
					       };
					if(field.validation.length > 0)
					{
						options.maskRe = new RegExp(field.validation);
					}
					additional.items.push(options);
				}
			}
		}
		
		var otPanel = {
				title: 'Owner Types',
				bodyBorder: false,
				border: false,
				frame: false,
				layout: 'form',
				autoHeight: true,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px; ',
				bodyCssClass: 'x-citewrite-panel-body',
				autoWidth: true,
				defaults: { width: '95%' },
				items: []
		};
		
		if(ownerTypes != undefined && ownerTypes.length > 0)
		{
			for(var i = 0; i < ownerTypes.length; i++)
			{
				var type = ownerTypes[i];
				otPanel.items.push({
			    	   xtype: 'checkbox',
			    	   id: 'ptype_owner_type_'+type.owner_type_id,
			    	   name: 'ptype-owner-type-'+type.owner_type_id,
			    	   boxLabel: type.name,
			    	   hideLabel: true,
			    	   value: type.owner_type_id
			       });
			}
		}
		var tabPanel = new Ext.TabPanel({
			autoHeight: true,
			autoWidth: true,
			activeTab: 0,
			border: false,
			frame: false,
			deferredRender: false,
			items:[new Ext.Panel(general), new Ext.Panel(additional), new Ext.Panel(otPanel)]
		});
		
		var formPanel = new Ext.FormPanel({
			border: false,
			frame: false,
			bodyBorder: false,
			items: [tabPanel]
			});
	  
		var ajaxParams = {};
		var title = "Add ";
		if(record != null)
		{
			title = "Edit ";
		}
		
		title += " Permit Type";
		
		var codeWindow = new Ext.Window({
            renderTo: document.body,
            title: title,
            width:325,
            height: 300,
            plain: true,
            resizable: false,
            modal: true,
            id: 'permitTypeFormWindow',
            items: formPanel,
            autoScroll: true,
            
            buttons: [{
                text:'Save',
                handler: function()
                {                	
                	var period = Ext.getCmp('ptype_period-type').getValue();
                	if(period.length == 0)
                	{
                		Ext.Msg.show({
          				   title:'Error!',
          				   msg: 'Please select a period.',
          				   buttons: Ext.Msg.OK,
          				   icon: Ext.MessageBox.ERROR
          				});
                		return;
                	}
                	else if(period == 'relative')
                	{
                		var days = Ext.getCmp('ptype-days').getValue();
                		if(days.length == 0)
                		{
                			Ext.Msg.show({
            				   title:'Error!',
            				   msg: 'Please enter a value for Days.',
            				   buttons: Ext.Msg.OK,
            				   icon: Ext.MessageBox.ERROR
            				});
                    		return;
                		}
                	}
                	else if(period == 'absolute')
                	{
                		var start = Ext.getCmp('ptype-start-date').getValue();
                		var end = Ext.getCmp('ptype-start-date').getValue();
                		if(start.length == 0)
                		{
                			Ext.Msg.show({
             				   title:'Error!',
             				   msg: 'Please select a Start Date.',
             				   buttons: Ext.Msg.OK,
             				   icon: Ext.MessageBox.ERROR
             				});
                    		return;
                		}
                		else if(end.length == 0)
                		{
                			Ext.Msg.show({
             				   title:'Error!',
             				   msg: 'Please select an End Date.',
             				   buttons: Ext.Msg.OK,
             				   icon: Ext.MessageBox.ERROR
             				});
                    		return;
                		}
                	}
                	
                	//validate form
                	formPanel.getForm().submit({
                	    url: _contextPath + '/managedpermittype/save',
                	    scope: this,
                	    params: ajaxParams,
                	    success: function(form, action) {
                	    	grid.store.reload();
                	    	
                	    	var parent = action.options.scope.findParentByType('window'); 
                	    	parent.close();
                	       
                	    	Ext.growl.message('Success', 'Permit type has been saved.');
                	    },
                	    failure: function(form, action) {
                	        switch (action.failureType) {
                	            case Ext.form.Action.CLIENT_INVALID:
                	                Ext.Msg.show({
                  	            	   title:'Error',
                  	            	   msg: 'All fields are required.',
                  	            	   buttons: Ext.Msg.OK,
                  	            	   icon: Ext.MessageBox.ERROR
                  	            	});
                	                break;
                	            case Ext.form.Action.CONNECT_FAILURE:
                	                Ext.Msg.show({
                 	            	   title:'Failure',
                 	            	   msg: 'Ajax communication failed',
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
                	               break;
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
		
		tabPanel.setActiveTab(0);
		codeWindow.show();
		
		
		if(record != null)
		{
			Ext.getCmp('ptype-type-id').setValue(record.mpermit_type_id);
			Ext.getCmp('ptype-name').setValue(record.name);
			Ext.getCmp('ptype-description').setValue(record.description);
			Ext.getCmp('ptype-max-vehicles').setValue(record.max_vehicles);
			Ext.getCmp('ptype_period-type').setValue(record.period_type);
			if(record.period_type == 'relative')
			{
				Ext.getCmp('ptype-absolute-panel').hide();
				Ext.getCmp('ptype-relative-panel').show();
				Ext.getCmp('ptype-days').setValue(record.period_days);
			}
			else
			{
				var field = Ext.getCmp('ptype-start-date');
				var start = new Date(Date.parse(record.period_start_date, 'Y-m-dTH:i:s'));
				field.setValue(start);
				
				field = Ext.getCmp('ptype-end-date');
				var end = new Date(Date.parse(record.period_end_date, 'Y-m-dTH:i:s'));
				field.setValue(end);
			}
			var cost = parseFloat(record.cost);
			Ext.getCmp('ptype-cost').setValue(Ext.util.Format.number(cost, '0.00'));
			Ext.getCmp('ptype-requires-shipping').setValue(record.requires_shipping);
			Ext.getCmp('ptype-pick-up').setValue(record.can_pickup);
			Ext.getCmp('ptype-print').setValue(record.can_print);
			if(fields != undefined && fields.length > 0)
			{
				for(var i = 0; i < fields.length; i++)
				{
					var field = fields[i];
					var input = Ext.getCmp('ptype_'+field.name);
					
					var attr = getAttributeByName(record.extra, field.name);
					if(attr != null)
					{
						input.setValue(attr.value);
					}
				}
			}
			
			if(record.owner_types != undefined)
			{
				for(var i = 0; i < record.owner_types.length; i++)
				{
					var ot = record.owner_types[i];
					var cb = Ext.getCmp('ptype_owner_type_'+ot.owner_type_id);
					cb.setValue(true);
				}
			}
		}
	},
	editPermitType: function(grid, record)
	{
		Ext.Ajax.request({
			url:_contextPath + '/managedpermittype/details',
			success:function(p1, p2)
			{ 
				var response = Ext.util.JSON.decode(p1.responseText);
				if(response.success)
				{
					grid.addPermitType(response.type, response.fields, response.ownerTypes); 
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
			params: {'permit-type-id': record.data.mpermit_type_id, fields: 'true'}
		});
	},
	permitTypeGridMenu: function(index, event)
	{
		var grid = this;
		event.stopEvent();
		var record = grid.getStore().getAt(index);

		var edit_item = 
		{
			text: 'Edit',
			handler: function() 
			{
				menu.hide();

				grid.editPermitType(grid, record);
			}
		};
		
		var delete_item = 
		{
			text: 'Delete',
			handler: function(p1, p2, p3) 
			{
				menu.hide();
							
				Ext.Msg.show({
					   title:'Delete Permit Type?',
					   msg: 'Delete '+record.data.name+'?',
					   buttons:
					   {
							yes:'Yes',
							cancel:'Cancel'
					   },
					   fn: function(button)
					   {
						   params ={ 'permit-type-id': record.data.mpermit_type_id };
					   
						   switch(button)
						   {
							   	case 'yes':
							   	{
									Ext.Ajax.request({
										url:_contextPath + '/managedpermittype/delete',
										success:function(p1, p2)
										{ 
											var response = Ext.util.JSON.decode(p1.responseText);
											if(response.success)
											{
												grid.store.reload();
												Ext.growl.message('Success', 'Permit Type has been deleted.');
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


var getAttributeByName = function(list, name)
{
	for(var i = 0; i < list.length; i++)
	{
		var item = list[i];
		if(item.name == name)
		{
			return item;
		}
	}
	return null;
};