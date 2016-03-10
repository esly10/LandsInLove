CitationPaymentPlanPanel = Ext.extend(Ext.Panel, {
		citation: null,
		initComponent: function()
	    {
			// create the Data Store
		    gridStore = new Ext.data.JsonStore({
				url: _contextPath + '/citation/listPaymentPlan',
				baseParams: {citation_id: this.citation_id },
				root: 'payment_plan',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'payment_plan_id',
		            'citation_id', 
		            'amount',
		            'frequency',
		            'status',
		            'count',
		            'paid',
		            {name:'date', type: 'date', dateFormat:'Y-m-d'}		            	          
		        ],
				sortInfo: {
					field: 'date',
					direction: 'DESC'
				},
				autoLoad: {params:{start:0, limit: 100}}
		    });
		    
			var panel = this;
			var configPaymenPlan = 
			{
				xtype: 'panel',
			    title: 'Payment Plan',
			    id: 'citationtab-paymentplan-' + this.citation_id,
			    padding: 5,
			    bodyCssClass: 'x-citewrite-panel-body',
			    autoScroll: true,
			    buttonAlign: 'left',
			    items: [this.loadPaymentPlanInfo(this, 0), this.loadPaymentPlanGrid(this, gridStore)], //
			    buttons:  [{
			    	xtype:'button',
			    	text: 'Add',
					handler: function(){
						
						var form = Ext.getCmp('paymentPlanForm');
						var data = form.getForm().getFieldValues();
						data.date = Ext.getCmp("date").value;
						
						var msg = "";
						if(data.amount == ""){
							msg=msg+'* Amount Date is required. <br>';
						}
						if(data.frequency == ""){
							msg=msg+'* Frequency Date is required. <br>';
						}
						if(data.date == ""){
							msg=msg+'* Date Date is required. <br>';
						}
						
						if(msg != ""){
							 Ext.Msg.show({
	          	    			   title:'Error',
	          	    			   msg: msg,
	          	    			   buttons: Ext.Msg.OK,
	          	    			   icon: Ext.MessageBox.ERROR
	          	    		});
							 
							return false;
						}
							
						Ext.Ajax.request({
							   url: _contextPath + '/citation/savePaymetPlan',
							   success: function(response, opts){
								   var response = Ext.decode(response.responseText);
			            	    	if(response.success)
			            	    	{			            	    
			            	    		var grid = Ext.getCmp('gridPaymentPlan');
			            	    		grid.getStore().load();
										Ext.growl.message('Success!', 'Payment Plan has been saved.');
										Ext.getCmp('amount').setValue();
										Ext.getCmp('citation-frequency').setValue("");
										Ext.getCmp('count').setValue(1);
										Ext.getCmp('date').setValue(Ext.util.Format.date(new Date(), 'Y-m-d'));										
			            	    	}
							   },
							   failure: function(response, opts){
								   Ext.Msg.show({
									   title:'Error!',
									   msg: 'Error saving Payment Plan.',
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   },
							   params: data
							});
						
					},
				}]
			};
			
			Ext.apply(this, Ext.apply(this.initialConfig, configPaymenPlan));
	        
			CitationPaymentPlanPanel.superclass.initComponent.apply(this, arguments);
	    },
	    editPaymentPlan: function (data){
	    	var panel = this;
	    	var paymetPlanWindow = new Ext.Window({
	            renderTo: document.body,
	            title: 'Edit Payment Plan',
	            plain: true,
	            resizable: true,
	            autoScroll: true,
	            modal: true,
	            id: 'editcitationPaymentPlanWindow',
	            items: panel.loadPaymentPlanInfo(data,1),
	            buttons: [{
	                text:'Save',
	                handler: function()
	                {   
	                	//validate form
	                	var formPanel = Ext.getCmp("editpaymentPlanForm");
	                	formPanel.getForm().submit({
	                	    url: _contextPath + '/citation/savePaymetPlan',
	                	    scope: this,
	                	    params: {xaction: 'save', citation_id: panel.citation_id },
	                	    success: function(form, action) {
	                	    	
	                	    	var store = Ext.getCmp("gridPaymentPlan").getStore();
				            	store.reload(); 
	                	    	var parent = action.options.scope.findParentByType('window'); 
	                	    	parent.close();
	                	       
	                	    	Ext.growl.message('Success', 'Payment Plan has been saved.');
	                	    },
	                	    failure: function(form, action) {
	                	        switch (action.failureType) {
	                	            case Ext.form.Action.CLIENT_INVALID:
	                	                Ext.Msg.show({
                	                	   title:'Error',
                	                	   msg: 'Please enter a note.',
                	                	   buttons: Ext.Msg.OK,
                	                	   icon: Ext.MessageBox.ERROR
                	                	});
	                	                break;
	                	            case Ext.form.Action.CONNECT_FAILURE:
	                	                Ext.Msg.show({
                	                	   title:'Failure',
                	                	   msg: 'Ajax communication failed.',
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
				width: 320,
                height: 290,
				listeners : {}
	        });
	    	
	    	paymetPlanWindow.show();
	    	
	    	Ext.getCmp('editpayment_plan_id').setValue(data.payment_plan_id);
			Ext.getCmp('editamount').setValue(data.amount);
			Ext.getCmp('editcitation-frequency').setValue(data.frequency);
			Ext.getCmp('editcitation-status').setValue(data.status);
			Ext.getCmp('editcount').setValue(data.count);
			Ext.getCmp('editdate').setValue(Ext.util.Format.date(data.date, 'Y-m-d'));
			
	    	paymetPlanWindow.center();
	    },
	    showContextMenu: function(grid, index, event)
		{
			event.stopEvent();
			var panel = this;
			var record = grid.getStore().getAt(index);
			
			var itemsPayment; 
			itemsPayment = new Array({
				text: 'Edit',
				handler: function() 
				{
					event.stopEvent();								
					panel.editPaymentPlan(record.data);
				}
			});
			if(hasPermission(PL_CITATION_MANAGE) || (hasPermission(PL_OWNER_MANAGE) && panel.owner.owner_id > 0))
			{
				itemsPayment.push({
					text: 'Delete',
					handler: function() 
					{
						  Ext.Msg.confirm("Delete?", "Delete Payment Plan ?", function(bid, p2){
						  if(bid == "yes")
						  {
							  Ext.Ajax.request({
								   url: _contextPath + '/citation/deletePaymentPlan',
								   params: { payment_plan_id: record.data.payment_plan_id }, 
								   success: function(p1, p2)
								   {
									   var response = Ext.decode(p1.responseText);
									   if(response.success)
									   {
										   var store = Ext.getCmp("gridPaymentPlan").getStore();
							            	store.reload(); 
										   Ext.growl.message('Success', 'Payment Plan has been deleted.');
										   
									   }
									   else
									   {
										   Ext.Msg.show({
											   title:'Failure',
											   msg: response.msg,
											   buttons: Ext.Msg.OK,
											   icon: Ext.MessageBox.ERROR
											});
									   }
								   },
								   failure: function(){
									   Ext.Msg.show({
										   title:'Failure',
										   msg: 'Error deleting the Payment Plan.',
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
									   },
									   scope: this
									}); 
							  }
						  });
					}
				});

			}
			
			new Ext.menu.Menu(
			{
				items: itemsPayment,
				autoDestroy: true
			}).showAt(event.xy);
				
		},
	    loadPaymentPlanGrid: function(citation, gridStore){
	    	var panel = this;
	    	   var columnModel = new Ext.grid.ColumnModel({
			        defaults: { sortable: true },
			        columns:[
			                   {header: 'Amount', sortable: true, dataIndex: 'amount', 
			                	renderer: function(value) 
			                		{
			                			return Ext.util.Format.number(value, '0.00');
			                		}
			                   },
					           {header: 'Frequency', sortable: true, dataIndex: 'frequency'},	
					           {header: 'Date', sortable: true, dataIndex: 'date', renderer: function(value){ return Ext.util.Format.date(value, 'Y-m-d'); }},
					           {header: 'Status', sortable: true, dataIndex: 'status'},
					           {header: 'Number of Payments', sortable: true, dataIndex: 'count'},
					           {header: 'Number of Payments Paid', sortable: true, dataIndex: 'paid'}
			                ]
			        });
			    
	    	    grid = this;	
	    	    
	    	    var toolbar = {
	    	            pageSize: 100,
	    	            store: gridStore,
	    	            displayInfo: true,
	    	            displayMsg: 'Displaying Payment Plans {0} - {1} of {2}',
	    	            emptyMsg: "No Payment Plans to display"
	    	        };
	    	      
			    var gridPanel = {
			    	title: 'Payment plan List',
			    	xtype: 'grid',
			    	id:"gridPaymentPlan",
			    	//padding: '5 5 5 5',			    	
			        store: gridStore,
			        trackMouseOver:false,
			        disableSelection:false,
			        height:260,
			        frame: true,
			        border: true,			
			        // grid columns
			        colModel: columnModel,			
			        
			        // customize view config
			        viewConfig: { forceFit:true },			
			        // paging bar on the bottom
			        listeners: 
			        {
			        	'rowcontextmenu': this.showContextMenu,
						'rowdblclick': function(grid, index, event)
						{
							event.stopEvent();
							var record = grid.getStore().getAt(index);
							panel.editPaymentPlan(record.data);
						}, scope: this
			        },
			        bbar: new Ext.PagingToolbar(toolbar),
			        tbar: {
				    	xtype: 'toolbar',
				    	items: ['Filter: ',
				    	        {
				    				xtype: 'cleartrigger',
				    				emptyText:"Frequency or Status",
						            fieldLabel: 'Sample Trigger',
						            value: '',
						            enableKeyEvents: true,
						            listeners: {
						            	keyup: function(field, event)
						            	{
						            		if(event.keyCode == 13)
						            		{
							            		var filterValue = this.getValue();
							            		
								            	var store = this.findParentByType('grid').store;
								            	store.baseParams = {filter: filterValue, citation_id: grid.citation_id};
								            	store.load();
						            		}
						            	},
						            },
						            onTriggerClick: function() {
						            	this.setValue('');
						            	
						            	var store = this.findParentByType('grid').store;
						            	store.baseParams = {citation_id: grid.citation_id};
						            	store.load();
						            }
						        },
						        {
						            iconCls: 'x-tbar-loading'  
						            ,scope: this
						            ,handler: function(){
						            	var store = Ext.getCmp("gridPaymentPlan").getStore();
						            	store.reload(); 
						            }
						        } 
				    	        ]
				    },//top tool bar,
			        loadMask: true
			    };
			    
			    
			    return gridPanel;
	    },
	  
	    loadPaymentPlanInfo: function (citation, edit){
	    	if(edit){ edit="edit";}
	    	else { edit="";}
	    	
			return  {
					xtype: 'form',
					title: '',
					id:edit+"paymentPlanForm",
					bodyBorder: false,
					border: false,
					frame: false,
					autoScroll: true,
					bodyStyle: 'padding: 10px; ',
					bodyCssClass: 'x-citewrite-panel-body',
					buttonAlign: 'left',				
					items: [
							{
								xtype: 'hidden',
								name: 'payment_plan_id',
								id: edit+'payment_plan_id',
								value: 0
							},
							{
								xtype: 'hidden',
								name: 'citation_id',
								id: edit+'citation_id',
								value: citation.citation_id
							},
							/*{
								   xtype: 'combo',
								   id: 'citation-type ',
								   hiddenName: 'citation_type ',
								   fieldLabel: 'Type ',
								   submitValue: true,
								   width: 120,
								   lazyRender: false,
								   allowBlank: false,
								   store: new Ext.data.ArrayStore({
								   autoDestroy: true,
								   fields: ['id', 'value'],
								   data : [
								            ['payment_amount', 'Payment Amount'],
								            ['payments_number', 'Number of Payments']
								        ]
								    }),
								    value:'payment_amount',
								    displayField: 'value',
								    valueField: 'id',
									triggerAction: 'all',
									forceSelection: true,
									mode: 'local',
									listeners:{
										select: function(val, e)
										{}
									}
							},*/
							//payment amount or number of payments
							{
								xtype: 'numberfield',
								id: edit+'count',
								name: 'count',
								fieldLabel: 'Number of Payments',
								value: 1,
								width: 120,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								id: edit+'amount',
								name: 'amount',
								fieldLabel: 'Payment Amount',
								value: 0,
								width: 120,
								allowBlank: false,
								listeners:{keyup: function(field, e)
									{
									
									}
								}
							},
							{
							   xtype: 'combo',
							   id: edit+'citation-frequency',
							   hiddenName: 'frequency',
							   name: "frequency",
							   fieldLabel: 'Frequency',
							   submitValue: true,
							   width: 120,
							   lazyRender: false,
							   allowBlank: false,
							   store: new Ext.data.ArrayStore({
							   autoDestroy: true,
							   fields: ['id', 'value'],
							   data : [
							            ['weekly', 'weekly'],
							            ['monthly', 'monthly']
							        ]
							    }),
							    displayField: 'value',
							    valueField: 'id',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
							},
							{
								   xtype: 'combo',
								   id: edit+'citation-status',
								   hiddenName: 'status',
								   fieldLabel: 'Status',
								   submitValue: true,
								   width: 120,
								   lazyRender: false,
								   allowBlank: false,
								   store: new Ext.data.ArrayStore({
								   autoDestroy: true,
								   fields: ['id', 'value'],
								   data : [
								            ['Paid', 'Paid'],
								            ['No Paid', 'No Paid']
								        ]
								    }),
								    displayField: 'value',
								    valueField: 'id',
									triggerAction: 'all',
									forceSelection: true,
									mode: 'local',
									listeners:{

									}
							},
							{
						        xtype: 'datefield',
								id: edit+'date',
								name: 'date',
							    fieldLabel: 'Start Date',
								format: 'Y-m-d',
								width:120,
								allowBlank: false,
								value: Ext.util.Format.date(new Date(), 'Y-m-d')
						    }
						    
							],
							 buttons: []
				};
		
	    },
	    deletePaymentPlan: function(note_id)
	    {
	    	var panel = this;
	    	Ext.MessageBox.confirm("Delete Note?", 'Are you sure you want to delete this note?', function(p1, p2){
				if(p1 != 'no')
				{
					// Basic request
					Ext.Ajax.request({
					   url: _contextPath + '/citation/notes',
					   success: function(response, opts){
						   panel.load({url : _contextPath + '/citation/notes', scripts : true, params: {citation_id: panel.citation_id }});
						   Ext.growl.message('Success!', 'Note has been deleted.');
					   },
					   failure: function(response, opts){
						   Ext.Msg.show({
							   title:'Error!',
							   msg: 'Error deleting note.',
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR
							});
					   },
					   params: { note_id: note_id, citation_id: panel.citation_id, xaction: 'delete' }
					});
				}
			});
	    }
});


