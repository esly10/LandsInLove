var _ownerMutex = false;
OwnerPermitPanel = Ext.extend(Ext.grid.GridPanel, {
		owner: null,
		typeStore: null,
		stateStore: null,
		initComponent: function()
	    {
			// create the Data Store
		    var store = new Ext.data.JsonStore({
				url: _contextPath + '/owner/permits',
				root: 'permits',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'mpermit_id',
		            'permit_number',
		            'status',
		            {name: 'valid_start_date', type: 'date', dateFormat:'Y-m-dTH:i:s'},
		            {name: 'valid_end_date', type: 'date', dateFormat:'Y-m-dTH:i:s'},
		            'permit_type',
		            'permit_description'
		        ],
				sortInfo: {
					field: 'valid_start_date',
					direction: 'DESC'
				},
				baseParams: {owner_id: this.owner.owner_id},
				autoLoad: true
		    });
		    
		    this.typeStore = new Ext.data.JsonStore({
				url: _contextPath + '/managedpermittype/list',
				root: 'types',
				id: 'states-store',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'mpermit_type_id',
		            'description'
		        ],
				sortInfo: {
					field: 'description',
					direction: 'ASC'
				},
				baseParams: {start: 0, limit: 0},
				autoLoad: true
		    });
		    
		    this.stateStore = new Ext.data.JsonStore({
				url: _contextPath + '/codes/list',
				root: 'codes',
				id: 'states-store',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'codeid',
		            'description'
		        ],
				sortInfo: {
					field: 'description',
					direction: 'ASC'
				},
				baseParams: {start: 0, limit: 0, type: 'state'},
				autoLoad: true
		    });
		    
		    		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true }
		        ,columns:[{
		            header: "Permit Number",
		            dataIndex: 'permit_number',
		            width: 150
		        },{
		            header: "Status",
		            dataIndex: 'status',
		            width: 50
		        },{
		            header: "Type",
		            dataIndex: 'permit_type',
		            renderer: function(v, form, record){
		            	return v+' - '+record.data.permit_description;
		            },
		            width: 100
		        },{
		            header: "Validity",
		            dataIndex: 'valid_start_date',
		            renderer: function(value, form, record){ 
		            	if(typeof record.data.valid_end_date != "undefined" ){
		            		return value.format('m/d/Y') + ' - ' + record.data.valid_end_date.format('m/d/Y');
		            	} else {
		            		return "";
		            	}
		            	 
		            },
		            width: 150
		        }]});
		    
		    
		    var grid = this;
		    var config = {
		    		title: 'Permits',
		    		padding: '5 5 5 5',
		        store: store,
		        trackMouseOver:false,
		        disableSelection:false,
		        frame: false,
		        border: false,
		
		        // grid columns
		        colModel: columnModel,
		
		        // customize view config
		        viewConfig: { forceFit:true },
		
		        // paging bar on the bottom
		        tbar: {
			    	xtype: 'toolbar',
			    	items: ['Filter: ',
			    	        {
			    				xtype: 'cleartrigger',
					            fieldLabel: 'Sample Trigger',
					            value: '',
					            enableKeyEvents: true,
					            listeners: {
					            	keyup: function(field, event)
					            	{
					            		if(event.keyCode == 13)
					            		{
						            		var filterValue = this.getValue();
						            		
							            	var store = grid.store;
							            	store.baseParams = {filter: filterValue, owner_id: grid.owner.owner_id};
							            	store.load();
					            		}
					            	},
					            },
					            onTriggerClick: function() {
					            	this.setValue('');
					            	
					            	var store = grid.store;
					            	store.baseParams = {owner_id: grid.owner.owner_id};
					            	store.load();
					            }
					        },
					        {
					            iconCls: 'x-tbar-loading'  
					            ,scope: this
					            ,handler: function(){ grid.store.reload(); }
					        }
			    	        ]
			    },//top tool bar,
		        loadMask: true
		    };
		    
		    if(hasPermission(PL_OWNER_MANAGE))
			{
				config.tbar.items.push(
					'->',
					{
						xtype:'button',
						handler: function(){									
							grid.launchEditWindow(0);
						},
						text: 'Add'}	
					
					);
			}
		    
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			OwnerPermitPanel.superclass.initComponent.apply(this, arguments);
			
			this.on('rowcontextmenu', this.showContextMenu);
			this.on('rowdblclick', this.permitDetails);
	    },
	    showContextMenu: function(grid, index, event)
		{
			event.stopEvent();
			var record = grid.getStore().getAt(index);
			
			var items = new Array({
						text: 'Details',
						handler: function() 
						{
							grid.permitDetails(grid, index, event);
						}
					});
			
			if(hasPermission(PL_OWNER_MANAGE))
			{
				items.push({
					text: 'Edit',
					handler: function() 
					{
						grid.launchEditWindow(record.data.mpermit_id);
					}
				},{
					text: 'Delete',
					handler: function() 
					{
						Ext.MessageBox.confirm("Delete Permit?", 'Delete "'+record.data.permit_number+'"?', function(p1, p2){
							if(p1 != 'no')
							{
								// Basic request
								Ext.Ajax.request({
								   url: _contextPath + '/managedpermit/delete',
								   success: function(response, opts){
									   grid.getStore().reload();
									   Ext.growl.message('Success!', 'Permit has been deleted.');
								   },
								   failure: function(response, opts){
									   Ext.Msg.show({
										   title:'Error!',
										   msg: 'Error deleting permit.',
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
								   },
								   params: { mpermit_id: record.data.mpermit_id }
								});
							}
						});
					}
				},	{
					text: 'Print',
					handler: function() 
					{
						var menu = this.findParentByType('menu');
						menu.hide();
						
						var selection = grid.getStore().getAt(index);
						var permit_id = selection.data.mpermit_id ;
					
						var print_frame = document.getElementById('print_frame');
						if(print_frame != null)
						{
							print_frame.src = _rootContextPath+"/permit/print?permit_id="+permit_id;
						}
						else
						{
						
							new Ext.Window({
							    title : "iframe",
							    layout : 'fit',
							    id : "print_frame",
							    autoEl : {
							    	tag : "iframe",
								    width : 0,
								    height: 0,
								    frameborder: '0',
								    css: 'display:none;visibility:hidden;height:0px;',
								    src : _rootContextPath+"/permit/print?permit_id="+permit_id
							    }
							}).show();
						}
					}
				});
			}
			
			new Ext.menu.Menu(
			{
				items: items
			}).showAt(event.xy);
		},
		permitDetails: function(grid, index, event)
		{
			var record = grid.getStore().getAt(index);
			var vehicleWindow = new Ext.Window({
		        renderTo: document.body,
		        title: 'Permit Details - ' + record.data.permit_number,
		        width:325,
		        height: 375,
		        resizable: true,
		        modal: true,
		        autoScroll: true,
		        id: 'permitDetailsWindow',
		        autoDestroy: true,
		        items:[{
		        	xtype: 'tabpanel',
		        	activeTab: 0,
		        	bodyBorder: false,
		        	border: false,
		        	frame: false,
		        	defaults: { bodyCssClass: 'x-citewrite-panel-body',
	    		        padding: 5,
	    		        autoHeight: true,
	    		        autoScroll: true,
	    		        border: false,
	    		        bodyBorder: false,
	    		        frame: false },
		        	items:[{
		        			title: 'General',							
		    		        autoLoad: {url: _contextPath + '/managedpermit/details', params: {mpermit_id: record.data.mpermit_id, owner_id: this.owner.owner_id}},
		        		},{
		        			title: 'Shipping',
		    		        autoLoad: {url: _contextPath + '/managedpermit/details', params: {mpermit_id: record.data.mpermit_id, owner_id: this.owner.owner_id, type: 'shipping'}},
		        		},{
		        			title: 'Billing',
		    		        autoLoad: {url: _contextPath + '/managedpermit/details', params: {mpermit_id: record.data.mpermit_id, owner_id: this.owner.owner_id, type: 'billing'}},
		        		}]
		        }],
		        buttons: [{
		            text: 'Close',
		            handler: function(){
		            	this.findParentByType('window').close();
		            	}
		        }]
		    });
			
			vehicleWindow.show();
		},
		launchEditWindow: function(mpermit_id)
		{
			var grid = this;
			Ext.Ajax.request({
				   url: _contextPath + '/managedpermit/details',
				   success: function(response, opts){
					   var data = Ext.decode(response.responseText);
					   if(data.success)
					   {
						   	if(mpermit_id == 0)
							{
						   		grid.editPermit(null, data.vehicles, data.fields);
							}
						   	else
						   	{
						   		grid.editPermit(data.permit, data.vehicles, data.fields);
						   	}
					   }
					   else
					   {
						   Ext.Msg.show({
							   title:'Error!',
							   msg: data.msg,
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
					   }											   
				   },
				   failure: function(response, opts){
					   Ext.Msg.show({
						   title:'Error!',
						   msg: 'Error loading permit information.',
						   buttons: Ext.Msg.OK,
						   icon: Ext.MessageBox.ERROR
						});
				   },
				   params: { mpermit_id: mpermit_id, owner_id: this.owner.owner_id, xaction: 'get' }
				});
		},
		editPermit: function(permit, vehicles, fields)
		{
		
		if(_ownerMutex)
		{
			return;
		}
		
		_ownerMutex = true;
			var grid = this;
			var general = {
					xtype: 'panel',
					layout: 'form',
					title: 'General',
					bodyBorder: false,
					border: false,
					frame: false,
					defaultType:'textfield',
					bodyStyle: 'padding: 10px; ',
					bodyCssClass: 'x-citewrite-panel-body',
					defaults: { width: '95%' },
					items: [{
							xtype: 'hidden',
							id: 'edit-permit-id',
							name: 'mpermit_id',
							value: 0
						},{
							xtype: 'hidden',
							id: 'edit-permit-owner-id',
							name: 'owner_id',
							value: this.owner.owner_id
						},{
							id: 'edit-permit-number',
							name: 'permit_number',
				    	   fieldLabel: 'Permit Number',
				    	   maskRe: /^[0-9A-Za-z]*$/,
			               allowBlank: true
				       },{
				    	   xtype: 'combo',
				    	   id: 'edit-permit-status',
				    	   hiddenName: 'status',
				    	   fieldLabel: 'Status',
				    	   submitValue: true,
			               width: 165,
						 	lazyRender: false,
						 	store: new Ext.data.ArrayStore({
						        autoDestroy: true,
						        fields: ['id', 'description'],
						        data : [
						            ['Active', 'Active'],
						            ['Expired', 'Expired'],
						            ['Revoked', 'Revoked'],
						            ['Pending', 'Pending'],
						            ['Voided', 'Voided'],
						            ['Canceled', 'Canceled']
						        ]
						    }),
						    displayField: 'description',
						    valueField: 'id',
							triggerAction: 'all',
							forceSelection: true,
							mode: 'local'
				       },{
					    	   xtype: 'combo',
					    	   id: 'edit-permit-type',
					    	   hiddenName: 'mpermit-type-id',
					    	   fieldLabel: 'Permit Type',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: this.typeStore,
							    displayField: 'description',
							    valueField: 'mpermit_type_id',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local',
					            allowBlank: false
					       },{
					    	   xtype: 'datefield',
								id: 'edit-permit-valid-start',
								name: 'valid_start_date',
					    	   fieldLabel: 'Valid Start'
					       },{
					    	   xtype: 'datefield',
								id: 'edit-permit-valid-end',
								name: 'valid_end_date',
					    	   fieldLabel: 'Valid End'
					       }]
				};
			
			var additional = {
					xtype: 'panel',
					layout: 'form',
					title: 'Additional',
					bodyBorder: false,
					border: false,
					frame: false,
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
					    	   id: 'edit-permit-'+field.name,
					    	   hiddenName: 'mpermit-extra-'+field.name,
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
								submitValue: true,
								allowBlank: (field.required != true)
					       });
					}
					else //text
					{
						var options = {
						    	   id: 'edit-permit-'+field.name,
						    	   name: 'mpermit-extra-'+field.name,
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
			
			var vehiclePanel = {
					xtype: 'panel',
					layout: 'form',
					title: 'Vehicles',
					bodyBorder: false,
					border: false,
					frame: false,
					defaultType:'textfield',
					bodyStyle: 'padding: 10px; ',
					bodyCssClass: 'x-citewrite-panel-body',
					autoWidth: true,
					defaults: { width: '95%' },
					items: []
			};
			
			if(vehicles != undefined && vehicles.length > 0)
			{
				for(var i = 0; i < vehicles.length; i++)
				{
					var vehicle = vehicles[i];
					var bLabel = "";
					if(vehicle != undefined && vehicle.license != undefined && Ext.util.Format.trim(vehicle.license) != ""){
						bLabel = vehicle.license;
					}else if(vehicle != undefined && vehicle.vin != undefined && Ext.util.Format.trim(vehicle.vin) != ""){
						bLabel = vehicle.vin;
					}
					
					vehiclePanel.items.push({
							xtype: 'checkbox',
				    	   id: 'edit-permit-vehicle-'+vehicle.vehicle_id,
				    	   name: 'mpermit-vehicle-'+vehicle.vehicle_id,
				    	   boxLabel: bLabel,
				    	   
				    	   hideLabel: true
				       });
				}
			}
			 
			var shippingPanel = {
					xtype: 'panel',
					layout: 'form',
					title: 'Shipping',
					bodyBorder: false,
					border: false,
					frame: false,
					defaultType:'textfield',
					bodyStyle: 'padding: 10px; ',
					bodyCssClass: 'x-citewrite-panel-body',
					autoWidth: true,
					defaults: { width: '95%' },
					items: [ 
							{
								xtype: 'checkbox',
								boxLabel: 'Will pick-up in office',
								name:'will_pickup',
								id: 'will_pickup',
								bodyBorder : false,
								hideLabel: true,
								padding:'10px',
								bodyStyle: 'margins:20px 20px 10px 0px',
								checked:false,
								listeners: {
								    check: function(checkbox, checked) {
								    	
								    	if(!checked){
								        	Ext.getCmp('edit-permit-shipping-first-name').enable();
								        	Ext.getCmp('edit-permit-shipping-first-name').label.removeClass('x-item-disabled');
								        	Ext.getCmp('edit-permit-shipping-last-name').enable();
								        	Ext.getCmp('edit-permit-shipping-last-name').label.removeClass('x-item-disabled');
								        	Ext.getCmp('edit-permit-shipping-address').enable();
								        	Ext.getCmp('edit-permit-shipping-address').label.removeClass('x-item-disabled');
								        	Ext.getCmp('edit-permit-shipping-city').enable();
								        	Ext.getCmp('edit-permit-shipping-city').label.removeClass('x-item-disabled');
								        	Ext.getCmp('edit-permit-shipping-state').enable();
								        	Ext.getCmp('edit-permit-shipping-state').label.removeClass('x-item-disabled');
								        	Ext.getCmp('edit-permit-shipping-zip').enable();
								        	Ext.getCmp('edit-permit-shipping-zip').label.removeClass('x-item-disabled');
										}else{
											Ext.getCmp('edit-permit-shipping-first-name').disable();
											Ext.getCmp('edit-permit-shipping-first-name').label.addClass("x-item-disabled");
								        	Ext.getCmp('edit-permit-shipping-last-name').disable();
								        	Ext.getCmp('edit-permit-shipping-last-name').label.addClass("x-item-disabled");
								        	Ext.getCmp('edit-permit-shipping-address').disable();
								        	Ext.getCmp('edit-permit-shipping-address').label.addClass("x-item-disabled");
								        	Ext.getCmp('edit-permit-shipping-city').disable();
								        	Ext.getCmp('edit-permit-shipping-city').label.addClass("x-item-disabled");
								        	Ext.getCmp('edit-permit-shipping-state').disable();
								        	Ext.getCmp('edit-permit-shipping-state').label.addClass("x-item-disabled");
								        	Ext.getCmp('edit-permit-shipping-zip').disable(); 
								        	Ext.getCmp('edit-permit-shipping-zip').label.addClass("x-item-disabled");
										}
								    	
								    }
								}
							},
					        {
								id: 'edit-permit-shipping-first-name',
								name: 'shipping_first_name',
								fieldLabel: 'First Name'
							},{
								id: 'edit-permit-shipping-last-name',
								name: 'shipping_last_name',
								fieldLabel: 'Last Name'
							},{
								id: 'edit-permit-shipping-address',
								name: 'shipping_address',
								fieldLabel: 'Address'
							},{
								id: 'edit-permit-shipping-city',
								name: 'shipping_city',
								fieldLabel: 'City'
							},{
						    	   xtype: 'combo',
						    	   id: 'edit-permit-shipping-state',
						    	   hiddenName: 'shipping_state_id',
						    	   fieldLabel: 'State',
						    	   submitValue: true,
					               width: 165,
								 	lazyRender: false,
								 	store: this.stateStore,
								    displayField: 'description',
								    valueField: 'codeid',
									triggerAction: 'all',
									forceSelection: true,
									mode: 'local'
						       },{
								id: 'edit-permit-shipping-zip',
								name: 'shipping_zip',
								fieldLabel: 'Zip',
								width: 75
							}]
			};
			
			
			var permitTabPanel = {
					xtype: 'tabpanel',
					autoWidth: true,
					activeTab: 0,
					border: false,
					frame: false,
					defaults: {autoHeight: true, autoScroll: true},
					id: 'edit-permit-tabs',
					deferredRender: false,
					items: [general, additional, vehiclePanel, shippingPanel]
				};
					
			if(permit == null)
			{
				var years = [];
				
				var year = new Date().getFullYear();
				for(var i = year; i <= year+10; i++)
				{
					years[years.length] = [i, i];
				}
				
				var ccPanel = {
						xtype: 'panel',
						layout: 'form',
						id: 'permit-billing-panel-cc',
						defaultType:'textfield',
						defaults: { width: '175px' },
						bodyCssClass: 'x-citewrite-panel-body',
						bodyBorder: false,
						border: false,
						items:[{
							id: 'edit-permit-billing-first-name',
							name: 'billing_first_name',
							fieldLabel: 'First Name'
						},{
							id: 'edit-permit-billing-last-name',
							name: 'billing_last_name',
							fieldLabel: 'Last Name'
						},
						{
							id: 'edit-permit-billing-email',
							name: 'billing_email',
						    fieldLabel: 'Email'
						},
						{
							id: 'edit-permit-billing-cc',
							name: 'cc_number',
							fieldLabel: 'CC Number',
							maskRe: /^[0-9]*$/
						},
						{
							id: 'edit-permit-billing-cvv',
							name: 'cc_cvv',
							fieldLabel: 'CCV',
							maxLength: 4,
							maskRe: /^[0-9]*$/,
							width: 75
						},{
					    	   xtype: 'combo',
					    	   id: 'edit-permit-billing-exp-month',
					    	   hiddenName: 'cc_exp_month',
					    	   fieldLabel: 'Expiration Month',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: new Ext.data.ArrayStore({
							        autoDestroy: true,
							        fields: ['id', 'month'],
							        data : [
							            ['1', '1 - January'],
							            ['2', '2 - February'],
							            ['3', '3 - March'],
							            ['4', '4 - April'],
							            ['5', '5 - May'],
							            ['6', '6 - June'],
							            ['7', '7 - July'],
							            ['8', '8 - August'],
							            ['9', '9 - September'],
							            ['10', '10 - October'],
							            ['11', '11 - November'],
							            ['12', '12 - December']
							        ]
							    }),
							    displayField: 'month',
							    valueField: 'id',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local',
								width: 75
					       },{
					    	   xtype: 'combo',
					    	   id: 'edit-permit-billing-exp-year',
					    	   hiddenName: 'cc_exp_year',
					    	   fieldLabel: 'Expiration Year',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: new Ext.data.ArrayStore({
							        autoDestroy: true,
							        fields: ['id', 'year'],
							        data : years
							    }),
							    displayField: 'year',
							    valueField: 'id',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local',
								width: 75
					       },				
						{
							xtype: 'box',
							height: '15px;'
						},
						{
							id: 'edit-permit-billing-address',
							name: 'billing_address',
							fieldLabel: 'Address'
						},{
							id: 'edit-permit-billing-city',
							name: 'billing_city',
							fieldLabel: 'City'
						},{
					    	   xtype: 'combo',
					    	   id: 'edit-permit-billing-state',
					    	   hiddenName: 'billing_state_id',
					    	   fieldLabel: 'State',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: this.stateStore,
							    displayField: 'description',
							    valueField: 'codeid',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
					       },{
							id: 'edit-permit-billing-zip',
							name: 'billing_zip',
							fieldLabel: 'Zip',
							width: 75
						}]
				};
				
				var checkPanel = {
						xtype: 'panel',
						layout: 'form',
						id: 'permit-billing-panel-check',
						hidden: true,
						bodyBorder: false,
						border: false,				
						defaultType: 'textfield',
						defaults: { width: '175px' },
						bodyCssClass: 'x-citewrite-panel-body',
						items:[{
							id: 'citation-billing-check-number',
							name: 'check_number',
							fieldLabel: 'Check Number',
							maskRe: /^[0-9]*$/,
							width: 150
						},{
							id: 'citation-check-billing-email',
							name: 'check_billing_email',
						    fieldLabel: 'Receipt Email',
							width: 150
						}]
					};
					
					var cashPanel = {
							xtype: 'panel',
							layout: 'form',
							id: 'permit-billing-panel-cash',
							hidden: true,
							bodyBorder: false,
							border: false,				
							defaultType: 'textfield',
							defaults: { width: '175px' },
							bodyCssClass: 'x-citewrite-panel-body',
							items:[{
								id: 'citation-cash-billing-email',
								name: 'cash_billing_email',
							    fieldLabel: 'Receipt Email',
								width: 150
							}]
						};
					
				var billingPanel = {
						xtype: 'panel',
						layout: 'form',
						title: 'Billing',
						autoScroll: true,
						bodyBorder: true,
						border: false,
						frame: false,
						bodyStyle: 'padding: 10px; ',
						bodyCssClass: 'x-citewrite-panel-body',
						items: [{       
							   xtype: 'combo',
							   id: 'permit-billing-payment-method',
							   hiddenName: 'payment_method',
							   fieldLabel: 'Payment Method',
							   submitValue: true,
							   margins: {top:15, bottom: 10},
						       width: 150,
							 	lazyRender: false,
							 	store: new Ext.data.ArrayStore({
							        autoDestroy: true,
							        fields: ['id', 'description'],
							        data : [
							            ['Credit Card', 'Credit Card'],
							            ['Check', 'Check'],
							            ['Cash', 'Cash'],
							            ['None', 'None']
							        ]
							    }),
							    displayField: 'description',
							    valueField: 'id',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local',
								value: 'Credit Card',
								listeners: {
									select: function( combo, record, index )
									{
										var ccBillingPanel = Ext.getCmp('permit-billing-panel-cc');
										ccBillingPanel.hide();
										var checkBillingPanel = Ext.getCmp('permit-billing-panel-check');
										checkBillingPanel.hide();
										var cashBillingPanel = Ext.getCmp('permit-billing-panel-cash');
										cashBillingPanel.hide();
										
										if(record.data.id == 'Credit Card')
										{
											ccBillingPanel.show();
										}
										else if(record.data.id == 'Check')
										{
											checkBillingPanel.show();
										}
										else if(record.data.id == 'Cash')
										{
											cashBillingPanel.show();
										}
									}
								}
						   },
						   ccPanel,
						   checkPanel,
						   cashPanel]
				};
				
				permitTabPanel.items.push(billingPanel);
			}
			
			var formPanel = new Ext.form.FormPanel({
				xtype: 'form',
				border: false,
				frame: false,
				bodyBorder: false,
				autoHeight: true,
				items: [permitTabPanel]
				});
		  
			var url = _contextPath + '/managedpermit/purchase';
			var ajaxParams = {};
			var title = "Add ";
			if(permit != null)
			{
				title = "Edit ";
				url = _contextPath + '/managedpermit/save';
			}
			
			title += " Permit";
		
			var aWindow = new Ext.Window({
		        renderTo: document.body,
		        title: title,
		        width:350,
		        height: 300,
		        plain: true,
		        resizable: true,
	            stateful: false,
	            autoScroll: true,
		        modal: true,
		        id: 'editPermitFormWindow',
		        items: formPanel,
		        
		        buttons: [{
		            text:'Save',
		            handler: function()
		            {   
						if(_ownerMutex)
						{
							return;
						}
						
						_ownerMutex = true;
		            	//validate form
		            	formPanel.getForm().submit({
		            	    url: url,
		            	    scope: this,
		            	    params: ajaxParams,
		            	    success: function(form, action) {
		            	    	grid.store.reload();
		            	    	
		            	    	var parent = action.options.scope.findParentByType('window'); 
		            	    	parent.close();
		            	       
		            	    	if(permit == null)
		            	    	{
		            	    		Ext.growl.message('Success', 'Permit has been added.');
		            	    	}
		            	    	else
		            	    	{
		            	    		Ext.growl.message('Success', 'Permit has been updated.');
		            	    	}
		            	    },
		            	    failure: function(form, action) {
								_ownerMutex = false;
		            	        switch (action.failureType) {
		            	            case Ext.form.Action.CLIENT_INVALID:
		            	                Ext.Msg.show({
										   title:'Error',
										   msg: 'The fields outlined in red are required.',
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
		            	                break;
		            	            case Ext.form.Action.CONNECT_FAILURE:
		            	                Ext.Msg.show({
										   title:'Failure',
										   msg:  'Ajax communication failed',
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
		        }],
				listeners: {
					close: function()
					{
						_ownerMutex = false;							
					}
				}

		    });
			
			aWindow.show();
			window.setTimeout(setOwnerMutex,500);
			
			if(permit != null)
			{
				Ext.getCmp('edit-permit-valid-start').show();
				Ext.getCmp('edit-permit-valid-end').show();
				Ext.getCmp('edit-permit-number').show();
				Ext.getCmp('edit-permit-status').show();
				
				Ext.getCmp('edit-permit-id').setValue(permit.mpermit_id);
				Ext.getCmp('edit-permit-number').setValue(permit.permit_number);
				Ext.getCmp('edit-permit-status').setValue(permit.status);
				Ext.getCmp('edit-permit-type').setValue(permit.mpermit_type_id);
				Ext.getCmp('edit-permit-valid-start').setValue(new Date(permit.valid_start_date));
				Ext.getCmp('edit-permit-valid-end').setValue(new Date(permit.valid_end_date));
				
				if(fields != undefined && fields.length > 0)
				{
					for(var i = 0; i < fields.length; i++)
					{
						var field = fields[i];
						var input = Ext.getCmp('edit-permit-'+field.name);
						
						var attr = this.getAttributeByName(permit.extra, field.name);
						if(attr != null)
						{
							input.setValue(attr.value);
						}
					}
				}
				
				if(permit.vehicles != undefined && permit.vehicles.length > 0)
				{
					for(var i = 0; i < permit.vehicles.length; i++)
					{
						var vehicle = permit.vehicles[i];
						var cb = Ext.getCmp('edit-permit-vehicle-'+vehicle.vehicle_id);
						cb.setValue(true);
					}
				}
				
				if(permit.invoice != undefined)
				{
					Ext.getCmp('edit-permit-shipping-first-name').setValue(permit.invoice.shipping_first_name);
					Ext.getCmp('edit-permit-shipping-last-name').setValue(permit.invoice.shipping_last_name);
					Ext.getCmp('edit-permit-shipping-address').setValue(permit.invoice.shipping_address);
					Ext.getCmp('edit-permit-shipping-city').setValue(permit.invoice.shipping_city);
					Ext.getCmp('edit-permit-shipping-state').setValue(permit.invoice.shipping_state_id);
					Ext.getCmp('edit-permit-shipping-zip').setValue(permit.invoice.shipping_zip);
					Ext.getCmp('will_pickup').setValue(permit.invoice.will_pickup == 1 ? true:false);
				}
				
			}//end if vehicle != null
			else
			{
				Ext.getCmp('edit-permit-id').setValue(0);
				Ext.getCmp('edit-permit-valid-start').hide();
				Ext.getCmp('edit-permit-valid-end').hide();
				Ext.getCmp('edit-permit-number').hide();
				Ext.getCmp('edit-permit-status').hide();
			}
		},
		getAttributeByName: function(attributes, name)
		{
			if(attributes != undefined && attributes.length > 0)
			{
				for(var i = 0; i < attributes.length; i++)
				{
					var attr = attributes[i];
					if(attr.name == name)
					{
						return attr;
					}
				}
			}
			
			return null;
		}
});
function setOwnerMutex(){
	_ownerMutex = false;
}