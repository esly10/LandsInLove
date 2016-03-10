PermitGeneralPanel = Ext.extend(Ext.FormPanel,{
	initComponent : function()
	{
		var panel = this;
		
		var intervalsTimeStore = new Ext.data.SimpleStore({
			id: 'intervalsTimeStore',
			fields: [ 'id', 'intervals' ],
			data: [
					['DAY','Days'],
					['WEEK','Weeks']]
		});
		
		var config = {
				
			// form panel general
			xtype: 'form',
			frame: false,
			bodyCssClass: 'x-citewrite-panel-body',
			border: false,
			defaultType:'textfield',
			padding: '15px 5px 0px 10px',
			width: 600,
			buttonAlign: 'left',
			autoScroll: true,
			items: [
			{
				xtype: 'label',
				forId: 'lb_default',
				text: 'Owner Management',
				style: 'font-weight:bold;',
				margins: '0px 10px 10px 0'
			},
			{
				xtype: 'box',
				height: 10
			},       
			{
	        	xtype: 'checkbox',
	        	boxLabel: 'Enable Registration',
	        	name:'enable-registration-check-name',
	        	id: 'enable-registration-check-id',
	        	bodyBorder : false,
	        	hideLabel: true,
	        	padding:'10px',
	        	bodyStyle: 'margins:20px 0px 10px 0px',
	        	checked:false
	        },
	        {
	        	xtype: 'checkbox',
	        	boxLabel: 'Enable Edit',
	        	name:'enable-edit-check-name',
	        	id: 'enable-edit-check-id',
	        	bodyBorder : false,
	        	hideLabel: true,
	        	padding:'10px',
	        	bodyStyle: 'margins:20px 0px 10px 0px',
	        	checked:false
	        },  
	        {
				xtype: 'box',
				height: 13
			},
			{
				xtype: 'label',
				forId: 'lb_default',
				text: 'Owner Password Restrictions',
				style: 'font-weight:bold;',
				margins: '0px 10px 10px 0'
			},   
			{
				xtype: 'box',
				height: 8
			},
	        {
	        	xtype: 'checkbox',
	        	boxLabel: 'Enable Password ',
	        	name:'owner-password-check-name',
	        	id: 'owner-password-check-id',
	        	bodyBorder : false,
	        	hideLabel: true,
	        	padding:'10px',
	        	bodyStyle: 'margins:20px 0px 10px 0px',
	        	checked:false,
	        	listeners: {
	        	    check: function(checkbox, checked) {
	        	    	panel.enableFilds(checked);
	        	    }
	        	}
	        },
	        {
				xtype: 'box',
				height: 5
			},
	        {
				xtype: 'label',
				text: 'Regular Expression',
				id:'ownerRegexLb',
				disabled:true
			},
	        {
	        	hideLabel: true,
                name: 'ownerRegExpression',
                id:'ownerRegExpression',
                width:250,
                disabled:true
    		},
    		{
				xtype: 'box',
				height: 5
			},
	        {
				xtype: 'label',
				text: 'Message',
				id:'ownerMessageLb',
                disabled:true
			},
	        {
	        	hideLabel: true,
                name: 'ownerMessage',
                id:'ownerMessage',
                width:250,
                disabled:true
    		},
    		{
				xtype: 'box',
				height: 5
			},
			{
	        	xtype: 'checkbox',
	        	boxLabel: 'Enable Expiration ',
	        	name:'owner-expiration-password-check-name',
	        	id: 'owner-expiration-password-check-id',
	        	bodyBorder : false,
	        	hideLabel: true,
	        	padding:'10px',
	        	bodyStyle: 'margins:20px 0px 10px 0px',
	        	checked:false,
	        	disabled:true,
	        	listeners: {
	        	    check: function(checkbox, checked) {
	        	    	panel.enableFildsExpiration(checked);
	        	    }
	        	}
	        },
	        {
				xtype: 'box',
				height: 5
			},
			{
				xtype: 'panel',
				layout:'table',
			    padding: '5px',
			    defaultType:'textfield',
			    bodyBorder: false,
			    bodyCssClass: 'x-citewrite-panel-body',
			    layoutConfig: {
			        // The total column count must be specified here
			        columns: 3,
			        tableAttrs: {
			            style: {
			                width: '250px'
			            }
			        }
			    },
			    items: [{
					xtype: 'label',
					text: 'Every',
					id: 'ownerEveryLb',
					disabled:true
				},{
					xtype: 'textfield',
		        	hideLabel: true,
	                name: 'ownerTime',
	                id: 'ownerTime',
	                width:105,
	                disabled:true,
	                maskRe: /^[0-9-]*$/,
	                regex : new RegExp(/^\d*[1-9]\d*$/),
				    regexText : "Invalid time"
	    		},{
					xtype: 'combo',
					hideLabel: true,
					hiddenName: 'ownerIntervals',
					id:'ownerIntervals',
					typeAhead: false,
					triggerAction: 'all',
					lazyRender: true,
					mode: 'local',
					store: intervalsTimeStore,
					displayField: 'intervals',
					valueField: 'id',
					width: 100,
					editable: false,
					disabled:true
				}]
			},{
				xtype: 'box',
				height: 8
			},{
				xtype: 'label',
				forId: 'lb_default',
				text: 'Owner Authenticate Class',
				style: 'font-weight:bold;',
				margins: '0px 10px 10px 0'
			},{
				xtype: 'box',
				height: 5
			},
			{
				xtype: 'label',
				text: 'Class Name',
				id:'authenticatePathLb',
			},
	        {
				xtype: 'textfield',
	        	hideLabel: true,
	        	id: 'authenticateClass',
                name: 'authenticateClass',
                width:250,
    		}
	        ],// end Table
			buttons: [{
						text: 'Save',
						scope:this,
						handler: this.submit
						}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig,  config));
		GeneralPanel.superclass.initComponent.apply(this, arguments);
	
		Ext.Ajax.request({
			url: _contextPath + '/managedpermit/general',
			success: function(form, action) {
				var response = Ext.decode(form.responseText);
				 Ext.getCmp('ownerIntervals').setValue(response.passwordConfig.intervalsTime);
				 Ext.getCmp('owner-password-check-id').setValue(response.passwordConfig.isEnable ? true:false);
				 Ext.getCmp('owner-expiration-password-check-id').setValue(response.passwordConfig.isExpirationEnable ? true:false);
				 Ext.getCmp('ownerMessage').setValue(response.passwordConfig.message);
				 Ext.getCmp('ownerRegExpression').setValue(response.passwordConfig.regExpresion);
				 Ext.getCmp('ownerTime').setValue(response.passwordConfig.time);
				 Ext.getCmp('enable-registration-check-id').setValue(String(response.enableRegistration.int_value));
				 Ext.getCmp('enable-edit-check-id').setValue(String(response.enableEdit.int_value));
				 Ext.getCmp('authenticateClass').setValue(response.authenticationClass.text_value);
				 
			},
			params: {
				action: 'read-permitGeneral'
			}
		});
		
		
	},
	submit:function() 
	{
		this.getForm().submit(
		{
			url:_contextPath + '/managedpermit/general'
			,scope:this
			,success:this.onSuccess
			,failure:this.onFailure
			,params:{action:'save'}
			,waitMsg:'Saving...'
		});
	},
	onSuccess:function(form, action) 
	{
		Ext.growl.message('Success', 'General administration saved successfull');
		//reportStore.reload();
	},
	onFailure:function(form, action) 
	{
		var msg = "";
		if(action.failureType == 'client')	
		{
			msg = 'Error saving the general administration.';
		}
		else
		{
			msg = action.result.msg || action.response.responseText;
		}
		Ext.Msg.show({
		   title:'Error',
		   msg: msg,
		   buttons: Ext.Msg.OK,
		   icon: Ext.MessageBox.ERROR
		});

	},
	showError:function(msg, title) 
	{
		Ext.Msg.show({
		   title:'Error',
		   msg: msg,
		   buttons: Ext.Msg.OK,
		   icon: Ext.MessageBox.ERROR
		});
	},
	enableFilds : function(enable)
	{
		if(enable){
        	Ext.getCmp('ownerRegExpression').enable();
        	Ext.getCmp('ownerMessage').enable();
        	Ext.getCmp('owner-expiration-password-check-id').enable();
        	Ext.getCmp('ownerRegexLb').enable();
        	Ext.getCmp('ownerMessageLb').enable();
        	
        	if(Ext.getCmp('owner-expiration-password-check-id').getValue()){
        		Ext.getCmp('ownerTime').enable();
            	Ext.getCmp('ownerIntervals').enable();
            	Ext.getCmp('ownerEveryLb').enable();
        	}
		}else{
			Ext.getCmp('ownerRegExpression').disable();
        	Ext.getCmp('ownerMessage').disable();
        	Ext.getCmp('owner-expiration-password-check-id').disable();
        	Ext.getCmp('ownerRegexLb').disable();
        	Ext.getCmp('ownerMessageLb').disable();
        	Ext.getCmp('ownerTime').disable();
        	Ext.getCmp('ownerIntervals').disable();
        	Ext.getCmp('ownerEveryLb').disable(); 
		}
	},
	enableFildsExpiration : function(enable)
	{
		if(enable){
			Ext.getCmp('ownerTime').enable();
        	Ext.getCmp('ownerIntervals').enable();
        	Ext.getCmp('ownerEveryLb').enable();
		}else{
			Ext.getCmp('ownerTime').disable();
        	Ext.getCmp('ownerIntervals').disable();
        	Ext.getCmp('ownerEveryLb').disable();
		}
	}
});