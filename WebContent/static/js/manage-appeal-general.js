AppealGeneralPanel = Ext.extend(Ext.Panel, {
		owner: null,
		initComponent: function()
	    {

			var panel = this;
			var buttons = [];
			
			if(true){
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
				if(hasPermission(PL_APPEAL_MANAGE))
				{
				buttons.push({xtype:'button',
					handler: function(){
						
						Ext.Ajax.request({
							   url: _contextPath + '/citation/appealDetails',
							   success: function(response, opts){
								   var data = Ext.decode(response.responseText);
								   if(data.success)
								   {
									   data = data.citation_appeal;
									   var appealPanel = new Ext.Panel({
				                			title: '',
				                			id:'appelCitationPanel',
				                			bodyCssClass: 'x-citewrite-panel-body',
				                			autoScroll: true,
				                			layout: 'form',
				                			padding: 10,
				                			buttonAlign: 'left'
				                		});						   									   
									
									   appealPanel.add(appealForm(data));
									   									   
									   citationAppealWindow = new Ext.Window({
											title: 'Citation Appeals Detail - ' + data.name,
							                renderTo: document.body,
							                layout:'fit',
							                width:455,
							                height:475,
							                closeAction:'close',
							                plain: true,
							                resizable: true,
							                modal: true,	
							                items:[ appealPanel ],	
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
									            	    	
									            	    	
									            	    	panel.load({ url : _contextPath + '/citation/appealDetailsView', 
									            	    				 scripts : true, 
									            	    				 params: {citation_appeal_id: data.citation_appeal_id, citation_id: data.citation_id } 
							            	    			});
									            	    	
									            	    	//panel.store.reload();
									            	    	
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
									   									 
									   citationAppealWindow.show();
	
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
							   params: { citation_appeal_id: panel.appeal.citation_appeal_id, owner_id:  0}
							});
					},
					text: 'Edit'});
				}
			}
			
			var config = 
			{
				xtype: 'panel',
			    title: 'General',
			    id: 'ownertab-general-' + this.appeal.citation_appeal_id,
			    padding: 5,
			    bodyCssClass: 'x-citewrite-panel-body',
			    autoScroll: true,
			    buttonAlign: 'left',
			    autoLoad : { url : _contextPath + '/citation/appealDetailsView', scripts : true, params: {citation_appeal_id: this.appeal.citation_appeal_id, citation_id: this.appeal.citation_id } },
			    buttons:  buttons
			};

			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			AppealGeneralPanel.superclass.initComponent.apply(this, arguments);

	    }
});

var _ownerMutex = false;

function appealForm(citationAppeal, stateStore)
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

}


function setOwnerMutex(){
	_ownerMutex = false;
}
