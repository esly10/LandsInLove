Ext.onReady(function(){

	var pageLimit = 50;
	var manageAppeals = Ext.get('nav-appeals');
	if(manageAppeals != null)
	{
		manageAppeals.on('click', function(){
				var panel = this;
				if(this.owner == null)
				{
					this.owner = {owner_id: 0};
				}
				store = new Ext.data.JsonStore({
					url: _contextPath + '/citation/listAppeal',
					baseParams: {owner_id: this.owner.owner_id },
					root: 'appeals',
			        totalProperty: 'count',
			        remoteSort: true,
			        fields: [
			            'citation_appeal_id',
			            'citation_id', 
			            'address',
			            'name',
			            'status',
			            'email',
			            'phone',
			            'citation_number',
			            {name:'appeal_date', type: 'date', dateFormat:'Y-m-dTH:i:s'},
			            'city', 
			            'state_id', 
			            'zip',
			            'reason', 
			            'decision_reason',
			            {name:'decision_date', type: 'date', dateFormat:'Y-m-dTH:i:s'}	          
			        ],
					sortInfo: {
						field: 'name',
						direction: 'DESC'
					},
					autoLoad: {params:{start:0, limit: panel.pageLimit}}
			    });
			 
			   stateStore = new Ext.data.JsonStore({
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
			    
			    violationStore = new Ext.data.JsonStore({
					url: _contextPath + '/codes/list',
					id: 'violations-store',
					root: 'codes',
			        totalProperty: 'count',
			        remoteSort: true,
			        fields: [
			            'codeid',
			            'description',
			            'is_overtime'
			        ],
					sortInfo: {
						field: 'description',
						direction: 'ASC'
					},
					baseParams: {start: 0, limit: 0, type: 'violation'},
					autoLoad: true
			    });
			 
					
			var AppealList = function(viewer, config) {
			    this.viewer = viewer;
			    Ext.apply(this, config);
			
			    this.store = store,
			    this.colModel = new Ext.grid.ColumnModel({
			        defaults: {
			            width: 120,
			            sortable: true
			        },
			        columns: [
			            {header: 'Citation Number', sortable: true, dataIndex: 'citation_number'},
			            {header: 'Ststus', sortable: true, dataIndex: 'status'},			            
			            {header: 'Date', sortable: true, dataIndex: 'appeal_date'}
			        ]
			    });
			    this.viewConfig = {
			        forceFit: true
			    };
			
			    AppealList.superclass.constructor.call(this, 
			    {
			        region: 'center',
			        id: 'appeal-list-grid',
			        loadMask: {msg:'Loading Appeal...'},
			
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
			
			Ext.extend(AppealList, Ext.grid.GridPanel, {
			    listeners:{
			    	rowcontextmenu: function(grid, index, event ){
			    		AppealListMenu(grid,index,event);
			    	},
			    	rowdblclick: function(grid, index, event )
			    	{
			    		var record = grid.getStore().getAt(index);
			    		var tabs = Ext.getCmp('appealtabs');

			    		var appealPanel = tabs.find('id', 'appeartab-'+record.data.owner_id);
			    		if(appealPanel.length > 0)
			    		{
			    			tabs.setActiveTab(appealPanel[0]);
			    		}
			    		else
			    		{
				    		appealPanel = new AppealTabPanel({appeal: record.data });
							tabs.add(appealPanel);
							tabs.setActiveTab(appealPanel.id);
			    		}
			    		
			    	}
			    },
			    bbar: new Ext.PagingToolbar({
			            pageSize: pageLimit,
			            store: store,
			            displayInfo: true,
			            displayMsg: '{0} - {1} of {2}',
			            emptyMsg: "No appeals to display",
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
					            emptyText :'Status or Citation Number',
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
					        }
			    	        ]
			    }//top tool bar
			});
			
			var AppealListMenu = function(grid, index, event)
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
										   Ext.growl.message('Success','Appeal has been deleted.');
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
			
			AppealTabPanel = Ext.extend(Ext.TabPanel, {
				owner: null,
				bodyCssClass: 'x-citewrite-panel-body',
				initComponent: function()
			    {
					var config = {
							id: 'appealtab-' + this.appeal.citation_appeal_id,
							title: this.appeal.name,
							tabPosition: 'bottom',
							activeTab: 0,
							closable: true,
							autoDestroy: true,
							enableTabScroll: true,
						    items: [
						            new AppealGeneralPanel({appeal: this.appeal}),						           
						            new AppealNotesPanel({appeal: this.appeal})
						           ]
						        };
					
			        Ext.apply(this, Ext.apply(this.initialConfig, config));
			        
			        AppealTabPanel.superclass.initComponent.apply(this, arguments);
			    }				
			});
			
			
			var AppealManager = Ext.extend(Ext.Panel, 
			{
			    initComponent: function()
			    {
			        Ext.apply(this, 
			        {
			        	title: 'Manage Appeals',
			            layout: 'border',
			            border: false,
			            frame: false,
			            items: [{
							title: 'Appeals',
			                region:'west',
			                margins: '5 0 5 5',
			                border: false,
			                split: true,
			                width: 275,
			                collapsible: true,   // make collapsible
			                layout: 'fit',
			                items: [new AppealList()]
			            },{
			            	 title: 'Center Region',
			                 region: 'center',     // center region is required, no width/height specified
			                 xtype: 'container',
			                 layout: 'fit',
			                 margins: '5 5 5 5',
			                 items:[{
			                	 xtype: 'tabpanel',
			                	 id: 'appealtabs',
			                	 frame: false,
			                	 closable: true
			                	 
			                 }]	
			            }]
			        });
			        AppealManager.superclass.initComponent.apply(this, arguments);
			    }
			});
			
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			content.add(new AppealManager({bodyCssClass: 'x-citewrite-border-ct'}));
			content.doLayout();
		}); //end managers on click
	}//end if	
	
});

