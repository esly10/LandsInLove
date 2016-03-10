OwnerGeneralPanel = Ext.extend(Ext.Panel, {
		owner: null,
		initComponent: function()
	    {

			var panel = this;
			var buttons = [];
			
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
							   url: _contextPath + '/owner/details',
							   success: function(response, opts){
								   var data = Ext.decode(response.responseText);
								   if(data.success)
								   {
									   editOwner(data.owner, data.fields, data.types, panel, stateStore);
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
							   params: { owner_id: panel.owner.owner_id, xaction: 'get' }
							});
					},
					text: 'Edit'});
			}
			
			var config = 
			{
				xtype: 'panel',
			    title: 'General',
			    id: 'ownertab-general-' + this.owner.owner_id,
			    padding: 5,
			    bodyCssClass: 'x-citewrite-panel-body',
			    autoScroll: true,
			    buttonAlign: 'left',
			    autoLoad : { url : _contextPath + '/owner/details', scripts : true, params: {owner_id: this.owner.owner_id } },
			    buttons:  buttons
			};

			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			OwnerGeneralPanel.superclass.initComponent.apply(this, arguments);
	    }
});

var _ownerMutex = false;
function editOwner(owner, fields, ownerTypes, panel, stateStore)
{
	if(_ownerMutex)
	{
		return;
	}
	
	_ownerMutex = true;
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
	               allowBlank: false
		       },{
					id: 'edit-owner-password',
					name: 'password',
		    	   fieldLabel: 'Password'
		       },{
					id: 'edit-owner-email',
					name: 'email',
		    	    fieldLabel: 'Email',
					regex: /^([\w\-\'\-]+)(\.[\w-\'\-]+)*@([\w\-]+\.){1,5}([A-Za-z]){2,4}$/,
					regexText:'This field should be an e-mail address in the format "user@example.com"',
					allowBlank: false
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
				   allowBlank: false
		       },{
					id: 'edit-owner-city',
					name: 'city',
		    	   fieldLabel: 'City',
				   allowBlank: false
		       },
		       {
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
		       /*{
					id: 'edit-owner-state',
					name: 'state',
					fieldLabel: 'State',
					allowBlank: false
		       },*/{
		    	   id: 'edit-owner-zip',
		    	   name: 'zip',
		    	   fieldLabel: 'Zip',
		    	   maskRe: /^[0-9-]*$/,
				   allowBlank: false
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
			    	   id: 'edit-owner-'+field.name,
			    	   hiddenName: 'owner-extra-'+field.name,
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
				    	   id: 'edit-owner-'+field.name,
				    	   name: 'owner-extra-'+field.name,
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
	if(owner != null)
	{
		title = "Edit ";
	}
	
	title += " Owner";
	
	var ownerWindow = new Ext.Window({
        renderTo: document.body,
        title: title,
        width:325,
        height: 300,
        plain: true,
        resizable: true,
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
					url: _contextPath + '/owner/save',
					scope: this,
					params: ajaxParams,
					success: function(form, action) {
						if(owner != null)
						{
							//var ownerPanel = Ext.getCmp('ownertab-general-' + owner.owner_id);
							if(panel != null)
							{
								
								panel.load({url: _contextPath + '/owner/details', scripts : true, params: {owner_id: owner.owner_id }});
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
					_ownerMutex = false;							
				}
			}
    });
	ownerWindow.show();
	window.setTimeout(setOwnerMutex,500);
	
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
		
		
		if(fields != undefined && fields.length > 0)
		{
			for(var i = 0; i < fields.length; i++)
			{
				var field = fields[i];
				var input = Ext.getCmp('edit-owner-'+field.name);
				
				var attr = getAttributeByName(owner.extra, field.name);
				if(attr != null)
				{
					input.setValue(attr.value);
				}
			}
		}
	}//end if owner != null
	else
	{
		Ext.getCmp('edit-owner-id').setValue(0);
	}
}//end editOwner function

function setOwnerMutex(){
	_ownerMutex = false;
}
