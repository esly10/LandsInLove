CitationOwnerPanel = Ext.extend(Ext.Panel, {
	
	citation: null,
	initComponent: function()
    {
		var panel = this;
		var buttons = [];
		var buttonText= "Edit";
		if (panel.owner_id==0){
			buttonText = "Add";
		}
		if(hasPermission(PL_OWNER_MANAGE)){
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
			
			buttons.push({xtype:'button',
				handler: function(){
					
					Ext.Ajax.request({
						   url: _contextPath + '/citation/ownerDetails',
						   success: function(response, opts){
							   var data = Ext.decode(response.responseText);
							   if(data.success)
							   {
								   AddOwner(data.owner, data.fields, data.types, panel, stateStore, panel.citation_id);
								   //panel.load: { url : _contextPath + '/citation/ownerDetails', scripts : true, params: {owner_id: this.owner_id } };
								   panel.store.load({params: {owner_id: this.owner_id, citation_id:this.citation_id }});
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
						   params: { owner_id: panel.owner_id, citation_id:panel.citation_id  , xaction: 'get' }
						});
				},
				text: buttonText});
			
			buttons.push({xtype:'button',
				handler: function(){
					
					Ext.Ajax.request({
						   url: _contextPath + '/citation/ownerDetails',
						   success: function(response, opts){
							   var data = Ext.decode(response.responseText);
							   if(data.success)
							   {
								   SelectOwner(data.owner, data.fields, data.types, panel, stateStore, panel.citation_id);
								   //panel.load: { url : _contextPath + '/citation/ownerDetails', scripts : true, params: {owner_id: this.owner_id } };
								   panel.store.load({params: {owner_id: this.owner_id, citation_id:this.citation_id}});
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
						   params: { owner_id: panel.owner_id, citation_id:panel.citation_id , xaction: 'get' }
						});
				},
				text: 'Select'});
		}
		
		
		
	panel = this;
		var config = 
		{
			xtype: 'panel',
			title: 'Owner',
			id: 'citationOwnerTab',
			padding: 5,
			bodyCssClass: 'x-citewrite-panel-body',
			autoScroll: true,
			buttonAlign: 'left',
		    autoLoad : { url : _contextPath + '/citation/ownerDetails', scripts : true, params: {owner_id: this.owner_id } },
		    buttons: buttons 
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
        
		CitationOwnerPanel.superclass.initComponent.apply(this, arguments);
    }   
});

var _ownerCitationMutex = false;
function AddOwner(owner, fields, ownerTypes, panel, stateStore, citation_id)
    {
    	if(_ownerCitationMutex)
    	{
    		return;
    	}
    	
    	_ownerCitationMutex = true;
    	var general = {
    			xtype: 'panel',
    			layout: 'form',
    			title: 'Owner',
    			bodyBorder: false,
    			border: false,
    			frame: false,
    			defaultType:'textfield',
    			bodyStyle: 'padding: 10px; ',
    			bodyCssClass: 'x-citewrite-panel-body',
    			defaults: { width: '95%' },
    			items: [{
					xtype: 'hidden',
					id: 'edit-citation-id',
					name: 'citation_id',
					value: 0
					},{
    					xtype: 'hidden',
    					id: 'edit-owner-id',
    					name: 'owner_id',
    					value: 0
    				},{
    			    	   xtype: 'combo',
    			    	   id: 'edit-owner-status',
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
    					            ['Inactive', 'Inactive'],
    					            ['Pending', 'Pending']
    					        ]
    					    }),
    					    displayField: 'description',
    					    valueField: 'id',
    						triggerAction: 'all',
    						forceSelection: true,
    						mode: 'local',
    						allowBlank: false
    			       },{
    			    	   xtype: 'combo',
    			    	   id: 'edit-owner-type',
    			    	   hiddenName: 'type_id',
    			    	   fieldLabel: 'Type',
    			    	   submitValue: true,
    		               width: 165,
    					 	lazyRender: false,
    					 	store: new Ext.data.JsonStore({
    					        autoDestroy: true,
    					        fields: ['owner_type_id', 'name'],
    					        data : ownerTypes
    					    }),
    					    displayField: 'name',
    					    valueField: 'owner_type_id',
    						triggerAction: 'all',
    						forceSelection: true,
    						mode: 'local',
    						allowBlank: false
    			       },{
    					id: 'edit-owner-first-name',
    					name: 'first_name',
    		    	   fieldLabel: 'First Name',
    	               allowBlank: false
    		       },{
    					id: 'edit-owner-last-name',
    					name: 'last_name',
    		    	   fieldLabel: 'Last Name',
    	               allowBlank: false
    		       },{
    		    	   id: 'edit-owner-username',
    		    	   name: 'username',
    		    	   fieldLabel: 'Username',
    		    	   maskRe: /^[0-9A-Za-z]*$/,
    	           },{
    					id: 'edit-owner-email',
    					name: 'email',
    		    	    fieldLabel: 'Email',
       		       },{
    					id: 'edit-owner-home-phone',
    					name: 'home_phone',
    		    	   fieldLabel: 'Home Phone'
    		       },{
    					id: 'edit-owner-mobile-phone',
    					name: 'mobile_phone',
    		    	   fieldLabel: 'Mobile Phone'
    		       },{
    					id: 'edit-owner-address',
    					name: 'address',
    		    	   fieldLabel: 'Address',
    			  },{
    					id: 'edit-owner-city',
    					name: 'city',
    		    	   fieldLabel: 'City',
    			  },{
    				   xtype: 'combo',
    				   id: 'edit-owner-state',
    				   hiddenName: 'state',
    				   name: 'state',
    				   fieldLabel: 'State',
    				   submitValue: true,
    			       width: 165,
    				 	lazyRender: false,
    				 	store: stateStore,
    				    displayField: 'description',
    				    valueField: 'codeid',
    					triggerAction: 'all',
    					forceSelection: true,
    					mode: 'local'
    			   },
    		      {
    		    	   id: 'edit-owner-zip',
    		    	   name: 'zip',
    		    	   fieldLabel: 'Zip',
    		    	   maskRe: /^[0-9-]*$/,
    		      }]
    		};
    	
    	  			
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
    				items: [general]
    			}]
    		});
      
    	var ajaxParams = {};
    	var title = "Add ";
    	if(owner != null)
    	{
    		title = "Edit ";
    	}
    	
    	title += " Owner";
    	
    	var ownerWindow = new Ext.Window({
            renderTo: document.body,
            title: title,
            width:330,
            height: 430,
            plain: true,
            resizable: false,
            autoScroll: true,
            modal: true,
            id: 'editOwnerFormWindow',
            items: formPanel,
            
            buttons: [{
                text:'Save',
                handler: function()
                {   
    				//validate form
    				formPanel.getForm().submit({
    					url: _contextPath + '/citation/saveOwner',
    					scope: this,
    					params: ajaxParams,
    					success: function(form, action) {
    						if(owner != null)
    						{
    							//var ownerPanel = Ext.getCmp('ownertab-general-' + owner.owner_id);
    							if(panel != null)
    							{
    								
    								panel.load({url: _contextPath + '/citation/ownerDetails', scripts : true, params: {owner_id: owner.owner_id, citation_id:citation_id }});
    							}
    						}
    						else
    						{
    							var data = Ext.decode(action.response.responseText);
    							//new panel
    							var tabs = Ext.getCmp('ownertabs');
    							if(tabs != null)
    							{
    								var ownerPanel = new OwnerTabPanel({owner: data.owner});
    								tabs.add(ownerPanel);
    								tabs.setActiveTab(ownerPanel.id);
    								ownerPanel.setTitle(data.owner.first_name);
    							}
    						}
    						
    						var grid = Ext.getCmp('owner-list-grid');
    						if(grid != null)
    						{
    							grid.store.reload();
    						}
    						
    						var parent = action.options.scope.findParentByType('window'); 
    						parent.close();
    					   
    						Ext.growl.message('Success', 'Owner has been updated.');
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
    								   title:'Failure!',
    								   msg: 'Ajax communication failed',
    								   buttons: Ext.Msg.OK,
    								   icon: Ext.MessageBox.ERROR
    								});
    								break;
    							case Ext.form.Action.SERVER_INVALID:
    								Ext.Msg.show({
    								   title:'Failure!',
    								   msg:  action.result.msg,
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
            }],listeners: {
    				close: function()
    				{
    					_ownerCitationMutex = false;							
    				}
    			}
        });
    	ownerWindow.show();
    	window.setTimeout(setOwnerMutex,500);
    	
    	Ext.getCmp('edit-citation-id').setValue(citation_id);
    	if(owner != null)
    	{
    		Ext.getCmp('edit-owner-id').setValue(owner.owner_id);
    		Ext.getCmp('edit-owner-first-name').setValue(owner.first_name);
    		Ext.getCmp('edit-owner-last-name').setValue(owner.last_name);
    		Ext.getCmp('edit-owner-type').setValue(owner.type_id);
    		Ext.getCmp('edit-owner-status').setValue(owner.status);
    		Ext.getCmp('edit-owner-username').setValue(owner.username);
    		Ext.getCmp('edit-owner-email').setValue(owner.email);
    		Ext.getCmp('edit-owner-home-phone').setValue(owner.home_phone);
    		Ext.getCmp('edit-owner-mobile-phone').setValue(owner.mobile_phone);
    		Ext.getCmp('edit-owner-address').setValue(owner.address);
    		Ext.getCmp('edit-owner-city').setValue(owner.city);
    		Ext.getCmp('edit-owner-state').setValue(owner.state_id);
    		Ext.getCmp('edit-owner-zip').setValue(owner.zip);
    		
    	}//end if owner != null
    	else
    	{
    		Ext.getCmp('edit-owner-id').setValue(0);
    	}
    }
	var listWindowGeneral = null;
	function setOwnerMutex(){
		_ownerCitationMutex = false;
	}

	//var _OwnerListMutex = false;
	function SelectOwner(owner, fields, ownerTypes, panel, stateStore, citation_id)
	{
		var pageLimit = 50;
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
		
	
	    	var list = {
	    			xtype: 'panel',
	    			
	    			//layout: 'form',
	    			title: 'Owner',
	    			bodyBorder: false,
	    			border: false,
	    			frame: false,
	    			defaultType:'textfield',
	    			bodyStyle: 'padding: 0px; ',
	    			bodyCssClass: 'x-citewrite-panel-body',
	    			defaults: { width: '100%' },
	    			items: [this.loadOwnerGrid(this, ownerStore, citation_id, panel)/*OwnerList*/]
	    		};
	    	
	    	  			
	    	var listPanel = new Ext.form.FormPanel({
	    		xtype: 'form',
	    		id: 'ownerListPanel',
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
	    				items: [list]
	    			}]
	    		});
	    	
	    	var ajaxParams = {};
	    	var title = "Add ";
	    	if(owner != null)
	    	{
	    		title = "Select ";
	    	}
	    	
	    	title += " Owner";
	    	
	    	var listWindow = new Ext.Window({
	            //renderTo: document.body,
	            title: title,
	            id: 'listWindowOwner',
	            width:350,
	            height: 430,
	            plain: true,
	            resizable: false,
	            autoScroll: true,
	            modal: true,
	            id: 'editOwnerFormWindow',
	            items: listPanel,
	            
	            buttons: [{
	                text: 'Close',
	                handler: function(){
	                	this.findParentByType('window').close();
	                }
	            }],listeners: {
	    				close: function()
	    				{
	    				//	_OwnerListMutex = false;							
	    				}
	    			}
	        });
	    	listWindow.show();
	    	listWindowGeneral = listWindow;
	    	//window.setTimeout(setOwnerListMutex,500);
	    	
	    }

		function setOwnerListMutex(){
			_OwnerListMutex = false;
		}
		
		function loadOwnerGrid (Owner, ownerStore, citation_id, panel){
	    	
	    	   var columnModel = new Ext.grid.ColumnModel({
			        defaults: { sortable: true },
			        columns:[
			                 		{header: 'Name', sortable: true, dataIndex: 'name'},
			       		            {header: 'Type', sortable: true, dataIndex: 'ownertype'},
			       		            {header: 'Status', sortable: true, dataIndex: 'status'}
			       		   ]
			        });
			    
	    	    //grid = this;	
			    var gridPanel = {
			    	//title: 'Owner List',
			    	xtype: 'grid',
			    	id:"gridOwners",
			    	//padding: '5 5 5 5',			    	
			        store: ownerStore,
			        trackMouseOver:false,
			        disableSelection:false,
			        height:330,
			        frame: true,
			        border: true,			
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
				    				emptyText:"Name or Status",
						            fieldLabel: 'Sample Trigger',
						            value: '',
						            enableKeyEvents: true,
						            listeners: {
						            	keyup: function(field, event)
						            	{
						            		if(event.keyCode == 13)
						            		{
							            		var filterValue = this.getValue();
							            		
								            	var store = this.findParentByType('grid').store;
								            	store.baseParams = {filter: filterValue};
								            	store.load();
						            		}
						            	},
						            },
						            onTriggerClick: function() {
						            	this.setValue('');
						            	
						            	var store = this.findParentByType('grid').store;
						            	store.baseParams = {owner_id: grid.owner_id};
						            	store.load();
						            }
						        },
						        {
						            iconCls: 'x-tbar-loading'  
						            ,scope: this
						            ,handler: function(){
						            	var store = Ext.getCmp("gridOwners").getStore();
						            	store.reload(); 
						            }
						        } 
				    	        ]
				    },//top tool bar,
				    listeners: {
				    	rowdblclick: function(gridPanel, index, event )
				    	{
				    		var record = gridPanel.getStore().getAt(index);
				    		saveOwner(record, citation_id, panel);
				    	},
				    	rowcontextmenu: function(gridPanel, index, event ){
				    		OwnerListMenu(gridPanel,index,event);
				    	}
				    },
			        loadMask: true
			    };
			    
				var OwnerListMenu = function(gridPanel, index, event)
				{
					event.stopEvent();
					var record = gridPanel.getStore().getAt(index);
					var items = new Array({
							text: 'Select',
							handler: function() 
							{
								//var record = gridPanel.getStore().getAt(index);
					    		saveOwner(record, citation_id, panel);
					    		this.findParentByType('window').close();
							}
						});
					
					
					var menu = new Ext.menu.Menu(
					{
						items: items
					}).showAt(event.xy);
				};
			    return gridPanel;
	    }
		
		function saveOwner(record, citation_id, panel){
			var currentOwner = record.id;
			var listFormPanel = Ext.getCmp('ownerListPanel');
			
			//var listForm = action.options.scope.findParentByType('form'); 
			listFormPanel.getForm().submit({	
				url: _contextPath + '/citation/selectOwner',
				scope: this,
				params: {owner_id: currentOwner, citation_id:citation_id },
				//params:owner_id=currentOwner, 
				success: function(form, action) {
					panel.load({url: _contextPath + '/citation/ownerDetails', 
						scripts : true, params: {owner_id: currentOwner, citation_id:citation_id }});			
					/*var parent = action.options.scope.findParentByType('window'); 
					parent.close();*/
					
					Ext.growl.message('Success', 'Owner has been changed.');
					//this.findParentByType('window').close();
					listWindowGeneral.close();
					//var parent = action.options.scope.findParentByType('window'); 
					
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
							   title:'Failure!',
							   msg: 'Ajax communication failed',
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
							break;
						case Ext.form.Action.SERVER_INVALID:
							Ext.Msg.show({
							   title:'Failure!',
							   msg:  action.result.msg,
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
				   }
				}
			});
			
			
		}
