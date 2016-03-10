InvoicePanel = Ext.extend(Ext.grid.GridPanel, {
		owner: null,
		pageLimit: 50,
		initComponent: function()
	    {
			if(this.owner == null)
			{
				this.owner = {owner_id: 0};
			}
			
			// create the Data Store
		    var store = new Ext.data.JsonStore({
				url: _contextPath + '/invoice/list',
				root: 'invoices',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'invoice_id',
		            'status',
		            'payment_method',
		            {name: 'type', type: 'int'},
		            'amount',
		            {name: 'create_date', type: 'date', dateFormat:'Y-m-dTH:i:s'},
		            {name: 'cc_type', type: 'int'},
		            'cc_type_name',
		            'cc_number',
		            'cc_exp_month',
		            'cc_exp_year'		            
		        ],
				sortInfo: {
					field: 'create_date',
					direction: 'DESC'
				},
				baseParams: {owner_id: this.owner.owner_id},
				autoLoad: {params:{start:0, limit: this.pageLimit}}
		    });

		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true }
		        ,columns:[{
		            header: "ID",
		            dataIndex: 'invoice_id',
		            width: 30
		        },{
		            header: "Date",
		            dataIndex: 'create_date',
		            renderer: function(value, form, record){ 
		            	if(typeof value != 'undefined'){
		            		return value.format('m/d/Y');
		            	} else {
		            		return "";
		            	}		            		
		            	
		            },
		            width: 50
		        },{
		            header: "Status",
		            dataIndex: 'status',
		            width: 50
		        },{
		            header: "Type",
		            dataIndex: 'type',
		            width: 50,
		            renderer: function(value){ 
		            	if(value == 1)
		            	{
		            		return "Permit";
		            	}
		            	
		            	return "Citation";
		            }
		        },{
		            header: "Method",
		            dataIndex: 'payment_method',
		            width: 50
		        },{
		            header: "Amount",
		            dataIndex: 'amount',
		            renderer: Ext.util.Format.usMoney,
		            width: 50
		        },{
		            header: "CC Type",
		            dataIndex: 'cc_type_name',
		            width: 50
		        },{
		            header: "Credit Card",
		            dataIndex: 'cc_number',
		            width: 100
		        },{
		            header: "Expiration",
		            dataIndex: 'cc_exp_month',
		            renderer: function(value, p1, record){ if(record.data.cc_exp_month == 0){ return '-'; } return record.data.cc_exp_month + "/" + record.data.cc_exp_year; },
		            width: 50
		        }]});
		    
		    
		    var grid = this;
		    var config = {
		    		title: 'Invoices',
		    		padding: '5 5 5 5',
		        store: store,
		        trackMouseOver:false,
		        disableSelection:false,
		        frame: false,
		        border: false,
		
		        // grid columns
		        colModel: columnModel,
		
		        // customize view config
		        viewConfig: { forceFit:true },
		
		        // paging bar on the bottom
		        tbar: {
			    	xtype: 'toolbar',
			    	items: ['Filter: ',
			    	        {
			    				xtype: 'cleartrigger',
					            fieldLabel: 'Sample Trigger',
					            value: '',
					            enableKeyEvents: true,
					            listeners: {
					            	keyup: function(field, event)
					            	{
					            		if(event.keyCode == 13)
					            		{
						            		var filterValue = this.getValue();
						            		
							            	var store = grid.store;
							            	store.baseParams = {filter: filterValue, owner_id: grid.owner.owner_id, limit: grid.pageLimit};
							            	store.load();
					            		}
					            	},
					            },
					            onTriggerClick: function() {
					            	this.setValue('');
					            	
					            	var store = grid.store;
					            	store.baseParams = {owner_id: grid.owner.owner_id, limit: grid.pageLimit};
					            	store.load();
					            }
					        }
			    	        ]
			    },
			    bbar: {
			    	xtype : 'paging',
			    	pageSize: grid.pageLimit,
			    	store: store,
			    	displayInfo: true,
			    	displayMsg: 'Displaying invoice {0} - {1} of {2}',
			    	emptyMsg: "No invoice to display"
			    },//top tool bar,
		        loadMask: true
		    };
		   
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			InvoicePanel.superclass.initComponent.apply(this, arguments);
			
			this.on('rowcontextmenu', this.showContextMenu);
			this.on('rowdblclick', this.viewInvoice);
	    },
	    doRefund: function(invoice_id, amount)
	    {
	    	var grid = this;
	    	
	    	var title = "Refund Invoice";
	    	var msg = "Please enter the amount to refund:";
	    	
	    	Ext.MessageBox.prompt(title, msg, function(p1, p2){
				if(p1 == 'ok')
				{
			    	Ext.Ajax.request({
						   url: _contextPath + '/invoice/doRefund',
						   success: function(response, opts){
							   var data = Ext.decode(response.responseText);
							   if(data.success)
							   {
								   grid.store.reload();
								   Ext.growl.message('Success!', 'Refund has been applied to the invoice.');
								   
								   Ext.getCmp('invoice-view-window').close();
							   }
							   else
							   {
								   Ext.Msg.show({
									   title:'Error!',
									   msg: data.msg,
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   }											   
						   },
						   failure: function(response, opts){
							   Ext.Msg.show({
								   title:'Error!',
								   msg: 'Error refunding.',
								   buttons: Ext.Msg.OK,
								   icon: Ext.MessageBox.ERROR
								});
						   },
						   params: { invoice_id: invoice_id, owner_id: grid.owner.owner_id, refund_amount: p2 }
						});
				}
	    	}, this, false, Ext.util.Format.number(amount, '0.00'));
	    },
	    doVoid: function(invoice_id)
	    {
	    	var grid = this;
	    	
	    	var title = "Void Invoice?";
		    var msg = "Are you sure you want to void this invoice?";
	    	
	    	Ext.MessageBox.confirm(title, msg, function(p1, p2){
				if(p1 != 'no')
				{
			    	Ext.Ajax.request({
						   url: _contextPath + '/invoice/doVoid',
						   success: function(response, opts){
							   var data = Ext.decode(response.responseText);
							   if(data.success)
							   {
								   grid.store.reload();
								   Ext.growl.message('Success!', 'Invoice has been voided.');
								   
								   Ext.getCmp('invoice-view-window').close();
							   }
							   else
							   {
								   Ext.Msg.show({
									   title:'Error!',
									   msg: data.msg,
									   buttons: Ext.Msg.OK,
									   icon: Ext.MessageBox.ERROR
									});
							   }											   
						   },
						   failure: function(response, opts){
							   Ext.Msg.show({
								   title:'Error!',
								   msg: 'Error voiding.',
								   buttons: Ext.Msg.OK,
								   icon: Ext.MessageBox.ERROR
								});
						   },
						   params: { invoice_id: invoice_id, owner_id: grid.owner.owner_id }
						});
				}
	    	});
	    },
	    showContextMenu: function(grid, index, event)
		{
			event.stopEvent();
			var items = new Array(
					{
						text: 'Details',
						handler: function() 
						{
							grid.viewInvoice(grid, index, event);
						}
			
					},
					{
						text: 'Print',
						handler: function() 
						{
							var menu = this.findParentByType('menu');
							menu.hide();
							
							var selection = grid.getStore().getAt(index);
							var invoice_id = selection.data.invoice_id;
						
							var print_frame = document.getElementById('print-frame');
							if(print_frame != null)
							{
								print_frame.src = _rootContextPath+"/owner/printInvoice?invoiceId="+invoice_id;
							}
							else
							{
							
								new Ext.Window({
								    title : "iframe",
								    layout : 'fit',
								    id : "print-frame",
								    autoEl : {
								    	tag : "iframe",
									    width : 0,
									    height: 0,
									    frameborder: '0',
									    css: 'display:none;visibility:hidden;height:0px;',
									    src : _rootContextPath+"/owner/printInvoice?invoiceId="+invoice_id
								    }
								}).show();
							}
						}
					}
					);
			
			
			new Ext.menu.Menu(
			{
				items: items
			}).showAt(event.xy);
		},
		viewInvoice: function(grid, index, event)
		{
			var record = grid.getStore().getAt(index);
			
			var buttons = [];
			
			if((hasPermission(PL_INVOICE_REFUND_VOIDE)) || ((hasPermission(PL_OWNER_MANAGE))&&(this.owner.owner_id > 0))){
				
				if(record.data.status == 'Approved' && record.data.payment_method == 'Credit Card')
				{
					buttons.push({
						            text: 'Void',
						            handler: function(){
						            	grid.doVoid(record.data.invoice_id);
						            	}
						        });
				}
				
				if((record.data.status == 'Approved' || record.data.status == 'Refunded') && record.data.payment_method != 'None')
				{
					buttons.push({
						            text: 'Refund',
						            handler: function(){
						            	grid.doRefund(record.data.invoice_id, record.data.amount);
						            	}
						        });
				}
			}
			
			buttons.push({
				            text: 'Close',
				            handler: function(){ this.findParentByType('window').close(); }
				        });
			
			var vehicleWindow = new Ext.Window({
		        renderTo: document.body,
		        title: 'Invoice Details - ' + record.data.invoice_id,
		        width:350,
		        height: 300,
		        plain: true,
		        resizable: true,
		        autoScroll: true,
		        modal: true,
		        id: 'invoice-view-window',
		        closeAction: 'close',
		        autoDestroy: true,
		        items: [{
		        	xtype: 'panel',
					bodyCssClass: 'x-citewrite-panel-body',
			        padding: 5,
		        	autoLoad: {url: _contextPath + '/invoice/details', params: {invoice_id: record.data.invoice_id, owner_id: grid.owner.owner_id }},
		        }],
		        buttons: buttons
		    });
			
			vehicleWindow.show();
			vehicleWindow.center();
		}
		
});