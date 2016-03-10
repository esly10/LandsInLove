Ext.onReady(function(){

	var pageLimit = 50;
	var adminUsers = Ext.get('nav-users');
	if(adminUsers != null)
	{
		adminUsers.on('click',function(){
			
		    // create the Data Store
			var store = new Ext.data.JsonStore({
				root: 'users',
				url: _contextPath + '/user/list',
				totalProperty: 'count',
				fields: [ 'user_id','first_name', 'last_name', 'username', 'permissions'],
				remoteSort: true,
				sortInfo: {
						field: 'user_id',
						direction: 'ASC'
					}

			});
		    
			var toolbar = {
	            pageSize: pageLimit,
	            store: store,
	            displayInfo: true,
	            displayMsg: 'Displaying users {0} - {1} of {2}',
	            emptyMsg: "No users to display"
	        };
		    			
			if(hasPermission(PL_ADMIN)){
				toolbar.items = ['-', {
			                text: 'Add User',
			                cls: 'x-btn-text details',
			                handler: function(btn, event){ addUser(); }
			                }];
			}
		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true }
		        ,columns:[{
		            header: "ID",
		            dataIndex: 'user_id',
		            width: 50
		        },{
		            header: "User Name",
		            dataIndex: 'username',
		            width: 100
		        },{
		            header: "First Name",
		            dataIndex: 'first_name',
		            width: 150
		        },{
		            header: "Last Name",
		            dataIndex: 'last_name',
		            width: 100
		        }/*,{
		            header: "Officer ID",
		            dataIndex: 'officer_id',
		            width: 50
		        }*/]
		    });
		    
		    var grid = new Ext.grid.GridPanel({
		        layout: 'fit',
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
		        bbar: new Ext.PagingToolbar(toolbar)
		    });
		    
		    store.load({params:{start:0, limit:pageLimit}});
		    		
		    var _selectedRow = null;
		    var _userContextMenu = new Ext.menu.Menu({
		      id: 'UserGridContextMenu',
		      items: [
				  { text: 'Edit', handler: editUser },
				  { text: 'Delete', handler: deleteUser }
		      ]
		   }); 
		    
		    if(hasPermission(PL_ADMIN))
		    {
		    	grid.addListener('rowcontextmenu', onUserGridContextMenu);
		    	grid.addListener('rowdblclick', function(grid, index, event ){
										    		var record = grid.getStore().getAt(index);
										    		
										    		_userContextMenu.rowRecord = record;
										    		editUser();
										    	});
		    }
		    function onUserGridContextMenu(grid, rowIndex, e) {
				e.stopEvent();
				var coords = e.getXY();
				_userContextMenu.rowRecord = grid.store.getAt(rowIndex);
				grid.selModel.selectRow(rowIndex);
				_selectedRow=rowIndex;
				_userContextMenu.showAt([coords[0], coords[1]]);
			  }
			  
			  var filterForm = new Ext.FormPanel({
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
					items:[
					       {
					    	   id: 'filter_user_name',
					    	   fieldLabel: 'Name'
					       },
					       {
					    	   id: 'filter_user_username',
					    	   fieldLabel: 'UserName'
					       }/*,
					       {
					    	   id: 'filter_officer_id',
					    	   fieldLabel: 'Officer ID'
					       }*/
					       ],
			       buttons: [{
			            text: 'Apply',
			            width: 60,
			            handler: function(){
			               var params = filterForm.getForm().getFieldValues();
			               store.baseParams = params;
			              store.load({params: {start: 0, limit: pageLimit}});
			            }
			        },{
			            text: 'Reset',
			            width: 60,
			            handler: function(){
			            	filterForm.getForm().reset();					
			            	store.baseParams = {};
			            	store.load({params: {start: 0, limit: pageLimit}});
			            }
			        }]
			});
			  
			  var content = Ext.getCmp('content-panel');
				content.removeAll(true);
				
				content.add({
						xtype: 'panel',
						title: 'Users',
						id: 'user-content-panel',
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
							items: [grid]
						},
						{
							collapsible: false,
							region:'east',
							margins: '5 5 5 0',
							width: 200,
							items: [filterForm]
						}]
					});
				//content.add(grid);
				content.doLayout();
			  
				var userFormPanel = Ext.getCmp('userFormPanel');
				var userDialog = Ext.getCmp('userWindow');
				if(!userDialog)
				{
				//device form
				userFormPanel = new Ext.FormPanel({
					bodyBorder: false,
					border: false,
					frame: false,
					labelAlign: 'top',
					buttonAlign:'center',
					bodyStyle: 'padding: 10px; ',
					autoWidth: true,
					defaults: { width: '95%' },
					bodyCssClass: 'x-citewrite-panel-body',
					id: 'userFormPanel',
					items:[
					       {
					            layout:'column',
					            border: false,
					            bodyCssClass: 'x-citewrite-panel-body',
					            items:[
					            {
					                columnWidth:.5,
					                defaultType:'textfield',
					                layout: 'form',
					                border: false,
					                bodyBorder: false,
					                bodyCssClass: 'x-citewrite-panel-body',
					                items: [{
								    	   id: 'user_first_name',
								    	   fieldLabel: 'First Name',
						                    anchor:'95%',
						                    tabIndex: 1

								       }, {
								    	   id: 'user_username',
								    	   fieldLabel: 'User Name',
								    	   anchor:'95%',
						                    tabIndex: 3

								       }/*, {
								    	   id: 'user_officer_id',
								    	   fieldLabel: 'Officer ID',
								    	   anchor:'95%',
						                    tabIndex: 5

								       }*/]
					            },{
					                columnWidth:.5,
					                layout: 'form',
					                defaultType:'textfield',
					                border: false,
					                bodyBorder: false,
					                bodyCssClass: 'x-citewrite-panel-body',
					                items: [{
								    	   id: 'user_last_name',
								    	   fieldLabel: 'Last Name',
						                    anchor:'95%',
						                    tabIndex: 2
								       },{
								    	   id: 'user_password',
								    	   fieldLabel: 'Password',
								    	   anchor:'95%',
						                    tabIndex: 4,
						                    inputType: 'password',
						                    allowBlank: true
								       }]
					            }]
					        },
							{
								xtype: 'box',
								height: 5
							},{
								xtype: 'box',
								height: 5
							},{
								xtype: 'box',
								height: 5
							},{
								xtype: 'box',
								height: 5
							},
					       {
								   id: 'user_id',
								   xtype: 'hidden',
								   value: '0'
							},
					       {
					    	   xtype: 'panel',
					    	   border: false,
					    	   frame:false,
					           plain: false,
					           activeTab: 0,
					           height: 240,
					           items: [{
							    		   title: 'Permissions',
							    		   xtype: 'panel',
							    		   border: false,
							    		   title: false,
							    		   id: 'user-permissions',
							    		   bodyCssClass: 'x-citewrite-panel-body',
							    		   autoScroll: true,
							    		   items:[{
							    			   		layout:'column',
							    			   		border: false,
							    			   		bodyCssClass: 'x-citewrite-panel-body',
									            items:[{
									            	columnWidth:.5,
									                layout: 'form',
									                id: 'user-permissions-column1',
									                border: false,
									                bodyBorder: false,
									                bodyCssClass: 'x-citewrite-panel-body',
									                items: [{
												    			   xtype: 'checkbox',
																   id: 'user-permission-1',
																   name: 'user-permission-1',
																   hideLabel: true,
																   boxLabel: 'Administrator'
										    		   		},
										    		   		{
												    			   xtype: 'checkbox',
																   id: 'user-permission-2',
																   name: 'user-permission-2',
																   hideLabel: true,
																   boxLabel: 'View Reservations List'
										    		   		},
										    		   		{
												    			   xtype: 'checkbox',
																   id: 'user-permission-4',
																   name: 'user-permission-4',
																   hideLabel: true,
																   boxLabel: 'View Ocupancy'
										    		   		},
										    		   		{
												    			   xtype: 'checkbox',
																   id: 'user-permission-8',
																   name: 'user-permission-8',
																   hideLabel: true,
																   boxLabel: 'View Charges'
										    		   		},
									                        {
												    			   xtype: 'checkbox',
																   id: 'user-permission-16',
																   name: 'user-permission-16',
																   hideLabel: true,
																   boxLabel: 'View Reports'
										    		   		},
										    		   		{
												    			   xtype: 'checkbox',
																   id: 'user-permission-32',
																   name: 'user-permission-32',
																   hideLabel: true,
																   boxLabel: 'Payments Report'
										    		   		}
										    		   		
										    		   		]
									            	},//first checkbox column
									            	{
										            	columnWidth:.5,
										                layout: 'form',
										                border: false,
										                bodyBorder: false,
										                bodyCssClass: 'x-citewrite-panel-body',
										                id: 'user-permissions-column2',
										                items: [
										    		   	{
												    		   xtype: 'checkbox',
															   id: 'user-permission-64',
															   name: 'user-permission-64',
															   hideLabel: true,
															   boxLabel: 'Make Reservations'
										    		   	},{
											    			   xtype: 'checkbox',
															   id: 'user-permission-128',
															   name: 'user-permission-128',
															   hideLabel: true,
															   boxLabel: 'Manage Agencies'
									    		   		},{
											    			   xtype: 'checkbox',
															   id: 'user-permission-256',
															   name: 'user-permission-256',
															   hideLabel: true,
															   boxLabel: 'Manage Guest'
									    		   		},
														{
															   xtype: 'checkbox',
															   id: 'user-permission-512',
															   name: 'user-permission-512',
															   hideLabel: true,
															   boxLabel: 'Manage Services'
														},
														{
															   xtype: 'checkbox',
															   id: 'user-permission-1028',
															   name: 'user-permission-1028',
															   hideLabel: true,
															   boxLabel: 'Manage Rooms'
														},
									    		   		{
											    			   xtype: 'checkbox',
															   id: 'user-permission-2056',
															   name: 'user-permission-2056',
															   hideLabel: true,
															   boxLabel: 'Manage Settings'
									    		   		}]
									            	} //second column of checkboxes
							    		      ]//column panel items
							    		   }//column panel
						    		   ] //permission panel items
					           		}//permission panel
					           ]//tab panel items
					       }//tab panel
						]// form panel items
					});//end user form panel
							  
				//device dialog
				userDialog = new Ext.Window({
		                renderTo: document.body,
		                layout:'fit',
		                width:450,
		                height:400,
		                closeAction:'hide',
		                plain: true,
		                resizable: false,
		                modal: true,
		                id: 'userWindow',
		                items: userFormPanel,
	
		                buttons: [{
		                    text:'Save',
		                    handler: function()
		                    {
		                    	var user_id = Ext.getCmp('user_id').getValue();
		                    	var fname = Ext.getCmp('user_first_name').getValue();
		                    	var lname = Ext.getCmp('user_last_name').getValue();
		                    	var username = Ext.getCmp('user_username').getValue();
		                    	var password = Ext.getCmp('user_password').getValue();
		                    	/*var officerid = Ext.getCmp('user_officer_id').getValue();*/
		                    	if(fname.length == 0 || lname.length == 0 || username.length == 0 || (password.length == 0 && user_id == 0))
	                    		{
		                    		Ext.Msg.show({
			            				   title:'Missing Field',
			            				   msg: 'All fields are required.',
			            				   buttons: Ext.Msg.OK,
			            				   icon: Ext.MessageBox.ERROR
			            				});
		                    		
		                    		return false;
	                    		}
		                    	
		                    	//validate form
		                    	userFormPanel.getForm().submit({
		                    	    url: _contextPath + '/user/save',
		                    	    params: {
		                    	        xaction: 'save'
		                    	    },
		                    	    success: function(form, action) {
		                    	    	userDialog.hide();
		                    	       store.load({params: {start: 0, limit: pageLimit}});
		                    	       Ext.growl.message('Success', 'User has been saved.');
		                    	    },
		                    	    failure: function(form, action) {
		                    	        switch (action.failureType) {
		                    	            case Ext.form.Action.CLIENT_INVALID:
		                    	                Ext.Msg.show({
	                    	                	   title:'Failure',
	                    	                	   msg: 'Form fields may not be submitted with invalid values',
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
		                    	userDialog.hide();
		                    }
		                }]
		            });
				} //end if
				
				 function editUser()
				 {
					 	var user = _userContextMenu.rowRecord.data;
					  
						Ext.getCmp('user_id').setValue(user.user_id);
						Ext.getCmp('user_first_name').setValue(user.first_name);
						Ext.getCmp('user_last_name').setValue(user.last_name);
						Ext.getCmp('user_username').setValue(user.username);
						Ext.getCmp('user_password').setValue(user.password);
						/*Ext.getCmp('user_officer_id').setValue(user.officer_id);*/
						
						var permissions = user.permissions;
						panel = Ext.getCmp('user-permissions-column1');
						for(var i = 0; i < panel.items.items.length; i++)
						{
							var cb = panel.items.items[i];
							var name = cb.name;
							var parts = name.split('-');
							if((parseInt(parts[2]) & permissions) > 0)
							{
								cb.setValue(true);
							}
							else
							{
								cb.setValue(false);
							}
						}
						
						panel = Ext.getCmp('user-permissions-column2');
						for(var i = 0; i < panel.items.items.length; i++)
						{
							var cb = panel.items.items[i];
							var name = cb.name;
							var parts = name.split('-');
							if((parseInt(parts[2]) & permissions) > 0)
							{
								cb.setValue(true);
							}
							else
							{
								cb.setValue(false);
							}
						}
						
						userDialog.setTitle('Edit User');
						userDialog.show();
						userDialog.center();
				  }
				  
				  function deleteUser()
				  {
					  var user = _userContextMenu.rowRecord.data
					  
					  Ext.Msg.confirm("Delete?", "Remove user '"+user.first_name+" "+user.last_name+"'?", function(bid, p2){
						  if(bid == "yes")
						  {
							  Ext.Ajax.request({
								   url: _contextPath + '/user/delete',
								   success: function(p1, p2)
								   {
									   var response = Ext.decode(p1.responseText);
									   if(response.success)
									   {
										   store.reload();
										   Ext.growl.message('Success', 'User has been deleted.');
										   
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
			            				   msg: 'Error deleting user.',
			            				   buttons: Ext.Msg.OK,
			            				   icon: Ext.MessageBox.ERROR
			            				});
								   },
								   params: { xaction: 'delete', user_id: user.user_id }
								}); 
						  }
					  });
				  }
				  
				  function addUser()
				  {
					  userDialog.setTitle('New User');
					  userFormPanel.getForm().reset();
					  userDialog.show();
					  userDialog.center();
				  }

		});//end function
	}//end if
	  
});
