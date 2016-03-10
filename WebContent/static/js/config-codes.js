var codePageLimit = 50;

CodesPanel = Ext.extend(Ext.TabPanel, {
	initComponent: function() 
	{
		var storeParams = {
			root: 'codes',
			url: _contextPath + '/codes/list',
			totalProperty: 'count',
			fields: [ 'codeid', 'description' ],
			remoteSort: true,
			autoLoad: true,
			sortInfo: {
				field: 'description',
				direction: 'ASC'
			}
		};
		var storeSort = new Ext.data.SimpleStore({
			id: 'sort-store',
			fields: [ 'id', 'sort' ],
			data: [
					[ 'codeid:string:asc','ID as String Ascending' ],
					[ 'codeid:string:desc','ID as String Descending' ],
					[ 'codeid:number:asc','ID as Number Ascending' ],
					[ 'codeid:number:desc','ID as Number Descending' ],
					[ 'description:string:asc','Description Ascending' ],
					[ 'description:string:desc','Description Descending' ] ]
		});
		var filterForm = {
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
						fieldLabel: 'Code ID',
						name: 'codeid'
					},{
						fieldLabel: 'Description',
						name: 'description'
					}],
			buttons: [{
						text: 'Apply',
						width: 60,
						handler: function() 
						{
							var form = this.findParentByType('form');
							var params = form.getForm().getFieldValues();
							var parent = form.findParentBy(function(c) 
										{
											if (c.layout.type == 'border'){return true;}
		
											return false;
										});
							var grids = parent.findBy(function(c) 
										{
											if (c.store != undefined&& c.store != null) {return true;}
											return false;
										});
							if (grids.length > 0) 
							{
								var grid = grids[0];
								Ext.apply(grid.store.baseParams, params);
								grid.store.load({
										params: {start: 0, limit: codePageLimit}
											});
							}
						}
					},
					{
						text: 'Reset',
						width: 60,
						handler: function() 
								{
									var form = this.findParentByType('form');
									form.getForm().reset();
									
									var parent = form.findParentBy(function(c) 
												{
													if (c.layout.type == 'border') {return true;}
		
													return false;
												});
									var grids = parent.findBy(function(c) 
												{
													if (c.store != undefined && c.store != null) { return true; }
													return false;
												});
									
									if (grids.length > 0) 
									{
										var grid = grids[0];
										var type = grid.store.baseParams.type;
										grid.store.baseParams = {type: type};
										grid.store.load({
											params: {start: 0, limit: codePageLimit}
										});
									}
								}
					}]
				}; // filterForm
		
				var violationFilterForm = Ext.apply({}, {}, filterForm);
				violationFilterForm.items = [ {
					fieldLabel: 'Code ID',
					name: 'codeid'
				}, {
					fieldLabel: 'Description',
					name: 'description'
				}, {
					fieldLabel: 'Amount',
					name: 'fine_amount'
				}, {
					fieldLabel: 'Type',
					name: 'fine_type'
				}, {
					xtype: 'checkbox',
					boxLabel: 'Overtime Only',
					name: 'is_overtime',
					hideLabel: true
				} ];
		
				var config = {
					activeTab: 0,
					tabPosition: 'bottom',
					border: false,
					frame: false,
					items: [
			        {
							// form panel general
							xtype: 'form',
							title: 'General',
							border: false,
							bodyCssClass: 'x-citewrite-panel-body',
							autoScroll: true,
							padding: '20px 0px 0px 10px',
							width: 600,
							buttonAlign: 'left',
							id: 'code-form-general',
							items: [
					        {
					        	xtype: 'checkbox',
					        	boxLabel: 'Enable Sticky ',
					        	name:'use-stick-check-name',
					        	id: 'use-stick-check-id',
					        	hiddenName : 'use-stick-check',
					        	bodyBorder : false,
					        	hideLabel: true,
					        	padding:'10px',
					        	bodyStyle: 'margins:20px 0px 10px 0px',
					        	checked:false
					        },{
								xtype: 'box',
								height: 5
							},{
								// table layout configuration
								border: false,
								bodyCssClass: 'x-citewrite-panel-body',
	
								layout: {
									type: 'table',
									columns: 3,
									tableAttrs: 
										{
											style: {width: '500px'},
											border: '0'
										}
								},
	
								items: [
								        {
											xtype: 'box',
											height: 5,
											colspan: 3
										},
										{
											xtype: 'label',
											forId: 'lb_Code',
											text: 'Code',
											style: 'font-weight:bold;',
											margins: '0px 10px 10px 0'
	
										},
										{
											xtype: 'label',
											forId: 'lb_sort',
											text: 'Sort by',
											style: 'font-weight:bold;',
											margins: '0px 10px 10px 0'
	
										},
	
										{
											xtype: 'label',
											forId: 'lb_default',
											text: 'Default Value',
											style: 'font-weight:bold;',
											margins: '0px 10px 10px 0'
										},
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
	
										{
											xtype: 'label',
											forId: 'lb_state',
											text: 'State',
											margins: '20px 0px 10px 0px'
										},
	
										{
											xtype: 'combo',
											id: 'state-sort-name',
											hiddenName: 'state-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an Order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'state-default-name',
											hiddenName: 'state-default',
											fieldLabel: 'state ',
											emptyText: 'Select a State',
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'state', limit: 0}}, storeParams)),
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										},
	
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
	
										{
											xtype: 'label',
											forId: 'lb_Color',
											text: 'Color',
											margins: '0 0 0 10'
										},
	
										{
											xtype: 'combo',
											id: 'color-sort-name',
											hiddenName: 'color-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'color-default-name',
											hiddenName: 'color-default',
											fieldLabel: 'Color ',
											emptyText: 'Select a Color',
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'color',limit: 0}},storeParams)),
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										},
	
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
	
										{
											xtype: 'label',
											forId: 'lb_makes',
											text: 'Make',
											margins: '0 0 0 10'
										},
	
										{
											xtype: 'combo',
											id: 'make-sort-name',
											hiddenName: 'make-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an Order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'make-default-name',
											hiddenName: 'make-default',
											fieldLabel: 'Make ',
											emptyText: 'Select a Make',
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'make',limit: 0}}, storeParams)),
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										},
	
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
										{
											xtype: 'label',
											forId: 'lb_locations',
											text: 'Locations',
											margins: '0 0 0 10'
										},
	
										{
											xtype: 'combo',
											id: 'location-sort-name',
											hiddenName: 'location-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an Order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'location-default-name',
											hiddenName: 'location-default',
											fieldLabel: 'Locations ',
											emptyText: 'Select a Location',
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'location', limit: 0}},storeParams)),		
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										},
	
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
	
										{
											xtype: 'label',
											forId: 'lb_comments',
											text: 'Comments',
											margins: '0 0 0 10'
	
										},
	
										{
											xtype: 'combo',
											id: 'comment-sort-name',
											hiddenName: 'comment-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an Order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'comment-default-name',
											hiddenName: 'comment-default',
											fieldLabel: 'Comment ',
											emptyText: 'Select a Comment',
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'comment', limit: 0}}, storeParams)),
	
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										},
										{
											xtype: 'box',
											height: 5,
											colspan: 3
										},
										{
											xtype: 'label',
											forId: 'lb_violations',
											text: 'Violations',
											margins: '0 0 0 10'
										},
	
										{
											xtype: 'combo',
											id: 'violation-sort-name',
											hiddenName: 'violation-sort',
											fieldLabel: 'sort by ',
											emptyText: 'Select an Order',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: storeSort,
											displayField: 'sort',
											valueField: 'id',
											width: 170
										},
	
										{
											xtype: 'combo',
											id: 'violation-default-name',
											hiddenName: 'violation-default',
											fieldLabel: 'Violations ',
											emptyText: 'Select a Violation',
											typeAhead: false,
											triggerAction: 'all',
											lazyRender: true,
											typeAhead: true,
											triggerAction: 'all',
											lazyRender: true,
											mode: 'local',
											store: new Ext.data.JsonStore(Ext.apply({baseParams: {type: 'violation', limit: 0}}, storeParams)),		
											displayField: 'description',
											valueField: 'codeid',
											width: 200
										}
	
								]
							// end items table
	
							} ],// end Table
	
							buttons: [ {
								text: 'Save',
								handler: function() {
									// validate form
									var formPanel = Ext.getCmp('code-form-general');
									formPanel.getForm().submit(
													{
														url: _contextPath + '/administration/codes',
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
							},
					        {
						
								xtype: 'panel',
								title: 'States',
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
											items: [ new CodesGrid({codeType: 'state'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(filterForm)]
										}]
							},
							{
								xtype: 'panel',
								title: 'Colors',
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
											items: [ new CodesGrid({codeType: 'color'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(filterForm)]
										} ]
							},
							{
								xtype: 'panel',
								title: 'Makes',
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
											items: [ new CodesGrid({codeType: 'make'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(filterForm) ]
										}]
							},
							{
								xtype: 'panel',
								title: 'Locations',
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
											items: [ new CodesGrid({codeType: 'location'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(filterForm) ]
										}]
							},
							{
								xtype: 'panel',
								title: 'Comments',
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
											items: [ new CodesGrid({codeType: 'comment'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(filterForm) ]
										} ]
							},
							{
								xtype: 'panel',
								title: 'Violations',
								layout: 'border',
								border: false,
								bodyCssClass: 'x-citewrite-border-ct',
								defaults: {
									collapsible: true,
									split: true,
									layout: 'fit'
								},
								items: [
										{
											collapsible: false,
											region: 'center',
											margins: '5 0 5 5',
											items: [ new CodesGrid({codeType: 'violation'}) ]
										},
										{
											title: 'Filter',
											region: 'east',
											margins: '5 5 5 0',
											width: 200,
											items: [ new Ext.FormPanel(violationFilterForm) ]
										} ]
							}
					]//end items
				};
		
				Ext.apply(this, Ext.apply(this.initialConfig, config));
		
				CodesPanel.superclass.initComponent.apply(this,arguments);
		
				Ext.Ajax.request({
							url: _contextPath + '/administration/codes',
							success: function(form, action) {
								var response = Ext.decode(form.responseText);
		
								if (response.configDefault) 
								{
		
									for ( var i = 0; i < response.configDefault.length; i++) 
									{
		
										var data = response.configDefault[i];
										var field = null;
		
										switch (data.name) {
										case 'CODE_SORT_STATE':
											field = Ext.getCmp('state-sort-name');
											break;
										case 'CODE_DEFAULT_STATE':
											field = Ext.getCmp('state-default-name');
											break;
										case 'CODE_SORT_COLOR':
											field = Ext.getCmp('color-sort-name');
											break;
										case 'CODE_DEFAULT_COLOR':
											field = Ext.getCmp('color-default-name');
											break;
										case 'CODE_SORT_MAKE':
											field = Ext.getCmp('make-sort-name');
											break;
										case 'CODE_DEFAULT_MAKE':
											field = Ext.getCmp('make-default-name');
											break;
										case 'CODE_SORT_LOCATION':
											field = Ext.getCmp('location-sort-name');
											break;
										case 'CODE_DEFAULT_LOCATION':
											field = Ext.getCmp('location-default-name');
											break;
										case 'CODE_SORT_COMMENT':
											field = Ext.getCmp('comment-sort-name');
											break;
										case 'CODE_DEFAULT_COMMENT':
											field = Ext.getCmp('comment-default-name');
											break;
										case 'CODE_SORT_VIOLATION':
											field = Ext.getCmp('violation-sort-name');
											break;
										case 'CODE_DEFAULT_VIOLATION':
											field = Ext.getCmp('violation-default-name');
											break;
										case 'CODE_USE_STICKY':
											field = Ext.getCmp('use-stick-check-id');
											break;
										}
		
										field.setValue(data.text_value);
		
									}
								}
							},
		
							params: {
								action: 'read-general'
							}
						});
			}//end initComponent
});

CodesGrid = Ext.extend(Ext.grid.GridPanel, {
	codeType: 'state',
	initComponent: function() {
		var columns = [ {
			dataIndex: 'codeid',
			header: 'Code ID',
			sortable: true,
			width: 85
		}, {
			dataIndex: 'description',
			header: 'Description',
			sortable: true
		} ];

		if (this.codeType == 'violation') {
			columns.push({
				dataIndex: 'fine_amount',
				header: 'Amount',
				sortable: true,
				xtype: 'numbercolumn'
			}, {
				dataIndex: 'fine_type',
				header: 'Type',
				sortable: true
			}, {
				dataIndex: 'is_overtime',
				header: 'OverTime',
				renderer: function(v) {
					if (v == 0) {
						return 'No';
					}
					return 'Yes';
				},
				sortable: true
			});
		}
		else
		{
			columns.push({
				dataIndex: 'is_other',
				header: 'Other',
				renderer: function(v) {
					if (v == 0) {
						return 'No';
					}
					return 'Yes';
				},
				sortable: true
			});
		}

		var store = new Ext.data.JsonStore({
			root: 'codes',
			url: _contextPath + '/codes/list',
			baseParams: {type: this.codeType },
			totalProperty: 'count',
			fields: ['codeid', 'description', 'type', 'fine_amount', 'fine_type', 'is_overtime', 'is_other'],
			remoteSort: true,
			sortInfo: {
					field: 'codeid',
					direction: 'ASC'
	        },
	        autoLoad: {start: 0, limit: codePageLimit}
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
				emptyMsg: "No codes to display",
				pageSize: codePageLimit,
				items: [ '-', {
					text: 'Add',
					cls: 'x-btn-text details',
					handler: function(btn, event) {
						addCode(grid, null);
					}
				}, '-', {
					text: 'Import',
					cls: 'x-btn-text details',
					handler: function(btn, event) {
						importCode(grid);
					}
				} ]
			},
			viewConfig: {
				forceFit: true
			},
			columns: columns,
			listeners: {
				'rowcontextmenu': function(grid, index, event) {
					CodeGridMenu(grid, index, event);
				},
				'rowdblclick' : function(grid, index, event) {
					var record = grid.getStore().getAt(index);
					addCode(grid, record);
				}
			}

		};

		Ext.apply(this, Ext.apply(this.initialConfig, config));

		CodesGrid.superclass.initComponent.apply(this, arguments);
	}
});

var addCode = function(grid, record) 
{
	var height = 170;
	var fp = {
		bodyBorder: false,
		border: false,
		frame: false,
		defaultType: 'textfield',
		bodyStyle: 'padding: 10px; ',
		bodyCssClass: 'x-citewrite-panel-body',
		autoWidth: true,
		defaults: {
			width: '95%'
		},
		items: [ {
			id: 'code-codeid',
			name: 'codeid',
			fieldLabel: 'Code ID',
			maskRe: /^[a-zA-Z0-9-]*$/,
			tabIndex: 1,
			allowBlank: false
		}, {
			id: 'code-description',
			name: 'description',
			fieldLabel: 'Description',
			tabIndex: 2,
			allowBlank: false
		} ]
	};

	if (grid.codeType == 'violation') {
		height = 250;
		fp.items.push({
			id: 'code-fine-amount',
			name: 'fine_amount',
			fieldLabel: 'Amount',
			maskRe: /^[0-9\.]*$/,
			tabIndex: 3,
			allowBlank: false
		}, {
			id: 'code-fine-type',
			name: 'fine_type',
			fieldLabel: 'Type',
			tabIndex: 4,
			allowBlank: false
		}, {
			xtype: 'checkbox',
			id: 'code-is-ot',
			name: 'is_overtime',
			boxLabel: 'Is Overtime Violation',
			hideLabel: true,
			tabIndex: 5,
			allowBlank: false
		});
	}
	else
	{
		fp.items.push({
			xtype: 'checkbox',
			id: 'code-is-other',
			name: 'is_other',
			boxLabel: 'Is Other',
			hideLabel: true,
			tabIndex: 5,
			allowBlank: false
		});
	}

	var formPanel = new Ext.FormPanel(fp);

	var ajaxParams = {
		xaction: 'add',
		type: grid.codeType
	};
	var title = "Add ";
	if (record != null) {
		title = "Edit ";
		ajaxParams.xaction = 'update';
		ajaxParams.orig_codeid = record.data.codeid;
	}

	title += grid.codeType.charAt(0).toUpperCase() + grid.codeType.slice(1);

	var codeWindow = new Ext.Window({
        renderTo: document.body,
        title: title,
        width:300,
        height: height,
        plain: true,
        resizable: false,
        modal: true,
        id: 'codeFormWindow',
        items: formPanel,

        buttons: [{
            text:'Save',
            handler: function()
            {                	
            	// validate form
            	formPanel.getForm().submit({
            	    url: _contextPath + '/codes/save',
            	    scope: this,
            	    params: ajaxParams,
            	    success: function(form, action) {
            	    	grid.store.reload();
            	    	
            	    	var parent = action.options.scope.findParentByType('window'); 
            	    	parent.close();
            	       
            	    	Ext.growl.message('Success', 'Code has been saved.');
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
            	this.findParentByType('window').close();
            }
        }]
    });
	
	codeWindow.show();
	
	if(record != null)
	{
		Ext.getCmp('code-codeid').setValue(record.data.codeid);
		Ext.getCmp('code-description').setValue(record.data.description);
		if (grid.codeType == 'violation') 
		{
			Ext.getCmp('code-fine-amount').setValue(record.data.fine_amount);
			Ext.getCmp('code-fine-type').setValue(record.data.fine_type);
			Ext.getCmp('code-is-ot').setValue((record.data.is_overtime == 1));
		}
		else
		{
			Ext.getCmp('code-is-other').setValue((record.data.is_other == 1));
		}
	}
};

var importCode = function(grid) {
	var codeType = grid.codeType;
	var codeStore = grid.store;

	var fibasic = new Ext.ux.form.FileUploadField({
		name: 'import-file',
		fieldLabel: 'CSV File',
		tabIndex: 1,
		allowBlank: false
	});

	var fp = {
		bodyBorder: false,
		border: false,
		frame: false,
		defaultType: 'textfield',
		bodyStyle: 'padding: 10px; ',
		bodyCssClass: 'x-citewrite-panel-body',
		autoWidth: true,
		labelAlign: 'top',
		fileUpload: true,
		items: [
				{
					xtype: 'box',
					html: '<p style="font-style: italic;">Warning: This will override all codes of this type.</p>'
				}, fibasic ]

	};

	var formPanel = new Ext.FormPanel(fp);

	var ajaxParams = { xaction: 'add', type: codeType };
	var title = "Import " + codeType.charAt(0).toUpperCase() + codeType.slice(1) + "s";
	
		var codeWindow = new Ext.Window({
            renderTo: document.body,
            title: title,
            width: 275,
            height: 200,
            closeAction:'hide',
            plain: true,
            resizable: false,
            modal: true,
            id: 'codeImportFormWindow',
            items: formPanel,

            buttons: [{
                text:'Upload',
                handler: function()
                {                	
                	// validate form
                	formPanel.getForm().submit({
                	    url: _contextPath + '/codes/importCSV',
                	    scope: this,
                	    params: ajaxParams,
                	    success: function(form, action) {
                	    	// grid.store.reload();
                	    	
                	    	var parent = action.options.scope.findParentByType('window'); 
                	    	parent.close();
                	       
                	    	codeStore.reload();
                	    	Ext.growl.message('Success', 'Codes have been imported.');
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
		
		codeWindow.show();
};

var CodeGridMenu = function(grid, index, event) {
	event.stopEvent();
	var record = grid.getStore().getAt(index);

	var edit_item = {
		text: 'Edit',
		handler: function() {
			menu.hide();

			addCode(grid, record);

		}
	};

	var delete_item = {
		text: 'Delete',
		handler: function(p1, p2, p3) {
			menu.hide();

			Ext.Msg.show({
				   title:'Delete Code?',
				   msg: 'Delete Code?',
				   buttons:
				   {
						yes:'Yes',
						cancel:'Cancel'
				   },
				   fn: function(button)
				   {
					   params =
					   {
							codeid: record.data.codeid,
							type: grid.codeType
						};
				   
					   switch(button)
					   {
						   	case 'yes':
						   	{
								Ext.Ajax.request({
									url:_contextPath + '/codes/delete',
									success:function(p1, p2)
									{ 
										var response = Ext.util.JSON.decode(p1.responseText);
										if(response.success)
										{
											grid.store.reload();
											Ext.growl.message('Success', 'Code has been deleted.');
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
