Ext.layout.FormLayout.prototype.trackLabels = true;
Ext.onReady(function(){

	var flagCriteria = false;
	var CITATION_TYPE = 0;
	var MPERMIT_TYPE = 1;
	var FINANCIA_CITATIONL__TYPE = 2;
	var FINANCIA_PERMIT__TYPE = 3;

	var pageLimit = 50;
	var adminReports = Ext.get('nav-reports');
	if(adminReports != null)
	{
		adminReports.on('click', function(){
			
			var reportStore = new Ext.data.JsonStore
			({
			    // store configs
			    autoDestroy: true,
			    autoLoad: true,
			    url: _contextPath + '/report/list',
			    remoteSort: true,
			    storeId: 'reportStore',
			    // reader configs
			    idProperty: 'report_id',
			    root: 'reports',
			    fields:
			    	['report_id', 'name','reportFields', 'report_type', 'reportCriterias']
			});
			
			var citeFieldStore = new Ext.data.JsonStore({
				root: 'selectCiteFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label'],
				autoLoad: true,
				baseParams: {type: CITATION_TYPE, section: "select"}
			});
			
			var citeCriteriaStore  = new Ext.data.JsonStore({
				root: 'criteriaCiteFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label'],
				autoLoad: true,
				baseParams: {type: CITATION_TYPE, section: "criteria"}
			});
			
			var mpermitFieldStore = new Ext.data.JsonStore({
				root: 'selectPermitFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label'],
				autoLoad: true,
				baseParams: {type: MPERMIT_TYPE, section: "select"}
			});
			
			var mpermitCriteriaStore  = new Ext.data.JsonStore({
				root: 'criteriaPermitFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label'],
				autoLoad: true,
				baseParams: {type: MPERMIT_TYPE, section: "criteria"}
			});
			
			var financialCiteFieldStore  = new Ext.data.JsonStore({
				root: 'selectFinancialCiteFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label', 'group'],
				autoLoad: true,
				baseParams: {type: FINANCIA_CITATIONL__TYPE, section: "select"}
			});
			
			var financialCiteCriteriaStore  = new Ext.data.JsonStore({
				root: 'criteriaFinancialCiteFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label', 'group'],
				autoLoad: true,
				baseParams: {type: FINANCIA_CITATIONL__TYPE, section: "criteria"}
			});
			
			var financialPermitFieldStore  = new Ext.data.JsonStore({
				root: 'selectFinancialPermitFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label', 'group'],
				autoLoad: true,
				baseParams: {type: FINANCIA_PERMIT__TYPE, section: "select"}
			});
			
			var financialPermitCriteriaStore  = new Ext.data.JsonStore({
				root: 'criteriaFinancialPermitFields',
				url: _contextPath + '/report/fields',
				totalProperty: 'count',
				fields: ['name', 'label', 'group'],
				autoLoad: true,
				baseParams: {type: FINANCIA_PERMIT__TYPE, section: "criteria"}
			});
			
			var reportFormPanel = function(title, count, data){
				var reportTab = {
						closable: true,
						buttonAlign: 'left',
						bodyCssClass: 'x-citewrite-panel-body',
						layout:'border',
						defaults: {
						    collapsible: false,
						    split: true,
						    bodyStyle: 'padding:15px'
						},
						border: false,	
				        bodyBorder: false,
						items: [],
						buttons: []
				};
				
				var form = new Ext.form.FormPanel(reportTab);
				form.title = title;
				form.add(
			    {	region:'north',
				    bodyCssClass: 'x-citewrite-panel-body',
				    collapsible: false,
				    margins: '0 0 0 0', //'5 0 0 0',
				    padding: '0 0 0 0',
				    split: false,
				    height: 50, //60
				    layoutConfig: {
						columns: 2,
						tableAttrs: {
							style: {
								width: '270px'
							}
						}
					},
				    items: [{
			    			xtype: 'hidden',
			    			name: 'report_id',
			    			value: (data.report_id != null) && (data.report_id != "") ?  data.report_id : 0
	    				},
	    				{
			    			xtype: 'hidden',
			    			name: 'reportType',
			    			value: data.report_type
	    				},
				        {
							xtype: 'label',
							text: 'Report Name',
							style: 'font-size:12px; margin-right: 15px;'
						},
						{
							xtype:'textfield',
							name:'reportName',
							value:(data.name != null) && (data.name != "") ?  data.name : "",
						}]
				},new FieldsPanel({type: data.report_type, count:count, reportFields:data.reportFields, store: data.report_type == CITATION_TYPE ? citeFieldStore : data.report_type == MPERMIT_TYPE ? mpermitFieldStore : data.report_type == FINANCIA_CITATIONL__TYPE ? financialCiteFieldStore: data.report_type == FINANCIA_PERMIT__TYPE ? financialPermitFieldStore: undefined  }), new CriteriaPanel({type: data.report_type, count:count, reportCriterias:data.reportCriterias, store: data.report_type == CITATION_TYPE ? citeCriteriaStore : data.report_type == MPERMIT_TYPE ? mpermitCriteriaStore : data.report_type == FINANCIA_CITATIONL__TYPE ? financialCiteCriteriaStore : data.report_type == FINANCIA_PERMIT__TYPE ? financialPermitCriteriaStore: undefined }));
				
				form.addButton({
					text: 'Save' 
				  }, function(){
					  
					  
					  var reportName = form.items.items[0].items.items[3];
					  
					  if(Ext.util.Format.trim(reportName.getValue()) == "" || reportName.getValue() == null){
						  Ext.Msg.show({
							   title:'Error!',
							   msg: 'Report name is  required.',
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
						  return;
					  }

					  
					  var field = form.items.items[1].items.items[0];
					  
					  for (var i=0; i<field.items.items.length; i++){
						  
						  var row = field.items.items[i];
						  var order = row.items.items[0];
						  order.setValue(i);
						  
						  for (var w=0; w<row.items.items.length; w++){
						  	
							  var item = row.items.items[w];

							  if(item.xtype == 'combo'){
								  
								  if(item.hiddenField.name == "field" && (Ext.util.Format.trim(item.value) == "" || item.value == null)){
									  Ext.Msg.show({
										   title:'Error!',
										   msg: 'All fields are required.',
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
									  return;
								  }

								  item.el.dom.name = item.name+"_"+i;
								  item.hiddenField.name  = item.hiddenName+"_"+i;
							  }else{
								  item.el.dom.name = item.name +"_"+i;
							  }
							 
						  }
						  
					  }

					  
					  var criteria = form.items.items[2].items.items[1];
					  
					  if (flagCriteria == true)
					  {
								 					  
					  for (var i=0; i<criteria.items.items.length; i++){
						  
						  var row = criteria.items.items[i];
						  var order = row.items.items[0];
						  order.setValue(i);
						  var flag = false;
						 
						  for (var w=0; w<row.items.items.length; w++){
						  	
							  var item = row.items.items[w];
							  
							  if (item.xtype == 'combo'){							  
								  	if(Ext.util.Format.trim(item.value) == "is null" || Ext.util.Format.trim(item.value) == "is not null"){
								  		flag = true;
								  	}
								  	
									if(item.hiddenField.name == "criteria" && (Ext.util.Format.trim(item.value) == "" || item.value == null)){
										  Ext.Msg.show({
											   title:'Error!',
											   msg: 'All fields are required',
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
										  return;
									}
							  				  
								  item.hiddenField.name  = item.hiddenName+"_"+i;
							  } if (item.xtype == 'panel'){
								  var itemValue = item.items.items[0];
								  
								  if(itemValue.xtype == 'combo'){
									  itemValue.hiddenField.name  = itemValue.hiddenName+"_"+i;  
								  }else{
									  if( (Ext.util.Format.trim(Ext.getCmp(itemValue.id).getValue())) == ""){
										  if(!flag){
											  Ext.Msg.show({
												   title:'Error!',
												   msg: 'All fields are required',
												   buttons: Ext.Msg.OK,
												   icon: Ext.MessageBox.ERROR
												});
											  return;
										  }
										  
									  }
									  
									  itemValue.el.dom.name = itemValue.name +"_"+i;  
								  }

							  } else {
								  item.el.dom.name = item.name +"_"+i;
							  }
							  
						  }
					  }
					  
					  }
					  else 
					  {
						  criteria.items.items.length = 0;
						  //criteriaCount:criteria.items.items.length == 0;
					  }
				  	 
				  	form.getForm().submit(
				  			{
				  				url:_contextPath + '/report/save'
				  				,scope:this
				  				,success: function(response, opts) { 
				  					var data = Ext.decode(opts.response.responseText);

				  					if(data.success){
										   
				  						var panel = form.items.items[0];
					  					for(var i=0; i<panel.items.items.length; i++){
					  						var item = panel.items.items[i];
					  						if(item.name == "report_id"){
					  							item.setValue(data.report.report_id);
					  							break;
					  						}
					  					}
					  					
					  					var menu = Ext.getCmp('west-region-container');
					  					menu.items.items[0].store.reload();
										
								   }else{

									   var msg = "Error saving the report";
									   if(typeof data.msg !== 'undefined'){
										   msg =data.msg; 
					  				   }
					  					  
									   Ext.Msg.show({
										   title:'Error!',
										   msg: msg,
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
									   
								   }

		            			},
		            			failure: function(response, opts) {
		            				  var msg = "Error saving the report";
				  					  var data = Ext.decode(opts.response.responseText);
				  					  if(typeof data.msg !== 'undefined'){
				  						  msg =data.msg; 
				  					  }
		            				  Ext.Msg.show({
		            					   title:'Error!',
		            					   msg: msg,
		            					   buttons: Ext.Msg.OK,
		            					   icon: Ext.MessageBox.ERROR
		            					});
				  				},
				  				params:{fieldCount:field.items.items.length, criteriaCount:criteria.items.items.length}
				  				,waitMsg:'Saving...'
				  			});

				  }, form);
				
				form.addButton({
					text: 'Close' 
				  }, function(){
					var tabs = Ext.getCmp('reporttabs');
					tabs.remove(form);
					form.destroy();
				  }, form);

				form.doLayout();

				return form;
				
			};

			var ReportList = function(viewer, config) {
			    this.viewer = viewer;
			    Ext.apply(this, config);
			
			    this.store = reportStore,
			    this.colModel = new Ext.grid.ColumnModel({
			        defaults: {
			            width: 60,
			            sortable: true
			        },
			        columns: [{
				            	id: 'reports', 
				            	header: 'Reports', 
				            	width: 60, 
				            	//height:60,
				            	sortable: true, 
				            	dataIndex: 'name'
		            		}/*,
		            		{
		            			id: 'type', 
		            			header: 'Type', 
		            			width: 200, 
		            			sortable: true, 
		            			dataIndex: 'report_type', 
		            			renderer: function(value) { 
		            				
		            				switch (value)
		            				{
		            				  case CITATION_TYPE: 
		            					  return "Citation";
		            					  
		            				  case MPERMIT_TYPE: 
		            					  return "Permit";
		            					  
		            				  case FINANCIA_CITATIONL__TYPE:
		            					  return "Financial-Citation";
		            					  
		            				  case FINANCIA_PERMIT__TYPE:
		            					  return "Financial-Permit";
		            					  
		            				}
		            							
		            			}
		            		}*/
			        ]
			    });
			    this.viewConfig = {
			        forceFit: true
			    };
			    
			    if(hasPermission(PL_REPORT_VIEW)){
			    	
						var menuReport = {
	    	                text: 'New',
	    	                menu: {
	    	                    items: []
	    	                },
	    	                listeners: {
	    	                	click: function(){
	    							return false;	
								}
    						}
	    	            };	
						
						menuReport.menu.items.push('<b class="menu-title" style="margin-left:25px">Choose type</b>',{
	                        text: 'Citation',
	                        listeners: {
	                        	click: function(){
	    							var tabs = Ext.getCmp('reporttabs');
	    							var count = tabs.count +1;
	    							tabs.count = count;
	    							
	    							form = reportFormPanel('Citation', count, {report_type: CITATION_TYPE});										                							
	    							var panel = tabs.add(form);
	    							tabs.setActiveTab(panel.id);
								}
							}
                        });
													
						if(IS_MANAGED_PERMITS_ENABLED){
							menuReport.menu.items.push({
						        text: 'Permit',
							    listeners: {
							    	click: function(){
							    		var tabs = Ext.getCmp('reporttabs');
										var count = tabs.count +1;
										tabs.count = count;
										
										form = reportFormPanel('Permit', count, {report_type: MPERMIT_TYPE});										                							
										var panel = tabs.add(form);
										tabs.setActiveTab(panel.id);	
									}
							    }
							});
						}
						
						if(IS_CITATION_PAYMENT_ENABLED){
							menuReport.menu.items.push({
						        text: 'Citation Financial',
						        listeners: {
						        	click: function(){
										var tabs = Ext.getCmp('reporttabs');
										var count = tabs.count +1;
										tabs.count = count;
										
										form = reportFormPanel('Citation Financial', count, {report_type: FINANCIA_CITATIONL__TYPE});										                							
										var panel = tabs.add(form);
										tabs.setActiveTab(panel.id);
									}
								}
							});
						}
						
						if(IS_MANAGED_PERMITS_ENABLED && IS_CITATION_PAYMENT_ENABLED){
							menuReport.menu.items.push({
							    text: 'Permit Financial',
							    listeners: {
							    	click: function(){
							    		var tabs = Ext.getCmp('reporttabs');
										var count = tabs.count +1;
										tabs.count = count;
										
										form = reportFormPanel('Permit Financial', count, {report_type: FINANCIA_PERMIT__TYPE});										                							
										var panel = tabs.add(form);
										tabs.setActiveTab(panel.id);		
									}
								}
							});
						}
						
						var menuTemplate  = {
	    	                text: 'Template',
	    	                menu: {
	    	                    items: []
	    	                },
	    	                listeners: {
	    	                	click: function(){
	    							return false;	
								}
    						}
	    	            };
						
						
						function loadTemplate(id){
							Ext.Ajax.request({
								   url: _contextPath + '/report/templateFinancial',
							   success: function(response, opts){
								   var resp = Ext.decode(response.responseText);
								   
								   if(resp.success){
									   
										var tabs = Ext.getCmp('reporttabs');
										var count = tabs.count +1;
										tabs.count = count;
										var title = "";
										
										var type = resp.report.report_type;
										
										if(type == CITATION_TYPE){
											title = "Citation"; 
										}else if(type == MPERMIT_TYPE){
											title = "Permit";
										}else if(type == FINANCIA_CITATIONL__TYPE){
											title = "Citation Financial";
										}else if(type == FINANCIA_PERMIT__TYPE){
											title = "Permit Financial";
										}
										
										form = reportFormPanel(title, count, resp.report);										                							
										var panel = tabs.add(form);
										tabs.setActiveTab(panel.id);
										
								   }else{

									   Ext.Msg.show({
										   title:'Error!',
										   msg: 'Error loading the template',
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
								   }
								    
							   },
							   failure: function(response, opts){
								   Ext.Msg.show({
									   title:'Error!',
									   msg:  'Error loading the template',
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   },
							   params: {
								   idTemplate: id
								}
							});
						}
						
						
						menuTemplate.menu.items.push('<b class="menu-title">Choose a pre designed</b>',{
					        text: 'Template1',
					        listeners: {
					        	click: function(){
					        		loadTemplate("1");
								}
							}
						});
						
						menuTemplate.menu.items.push({
					        text: 'Template2',
					        listeners: {
					        	click: function(){
					        		loadTemplate("2");	
								}
							}
						});
						
						menuTemplate.menu.items.push({
					        text: 'Template3',
					        listeners: {
					        	click: function(){
					        		loadTemplate("3");	
								}
							}
						});
						
						menuTemplate.menu.items.push({
					        text: 'Template4',
					        listeners: {
					        	click: function(){
					        		loadTemplate("4");
								}
							}
						});
						
			    	   var menu = new Ext.menu.Menu({
			    	        id: 'mainMenu',
			    	        count:0,
			    	        style: {
			    	            overflow: 'visible'     // For the Combo popup
			    	        },
			    	        items: [menuReport
			    	   ]//menuTemplate]//hideen template
			    	    });
	    	
			    	this.tbar = {
			    			xtype: 'toolbar',
							items: ['->',{
					            text:'Create Report',
					            iconCls: 'bmenu',  // <-- icon
					            menu: menu  // assign menu by instance
					        }]
					};
			    }
			    
			    ReportList.superclass.constructor.call(this, 
			    {
			        region: 'center',
			        id: 'report-list-grid',
			        loadMask: {msg:'Loading Reports...'},			
			        sm: new Ext.grid.RowSelectionModel
			        ({
			            singleSelect:true
			        }),
			
			        viewConfig: 
			        {
			            forceFit:true,
			            enableRowBody:true,
			            showPreview:false,
			            getRowClass : this.applyRowClass
			        }
			    });
			
			};
			
			Ext.extend(ReportList, Ext.grid.GridPanel, {
			    listeners:{
			    	rowcontextmenu: function(grid, index, event ){
			    		
			    		if(hasPermission(PL_REPORT_VIEW)){
			    			reportListMenu(grid,index,event);	
			    		}
			    		
			    	},
			    	rowdblclick: function(grid, index, event )
			    	{
			    	   var record = grid.getStore().getAt(index);
			    		
					   var formPanel = new ReportEngineReport({report: record.data});
					   formPanel.setTitle(record.data.name);
					
					   var tabs = Ext.getCmp('reporttabs');
					   var panel = tabs.add(formPanel);
					   tabs.setActiveTab(formPanel.id);
			    		
			    	}
			    }
			});

			var reportListMenu = function(grid, index, event)
			{
				event.stopEvent();
				var record = grid.getStore().getAt(index);
				
				var items = new Array({
						text: 'Edit',
						handler: function() 
						{
							var tabs = Ext.getCmp('reporttabs');
							var count = tabs.count +1;
							tabs.count = count;
							
							form = reportFormPanel(record.data.name, count, record.data);										                							
							var panel = tabs.add(form);
							tabs.setActiveTab(panel.id);

						}
					},
					{
						text: 'Copy',
						handler: function() 
						{
							// Basic request
							Ext.Ajax.request({
							   url: _contextPath + '/report/copy',
							   success: function(response, opts){
								   reportStore.reload();
								   Ext.growl.message('Success!', 'Report has been copied.');
							   },
							   failure: function(response, opts){
								   Ext.Msg.show({
									   title:'Error!',
									   msg: 'Error copying report.',
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   },
							   params: { report_id: record.data.report_id, report_type: record.data.report_type}
							});		
						}
					},
					{
						text: 'Delete',
						handler: function() 
						{
							Ext.MessageBox.confirm("Delete Report?", 'Delete "'+record.data.name+'"?', function(p1, p2){
								if(p1 != 'no')
								{
									// Basic request
									Ext.Ajax.request({
									   url: _contextPath + '/report/delete',
									   success: function(response, opts){
										   reportStore.reload();
										   Ext.growl.message('Success!', 'Report has been removed.');
									   },
									   failure: function(response, opts){
										   Ext.Msg.show({
											   title:'Error!',
											   msg: 'Error deleting report.',
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
									   },
									   params: { report_id: record.data.report_id }
									});
								}
							});
						}
					});
				
				
				var menu = new Ext.menu.Menu(
				{
					items: items
				}).showAt(event.xy);
			};
			
			var ReportEngine = Ext.extend(Ext.Panel, 
			{
			    initComponent: function()
			    {
			        Ext.apply(this, 
			        {
			        	title: 'Reports',
			            layout: 'border',
			            border: false,
			            frame: false,
			            items: [{
			                region:'west',
			                margins: '5 0 5 5',
			                border: false,
			                width: 250,
			                collapsible: true,   // make collapsible
			                id: 'west-region-container',
			                layout: 'fit',
			                items: [new ReportList()]
			            },{
			            	 title: 'Center Region',
			                 region: 'center',     // center region is required, no width/height specified
			                 xtype: 'container',
			                 layout: 'fit',
			                 margins: '5 5 5 5',
			                 items:[{
			                	 xtype: 'tabpanel',
			                	 id: 'reporttabs',
			                	 frame: false,
			                	 count:0
			                	 
			                 }]	
			            }]
			        });
			        ReportEngine.superclass.initComponent.apply(this, arguments);
			    }
			});
			
			var content = Ext.getCmp('content-panel');
			content.removeAll(true);
			content.add(new ReportEngine({bodyCssClass: 'x-citewrite-border-ct'}));
			content.doLayout();

			FieldsPanel = Ext.extend(Ext.Panel, {
				type: null,
				report: null,
				count:0,
				store: null,
				reportFields: null,
				initComponent: function()
			    {
					
					var fieldsPanel = this;
					
					var fieldRows= new Ext.Panel({
                    	xtype:'panel',
                    	layout: {
                    		 type: 'vbox', align: 'stretch'
                    	},
                    	width:670,
                    	border: false,
	    	            bodyBorder: false,
        				bodyCssClass: 'x-citewrite-panel-body',
                    	height:30,
                    	count:0,
                    	fieldsPanelNumber:fieldsPanel.count,
                    	items: []
                    });
					
			        Ext.apply(this, Ext.apply(this.initialConfig, 
			        {
			        	title: 'Fields',
			            collapsible: false,
			            region:'center',
			            margins: '2 0 0 0',
			    		buttonAlign: 'left',
						bodyCssClass: 'x-citewrite-panel-body',
			    		autoScroll:true,
			    		heigth:100,
			            items: [fieldRows]	
			        }));
			        
			        FieldsPanel.superclass.initComponent.apply(this, arguments); 

			    	if((this.reportFields == null) || (this.reportFields.length == 0)){
			    		fieldRows.insert(fieldRows.items.items.length, new FieldsRowPanel({fieldStore: this.store, fieldsPanelNumber:fieldsPanel.count, type: fieldsPanel.type}));
			    		fieldRows.doLayout();
			    					    		
			    	}else{
			    		for(var i=0; i< this.reportFields.length; i++){ 
			    			var fieldsRowPanel = new FieldsRowPanel({fieldStore: this.store, count: fieldRows.count, fieldsPanelNumber:fieldsPanel.count, type: fieldsPanel.type});
			    			fieldRows.insert(fieldRows.items.items.length, fieldsRowPanel);
			    			
			    			var field = Ext.getCmp('id_fieldsPanel' + fieldsPanel.count + '_field_' + fieldRows.count);			        		
			    			field.setValue(this.reportFields[i].name);
			    			
			    			var view = Ext.getCmp('id_fieldsPanel' + fieldsPanel.count + '_view_' + fieldRows.count);			        		
			    			view.setValue(this.reportFields[i].view);
			    			
			    			var exports = Ext.getCmp('id_fieldsPanel' + fieldsPanel.count + '_export_' + fieldRows.count);		
			    			exports.setValue(this.reportFields[i]['export']);
			    			
			    			fieldRows.count = fieldRows.count + 1;
			    	}
			    		fieldRows.height = this.reportFields.length * 30;
			    		fieldRows.doLayout();
			    		 
			    	}
			    	
			    }
			});
			
			
			CriteriaPanel = Ext.extend(Ext.Panel, {
				type: null,
				report: null,
				store: null,
				count:0,
				reportCriterias:null,
				initComponent: function()
			    {
					var criteriaPanel = this;	
					
					var criteriaRows= new Ext.Panel({
	                    	xtype:'panel',
	                    	layout: {
	                    		 type: 'vbox', align: 'stretch'
	                    	},
	                    	autoWidth:false,
	                    	width:760,
	                    	border: false,
		    	            bodyBorder: false,
	        				bodyCssClass: 'x-citewrite-panel-body',
	                    	height:35,
	                    	count:0,
	                    	criteriaPanelNumber:criteriaPanel.count,
	                    	items: []                 	
	                });
					
				
					var criteriaShow= new Ext.Panel({
	                    	xtype:'panel',
	                    	autoWidth:false,
	                    	border: false,
		    	            bodyBorder: false,
	        				bodyCssClass: 'x-citewrite-panel-body',
	                    	height:25,
	                    	count:0,
	                    	criteriaPanelNumber:criteriaPanel.count,    	
	                    	items: [{
	                    	    xtype: 'container',
	                    	    items:[{
		                    	    xtype:'button',
									handler: function(){
									
										if(criteriaRows.isVisible( )) // Is show
										{
											criteriaRows.hide();
											flagCriteria = false;
											this.setText("ADD");
											criteriaRows.removeAll(true);
											criteriaRows.doLayout();              	                		
										} 
										else 
										{
											if(!(criteriaRows.items.items.length)){
												criteriaRows.insert(criteriaRows.items.items.length, new CriteriaRowPanel({criteriaStore: criteriaPanel.store, criteriaPanelNumber:criteriaPanel.count, type: criteriaPanel.type}));		      	
				        						criteriaRows.setHeight(25+30);
				        						criteriaRows.doLayout();
											}

											flagCriteria = true;
											this.setText("CLEAR");
											criteriaRows.show();
										}
									},
									text: ((this.reportCriterias != undefined  && this.reportCriterias.length != 0)?'CLEAR':'ADD')
										}
	                    		 ]	                    	
					 		 }]	
					 }); 

			        Ext.apply(this, Ext.apply(this.initialConfig, 
			        {
			        	title: 'Criteria',
			            region: 'south',
			            height: 155,
					    cmargins: '5 0 0 0',
					    collapsible: true,
					    split: true,
						bodyCssClass: 'x-citewrite-panel-body',					
						hidden: false,
	     	    		autoScroll:true,
			            items: [criteriaShow, criteriaRows]
			        	
			        })	        
			        );
			        			        
			        CriteriaPanel.superclass.initComponent.apply(this, arguments); 
		        
					if((this.reportCriterias == null) || (this.reportCriterias.length == 0) || (this.reportCriterias.toString() == [null])){
						
													
			        	criteriaRows.insert(criteriaRows.items.items.length, new CriteriaRowPanel({criteriaStore: this.store, criteriaPanelNumber:criteriaPanel.count, type: criteriaPanel.type}));		      	
			        	criteriaRows.doLayout();
			        	
			        	criteriaRows.hide();
			        	flagCriteria = false;
			        	
			        }else{
			        	
			        	
			        	//Ext.get('linkElement').update('new value');
			        	//Ext.getCmp("linkElement").autoEl.html="new value";
			      
			        		for(var i=0; i< this.reportCriterias.length; i++){ 
				        		var criteriaRowPanel = new CriteriaRowPanel({criteriaStore: this.store, count: criteriaRows.count, criteriaPanelNumber:criteriaPanel.count,  type: criteriaPanel.type});
				        		criteriaRows.insert(criteriaRows.items.items.length, criteriaRowPanel);
				        		
				        		var field = Ext.getCmp('id_criteriaPanel' + criteriaPanel.count + '_field_' + criteriaRows.count);			        		
				        		field.setValue(this.reportCriterias[i].name);
				        		var index = Ext.StoreMgr.lookup(field.store).findExact('name',this.reportCriterias[i].name);
				        		var record = field.store.getAt(index); 
				        		field.fireEvent('select', field, record);
				        		
				        		var value = Ext.getCmp('id_criteriaPanel' + criteriaPanel.count + '_value_' + criteriaRows.count); 
				        		value.setValue(this.reportCriterias[i].value);
				        		
				        		var operator = Ext.getCmp('id_criteriaPanel' + criteriaPanel.count + '_operator_' + criteriaRows.count); 
				        		operator.setValue(this.reportCriterias[i].operator);
				        		
				        		if(this.reportCriterias[i].operator == "is not null"  || this.reportCriterias[i].operator == "is null"){
				        			value.setDisabled(true); 
				        		}

				        		var logicOperator = Ext.getCmp('id_criteriaPanel' + criteriaPanel.count + '_logicOperator_' + criteriaRows.count);  
				        		logicOperator.setValue(this.reportCriterias[i].logicOperator);
				        		
				        		criteriaRows.count = criteriaRows.count + 1;
			        	    }
				        	criteriaRows.height = this.reportCriterias.length * 30;
				        	criteriaRows.doLayout();
				        	flagCriteria = true;
			        	 
			        }
					
					 			        
			    }
			});
			
			FieldsRowPanel = Ext.extend(Ext.Panel, {
				fieldStore: null,
				count:0,
				fieldsPanelNumber:0,
				type: null,
	            initComponent: function()
			    {
	            	var fieldsRowPanel = this;
	            	
	            	var combo = {
						xtype: 'combo',
						id: 'id_fieldsPanel' + this.fieldsPanelNumber + '_field_' + this.count,
						name: 'label',
						hiddenName: 'field',
						valueField: 'name',
						mode: 'local',
						displayField: 'label',
						lazyRender: false,
						store: this.fieldStore,
						editable: false,
					    triggerAction: 'all',
						margins: '0 15 0 0',
						width: 165,
				   };
	            	
	            	if ((fieldsRowPanel.type == FINANCIA_CITATIONL__TYPE) || (fieldsRowPanel.type == FINANCIA_PERMIT__TYPE)){
						combo.tpl = new Ext.XTemplate(
						    '<tpl for=".">',
						    '<tpl if="this.group != values.group">',
						    '<tpl exec="this.group = values.group"></tpl>',
						    '<div class="x-panel-header">{group}</div>',
						    '</tpl>',
						    '<div class="x-combo-list-item">{label}</div>',
						    '</tpl>'
						);
	            	}
	            	
	            	var config = 
	        		{
	            		layout:'hbox',
	            		bodyCssClass: 'x-citewrite-panel-body',
	        			border: false,
	    	            bodyBorder: false,
	    	            padding:3,
	    	            width: '100%',   
	    	            items: [{
								xtype: 'hidden',
								name:'fieldOrder',
								id: 'fieldsPanel' + this.fieldsPanelNumber + '_order_' + this.count,
								value: 0
							},
							new Ext.form.ComboBox(combo),
					       {
					        	xtype: 'checkbox',
					        	name:'view',
					        	id:'id_fieldsPanel' + this.fieldsPanelNumber + '_view_' + this.count,
					        	boxLabel: 'View',
					        	bodyBorder : false,
					        	hideLabel: true,
					        	padding:'10px',
					        	bodyStyle: 'margins:20px 0px 10px 0px',
					        	margins: '0 15 0 0',
					        	checked:false
					        },
					        {
					        	xtype: 'checkbox',
					        	name:'export',
					        	id:'id_fieldsPanel' + this.fieldsPanelNumber + '_export_' + this.count,
					        	boxLabel: 'Export',
					        	bodyBorder : false,
					        	hideLabel: true,
					        	padding:'10px',
					        	bodyStyle: 'margins:20px 0px 10px 0px',
					        	margins: '0 15 0 0',
					        	checked:false
					        },
					        {
					        	xtype: 'box',
					        	height:15,
					        	width:15,
					        	padding:'10px',
					        	cls: 'report-tool-btn report-option-up',
					        	margins: '3 1 0 0',
					        	listeners: {
					                render: function(c){
					                    var el = c.getEl();
					                    el.on({click: function (){  
					                    
					                    	var box = Ext.getCmp(this.id);
					                        var panel = box.ownerCt;
					                        var form = panel.ownerCt;
					                        
					                        if(form.items.keys.length>1){
					                        	
					                        	var position = fieldsRowPanel.getPosition(panel, form);
												
											    if(position != 0){
											    	form.items.items[position] = form.items.items[position-1];
											    	form.items.items[position-1] = panel;
											    }
											    
											    form.doLayout();
					                        }

					                    }});	             
					                }
					        	}
					        },
					        {
					        	xtype: 'box',
					        	height:15,
					        	width:15,
					        	padding:'10px',
					        	cls: 'report-tool-btn report-option-down',
					        	margins: '3 1 0 0',
					        	listeners: {
					                render: function(c){
					                    var el = c.getEl();
					                    el.on({click: function (){  
					                    
					                    	var box = Ext.getCmp(this.id);
					                        var panel = box.ownerCt;
					                        var form = panel.ownerCt;
					                        
					                        if(form.items.keys.length>1){
					                        	
					                        	var position = fieldsRowPanel.getPosition(panel, form);
												
											    if(position != form.items.items.length-1){
											    	form.items.items[position] = form.items.items[position+1];
											    	form.items.items[position+1] = panel;
											    }
											    
											    form.doLayout();
					                        }

					                    }});	             
					                }
					        	}
					        },
					        {
					        	xtype: 'box',
					        	height:15,
					        	width:15,
					        	padding:'10px',
					        	cls: 'report-tool-btn report-option-add',
					        	margins: '3 1 0 0',
					        	listeners: {
					                render: function(c){
					                    var el = c.getEl();
					                    el.on({click: function (){  
					                        var box = Ext.getCmp(this.id);
					                        var panel = box.ownerCt;
					                        var form = panel.ownerCt;
					                        var position = fieldsRowPanel.getPosition(panel, form);
					                        form.count = form.count + 1;
					                        
					                        form.insert(position+1, new FieldsRowPanel({fieldStore: fieldsRowPanel.fieldStore, count: form.count, fieldsPanelNumber:form.fieldsPanelNumber, type:fieldsRowPanel.type}));
					                        form.setHeight(form.getHeight() + 30);
					                        form.doLayout();
					                        
					                    }});	             
					                }
					        	}
					        },
					        {
					        	xtype: 'box',
					        	height:15,
					        	width:15,
					        	padding:'10px',
					        	cls: 'report-tool-btn report-option-remove',
					        	margins: '3 1 0 0',
					        	listeners: {
					                render: function(c){
					                    var el = c.getEl();
					                    el.on({click: function (){  
					                        var box = Ext.getCmp(this.id);
					                        var panel = box.ownerCt;
					                        var form = panel.ownerCt;
					                        if(form.items.items.length > 1){
					                        	form.remove(panel);
						                        form.setHeight(form.getHeight() - 30);
						                        form.doLayout();
					                        }
					                    }});	             
					                }
					        	}
					        }]
	        			
	        		};

	            	Ext.apply(this, Ext.apply(this.initialConfig, config));
	            	FieldsRowPanel.superclass.initComponent.apply(this, arguments);	
			    },
			    getPosition: function(panel,form)
				{
			    	var position = 0;
                    for (var i = 0; i < form.items.items.length; i++) {
						if(panel.id == form.items.items[i].id){
							position = i;
							break;
						}
					}
                    
			    	return position;
				}
			});
			
			CriteriaRowPanel = Ext.extend(Ext.Panel, {
				count:0,
				criteriaPanelNumber:0,
				criteriaStore: null,
				type: null,
				logicOperatorByHaving : false,
	            initComponent: function()
			    {
	            	
	            	var criteriaRowPanel = this;
	            	
					var combo = {
						xtype: 'combo',
						id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_field_' + this.count,
						hiddenName: 'criteria',
						valueField: 'name',
						displayField: 'label',
						lazyRender: false,
						store: this.criteriaStore,
						mode: 'local',
						editable: false,
						triggerAction: 'all',
						margins: '0 15 0 0',
						width: 150,
						listeners: {
							select: function(combo, record, index)
							{
								if(record != undefined){

									var addBetweennRecord = false;
									var logicOperatorByHaving = false;
									
									var panelValue = Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_panelValue_' + criteriaRowPanel.count);
									panelValue.removeAll(true);
		 
									if((record.json.type == "list")||(record.json.type == "codes")||(record.json.type == "database")){
										
										var store = new Ext.data.JsonStore({
									        autoDestroy: true,
									        fields: ['id', 'name'],
									        data : record.json.options
									    });	
										
										var combo = new Ext.form.ComboBox({
										   id: 'id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_value_' + criteriaRowPanel.count,
										   xtype: 'combo',
										   hiddenName: 'value',
										   margins: '0 15 0 0',
										   submitValue: true,
										   width: 150,
										   lazyRender: false,
										   valueField: 'id',
										   displayField: 'name',
										   store: store,
										   forceSelection: true,
										   editable: false,
										   mode: 'local'
										});
										
										panelValue.add(combo);
		
									}else if((record.json.name == "cite-date_time")||(record.json.name == "cite-violation_start")||(record.json.name == "cite-violation_end") || (record.json.name == "invoice-refund_date")){
										    addBetweennRecord = true;		
											var datefield = new Ext.form.DateField ({
											   id: 'id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_value_' + criteriaRowPanel.count,
											   xtype: 'datefield',
									    	   name: 'value',
											   width: 150,
											   editable: false,
									    	   hideLabel: true
											});
											panelValue.add(datefield);
											
									}else{
										
										if(record.json.name == "attrFina-n5rec"){
											logicOperatorByHaving = true;
										}
		
										var textField = {
												id: 'id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_value_' + criteriaRowPanel.count,
												xtype: 'textfield',
										    	name: 'value',
												margins: '0 15 0 0',
												width: 150,
												hideLabel: false
										};	
										
										if(record.json.validation != undefined && record.json.validation != ""){
											textField.maskRe = new RegExp(record.json.validation);
										}
										panelValue.add(new Ext.form.TextField(textField));
																		
									}
								
								var op = Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_operator_' + criteriaRowPanel.count);
								op.setValue("=");
								Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_betweenField_' + criteriaRowPanel.count).disable();
								var str = op.getStore();
								
								if(logicOperatorByHaving){
									
									var recordDelete = [];
									
									for(var i=0; i<str.data.items.length; i++){
										if((str.data.items[i].data.id == "like") || (str.data.items[i].data.id == "!=") || (str.data.items[i].data.id == "is not null") || (str.data.items[i].data.id == "is null")){
											recordDelete.push(str.data.items[i]);
										}
									}
	
									for(var i=0; i<recordDelete.length; i++){
										str.remove(recordDelete[i]);
									}
									
									criteriaRowPanel.logicOperatorByHaving = true;
									
								}else {
									if(criteriaRowPanel.logicOperatorByHaving){
										
										var recordDelete = [];
										
										for(var i=0; i<str.data.items.length; i++){
											recordDelete.push(str.data.items[i]);
										}
		
										for(var i=0; i<recordDelete.length; i++){
											str.remove(recordDelete[i]);
										}
										
										var equal = {
											id: '=',
											op: '='	 
										};
										 
										var notEqual = {
											id: '!=',
											op: '!='
										};
										 
										var more  = {
											id: '>=',
											op: '>='
										};
										
										var less = {
											id: '<=',
											op: '<='		
										};
										 
										var like = {
											id: 'like',
											op: 'LIKE'
										};
											
										var notNull = {
											id: 'is not null',
											op: 'IS NOT NULL'
										};
											
										var isNull = {
											id: 'is null',
											op: 'IS NULL'
										};
										
										str.add(new str.recordType(equal, Ext.id()));
										str.add(new str.recordType(notEqual, Ext.id()));
										str.add(new str.recordType(more, Ext.id()));
										str.add(new str.recordType(less, Ext.id()));
										str.add(new str.recordType(like, Ext.id()));
										str.add(new str.recordType(notNull, Ext.id()));
										str.add(new str.recordType(isNull, Ext.id()));
										
										criteriaRowPanel.logicOperatorByHaving = false;
									}
									
								}
								
								if(addBetweennRecord){
									
									var betweennData = {
										id: 'between',
										op: 'BETWEEN'
									};
										
									var NotbetweennData = {
										id: 'not between',
										op: 'NOT BETWEEN'
									};
									
									str.add(new str.recordType(betweennData, Ext.id()));
									str.add(new str.recordType(NotbetweennData, Ext.id()));
									
								}else{
									
									var recordDelete = [];
	
									for(var i=0; i<str.data.items.length; i++){
										if((str.data.items[i].data.id == "between")||(str.data.items[i].data.id == "not between")){
											recordDelete.push(str.data.items[i]);
										}
									}
	
									for(var i=0; i<recordDelete.length; i++){
										str.remove(recordDelete[i]);
									}
								}
								
							   panelValue.doLayout();
							   criteriaRowPanel.doLayout();
							}
							}
						}
					};
							
					if ((criteriaRowPanel.type == FINANCIA_CITATIONL__TYPE) || (criteriaRowPanel.type == FINANCIA_PERMIT__TYPE)){
						combo.tpl = new Ext.XTemplate(
					        '<tpl for=".">',
						    '<tpl if="this.group != values.group">',
						    '<tpl exec="this.group = values.group"></tpl>',
						    '<div class="x-panel-header">{group}</div>',
						    '</tpl>',
						    '<div class="x-combo-list-item">{label}</div>',
						    '</tpl>'
						);
					}
	
	            	var config = 
	        		{
	            		layout:'hbox',
	            		bodyCssClass: 'x-citewrite-panel-body',
	        			border: false,
	    	            bodyBorder: false,
	    	            padding:3,  
	    	            width:'100%',
	    	            items: [{
								xtype: 'hidden',
								name:'criteriaOrder',
								id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_order_' + this.count,
								value: 0
							},     
							new Ext.form.ComboBox(combo),
					       {
					    	   xtype: 'combo',
					    	   id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_operator_' + this.count,
					    	   hiddenName: 'op',
					    	   margins: '0 15 0 0',
					    	   submitValue: true,
				               width: 120,
							   lazyRender: false,
							   forceSelection: true,
							   editable: false,
							   triggerAction: 'all',
							   store: new Ext.data.ArrayStore({
							        autoDestroy: true,
							        fields: ['id', 'op'],
							        data : [
							            ['=', '='],
							            ['!=', '!='],
							            ['>=', '>='],
							            ['<=', '<='],
							            ['like', 'LIKE'],
							            ['is not null', 'IS NOT NULL'],
							            ['is null', 'IS NULL'],
							        ]
							    }),
							    value: '=',
							    displayField: 'op',
							    valueField: 'id',
								forceSelection: true,
								mode: 'local',
								listeners: {
									select: function(combo, record, index)
									{
										if((record.data.id != 'between') && (record.data.id != 'not between')){
											Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_betweenField_' + criteriaRowPanel.count).disable();
										}else{
											Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber + '_betweenField_' + criteriaRowPanel.count).enable();
										}
										
										if((record.data.id != 'is not null') && (record.data.id != 'is null')){
											Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber +  '_value_' + criteriaRowPanel.count).enable();
											
										}else{
											Ext.getCmp('id_criteriaPanel' + criteriaRowPanel.criteriaPanelNumber +  '_value_' + criteriaRowPanel.count).disable();
										}
									}
								}
					       },
					       {
					    	   xtype: 'panel',
					    	   layout: 'fit',
					    	   id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_panelValue_' + this.count,
					    	   width: 150,
							   margins: '0 15 0 0',
							   autoHeight: true,
					    	   items: [{
										xtype: 'textfield',
								    	name: 'value',
										id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_value_' + this.count,
							        	width: 150,
							        	hideLabel: false
						    	  		}]
					       },
					       {
								xtype: 'datefield',
								name: 'betweenValue',
								id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_betweenField_' + this.count,
								width: 150,
								margins: '0 15 0 0',
								editable: false,
								hideLabel: true,
								disabled:true

						   },
					       {
					    	   xtype: 'combo',
					    	   id: 'id_criteriaPanel' + this.criteriaPanelNumber + '_logicOperator_' + this.count,
					    	   hiddenName: 'logicOperator',
					    	   margins: '0 15 0 0',
					    	   submitValue: true,
				               width: 120,
							   lazyRender: false,
							   store: new Ext.data.ArrayStore({
							        autoDestroy: true,
							        fields: ['id', 'op'],
							        data : [
							            ['done', 'DONE'],
							            ['and', 'AND'],
							            ['or', 'OR']
							        ]
							    }),
							    value:['done'],
							    displayField: 'op',
							    valueField: 'id',
							    allowBlank: false,
							    editable: false,
							    triggerAction: 'all',
								forceSelection: true,
								listeners: {
									select: function(combo, record, index)
									{
										var combo = this;
				                        var panel = combo.ownerCt;
				                        var form = panel.ownerCt;
										
										if(record.data.id == "done"){
											var remove = false;
											var position = criteriaRowPanel.getPosition(panel, form);

											while (form.items.items.length > position+1){
												form.remove(form.items.items[position+1]);
												form.setHeight(form.getHeight() - 30);
											}
											form.doLayout();
										}else{
											var position = criteriaRowPanel.getPosition(panel, form);
											if(position == form.items.items.length-1){
												form.count = form.count + 1;

												form.insert(form.items.items.length, new CriteriaRowPanel({criteriaStore:criteriaRowPanel.criteriaStore, count: form.count, criteriaPanelNumber:form.criteriaPanelNumber, type: criteriaRowPanel.type }));
						                        form.setHeight(form.getHeight() + 30);
						                        form.doLayout(); 
											}	
										}
									}
								},
								mode: 'local'
					       }]
	        		};

	            	Ext.apply(this, Ext.apply(this.initialConfig, config));
	            	CriteriaRowPanel.superclass.initComponent.apply(this, arguments);	
			    },
			    getPosition: function(panel,form)
				{
			    	var position = 0;
                    for (var i = 0; i < form.items.items.length; i++) {
						if(panel.id == form.items.items[i].id){
							position = i;
							break;
						}
					}
                    
			    	return position;
				}
			});
			
			ReportEngineReport = Ext.extend(Ext.grid.GridPanel, 
			{
				report: null,
				plugins: new Ext.ux.GridTotals(),
				summaryStore: null,
				
			    initComponent: function()
			    {
			    	
			    	var columns = [];
			    	var fields = [];
			    	
			    	for (var i=0; i < this.report.reportFields.length; i++){	
			    		var column = this.report.reportFields [i];
			    		if(!column.view){continue;}
			    		var dataIndex = column.name;
		    			var index = dataIndex.indexOf('-');
		    			
		    			if(index > 0)
		    			{
		    				dataIndex = column.name.substr(index+1);
		    				if(dataIndex == "date_time")
		    				{
		    					dataIndex = "citation_date";
		    				}
		    				fields.push({name : dataIndex});
		    			}
		    			
		    			if((column.name == "attrFina-n2rec") || (column.name == "cite-violation_amount") || (column.name == "attrFina-n6rec") || (column.name == "attrFina-n5rec") || (column.name == "invoice-amount")){
		    				columns.push({header: column.label, dataIndex: dataIndex, summaryType: 'sumServer'});
		    			}else{
		    				columns.push({header: column.label, dataIndex: dataIndex});
		    			}
		    			
		    			
			    	}
			    	
			    	this.summaryStore = new Ext.data.JsonStore({
						fields: fields
				    });
			    		
			    	this.store = new Ext.data.JsonStore({
			    	    // store configs
			    	    autoDestroy: true,
			    	    url: _contextPath + '/report/run',
			    	    remoteSort: true,
			    	    autoLoad: true,
			    	    loadMask: true,
			    	    sortInfo:
			    	    {
			    	        field: (this.report.report_type == CITATION_TYPE || this.report.report_type == FINANCIA_CITATIONL__TYPE)  ? 'citation_number' : 'permit_number',
			    	        direction: 'ASC'
			    	    },
			    	    idProperty: 'id',
			    	    root: 'data',
			    		totalProperty:'count',
			    		fields:fields,
			    	    baseParams: {report_id: this.report.report_id, report_type: this.report.report_type},
						autoLoad: {params:{start:0, limit: pageLimit}}
			    	});
			    	
			    	var object = this;
			        Ext.apply(this, 
			        {
			        	height: '100%',
			        	closable: true,
			        	autoDestroy: true,
			            loadMask: {msg:'Loading Report...'},
			            viewConfig: 
			            {
			                forceFit:true,
			                enableRowBody:true,
			                showPreview:false,
			                getRowClass : this.applyRowClass
			            },
			            colModel: new Ext.grid.ColumnModel({
			                defaults: {
			                    sortable: false
			                },
			                columns: columns
			            }),
			            bbar:
			        	{
			        		xtype:'paging',
			        		store: this.store,
			        		displayInfo:true,
			        		pageSize: pageLimit,
				            displayMsg: 'Displaying items {0} - {1} of {2}',
				            emptyMsg: "No items to display",
			        		items: ['-', {
				                text: 'Export',
				                cls: 'x-btn-text details',
				                handler: 	function(btn, event)
				                			{ 
				                				var store = this.ownerCt.store;
				                				var sort = store.getSortState();
				                				
				                				var exportFields = false;
				                				for(var i=0; i<object.report.reportFields.length; i++){
				                					exportFields = exportFields | object.report.reportFields [i]['export'];
				                				}
				                				
				                				if(!exportFields){
				                					 Ext.Msg.show({
				                						   title:'Error!',
				                						   msg: 'No fields selected for export.',
				                						   buttons: Ext.Msg.OK,
				                						   icon: Ext.MessageBox.ERROR
				                						});
				                				}else{
				                					window.location.href = _contextPath + "/report/export?report_id="+object.report.report_id+"&sort="+sort.field+"&dir="+sort.direction;	
				                				}
				                				
				                			}
				                }]
			        	}   
			        });
			        ReportEngineReport.superclass.initComponent.apply(this, arguments);
			    }
			});
		});//end onclick
	}//end if adminReports

});