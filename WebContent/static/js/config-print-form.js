ConfigPrintForm = Ext.extend(Ext.form.FormPanel,{
	print:null,
	formStore:null,
	initComponent : function()
	{
	    var form = this;

		var config = 
		{
				id: 'configForm-' + this.print.groupId,
				name: 'configForm-' + this.print.groupId,
				layout: 'form',
				title: form.print.name!=null?form.print.name:'New',
				bodyBorder: false,
				border: false,
				closable: true,
				frame: false,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px; ',
				bodyCssClass: 'x-citewrite-panel-body',
				labelWidth: 50,
				buttonAlign: 'left',
			    buttons: [{
		        	text: 'Save',
		        	bodyStyle: 'margins:20px 10px 10px 0px',
		        	width: 75,
		        	handler: function(){
		        		
		        		var formValues = form.getForm().getValues();

		        		var name = formValues['name'];
		        		if(name.length == 0)
		         		{
		         			Ext.Msg.show({
		         				   title:'Missing Field',
		         				   msg: 'Please enter a name.',
		         				   buttons: Ext.Msg.OK,
		         				   icon: Ext.MessageBox.ERROR
		         				});
		         			
		         			return false;
		         		}
		             	
		        		var xml = formValues['value'];
		             	if(xml.length == 0)
		         		{
		         			Ext.Msg.show({
		         				   title:'Missing Field',
		         				   msg: 'Please enter a xml value.',
		         				   buttons: Ext.Msg.OK,
		         				   icon: Ext.MessageBox.ERROR
		         				});
		         			
		         			return false;
		         		}
		        		
		        		
						form.submit();
					}
	        	},
	        	{
	        		text: 'Cancel',
		        	bodyStyle: 'margins:20px 10px 10px 0px',
		        	width: 75,
		        	handler: function(){
		        		form.destroy();
					}
	        	}],
				items: [
				    {
				    	xtype: 'hidden',
				    	id:'hidden'+ this.print.groupId,
				    	name: 'groupId',
				    	value:this.print.groupId
				    },
			        {
					xtype: 'textfield',
					name: 'name',
					fieldLabel: 'Name',
					width: 120,
					style: {
			            marginBottom: '10px'
			        },
			        value:this.print.name
		            },
		            {
			            xtype: 'textarea',
			            name: 'value',
			            fieldLabel: 'Message text',
			            anchor:'95% 80%',
			            hideLabel: true,
			            value:this.print.value
		            },
		            {
		            	xtype: 'checkbox',
		            	name: 'isDefault',
		            	hideLabel: true,
		            	checked:(this.print.isDefault == null)? false : this.print.isDefault,
		            	boxLabel: 'Default Format'
				    }
		            ]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		ConfigPrintForm.superclass.initComponent.apply(this, arguments);
	},
	submit:function() 
	{
		this.getForm().submit(
		{
			url:_contextPath + '/administration/printSave'
			,scope:this
			,success:this.onSuccess
			,failure:this.onFailure
			,params:{xaction:'store'}
			,waitMsg:'Saving...'
		});
	},
	onSuccess:function(form, action) 
	{
		
		Ext.growl.message('Success', 'Report saved successfully');
		
		this.setTitle(action.result.print.name);
		this.name = 'configForm-'+action.result.print.groupId;
		var groupIdHidden = Ext.getCmp('hidden'+ this.print.groupId);
		groupIdHidden.setValue(action.result.print.groupId);
		this.formStore.reload();
		
	},
	onFailure:function(form, action) 
	{
		var msg = "";
		if(action.failureType == 'client')	
		{
			msg = 'Please enter all required fields.';
		}
		else
		{
			msg = action.result.msg || action.response.responseText;
		}
		this.showError(msg);
	},
	showError:function(msg, title) 
	{
		title = title || 'Error';
		Ext.Msg.show(
		{
			title:title
			,msg:msg
			,modal:true
			,icon:Ext.Msg.ERROR
			,buttons:Ext.Msg.OK
		});
	}
			
});

