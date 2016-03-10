Ext.onReady(function(){
	
				Ext.get('my-account-link').on('click', function(){
					editAccountUser();
				});
				var accountFormPanel = Ext.getCmp('accountFormPanel');
				var accountDialog = Ext.getCmp('accountWindow');
				if(!accountDialog)
				{
				//device form
					accountFormPanel = new Ext.FormPanel({
					bodyBorder: false,
					border: false,
					frame: false,
					labelAlign: 'top',
					buttonAlign:'center',
					bodyStyle: 'padding: 10px; ',
					autoWidth: true,
					defaults: { width: '95%' },
					bodyCssClass: 'x-citewrite-panel-body',
					id: 'accountFormPanel',
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
								    	   id: 'account_first_name',
								    	   fieldLabel: 'First Name',
						                    anchor:'95%',
						                    tabIndex: 1

								       }, {
								    	   id: 'account_username',
								    	   fieldLabel: 'User Name',
								    	   anchor:'95%',
						                    tabIndex: 3

								       }]
					            },{
					                columnWidth:.5,
					                layout: 'form',
					                defaultType:'textfield',
					                border: false,
					                bodyBorder: false,
					                bodyCssClass: 'x-citewrite-panel-body',
					                items: [{
								    	   id: 'account_last_name',
								    	   fieldLabel: 'Last Name',
						                    anchor:'95%',
						                    tabIndex: 2
								       },{
								    	   id: 'account_password',
								    	   fieldLabel: 'Password',
								    	   anchor:'95%',
						                    tabIndex: 4,
						                    inputType: 'password',
						                    allowBlank: true
								       }]
					            }]
					        }]
				});
			  
				//device dialog
					accountDialog = new Ext.Window({
		                renderTo: document.body,
		                layout:'fit',
		                width:400,
		                height:200,
		                closeAction:'hide',
		                plain: true,
		                resizable: false,
		                modal: true,
		                id: 'accountWindow',
		                items: accountFormPanel,
		                title: 'My Account',
	
		                buttons: [{
		                    text:'Save',
		                    handler: function()
		                    {
		                    	var fname = Ext.getCmp('account_first_name').getValue();
		                    	var lname = Ext.getCmp('account_last_name').getValue();
		                    	var username = Ext.getCmp('account_username').getValue();
		                    	if(fname.length == 0 || lname.length == 0 || username.length == 0)
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
		                    	accountFormPanel.getForm().submit({
		                    	    url: _contextPath + '/user/saveAccount',
		                    	    params: {
		                    	        xaction: 'save'
		                    	    },
		                    	    success: function(form, action) {
		                    	    	accountDialog.hide();
		                    	       Ext.growl.message('Success', 'User has been saved.');
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
		                    	               Ext.growl.message('Failure', action.result.msg);
		                    	       }
		                    	    }
		                    	});
		                    }
		                },{
		                    text: 'Close',
		                    handler: function(){
		                    	accountDialog.hide();
		                    }
		                }]
		            });
				}
				
				 function editAccountUser()
				 {
					 Ext.Ajax.request({
						    url: _contextPath + '/user/account',
						    success: function(response)
						    {
						        var json = Ext.decode(response.responseText);
						        var user = json.user;
						        Ext.getCmp('account_first_name').setValue(user.first_name);
								Ext.getCmp('account_last_name').setValue(user.last_name);
								Ext.getCmp('account_username').setValue(user.username);
								Ext.getCmp('account_password').setValue('');
								
								accountDialog.show();
								accountDialog.center();
						        // process server response here
						    },
					 		failure: function(response, opts) 
					 		{
					 			Ext.growl.message('Failure', "Error retrieving account information");
					 		}

						});
				  }  
});
