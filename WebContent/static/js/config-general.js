GeneralPanel = Ext.extend(Ext.FormPanel,{
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
			autoScroll: true,
			defaultType:'textfield',
			padding: '15px 5px 0px 10px',
			width: 600,
			buttonAlign: 'left',
			items: [
			{
				xtype: 'label',
				forId: 'lb_default',
				text: 'User Password Restrictions',
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
	        	name:'user-password-check-name',
	        	id: 'user-password-check-id',
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
				id:'userRegexLb',
				disabled:true
			},
	        {
	        	hideLabel: true,
	        	id: 'userRegExpression',
                name: 'userRegExpression',
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
				id:'userMessageLb',
				disabled:true
			},
	        {
	        	hideLabel: true,
	        	id: 'userMessage',
                name: 'userMessage',
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
	        	name:'user-expiration-password-check-name',
	        	id: 'user-expiration-password-check-id',
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
					id:'userEveryLb',
					disabled:true
				},{
					xtype: 'textfield',
		        	hideLabel: true,
		        	id:'userTime',
	                name: 'userTime',
	                width:105,
	                disabled:true,
	                maskRe: /^[0-9-]*$/,
	                regex : new RegExp(/^\d*[1-9]\d*$/),
				    regexText : "Invalid time"
	    		},{
					xtype: 'combo',
					id:'userIntervals',
					hideLabel: true,
					hiddenName: 'userIntervals',
					typeAhead: false,
					triggerAction: 'all',
					value:'DAY',
					lazyRender: true,
					mode: 'local',
					store: intervalsTimeStore,
					displayField: 'intervals',
					valueField: 'id',
					width: 100,
					disabled:true
				}
				]
			},{
				xtype: 'box',
				height: 8
			},
			{
				xtype: 'label',
				text: 'Image Directory',
				style: 'font-weight:bold;',
				margins: '0px 10px 10px 0'
			},{
				xtype: 'box',
				height: 5
			},
	        {
				xtype: 'label',
				text: 'Path',
				id:'imgPathLb',
			},
	        {
	        	hideLabel: true,
	        	id: 'imgPath',
                name: 'imgPath',
                width:250,
    		},
			{
				xtype: 'box',
				height: 8
			},
			{
				xtype: 'label',
				forId: 'lb_default',
				text: 'Processor Class',
				style: 'font-weight:bold;',
				margins: '0px 10px 10px 0'
			},{
				xtype: 'box',
				height: 5
			},
	        {
				xtype: 'label',
				text: 'Class Name',
				id:'processorPathLb',
			},
	        {
	        	hideLabel: true,
	        	id: 'processPath',
                name: 'processPath',
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
			url: _contextPath + '/administration/general',
			success: function(form, action) {
				var response = Ext.decode(form.responseText);
				 Ext.getCmp('userIntervals').setValue(response.passwordConfig.intervalsTime);
				 Ext.getCmp('user-password-check-id').setValue(response.passwordConfig.isEnable ? true:false);
				 Ext.getCmp('user-expiration-password-check-id').setValue(response.passwordConfig.isExpirationEnable ? true:false);
				 Ext.getCmp('userMessage').setValue(response.passwordConfig.message);
				 Ext.getCmp('userRegExpression').setValue(response.passwordConfig.regExpresion);
				 Ext.getCmp('userTime').setValue(response.passwordConfig.time);
				 Ext.getCmp('processPath').setValue(response.processClass.text_value);
				 Ext.getCmp('imgPath').setValue(response.imgPath.text_value);
			},
			params: {
				action: 'read-general'
			}
		});

	},
	submit:function() 
	{
		this.getForm().submit(
		{
			url:_contextPath + '/administration/general'
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
        	Ext.getCmp('userRegExpression').enable();
        	Ext.getCmp('userMessage').enable();
        	Ext.getCmp('user-expiration-password-check-id').enable();
        	Ext.getCmp('userRegexLb').enable();
        	Ext.getCmp('userMessageLb').enable();
        	
        	if(Ext.getCmp('user-expiration-password-check-id').getValue()){
        		Ext.getCmp('userTime').enable();
            	Ext.getCmp('userIntervals').enable();
            	Ext.getCmp('userEveryLb').enable(); 
        	}
		}else{
			Ext.getCmp('userRegExpression').disable();
        	Ext.getCmp('userMessage').disable();
        	Ext.getCmp('user-expiration-password-check-id').disable();
        	Ext.getCmp('userTime').disable();
        	Ext.getCmp('userIntervals').disable();
        	Ext.getCmp('userEveryLb').disable(); 
        	Ext.getCmp('userRegexLb').disable();
        	Ext.getCmp('userMessageLb').disable();
		}
	},
	enableFildsExpiration : function(enable)
	{
		if(enable){
			Ext.getCmp('userTime').enable();
        	Ext.getCmp('userIntervals').enable();
        	Ext.getCmp('userEveryLb').enable(); 
        	
		}else{
			Ext.getCmp('userTime').disable();
        	Ext.getCmp('userIntervals').disable();
        	Ext.getCmp('userEveryLb').disable(); 
		}
	}
});