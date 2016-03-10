var _ownerMutex = false;
AppealsCitationPanel = Ext.extend(Ext.Panel, {
	grid: null,
	store: null,
	filter: null,
	pageLimit: 50,
	stateStore: null,
	violationStore: null,
	owner: null,
	windowFlag: false,
	initComponent: function()
    {
		var panel = this;
		if(this.owner == null)
		{
			this.owner = {owner_id: 0};
		}
		// create the Data Store
	    this.store = new Ext.data.JsonStore({
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
	    
	    this.violationStore = new Ext.data.JsonStore({
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
	    
	    var toolbar = {
	            pageSize: panel.pageLimit,
	            store: this.store,
	            displayInfo: true,
	            displayMsg: 'Displaying appeals {0} - {1} of {2}',
	            emptyMsg: "No appeals to display"
	        };
	      
	    var columnModel = new Ext.grid.ColumnModel({
	        defaults: { sortable: true },
	        columns:[{
	            header: "Citation Appeal Id",
	            dataIndex: 'citation_appeal_id',
	            hidden:true	            
	        },{
	            header: "Citation Id",
	            dataIndex: 'citation_id',
	            hidden:true
	        },{
	            header: "Citation Number",
	            dataIndex: 'citation_number',
	            width: 80
	        },{
	            header: "Status",
	            dataIndex: 'status',
	            width: 80
	        },{
	            header: "Date",
	            dataIndex: 'appeal_date',
	            renderer: function(value){ return value.format('F j, Y g:i A'); },
	            width: 130
	        },{
	            header: "Name",
	            dataIndex: 'name',
	            width: 100
	        },{
	            header: "Email",
	            dataIndex: 'email',
	            width: 130
	        },{
	            header: "Phone",
	            dataIndex: 'phone',
	            width: 100
	        },{
	            header: "Address",
	            dataIndex: 'address',
	            width: 150
	        },{
	            header: "City",
	            dataIndex: 'city',
	            width: 150
	        },{
	            header: "State",
	            dataIndex: 'state_id',
	            width: 70
	        },{
	            header: "Zip",
	            dataIndex: 'zip',
	            width: 70
	        },{
	            header: "Reason",
	            dataIndex: 'reason',
	            width: 200
	        },{
	            header: "Decision Date",
	            dataIndex: 'decision_date',
	            renderer: function(value){ return value.format('F j, Y g:i A'); },
	            width: 130
	        },{
	            header: "Decision Reason",
	            dataIndex: 'decision_reason',
	            width: 200
	        }]});
	    
	    this.grid = new Ext.grid.GridPanel({
	        width:550,
	        height:300,
	        store: this.store,
	        trackMouseOver:false,
	        disableSelection:false,
	        frame: false,
	        border: false,	
	        // grid columns
	        colModel: columnModel,	
	        // customize view config
	        viewConfig: { forceFit:false },	
	        // paging bar on the bottom
	        bbar: new Ext.PagingToolbar(toolbar),
	        loadMask: true,
	        listeners: 
	        {
	        	'rowcontextmenu': this.showContextMenu,
				'rowdblclick': function(grid, index, event)
				{
					event.stopEvent();
					var record = grid.getStore().getAt(index);
					panel.details(record.data);
				}, scope: this
	        }
	    });
	    
	    this.filter = new Ext.FormPanel({
			bodyBorder: false,
			border: false,
			frame: false,
			defaultType:'textfield',
			labelAlign: 'top',
			buttonAlign:'center',
			bodyStyle: 'padding: 10px; ',
			autoWidth: true,
			autoScroll: true,
			defaults: { width: '95%' },
			bodyCssClass: 'x-citewrite-panel-body',
			items:[
			       {
			    	   xtype: 'combo',
			    	   hiddenName: 'filter_citation_appeal_status',
			    	   fieldLabel: 'Appeal Status',
			    	   submitValue: true,
		               width: 165,
					 	lazyRender: false,
					 	store: new Ext.data.ArrayStore({
					        autoDestroy: true,
					        fields: ['id', 'description'],
					        data : [
					            ['New', 'New'],
					            ['Under Review', 'Under Review'],
					            ['Upheld', 'Upheld'],
					            ['Dismissed', 'Dismissed'],
					            ['Deny', 'Deny'],
					            ['Dismiss', 'Dismiss'],
					            ['Reduce', 'Reduce'],
					            ['Close', 'Close']
					        ]
					    }),
					    displayField: 'description',
					    valueField: 'id',
						triggerAction: 'all',
						forceSelection: true,
						mode: 'local',
			            allowBlank: true
			       },
			       {
			    	   name: 'filter_name',
			    	   fieldLabel: 'Name'
			       },
			       {
			    	   name: 'filter_email',
			    	   fieldLabel: 'Email'
			       },
			       {
			    	   name: 'filter_address',
			    	   fieldLabel: 'Address'
			       },
			       {
			    	   name: 'filter_city',
			    	   fieldLabel: 'City'
			       },
			       {
			    	   name: 'filter_zip',
			    	   fieldLabel: 'Zip'
			       },
			       {
			    	   name: 'filter_date',
			    	   fieldLabel: 'Date',
			    	   xtype: 'datefield'
			       }
			       ],
	        buttons: [{
	            text: 'Apply',
	            width: 60,
	            handler: function(){
	               var params = panel.filter.getForm().getFieldValues();
	               params.owner_id = panel.owner.owner_id;
	               panel.store.baseParams = params;
	               panel.store.load({params: {start: 0, limit: panel.pageLimit}});
	            }
	        },{
	            text: 'Reset',
	            width: 60,
	            handler: function(){
	            	panel.filter.getForm().reset();					
	            	panel.store.baseParams = {owner_id: panel.owner.owner_id};
	            	panel.store.load({params: {start: 0, limit: panel.pageLimit}});
	            }
	        }]
		}); //filterForm
		    
		var config = 
		{
			title: 'Appeals',
			layout:'border',
			border: false,
			bodyCssClass: 'x-citewrite-panel-body',
			autoDestroy: true,
			defaults: {
			    collapsible: true,
			    split: true,
			    layout: 'fit'
			},
			items: [{
				
				collapsible: false,
			    region:'center',
			    margins: '5 0 5 5',
				items: [this.grid]
			},
			{
				title: 'Filter',
				region:'east',
				margins: '5 5 5 0',
				width: 200,
				items: [this.filter]
			}]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
        
		AppealsCitationPanel.superclass.initComponent.apply(this, arguments);

    },
    showContextMenu: function(grid, index, event)
	{
		event.stopEvent();
		var panel = this;
		var record = grid.getStore().getAt(index);
		
		var items; 
		
		if(hasPermission(PL_CITATION_MANAGE) || (hasPermission(PL_OWNER_MANAGE) && panel.owner.owner_id > 0))
		{
			items = new Array({
				text: 'Edit',
				handler: function() 
				{
					panel.details(record.data);
				}
			});
			
		}	
		
		if(hasPermission(PL_ADMIN))
		{
			items.push({
				text: 'Delete',
				handler: function() 
				{
					panel.deleteCitationAppeal(record.data);
				}
			});
			
		}		

		new Ext.menu.Menu(
		{
			items: items,
			autoDestroy: true
		}).showAt(event.xy);
	},
	
	details: function(appeal)
	{
		var panel = this;
		
		
		if(!panel.windowFlag)
		{
			panel.windowFlag = true;
			Ext.Ajax.request({
				   url: _contextPath + '/citation/appealDetails',
				   params: { citation_appeal_id: appeal.citation_appeal_id, owner_id: panel.owner.owner_id },
				   success: function(p1, p2)
				   {
					   var response = Ext.decode(p1.responseText);
					   if(response.success)
					   {
						   var citationAppeal = response.citation_appeal;
						   var citationAppealWindow = 0;
						   citationAppealWindow = new Ext.Window({
								title: 'Citation Appeals Detail - ' + citationAppeal.name,
				                renderTo: document.body,
				                layout:'fit',
				                width:455,
				                height:475,
				                closeAction:'close',
				                plain: true,
				                resizable: true,
				                modal: true,	
				                items:[ {
				                	xtype: 'tabpanel',
				                	activeTab: 0,
				                	id: 'citationAppealDetailsTabPanel',
				                	autoScroll: true,
				                	frame: false,
				                	border: false,
				                	}],	
				                buttons: [{
					                text: 'Save',
					                handler: function(){
					                	var form = Ext.getCmp('appealCitationFormPanel');
					                	var citation_id =  Ext.getCmp('appeal-appeal-citation-id').getValue();
					                	form.getForm().submit({
						            	    url: _contextPath + '/citation/appeal',
						            	    scope: this,
						            	    params: {citation_id: citation_id, xaction: 'start'},
						            	    success: function(form, action) {
						            	    	panel.store.reload();
						            	    	
						            	    	var response = Ext.decode(action.response.responseText);
						            	    	if(response.success)
						            	    	{
							            	    	Ext.growl.message('Success', 'Appeal has been save.');
							            	    	panel.windowFlag = false;
							            	    	this.findParentByType('window').close();
							            	    	
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
						            	    failure: function(form, action) {
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
				                }],
				                listeners: {
				                	close: function(p){
				                		panel.windowFlag = false;
							        }
				                }
				            });
						   
						   var tabpanel = Ext.getCmp('citationAppealDetailsTabPanel');
						  
						   var appealPanel = new Ext.Panel({
	                			title: 'Appeal',
	                			id:'appelCitationPanel',
	                			bodyCssClass: 'x-citewrite-panel-body',
	                			autoScroll: true,
	                			layout: 'form',
	                			padding: 10,
	                			buttonAlign: 'left'
	                		});						   
						   
						   tabpanel.items.each(function(c){
							   tabpanel.remove(c);	  
						   });
						   
						   appealPanel.add(panel.appealForm(citationAppeal));
						   tabpanel.add(appealPanel);
						   tabpanel.add(new AppealNotesPanel(appeal));
						   
						   tabpanel.setActiveTab(appealPanel.id);
						 
						   citationAppealWindow.show();
					   }
					   else
					   {
						   Ext.Msg.show({
							   title:'Failure',
							   msg:  response.msg,
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
					   }
				   },
				   failure: function(){  
					   Ext.Msg.show({
						   title:'Failure',
						   msg:  'Error retrieving appeal.',
						   buttons: Ext.Msg.OK,
						   icon: Ext.MessageBox.ERROR
						});
				   }
				});
			}
	},
	
	appealForm: function(citationAppeal)
	{
		
		return {
			  xtype: 'form',
			  bodyBorder: false,
			  id:'appealCitationFormPanel',
				border: false,
				frame: false,
				defaultType:'textfield',
				bodyCssClass: 'x-citewrite-panel-body',
				buttonAlign: 'left',
				defaults: {width: '95%'},
			  items:[
				{
					 xtype: 'hidden',
					id: 'appeal-appeal-citation-id',
					name: 'citation_id',
					value: citationAppeal.citation_id
				},
		         {
		        	 xtype: 'hidden',
		        	id: 'appeal-citation_appeal_id',
		        	name: 'citation_id',
		        	value: citationAppeal.citation_appeal_id
		         },{
					id: 'appeal-name',
					name: 'appeal_name',
					fieldLabel: 'Name',
					allowBlank: false,
					value: citationAppeal.name
				},{
					id: 'appeal-email',
					name: 'appeal_email',
					fieldLabel: 'Email',
					allowBlank: false,
					value: citationAppeal.email
				},{
					id: 'appeal-phone',
					name: 'appeal_phone',
					fieldLabel: 'Phone',
					allowBlank: false,
					value: citationAppeal.phone
				},{
					id: 'appeal-shipping-address',
					name: 'appeal_address',
					fieldLabel: 'Address',
					allowBlank: false,
					value: citationAppeal.address
				},{
					id: 'appeal-city',
					name: 'appeal_city',
					fieldLabel: 'City',
					allowBlank: false,
					value: citationAppeal.city
				},{
			    	   xtype: 'combo',
			    	   id: 'appeal-state',
			    	   hiddenName: 'appeal_state_id',
			    	   fieldLabel: 'State',
			    	   submitValue: true,
		               width: 150,
		               allowBlank: false,
					 	lazyRender: false,
					 	store: this.stateStore,
					    displayField: 'description',
					    valueField: 'codeid',
						triggerAction: 'all',
						forceSelection: true,
						mode: 'local',
						value: citationAppeal.state_id
			    },{
			    	   xtype: 'combo',
			    	   id: 'appeal-status',
			    	   hiddenName: 'appeal_status',
			    	   fieldLabel: 'Status',
			    	   submitValue: true,
			    	   allowBlank: false,
		               width: 150,
					   lazyRender: false,
					   store: new Ext.data.ArrayStore({
					        autoDestroy: true,
					        fields: ['id', 'description'],
					        data : [
					            ['New', 'New'],
					            ['Under Review', 'Under Review'],
					            ['Upheld', 'Upheld'],
					            ['Dismissed', 'Dismissed'],
					            ['Deny', 'Deny'],
					            ['Dismiss', 'Dismiss'],
					            ['Reduce', 'Reduce'],
					            ['Close', 'Close'],
					            ['Edit', 'Edit Schedule']
					            
					        ]
					   }),
					   displayField: 'description',
					   valueField: 'id',
					   triggerAction: 'all',
					   forceSelection: true,
					   mode: 'local',
					   value: citationAppeal.status
			    },{
					id: 'appeal-zip',
					name: 'appeal_zip',
					fieldLabel: 'Zip',
					width: 150,
					allowBlank: false,
					value: citationAppeal.zip
				},{
					xtype: 'textarea',
					id: 'appeal-reason',
					name: 'appeal_reason',
					fieldLabel: 'Reason',
					allowBlank: false,
					value: citationAppeal.reason
				},{
					xtype: 'textarea',
					id: 'appeal-decision-reason',
					name: 'appeal_decision_reason',
					fieldLabel: 'Decision Reason',
					value: citationAppeal.decision_reason
				}],
				buttons:[]
		  };	

	},
	deleteCitationAppeal: function(citationAppeal)
    {
		  var panel = this;
		  Ext.Msg.confirm("Delete?", "Delete appeal "+citationAppeal.citation_number+"?", function(bid, p2){
		  if(bid == "yes")
		  {
			  Ext.Ajax.request({
				   url: _contextPath + '/citation/deleteAppeal',
				   params: { citation_id: citationAppeal.citation_id }, 
				   success: function(p1, p2)
				   {
					   var response = Ext.decode(p1.responseText);
					   if(response.success)
					   {
						   panel.store.reload();
						   Ext.growl.message('Success', 'Appeal has been deleted.');
						   
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
				   failure: function(){
					   Ext.Msg.show({
						   title:'Failure',
						   msg: 'Error deleting the Citation.',
						   buttons: Ext.Msg.OK,
						   icon: Ext.MessageBox.ERROR
						});
					   },
					   scope: this
					}); 
			  }
		  });
	  },//deleteRow
	  
});
function setOwnerMutex(){
	_ownerMutex = false;
}