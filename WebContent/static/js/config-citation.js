CiteConfigurationPanel = Ext.extend(Ext.TabPanel,{
	initComponent : function()
	{
		
		var formGeneral = {			       
				xtype: 'form',
				title: 'General',
				id: 'configCitationGeneral',
				frame: false,
				bodyCssClass: 'x-citewrite-panel-body',
				border: false,
				autoScroll: true,
				defaultType:'textfield',
				width: 600,
				buttonAlign: 'left',
				items: [
	{
					xtype: 'label',
					text: 'Citation Number Format',
				},
		{
		        	hideLabel: true,
		        	id: 'format',
	                name: 'format',
	                width:250,
	    		},
		{
					xtype: 'box',
					height: 5
	},
	{
					xtype: 'label',
					text: 'Edit Timer',
	},
	{
					xtype: 'panel',
					layout:'table',
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
					items: [
						{
							xtype: 'textfield',
							hideLabel: true,
							id: 'editTime',
							name: 'editTime',
							width:250,
							maskRe: /^[0-9-]*$/,
							regex : new RegExp(/^\d*[0-9]\d*$/)
						},{
							xtype: 'label',
							text: 'minutes',
							 style: {
						            marginLeft: '10px'
						        }
						}
					]
				}	
		        ],// end Table
		        buttons: [{
	                text:'Save',
	                handler: function()
	                {
	                	Ext.getCmp('configCitationGeneral').getForm().submit({
	                	    url: _contextPath + '/administration/citation',
	                	    success: function(form, action) {
	                	       Ext.growl.message('Success', 'Citation general saved successfull');
	                	    },
	                	    failure: function(form, action) {
	                	        switch (action.failureType) {
	                	            case Ext.form.Action.CLIENT_INVALID:
	                	                Ext.growl.message('Failure', 'Form fields may not be submitted with invalid values');
	                	                break;
	                	            case Ext.form.Action.CONNECT_FAILURE:
	                	                Ext.growl.message('Failure', 'Ajax communication failed');
	                	                break;
	                	            case Ext.form.Action.SERVER_INVALID:
	                	               Ext.growl.message('Error', action.result.msg);
	                	       }
	                	    },
	                	    params:{xaction:'cite-general'}
	                	});
	                }
	            }]
			   };
		
		if(IS_CITATION_PAYMENT_ENABLED){
			formGeneral.items.push(
					{
						xtype: 'box',
						height: 5
					},
			        {
						xtype: 'label',
						text: 'Days to Dispute',
					},
			        {
			        	hideLabel: true,
			        	id: 'daysDispute',
			            name: 'daysDispute',
			            width:250,
			            maskRe: /^[0-9-]*$/,
			            regex : new RegExp(/^\d*[0-9]\d*$/)
					}		
				);
		}

		var config = 
		{
			title: 'Citations',
			activeTab: 0,
			tabPosition: 'bottom',
			border: false,
			padding: '20px 15px 0px 15px',
			frame: false,
			items:[	
			       formGeneral,
			       {
					xtype: 'panel',
				    title: 'Fields',
					id: 'citation-fields-panel',
					bodyCssClass: 'x-citewrite-panel-body',
					padding: '10px',
					layout: 'fit',
					autoScroll: true,
					autoLoad : { url : _contextPath + '/administration/citation', scripts : true } 
			       },
			       new CiteConfigurationPagePanel()
			]//end items
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		CiteConfigurationPanel.superclass.initComponent.apply(this, arguments);
		
		Ext.Ajax.request({
			url: _contextPath + '/administration/citation',
			success: function(form, action) {
				var response = Ext.decode(form.responseText);
				 Ext.getCmp('format').setValue(response.numberFormat.text_value);
				 Ext.getCmp('editTime').setValue(response.editTime.text_value);
				 Ext.getCmp('daysDispute').setValue(response.daysDispute.text_value);
			},
			params: {
				xaction: 'read-general'
			}
		});
		
	}
});
