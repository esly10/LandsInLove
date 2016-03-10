PermitPanel = Ext.extend(Ext.TabPanel, {
	
	initComponent : function()
	{
		/*var config = {
		 *  title: 'Permit File',
			id: 'permit-config-panel',
			bodyCssClass: 'x-citewrite-panel-body',
			padding: '10px',
			autoScroll: true,
			autoLoad : { url : _contextPath + '/administration/permit', scripts : true }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		PermitPanel.superclass.initComponent.apply(this, arguments);
		*/
				
		 var stateStore = new Ext.data.JsonStore({
		    	root: 'states',
				url: _contextPath + '/permit/permitSearchableMultiple',
				totalProperty: 'count',
				fields: ['label', 'columnName'],
				autoLoad: true
		    });
		 	
			
					var config = {
						activeTab: 0,
						tabPosition: 'bottom',
						border: false,
						frame: false,
						items: [
				        {
								// form panel general
								xtype: 'form',
								title: 'Permit File',
								border: false,
								id: 'permits-config-panel',
								bodyCssClass: 'x-citewrite-panel-body',
								autoScroll: true,
								padding: '10px',
								autoScroll: true,
								autoLoad : { url : _contextPath + '/administration/permit', scripts : true },
								width: 600,
								buttonAlign: 'left',
									
								},
						        {
							 
									xtype: 'form', 
									title: 'Multiple Vehicle',
									border: false,
									bodyCssClass: 'x-citewrite-panel-body',
									autoScroll: true,
									padding: '20px 0px 0px 10px',
									width: 600,
									buttonAlign: 'left',
									id: 'vehicle-form-multiple',
									
									items: [
												{
													xtype: 'hidden',
													id: 'id_config_item',
													name: 'id_config_item',
													value: '0'
												},{
													xtype: 'label',
													forId: 'lb_default',
													text: 'Multiple Vehicle Indicator',
													style: 'font-weight:bold;',
													margins: '0px 10px 10px 0'
												},
												{		
													xtype: 'combo',
													id: 'searchType',
													valueField: 'columnName',
													hideLabel: true,
													displayField: 'label',
													lazyRender: false,
												 	store: stateStore,
													typeAhead: false,
													width: 200,
													triggerAction: 'all',
													forceSelection: true,
													mode: 'local',
													editable:false,
													margins: '0px 0px 0px 0px', 
												
													
												},
																																																							 
												{
													xtype: 'label',
													forId: 'lb_default',
													text: 'Read Sync Interval',
													style: 'font-weight:bold;',
													hidden:true
													//margins: '0px 10px 10px 0'
												},
												
												{
													xtype: 'panel',
													layout:'table',
													bodyBorder: false,
													bodyCssClass: 'x-citewrite-panel-body',
													layoutConfig: {
														// The total column count must be specified here
														columns: 4,
														tableAttrs: {
															style: {
																width: '250px'
															}
														}
													},
													items: [
														
														
													     {
															xtype: 'textfield',
															//hideLabel: true,
															hidden:true,
															id: 'timeMultiple',
															name: 'timeMultiple',
															width:200,
															maskRe: /^[0-9-]*$/,
															regex : new RegExp(/^\d*[0-9]\d*$/)
														},{
															xtype: 'label',
															hidden:true,
															text: 'minutes',
															 style: {
														            marginLeft: '10px'
														   }
														   
														},{
															xtype: 'hidden',
															id: 'id_config_minutes',
															name: 'id_config_minutes',
															value: '0'
														}
													]
												},	
												{
													xtype: 'box',
													height: 20
												},
										        
											{
												xtype: 'label',
												forId: 'lb_default2',
												text: 'Maximum time comparison',
												style: 'font-weight:bold;',
												margins: '0px 10px 10px 0'
											},
											
											{
												xtype: 'panel',
												layout:'table',
												bodyBorder: false,
												bodyCssClass: 'x-citewrite-panel-body',
												layoutConfig: {
													// The total column count must be specified here
													columns: 4,
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
														id: 'timeMax',
														name: 'timeMax',
														width:200,
														maskRe: /^[0-9-]*$/,
														regex : new RegExp(/^\d*[0-9]\d*$/)
													},{
														xtype: 'label',
														text: 'hours',
														 style: {
													            marginLeft: '10px'
													   }
													   
													},{
														xtype: 'hidden',
														id: 'id_max',
														name: 'id_max',
														value: '0'
													}
												]
											},	
													 																																									  
												{
													xtype: 'box',
													height: 8
												}
											        
											],
								buttons: [{
									text: 'Save',
									handler: function() {
										// validate form
										var PermitPanel = Ext.getCmp('vehicle-form-multiple');
										PermitPanel.getForm().submit(
														{
															url: _contextPath + '/administration/vehiclemultiple',
															scope: this,
															params: {action: 'save-general'},
															success: function(form,action) 
																	{
																		Ext.growl.message('Success','config has been saved.');
																	},
															failure: function(form,action) 
																	{
																		switch (action.failureType) 
																		{
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
										}//end hanlder
									}]
								}
						]//end items
					
						
					};
			
					Ext.apply(this, Ext.apply(this.initialConfig, config));
					
					PermitPanel.superclass.initComponent.apply(this, arguments);
					
					Ext.Ajax.request({
						url: _contextPath + '/administration/vehiclemultiple',
						success: function(form, action) {
							var response = Ext.decode(form.responseText);
							 Ext.getCmp('searchType').setValue(response.searchType.text_value);
							 Ext.getCmp('timeMultiple').setValue(response.timeMultiple.text_value);
							 Ext.getCmp('id_config_item').setValue(response.searchType.config_item_id);
							 Ext.getCmp('id_config_minutes').setValue(response.timeMultiple.config_item_id);
							 Ext.getCmp('timeMax').setValue(response.timeMax.text_value);
							 Ext.getCmp('id_max').setValue(response.timeMax.config_item_id);
							 
							 Ext.getCmp('ejemplo2').setValue(response.searchType.text_value);
						},
						params: {
							action: 'read-general'
						}
					});
						
			
	
		}					
	});



	