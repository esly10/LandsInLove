eCommercePanel = Ext.extend(Ext.TabPanel, {
	initComponent: function() 
	{
		var grid = new LateFeesGrid();
		var config = {
			activeTab: 0,
			tabPosition: 'bottom',
			border: false,
			frame: false,
			items: [
			        {
						xtype: 'panel',
						title: 'Merchant',
						id: 'merchant-config-panel',
						border: false,
						bodyCssClass: 'x-citewrite-panel-body',
						padding: '10px',
						autoScroll: true,
						autoLoad : { url : _contextPath + '/administration/merchant', scripts : true },
						buttonAlign: 'left',
				        buttons: [{
					        	text: 'Save',
					        	bodyStyle: 'margins:20px 10px 10px 0px',
					        	width: 75,
					        	id: 'merchant-save-btn'
				        	},
				        	{
				        		text: 'Cancel',
					        	bodyStyle: 'margins:20px 10px 10px 0px',
					        	width: 75,
					        	id: 'merchant-cancel-btn'
				        	}]
					}
				]//end items
		};//end config
		
		if(IS_CITATION_PAYMENT_ENABLED){
			config.items.push({
				xtype: 'panel',
				title: 'Late Fees',
				layout: 'border',
				border: false,
				bodyCssClass: 'x-citewrite-border-ct',
				defaults: {
					collapsible: true,
					split: true,
					layout: 'fit'
				},
				items: [{
							collapsible: false,
							region: 'center',
							margins: '5 0 5 5',
							items: [ grid ]
						},
						{
							title: 'Filter',
							region: 'east',
							margins: '5 5 5 0',
							width: 200,
							items: [ new LateFeesFilter({latefee_grid: grid})]
						}]
			});
		}
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		eCommercePanel.superclass.initComponent.apply(this,arguments);
		
	}//end initComponent
});

LateFeesFilter = Ext.extend(Ext.FormPanel, {
	latefee_grid: null,
	initComponent: function() {
		var object = this;
		var config = {
				bodyBorder: false,
				border: false,
				frame: false,
				defaultType: 'textfield',
				labelAlign: 'top',
				buttonAlign: 'center',
				bodyStyle: 'padding: 10px; ',
				autoWidth: true,
				defaults: { width: '95%' },
				bodyCssClass: 'x-citewrite-panel-body',
				items: [{
							fieldLabel: 'Violation ID',
							name: 'filter_violation_id'
						},{
							fieldLabel: 'Days Late',
							name: 'filter_days_late'
						}],
				buttons: [{
							text: 'Apply',
							width: 60,
							handler: function() 
							{
								var params = object.getForm().getFieldValues();
								
								Ext.apply(object.latefee_grid.store.baseParams, params);
								object.latefee_grid.store.load({params: {start: 0, limit: codePageLimit}});
							}
						},
						{
							text: 'Reset',
							width: 60,
							handler: function() 
									{
										var form = this.findParentByType('form');
										object.getForm().reset();
										
										var type = object.latefee_grid.store.baseParams.type;
										object.latefee_grid.store.baseParams = {type: type};
										object.latefee_grid.store.load({
											params: {start: 0, limit: latefeePageLimit}
										});
									}
						}]
					};

			Ext.apply(this, Ext.apply(this.initialConfig, config));

			LateFeesFilter.superclass.initComponent.apply(this, arguments);
	}
});

var latefeePageLimit = 50;
LateFeesGrid = Ext.extend(Ext.grid.GridPanel, {
	initComponent: function() {
		var columns = [ {
			dataIndex: 'violation_id',
			header: 'Violation ID',
			sortable: true,
			width: 85
		},{
			dataIndex: 'days_late',
			header: 'Days Late',
			sortable: true
		},{
			dataIndex: 'fee_amount',
			header: 'Fee Amount',
			sortable: true
		}  ];

		var store = new Ext.data.JsonStore({
			root: 'latefees',
			url: _contextPath + '/administration/lateFees',
			totalProperty: 'count',
			fields: ['late_fee_id', 'violation_id', {name: 'days_late', type: 'number'}, {name: 'fee_amount', type: 'dollar'}],
			remoteSort: true,
			sortInfo: {
					field: 'violation_id',
					direction: 'ASC'
	        },
	        autoLoad: {start: 0, limit: latefeePageLimit}
		});

		var grid = this;
		var config = {
			stripeRows: true,
			loadMask: true,
			layout: 'fit',
			frame: false,
			border: false,
			bodyBorder: false,
			store: store,
			bbar: {
				xtype: 'paging',
				store: store,
				displayInfo: true,
				displayMsg: 'Displaying {0} - {1} of {2}',
				emptyMsg: "No late fees to display",
				pageSize: latefeePageLimit,
				items: [ '-', {
					text: 'Add',
					cls: 'x-btn-text details',
					handler: function(btn, event) {
						addLateFee(grid, null);
					}
				} ]
			},
			viewConfig: {
				forceFit: true
			},
			columns: columns,
			listeners: {
				'rowcontextmenu': function(grid, index, event) {
					LateFeesGridMenu(grid, index, event);
				},
				'rowdblclick': function(grid, index, event) {
					addLateFee(grid, grid.getStore().getAt(index));
				}
			}

		};

		Ext.apply(this, Ext.apply(this.initialConfig, config));

		LateFeesGrid.superclass.initComponent.apply(this, arguments);
	}
});

var addLateFee = function(grid, record) 
{
	//late fee comp box
	var height = 250;
	var fp = {
		bodyBorder: false,
		border: false,
		frame: false,
		defaultType: 'textfield',
		bodyStyle: 'padding: 10px; ',
		bodyCssClass: 'x-citewrite-panel-body',
		autoWidth: true,
		labelAlign: 'top',
		defaults: {
			width: 75
		},
		items: [ {
			xtype: 'combo',
			id: 'latefee-violation-id',
			hiddenName: 'latefee_violation_id',
			fieldLabel: 'Violation',
			typeAhead: false,
			triggerAction: 'all',
			lazyRender: true,
			mode: 'local',
			allowBlank: false,
			store: new Ext.data.JsonStore({
				root: 'codes',
				url: _contextPath + '/codes/list',
				baseParams: {type: 'violation', limit: 0},
				totalProperty: 'count',
				fields: [ 'codeid', 'description' ],
				remoteSort: true,
				autoLoad: true,
				sortInfo: {
					field: 'description',
					direction: 'ASC'
				},
				listeners: {
		            load: function() {
		                if(record != null)
		            	{
		            		Ext.getCmp('latefee-violation-id').setValue(record.data.violation_id);
		            	}
		            }
				}
			}),
			displayField: 'description',
			valueField: 'codeid',
			width: 200
		}, {
			id: 'latefee-days-late',
			name: 'latefee_days_late',
			fieldLabel: 'Days Late',
			maskRe: /^[0-9]*$/,
			tabIndex: 2,
			allowBlank: false
		},{
			id: 'latefee-fee-amount',
			name: 'latefee_fee_amount',
			fieldLabel: 'Fee Amount',
			maskRe: /^[0-9\.]*$/,
			tabIndex: 3,
			allowBlank: false
		} ]
	};

	var formPanel = new Ext.FormPanel(fp);

	var ajaxParams = {
		xaction: 'save',
		late_fee_id: 0
	};
	var title = "Add Late Fee";
	if (record != null) {
		title = "Edit Late Fee";
		ajaxParams.late_fee_id = record.data.late_fee_id;
	}

	var latefeeWindow = new Ext.Window({
        renderTo: document.body,
        title: title,
        width:300,
        height: height,
        plain: true,
        resizable: false,
        modal: true,
        id: 'latefeeFormWindow',
        items: formPanel,

        buttons: [{
            text:'Save',
            handler: function()
            {                	
            	// validate form
            	formPanel.getForm().submit({
            	    url: _contextPath + '/administration/lateFees',
            	    scope: this,
            	    params: ajaxParams,
            	    success: function(form, action) {
            	    	grid.store.reload();
            	    	
            	    	var parent = action.options.scope.findParentByType('window'); 
            	    	parent.close();
            	       
            	    	Ext.growl.message('Success', 'Late Fee has been saved.');
            	    },
            	    failure: function(form, action) {
            	        switch (action.failureType) {
            	            case Ext.form.Action.CLIENT_INVALID:
            	                Ext.Msg.show({
            	                	   title:'Failure',
            	                	   msg:  'All fields are required.',
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
        }]
    });
	
	latefeeWindow.show();
	
	if(record != null)
	{
		Ext.getCmp('latefee-days-late').setValue(record.data.days_late);
		Ext.getCmp('latefee-fee-amount').setValue(record.data.fee_amount);
	}
};


var LateFeesGridMenu = function(grid, index, event) {
	event.stopEvent();
	var record = grid.getStore().getAt(index);

	var edit_item = {
		text: 'Edit',
		handler: function() {
			menu.hide();
			addLateFee(grid, record);
		}
	};

	var delete_item = {
		text: 'Delete',
		handler: function(p1, p2, p3) {
			menu.hide();

			Ext.Msg.show({
				   title:'Delete Late Fee?',
				   msg: 'Delete Late Fee?',
				   buttons:
				   {
						yes:'Yes',
						cancel:'Cancel'
				   },
				   fn: function(button)
				   {
					   params = {
							   		xaction:'delete', 
							   		late_fee_id: record.data.late_fee_id
						};
				   
					   switch(button)
					   {
						   	case 'yes':
						   	{
								Ext.Ajax.request({
									url:_contextPath + '/administration/lateFees',
									success:function(p1, p2)
									{ 
										var response = Ext.util.JSON.decode(p1.responseText);
										if(response.success)
										{
											grid.store.reload();
											Ext.growl.message('Success', 'Late fee has been deleted.');
										}
										else
										{
											Ext.Msg.show({
											   title:'Error!',
											   msg: response.msg,
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
										}
									},
									failure:function(){},
									params: params
								});
							}
					   }
				   },
				   animEl: 'elId',
				   icon: Ext.MessageBox.QUESTION
				});
		}
	};

	var menu = new Ext.menu.Menu({
		items: [ edit_item, delete_item ]
	});
	menu.showAt(event.xy);
};


MerchantConfiguration = Class.create();
MerchantConfiguration.prototype = {
	select: null,
	initialize: function() 
	{
		this.select = $('merchant-class');
		this.select.onchange = this.merchantChanged.bind(this);
		
		var save = Ext.getCmp('merchant-save-btn');
		save.purgeListeners();
		save.on("click", this.save.bind(this));
		
		var cancel = Ext.getCmp('merchant-cancel-btn');
		cancel.purgeListeners();
		cancel.on("click", this.cancel.bind(this));
	},
	cancel: function(button, event)
	{
		var panel = Ext.getCmp('merchant-config-panel');
		panel.load({ url : _contextPath + '/administration/merchant', scripts : true });
	},
	save: function(button, event)
	{
		var data = {xaction: 'save'};
		
		var inputs = $('merchant-form').getElements();
		inputs.each(function(input)
		{
			if(input.type == 'checkbox' || input.type == 'radio')
			{
				if(input.checked)
				{
					data[input.name] = input.value;
				}
			}
			else
			{
				data[input.name] = input.value;
			}
		});
		
		Ext.Ajax.request({
			   url: _contextPath + '/administration/merchant',
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   Ext.growl.message('Success', 'Configuration has been saved.');
				   }
				   else
				   {
					   Ext.Msg.show({
        				   title:'Error!',
        				   msg: response.msg,
        				   buttons: Ext.Msg.OK,
        				   icon: Ext.MessageBox.ERROR
        				});
				   }
			   },
			   failure: function(p1, p2, p3)
			   {
				   debugger;
				   Ext.Msg.show({
    				   title:'Error!',
    				   msg: 'Error saving configuration.',
    				   buttons: Ext.Msg.OK,
    				   icon: Ext.MessageBox.ERROR
    				});
			   },
			   params: data
			});
	},
	merchantChanged: function()
	{
		var merchant = this.select.options[this.select.selectedIndex].value;
		
		Ext.Ajax.request({
		   url: _contextPath + '/administration/merchant',
		   success: function(response) {
			   var data = Ext.util.JSON.decode(response.responseText);
			   if(data.success)
			   {
				   var details = Ext.get('merchant-details');
				   details.update(data.details);
			   }
			   else
			   {
				   Ext.Msg.show({
					   title:'Error!',
					   msg:  data.msg,
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
					});
			   }
		   },
		   failure: function() {
			   Ext.Msg.show({
				   title:'Error!',
				   msg:'Error loading merchant details',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
		   },
		   params: { xaction: 'details','merchant-class': merchant }
		});
	}
};