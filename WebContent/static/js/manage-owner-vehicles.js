var _ownerMutex = false;
OwnerVehiclePanel = Ext.extend(Ext.grid.GridPanel, {
		owner: null,
		stateStore: null,
		colorStore: null,
		makeStore: null,
		initComponent: function()
	    {
			// create the Data Store
		    var store = new Ext.data.JsonStore({
				url: _contextPath + '/owner/vehicles',
				root: 'vehicles',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'vehicle_id',
		            'license',
		            'vin',
		            'make',
		            'color',
		            'state'
		        ],
				sortInfo: {
					field: 'license',
					direction: 'ASC'
				},
				baseParams: {owner_id: this.owner.owner_id},
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
		    
		    this.colorStore = new Ext.data.JsonStore({
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
				baseParams: {start: 0, limit: 0, type: 'color'},
				autoLoad: true
		    });
		    
		    this.makeStore = new Ext.data.JsonStore({
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
				baseParams: {start: 0, limit: 0, type: 'make'},
				autoLoad: true
		    });
		    
		    		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true }
		        ,columns:[{
		            header: "License",
		            dataIndex: 'license',
		            width: 100
		        },{
		            header: "VIN",
		            dataIndex: 'vin',
		            width: 100
		        }
		        ,{
		            header: "State",
		            dataIndex: 'state',
		            width: 50
		        },{
		            header: "Make",
		            dataIndex: 'make',
		            width: 50
		        },{
		            header: "Color",
		            dataIndex: 'color',
		            width: 150
		        }]});
		    
		    
		    var grid = this;
		    var config = {
		    		title: 'Vehicles',
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
								Ext.Ajax.request({
									   url: _contextPath + '/vehicle/details',
									   success: function(response, opts){
										   var data = Ext.decode(response.responseText);
										   if(data.success)
										   {
											   grid.editVehicle(null, data.fields);
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
											   msg: 'Error loading vehicle information.',
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
									   },
									   params: { vehicle_id: 0, xaction: 'get' }
									});
							},
							text: 'Add'});
			}
		   
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			OwnerVehiclePanel.superclass.initComponent.apply(this, arguments);
			
			this.on('rowcontextmenu', this.showContextMenu);
			this.on('rowdblclick', this.viewVehicle);
	    },
	    showContextMenu: function(grid, index, event)
		{
			event.stopEvent();
			var record = grid.getStore().getAt(index);
			
			var items = new Array({
						text: 'Details',
						handler: function() 
						{
							grid.viewVehicle(grid, index, event);
						}
					});
			
			if(hasPermission(PL_OWNER_MANAGE))
			{
				items.push({
					text: 'Edit',
					handler: function() 
					{
						Ext.Ajax.request({
							   url: _contextPath + '/vehicle/details',
							   success: function(response, opts){
								   var data = Ext.decode(response.responseText);
								   if(data.success)
								   {
									   grid.editVehicle(data.vehicle, data.fields);
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
									   msg: 'Error loading vehicle information.',
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   },
							   params: { vehicle_id: record.data.vehicle_id, xaction: 'get' }
							});
					}
				},{
						text: 'Delete',
						handler: function() 
						{
							Ext.MessageBox.confirm("Delete Vehicle?", 'Delete "'+record.data.license+'"?', function(p1, p2){
								if(p1 != 'no')
								{
									// Basic request
									Ext.Ajax.request({
									   url: _contextPath + '/vehicle/delete',
									   success: function(response, opts){
										   grid.getStore().reload();
										   Ext.growl.message('Success!', 'Vehicle has been deleted.');
									   },
									   failure: function(response, opts){
										   Ext.Msg.show({
											   title:'Error!',
											   msg:  'Error deleting vehicle.',
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
									   },
									   params: { vehicle_id: record.data.vehicle_id }
									});
								}
							});
						}
					});
			}
				
			var menu = new Ext.menu.Menu(
			{
				items: items
			}).showAt(event.xy);
		},
		viewVehicle: function(grid, index, event)
		{
			var record = grid.getStore().getAt(index);
			var vehicleWindow = new Ext.Window({
		        renderTo: document.body,
		        title: 'Vehicle Details - ' + record.data.license,
		        width:350,
		        height: 300,
		        plain: true,
		        resizable: true,
		        autoScroll: true,
		        modal: true,
		        id: 'vehicleOwnerFormWindow',
		        autoDestroy: true,
		        padding: 5,
		        autoLoad: {url: _contextPath + '/vehicle/details', params: {vehicle_id: record.data.vehicle_id}},
		        buttons: [{
		            text: 'Close',
		            handler: function(){
		            	this.findParentByType('window').close();
		            	}
		        }]
		    });
			
			vehicleWindow.show();
		},
		editVehicle: function(vehicle, fields)
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
							id: 'edit-vehicle-id',
							name: 'vehicle_id',
							value: 0
						},{
							xtype: 'hidden',
							id: 'edit-vehicle-owner-id',
							name: 'owner_id',
							value: this.owner.owner_id
						},{
						    xtype: 'textfield',
							id: 'edit-vehicle-license',
							name: 'license',
				    	   fieldLabel: 'License',
				    	   maskRe: /^[0-9A-Za-z]*$/,
				    	   maxLength: 12,
			               allowBlank: true,
						   maxLengthText: 'The maximum length for this field is {0}'
				       },{
							id: 'edit-vehicle-vin',
					    	maskRe: /^[0-9A-Za-z]*$/,
							name: 'vin',
							fieldLabel: 'VIN',
							regex: /^([a-h,A-H,j-n,J-N,p-z,P-Z,0-9]{9})([a-h,A-H,j-n,J-N,p,P,r-t,R-T,v-z,V-Z,0-9])([a-h,A-H,j-n,J-N,p-z,P-Z,0-9])(\d{6})$/,
							regexText:'Invalid VIN',
							allowBlank: true
				       },{
					    	   xtype: 'combo',
					    	   id: 'edit-vehicle-state',
					    	   hiddenName: 'state_id',
					    	   fieldLabel: 'State',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: this.stateStore,
							    displayField: 'description',
							    valueField: 'codeid',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local',
								allowBlank: true
					       },{
					    	   xtype: 'combo',
					    	   id: 'edit-vehicle-make',
					    	   hiddenName: 'make_id',
					    	   fieldLabel: 'Make',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: this.makeStore,
							    displayField: 'description',
							    valueField: 'codeid',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
					       },{
					    	   xtype: 'combo',
					    	   id: 'edit-vehicle-color',
					    	   hiddenName: 'color_id',
					    	   fieldLabel: 'Color',
					    	   submitValue: true,
				               width: 165,
							 	lazyRender: false,
							 	store: this.colorStore,
							    displayField: 'description',
							    valueField: 'codeid',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
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
					    	   id: 'edit-vehicle-'+field.name,
					    	   hiddenName: 'vehicle-extra-'+field.name,
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
						    	   id: 'edit-vehicle-'+field.name,
						    	   name: 'vehicle-extra-'+field.name,
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
			
					
			var formPanel = new Ext.form.FormPanel({
				xtype: 'form',
				border: false,
				frame: false,
				bodyBorder: false,
				autoHeight: true,
				items: [{
						xtype: 'tabpanel',
						autoWidth: true,
						activeTab: 0,
						border: false,
						frame: false,
						deferredRender: false,
						defaults: {autoHeight: true, autoScroll: true},
						items: [general, additional]
					}]
				});
		  
			var ajaxParams = {};
			var title = "Add ";
			if(vehicle != null)
			{
				title = "Edit ";
			}
			
			title += " Vehicle";
			
			var vehicleWindow = new Ext.Window({
		        renderTo: document.body,
		        title: title,
		        width: 350,
		        height: 300,
		        plain: true,
		        resizable: true,
		        autoScroll: true,
		        modal: true,
		        id: 'editVehicleFormWindow',
		        items: formPanel,
		        
		        buttons: [{
		            text:'Save',
		            handler: function()
		            {   
					
					if(!((Ext.getCmp('edit-vehicle-vin').getValue() == undefined || Ext.getCmp('edit-vehicle-vin').getValue() == "" ) && (Ext.getCmp('edit-vehicle-license').getValue() == undefined || Ext.getCmp('edit-vehicle-license').getValue() == "")))
					{
						if(Ext.getCmp('edit-vehicle-vin').getValue() == undefined || Ext.getCmp('edit-vehicle-vin').getValue() == ""){
							Ext.getCmp('edit-vehicle-license').allowBlank = false;
							Ext.getCmp('edit-vehicle-state').allowBlank = false;
						}
						
						if(Ext.getCmp('edit-vehicle-license').getValue() == undefined || Ext.getCmp('edit-vehicle-license').getValue() == ""){
							Ext.getCmp('edit-vehicle-vin').allowBlank = false;
						}
					}else{
							Ext.getCmp('edit-vehicle-license').allowBlank = false;
							Ext.getCmp('edit-vehicle-state').allowBlank = false;
					}
		            	//validate form
		            	formPanel.getForm().submit({
		            	    url: _contextPath + '/vehicle/save',
		            	    scope: this,
		            	    params: ajaxParams,
		            	    success: function(form, action) {
		            	    	grid.store.reload();
		            	    	
		            	    	var parent = action.options.scope.findParentByType('window'); 
		            	    	parent.close();
		            	       
		            	    	Ext.growl.message('Success', 'Vehicle has been updated.');
		            	    },
		            	    failure: function(form, action) {
		            	        switch (action.failureType) {
		            	            case Ext.form.Action.CLIENT_INVALID:
									Ext.Msg.show({
									   title:'Error',
									   msg: getActiveErrorMessage(form),
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
		        }],listeners: {
						close: function()
						{
							_ownerMutex = false;							
						}
					}
		    });
			
			vehicleWindow.show();
			window.setTimeout(setOwnerMutex,500);
			if(vehicle != null)
			{
				Ext.getCmp('edit-vehicle-id').setValue(vehicle.vehicle_id);
				Ext.getCmp('edit-vehicle-license').setValue(vehicle.license);
				Ext.getCmp('edit-vehicle-vin').setValue(vehicle.vin);
				Ext.getCmp('edit-vehicle-state').setValue(vehicle.state_id);
				Ext.getCmp('edit-vehicle-make').setValue(vehicle.make_id);
				Ext.getCmp('edit-vehicle-color').setValue(vehicle.color_id);				
				
				if(fields != undefined && fields.length > 0)
				{
					for(var i = 0; i < fields.length; i++)
					{
						var field = fields[i];
						var input = Ext.getCmp('edit-vehicle-'+field.name);
						
						var attr = this.getAttributeByName(vehicle.extra, field.name);
						if(attr != null)
						{
							input.setValue(attr.value);
						}
					}
				}
			}//end if vehicle != null
			else
			{
				Ext.getCmp('edit-vehicle-id').setValue(0);
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
