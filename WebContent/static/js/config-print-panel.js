PrintPanel = Ext.extend(Ext.Panel, {
	grid: null,
	store: null,
	countNewTab : 0,
	initComponent : function() {
		
		var panel = this;

		this.store = new Ext.data.JsonStore
		({
		    // store configs
		    autoDestroy: true,
		    autoLoad: true,
		    url: _contextPath + '/administration/printList',
		    remoteSort: true,
			sortInfo: {
					field: 'name',
					direction: 'ASC'
				},
		    storeId: 'store',
		    root: 'printLayout',
		    fields: ['name', 'value', 'groupId', 'isDefault']
		});
		
		 var columnModel = new Ext.grid.ColumnModel({
		        defaults: { 
		        	width: 120,
		        	sortable: true }
		        ,columns:[{
		            header: "Name",
		            dataIndex: 'name',
		        }]});
		
	    this.grid = new Ext.grid.GridPanel({
	        // customize view config
	    	store: this.store,
	        region: 'center',
	        id: 'print-list-grid',
	        loadMask: {msg:'Loading Print...'},
	        colModel: columnModel,
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
	        },
	        
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
						            	store.load();
				            		}
				            	},
				            },
				            onTriggerClick: function() {
				            	this.setValue('');
				            	var grid = this.ownerCt.ownerCt;
				            	var store = grid.store;
				            	store.baseParams = {};
				            	store.load();
				            }
				        },
		    	        '->',
		    	        {
				        	xtype:'button',
							handler: function(){
								panel.countNewTab -=  1;
								panel.addPrintTab({groupId:panel.countNewTab});
							},
							text: 'Add'}
		    	        ]
		    	},
		    	listeners:{
		    		rowcontextmenu: function(grid, index, event ){
		    			PrintListMenu(grid,index,event);
			    	},
			    	rowdblclick: function(grid, index, event )
			    	{
			    		var record = grid.getStore().getAt(index);
			    		panel.addPrintTab(record.data);
			    	}
			    }
	    });
		
		var PrintListMenu = function(grid, index, event)
		{
			event.stopEvent();
			var record = grid.getStore().getAt(index);
			
			var items = new Array({
				text: 'Delete',
				handler: function() 
				{
					Ext.MessageBox.confirm("Delete Print Format?", 'Delete "'+record.data.name+'"?', function(p1, p2){
						if(p1 != 'no')
						{
							Ext.Ajax.request({
							   url: _contextPath + '/administration/deletePrint',
							   success: function(response, opts){
								   grid.getStore().reload();
								   Ext.growl.message('Success!', 'Print has been deleted.');
							   },
							   failure: function(response, opts){
								   Ext.Msg.show({
									   title:'Error!',
									   msg: 'Error deleting Print.',
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   },
							   params: { groupId: record.data.groupId }
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
	    
		var config = 
		{
			title: 'Print Layout',
            layout: 'border',
            border: false,
            frame: false,
            bodyCssClass: 'x-citewrite-border-ct',

			items: [{
				title: 'Print',
                region:'west',
                margins: '5 0 5 5',
                border: false,
                split: true,
                width: 275,
                collapsible: true,   // make collapsible
                layout: 'fit',
                items: [this.grid]
            },
            
            {
           	 title: 'Center Region',
                region: 'center',     // center region is required, no width/height specified
                xtype: 'container',
                layout: 'fit',
                margins: '5 5 5 5',
                items:[{
               	 xtype: 'tabpanel',
            	 id: 'printtabs',
            	 frame: false,
            	 closable: true
            	 
             }]
            
            }
            
            ]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
        
		PrintPanel.superclass.initComponent.apply(this, arguments);

	},
	addPrintTab: function(printResult)
	{
		var tabs = Ext.getCmp('printtabs');
		var configPrintForm = tabs.find('name', 'configForm-' +printResult.groupId);
		if(configPrintForm.length > 0)
		{
			tabs.setActiveTab(configPrintForm[0]);
		}
		else
		{
			configPrintForm = new ConfigPrintForm({print: printResult, formStore:this.store});
			tabs.add(configPrintForm);
			tabs.setActiveTab(configPrintForm.id);
		}
	}


});