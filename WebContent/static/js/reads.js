Ext.onReady(function(){
	
	var pageLimit = 50;
	var reads = Ext.get('nav-reads');
	if(reads != null)
	{
		
		reads.on('click',function(){
		    // create the Data Store
		    var store = new Ext.data.JsonStore({
				url: _contextPath + '/lprread/list',
				root: 'reads',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'lpr_read_id',
		            {name:'read_date', type: 'date'},
		            'license', 
		            'street_number', 
		            'street',
		            'city',
		            'state',
		            'lat',
		            'lng',
		            'processed'
		        ],
				sortInfo: {
					field: 'read_date',
					direction: 'DESC'
				}
		    });
		    
		    var toolbar = new Ext.PagingToolbar({
		            pageSize: pageLimit,
		            store: store,
		            displayInfo: true,
		            displayMsg: 'Displaying reads {0} - {1} of {2}',
		            emptyMsg: "No reads to display"
		        });
		    
		    var topToolbar = null;
		    
		    if(_isAdmin)
		    {
			    topToolbar = new Ext.Toolbar({
		    		buttonAlign: 'right',
		            items:[{
		                text: 'Clear Reads',
		                cls: 'x-btn-text details',
		                handler: function(btn, event){ clearReads(); }
		                }]
		        });
		    }
		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true, cellCls: 'valign-middle' }
		        ,columns:[{
		            header: "Context Photo",
		            width: 75,
		            renderer: function(value, p1, p2){ return '<img src="'+_contextPath+'/lprread/thumbnail?lpr_read_id='+p2.data.lpr_read_id+'&type=context" />'; }
		        },{
		            header: "LPR Photo",
		            width: 75,
		            renderer: function(value, p1, p2){ return '<img src="'+_contextPath+'/lprread/thumbnail?lpr_read_id='+p2.data.lpr_read_id+'&type=lpr" />'; }
		        },{
		            header: "License",
		            dataIndex: 'license',
		            width: 50
		        },{
		            header: "Street",
		            dataIndex: 'street',
		            width: 150,
		            renderer: function(value, p1, p2){ return p2.data.street_number+" "+p2.data.street; }
		        },{
		            header: "City",
		            dataIndex: 'city',
		            width: 50
		        },{
		            header: "Date",
		            dataIndex: 'read_date',
		            renderer: function(value){ return value.format('F j, Y g:i A'); },
		            width: 150
		        },{
		            header: "Processed",
		            dataIndex: 'processed',
		            renderer: renderProcessed,
		            width: 75
		        }]});
		    
		    var grid = new Ext.grid.GridPanel({
		        width:550,
		        height:300,
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
		        bbar: toolbar,
		        tbar: topToolbar,
		        loadMask: true
		    });
		    		    
		    function renderProcessed(value, p, r)
		    {
		        if(value == 1)
		        {
		            return "Yes";
		        }
		        
		        return "No";
		    }
		    		
		    // trigger the data store load
		    store.load({params:{start:0, limit:pageLimit}});
		    
		    var _selectedRow = null;
		    			
				function enforceRead()
				{
					var read = _readContextMenu.rowRecord.data;
					launchCitaionCreate(read.license, read.lpr_read_id, read.read_date);
				}
			
			  function viewRead()
			  {
				  var read = _readContextMenu.rowRecord.data;
				  Ext.Ajax.request({
					   url: _contextPath + '/lprread/photos',
					   success: function(p1, p2)
					   {
						   var response = Ext.decode(p1.responseText);
						   if(response.success)
						   {
							   var tab = Ext.getCmp('readDetailsGeneralTab');
								  
								  var html = new Array(
										  '<dl class="details">',
										  		'<dt>Date</dt>',
										  		'<dd>',read.read_date.format('F j, Y g:i A'),'</dd>',
										  		'<dt>License</dt>',
										  		'<dd>',read.license,'</dd>',
										  		'<dt>Street</dt>',
										  		'<dd>',read.street_number+' '+read.street,'</dd>',
										  		'<dt>City</dt>',
										  		'<dd>',read.city,'</dd>',
										  		'<dt>Processed</dt>',
										  		'<dd>',((read.processed==1)?'Yes':'No'),'</dd>');
								  
								  tab.update(html.join(''));
								  
								  //now add photos
								  var tabpanel = Ext.getCmp('readDetailsTabPanel');
								  var count = 1;
								  tabpanel.items.each(function(c){
									  if(count > 1)
									  {
										  tabpanel.remove(c);
									  }
									  count++;
									  });
								  
								  if(read.lat != 0)
								  {
									  var mapPanel = new Ext.ux.MapPanel({
				                			title: 'Map',
				                			zoomLevel: 18,
				                			lat: read.lat,
				                			lng: read.lng
				                		});
									  tabpanel.add(mapPanel);
								  }
								 
								  
								  if(response.photos.length  > 0)
								  {
									  for(var i = 0; i < response.photos.length; i++)
									  {
										  tabpanel.add({
					                			xtype: 'panel',
					                			title: 'Photo',
					                			bodyCssClass: 'x-citewrite-panel-body',
					                			autoScroll: true,
					                			html: '<img src="'+_contextPath+'/lprread/photo?pid='+response.photos[i].lpr_read_photo_id+'"/>'
					                		});
									  }
								  }
								  readDetailsDialog.show();
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
          				   msg: 'Error retrieving read photos.',
          				   buttons: Ext.Msg.OK,
          				   icon: Ext.MessageBox.ERROR
          				});
					   },
					   params: { lpr_read_id: read.lpr_read_id }
					}); 
			  }
			  
			  function markProcessed()
			  {
				  var read = _readContextMenu.rowRecord.data;				  
				  Ext.Ajax.request({
					   url: _contextPath + '/lprread/processed',
					   success: function(p1, p2)
					   {
						   var response = Ext.decode(p1.responseText);
						   if(response.success)
						   {
							   store.reload();
							   Ext.growl.message('Success', 'LPR Read has been marked as processed.');
							   
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
           				   msg: 'Error updating read.',
           				   buttons: Ext.Msg.OK,
           				   icon: Ext.MessageBox.ERROR
           				});
					   },
					   params: { lpr_read_id: read.lpr_read_id }
					}); 
			  }
			  
			  function deleteRow()
			  {
				  var read = _readContextMenu.rowRecord.data;
				  Ext.Msg.confirm("Delete?", "Delete read for "+read.license+"?", function(bid, p2){
					  if(bid == "yes")
					  {
						  Ext.Ajax.request({
							   url: _contextPath + '/lprread/delete',
							   success: function(p1, p2)
							   {
								   var response = Ext.decode(p1.responseText);
								   if(response.success)
								   {
									   store.reload();
									   Ext.growl.message('Success', 'Read has been deleted.');
									   
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
		            				   msg: 'Error deleting read.',
		            				   buttons: Ext.Msg.OK,
		            				   icon: Ext.MessageBox.ERROR
		            				});
							   },
							   params: { lpr_read_id: read.lpr_read_id }
							}); 
					  }
				  });
			  }//deleteRow
			  
			  function clearReads()
			  {
				  Ext.getCmp('clearReadsForm').getForm().reset();
				  clearReadsDialog.show();
			  }
			  
			  var filterForm = new Ext.FormPanel({
					bodyBorder: false,
					border: false,
					frame: false,
					defaultType:'textfield',
					labelAlign: 'top',
					buttonAlign:'center',
					bodyStyle: 'padding: 10px; ',
					autoWidth: true,
					defaults: { width: '95%' },
					bodyCssClass: 'x-citewrite-panel-body',
					items:[
					       {
					    	   id: 'filter_read_license',
					    	   fieldLabel: 'License'
					       },
					       {
					    	   id: 'filter_read_street',
					    	   fieldLabel: 'Street'
					       },
					       {
					    	   id: 'filter_read_city',
					    	   fieldLabel: 'City'
					       },
					       {
					    	   id: 'filter_read_date',
					    	   fieldLabel: 'Date',
					    	   xtype: 'datefield'
					       }
					       ],
		        buttons: [{
		            text: 'Apply',
		            width: 60,
		            handler: function(){
		               var params = filterForm.getForm().getFieldValues();
		               store.baseParams = params;
		              store.load({params: {start: 0, limit: pageLimit}});
		            }
		        },{
		            text: 'Reset',
		            width: 60,
		            handler: function(){
		            	filterForm.getForm().reset();					
		            	store.baseParams = {};
		            	store.load({params: {start: 0, limit: pageLimit}});
		            }
		        }]
			}); //filterForm
			  
			  var content = Ext.getCmp('content-panel');
				content.removeAll(true);
				
				content.add({
						xtype: 'panel',
						title: 'LPR Reads',
						layout:'border',
						border: false,
						bodyCssClass: 'x-citewrite-border-ct',
						defaults: {
						    collapsible: true,
						    split: true,
						    layout: 'fit'
						},

						items: [{
							
							collapsible: false,
						    region:'center',
						    margins: '5 0 5 5',
							items: [grid]
						},
						{
							title: 'Filter',
							region:'east',
							margins: '5 5 5 0',
							width: 200,
							items: [filterForm]
						}]
					});
				content.doLayout();
				
				function onReadGridContextMenu(grid, rowIndex, e) {
					e.stopEvent();
					var coords = e.getXY();
					_readContextMenu.rowRecord = grid.store.getAt(rowIndex);
					
					if(_isAdmin)
					{
						if(_readContextMenu.rowRecord.data.processed == 0)
						{
							Ext.getCmp('menuReadProcessed').show();
						}
						else
						{
							Ext.getCmp('menuReadProcessed').hide();
						}
					}
					
					grid.selModel.selectRow(rowIndex);
					_selectedRow=rowIndex;
					_readContextMenu.showAt([coords[0], coords[1]]);
					
				  }
				
				var _readContextMenu = Ext.getCmp('ReadGridContextMenu');
				var readDetailsDialog = Ext.getCmp('readDetailsDialog');
				var clearReadsDialog = Ext.getCmp('clearReadDialog');
				if(!clearReadsDialog)
				{
					
			    _readContextMenu = new Ext.menu.Menu({
				      id: 'ReadGridContextMenu',
				      items: [
						  { text: 'View', handler: viewRead, id: 'menuReadView' },
						  { text: 'Enforce', handler: enforceRead, id: 'menuReadEnforce' },
						  { text: 'Mark Processed', handler: markProcessed, id: 'menuReadProcessed' },
						  { text: 'Delete', handler: deleteRow, id: 'menuReadDelete' }
				      ]
				   });
					
				var clearFormPanel = new Ext.FormPanel({
					bodyBorder: false,
					border: false,
					frame: false,
					labelAlign: 'top',
					buttonAlign:'center',
					bodyStyle: 'padding: 10px; ',
					autoWidth: true,
					id: 'clearReadsForm',
					defaults: { width: 160 },
					bodyCssClass: 'x-citewrite-panel-body',
					items:[
					       {
					    	   xtype: 'datefield',
					    	   id: 'read_clear_start',
					    	   fieldLabel: 'Start Date'
					       },
					       {
					    	   xtype: 'datefield',
					    	   id: 'read_clear_end',
					    	   fieldLabel: 'End Date'
					       }]
				});
			  
				//device dialog
				clearReadsDialog = new Ext.Window({
					title: 'Clear LPR Reads',
	                renderTo: document.body,
	                id: 'clearReadDialog',
	                layout:'fit',
	                width:200,
	                height:200,
	                closeAction:'hide',
	                plain: true,
	                resizable: false,
	                modal: true,

	                items: clearFormPanel,

	                buttons: [{
	                    text:'OK',
	                    handler: function()
	                    {
	                    	//validate form
	                    	clearFormPanel.getForm().submit({
	                    	    url: _contextPath + '/lprread/clear',
	                    	    success: function(form, action) {
	                    	    	clearReadsDialog.hide();
	                    	       store.load({params: {start: 0, limit: pageLimit}});
	                    	       Ext.growl.message('Success', 'LPR Reads have been deleted.');
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
	                    text: 'Cancel',
	                    handler: function(){
	                    	clearReadsDialog.hide();
	                    }
	                }]
	            });
				
				readDetailsDialog = new Ext.Window({
					title: 'Read Details',
	                renderTo: document.body,
	                id: 'readDetailsDialog',
	                layout:'fit',
	                width:400,
	                height:400,
	                closeAction:'hide',
	                plain: true,
	                resizable: true,
	                modal: true,

	                items:[ {
	                	xtype: 'tabpanel',
	                	activeTab: 0,
	                	id: 'readDetailsTabPanel',
	                	autoScroll: true,
	                	frame: false,
	                	border: false,
	                	items:[{
	                			xtype: 'panel',
	                			title: 'General',
	                			id: 'readDetailsGeneralTab',
	                			bodyCssClass: 'x-citewrite-panel-body',
	                			bodyStyle: 'padding: 0px 5px;',
	                			autoScroll: true
	                		}]
	                	}],

	                buttons: [{
	                    text: 'Enforce',
	                    handler: function(){
	                    	readDetailsDialog.hide();
	                    	enforceRead();
	                    }},{
	                    text: 'Close',
	                    handler: function(){
	                    	readDetailsDialog.hide();
	                    }
	                }]
	            });
			}//end if clearReadsDialog
				grid.addListener('rowcontextmenu', onReadGridContextMenu);
		});//end onclick 
	}
});
