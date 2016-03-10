Ext.onReady(function(){

	var pageLimit = 50;
	var manageOwners = Ext.get('nav-agenciesu');
	if(manageOwners != null)
	{
		manageOwners.on('click', function(){
			var ownerStore = new Ext.data.JsonStore
			({
			    // store configs
			    autoDestroy: true,
			    autoLoad: true,
			    url: _contextPath + '/owner/manage',
			    remoteSort: true,
				sortInfo: {
						field: 'last_name',
						direction: 'ASC'
					},
			    storeId: 'ownerStore',
			    // reader configs
			    idProperty: 'owner_id',
			    root: 'owners',
			    fields: ['owner_id', 'name', 'status', 'ownertype'],
			    autoLoad: {params:{start:0, limit:pageLimit}}
			});
			
			var OwnerList = function(viewer, config) {
			    this.viewer = viewer;
			    Ext.apply(this, config);
			
			    this.store = ownerStore,
			    this.colModel = new Ext.grid.ColumnModel({
			        defaults: {
			            width: 120,
			            sortable: true
			        },
			        columns: [
			            {header: 'Name', sortable: true, dataIndex: 'name'},
			            {header: 'Type', sortable: true, dataIndex: 'ownertype'},
			            {header: 'Status', sortable: true, dataIndex: 'status'}
			        ]
			    });
			    this.viewConfig = {
			        forceFit: true
			    };
			
			    OwnerList.superclass.constructor.call(this, 
			    {
			        region: 'center',
			        id: 'owner-list-grid',
			        loadMask: {msg:'Loading Owner...'},
			
			        sm: new Ext.grid.RowSelectionModel
			        ({
			            singleSelect:true
			        }),
			
			        viewConfig: 
			        {
			            forceFit:true,
			            enableRowBody:true,
			            showPreview:false,
			            getRowClass : this.applyRowClass
			        }
			    });
			
			};
			
			Ext.extend(OwnerList, Ext.grid.GridPanel, {
			    listeners:{
			    	rowcontextmenu: function(grid, index, event ){
			    		OwnerListMenu(grid,index,event);
			    	},
			    	rowdblclick: function(grid, index, event )
			    	{
			    		var record = grid.getStore().getAt(index);
			    		var tabs = Ext.getCmp('ownertabs');

			    		var ownerPanel = tabs.find('id', 'ownertab-'+record.data.owner_id);
			    		if(ownerPanel.length > 0)
			    		{
			    			tabs.setActiveTab(ownerPanel[0]);
			    		}
			    		else
			    		{
				    		ownerPanel = new OwnerTabPanel({owner: record.data });
							tabs.add(ownerPanel);
							tabs.setActiveTab(ownerPanel.id);
			    		}
			    		
			    	}
			    },
			    bbar: new Ext.PagingToolbar({
			            pageSize: pageLimit,
			            store: ownerStore,
			            displayInfo: true,
			            displayMsg: '{0} - {1} of {2}',
			            emptyMsg: "No owners to display",
			            items: ['-',
			            		]
			    	}),
			    tbar: {
			    	xtype: 'toolbar',
			    	items: ['Filter: ',
			    	        {
			    				xtype: 'cleartrigger',
					            value: '',
					            enableKeyEvents: true,
					            listeners: {
					            	keyup: function(field, event)
					            	{
					            		if(event.keyCode == 13)
					            		{
						            		var filterValue = this.getValue();
						            		
						            		var grid = field.ownerCt.ownerCt;
							            	var store = grid.store;
							            	store.baseParams = {filter: filterValue};
							            	store.load({params: {start: 0, limit: pageLimit}});
					            		}
					            	},
					            },
					            onTriggerClick: function() {
					            	this.setValue('');
					            	var grid = this.ownerCt.ownerCt;
					            	var store = grid.store;
					            	store.baseParams = {};
					            	store.load({params: {start: 0, limit: pageLimit}});
					            }
					        },
			    	        '->',
			    	        {
					        	xtype:'button',
								handler: function(){
									
									Ext.Ajax.request({
										   url: _contextPath + '/owner/details',
										   success: function(response, opts){
											   var data = Ext.decode(response.responseText);
											   if(data.success)
											   {
												   editOwner(null, data.fields, data.types);
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
												   msg: 'Error loading owner information.',
												   buttons: Ext.Msg.OK,
												   icon: Ext.MessageBox.ERROR
												});
										   },
										   params: { owner_id: 0, xaction: 'get' }
										});
								},
								text: 'Add'}
			    	        ]
			    }//top tool bar
			});
			
			var OwnerListMenu = function(grid, index, event)
			{
				event.stopEvent();
				var record = grid.getStore().getAt(index);
				
				var items = new Array({
						text: 'Delete',
						handler: function() 
						{
							Ext.MessageBox.confirm("Delete Owner?", 'Delete "'+record.data.name+'"?', function(p1, p2){
								if(p1 != 'no')
								{
									// Basic request
									Ext.Ajax.request({
									   url: _contextPath + '/owner/delete',
									   success: function(response, opts){
										   grid.getStore().reload();
										   Ext.growl.message('Success','Owner has been deleted.');
									   },
									   failure: function(response, opts){
										   Ext.Msg.show({
											   title:'Error!',
											   msg: 'Error deleting report.',
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
									   },
									   params: { owner_id: record.data.owner_id }
									});
								}
							});
						}
					});
				
				
				var menu = new Ext.menu.Menu(
				{
					items: items
				}).showAt(event.xy);
			};
			
			OwnerTabPanel = Ext.extend(Ext.TabPanel, {
				owner: null,
				bodyCssClass: 'x-citewrite-panel-body',
				initComponent: function()
			    {
					var config = {
							id: 'ownertab-' + this.owner.owner_id,
							title: this.owner.name,
							tabPosition: 'bottom',
							activeTab: 0,
							closable: true,
							autoDestroy: true,
							enableTabScroll: true,
						    items: [
						            new OwnerGeneralPanel({owner: this.owner}),						           
								    new OwnerPermitPanel({owner: this.owner}),
								    new OwnerVehiclePanel({owner: this.owner}),
								    new CitationPanel({owner: this.owner}),
								    new InvoicePanel({owner: this.owner}),
								    new OwnerNotesPanel({owner: this.owner})]
						        };
					
			        Ext.apply(this, Ext.apply(this.initialConfig, config));
			        
			        OwnerTabPanel.superclass.initComponent.apply(this, arguments);
			    }				
			});
			
			
			var OwnerManager = Ext.extend(Ext.Panel, 
			{
			    initComponent: function()
			    {
			        Ext.apply(this, 
			        {
			        	title: 'Manage Owners',
			            layout: 'border',
			            border: false,
			            frame: false,
			            items: [{
							title: 'Owners',
			                region:'west',
			                margins: '5 0 5 5',
			                border: false,
			                split: true,
			                width: 275,
			                collapsible: true,   // make collapsible
			                layout: 'fit',
			                items: [new OwnerList()]
			            },{
			            	 title: 'Center Region',
			                 region: 'center',     // center region is required, no width/height specified
			                 xtype: 'container',
			                 layout: 'fit',
			                 margins: '5 5 5 5',
			                 items:[{
			                	 xtype: 'tabpanel',
			                	 id: 'ownertabs',
			                	 frame: false,
			                	 closable: true
			                	 
			                 }]	
			            }]
			        });
			        OwnerManager.superclass.initComponent.apply(this, arguments);
			    }
			});
			
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			content.add(new OwnerManager({bodyCssClass: 'x-citewrite-border-ct'}));
			content.doLayout();
		}); //end managers on click
	}//end if	
	
});

