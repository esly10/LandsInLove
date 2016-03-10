OwnerCitationPanel = Ext.extend(Ext.Panel, {
		owner: null,
		initComponent: function()
	    {
			// create the Data Store
			var pageLimit = 50;
		    var store = new Ext.data.JsonStore({
				url: _contextPath + '/citation/list',
				root: 'citations',
		        totalProperty: 'count',
		        remoteSort: true,
		        fields: [
		            'citation_id',
		            'citation_number', 
		            'officer_id',
		            {name:'citation_date', type: 'date'},
		            'license', 
		            'vin', 
		            'color',
		            'make',
		            'state',
		            'violation_id',
		            'violation_type',
		            'violation_description', 
		            'violation_amount',
		            {name:'violation_start', type: 'date'},
		            {name:'violation_end', type: 'date'},
		            'exported',
		            'location_id',
		            'location_description',
		            'lat',
		            'lng',
		            'comment_id',
		            'comments'
		        ],
				sortInfo: {
					field: 'citation_date',
					direction: 'DESC'
				}
		    });
		    
		    var toolbar = {
		            pageSize: pageLimit,
		            store: store,
		            displayInfo: true,
		            displayMsg: 'Displaying citations {0} - {1} of {2}',
		            emptyMsg: "No citations to display"
		        };
		    
		    if(hasPermission(PL_ADMIN))
		    {
			    toolbar.items = ['-', {
		                text: 'Clear Citations',
		                cls: 'x-btn-text details',
		                handler: function(btn, event){ clearCitations(); }
		                }];
		    }
		    
		    var columnModel = new Ext.grid.ColumnModel({
		        defaults: { sortable: true }
		        ,columns:[{
		            header: "Number",
		            dataIndex: 'citation_number',
		            width: 100
		        }
		        ,{
		            header: "Officer ID",
		            dataIndex: 'officer_id',
		            width: 50
		        },{
		            header: "License",
		            dataIndex: 'license',
		            width: 50
		        },{
		            header: "Vin",
		            dataIndex: 'vin',
		            width: 150
		        },{
		            header: "Violation",
		            dataIndex: 'violation_id',
		            renderer: function(value, p1, p2){ return p2.data.violation_description; },
		            width: 100
		        },{
		            header: "Amount",
		            dataIndex: 'violation_amount',
		            width: 50
		        },{
		            header: "Exported",
		            dataIndex: 'exported',
		            renderer: renderExport,
		            width: 75
		        },{
		            header: "Date",
		            dataIndex: 'citation_date',
		            renderer: function(value){ return value.format('F j, Y g:i A'); },
		            width: 150
		        }]});
		    
		    function renderExport(value, p, r)
		    {
		        if(value)
		        {
		            return "Yes";
		        }
		        
		        return "No";
		    }
		    
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
		        bbar: new Ext.PagingToolbar(toolbar),
		        loadMask: true
		    });
		    
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
					    	   id: 'filter_citation_number',
					    	   fieldLabel: 'Number'
					       },
					       {
					    	   id: 'filter_citation_license',
					    	   fieldLabel: 'License'
					       },
					       {
					    	   id: 'filter_citation_vin',
					    	   fieldLabel: 'VIN'
					       },
					       {
					    	   id: 'filter_citation_officer_id',
					    	   fieldLabel: 'Officer ID'
					       },
					       {
					    	   id: 'filter_citation_date',
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
			    
			var config = 
			{
					title: 'Citations',
					layout:'border',
					border: false,
					bodyCssClass: 'x-citewrite-panel-body',
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
			};
			
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			OwnerCitationPanel.superclass.initComponent.apply(this, arguments);
	    }
});