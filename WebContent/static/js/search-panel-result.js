SearchPanelResult = Ext.extend(Ext.TabPanel,{
	searchType:null,
	description:null,
	vehicleId:null,
	initComponent : function()
	{
		
	   var tabPanel = this;

		var config = 
		{
			activeTab: 0,
			tabPosition: 'bottom',
			border: false,
			frame: false,
			items:[{
					xtype: 'panel',
					title: 'Description',
					bodyCssClass: 'x-citewrite-panel-body',
					padding: '10px',
					autoScroll: true,
					contentEl: tabPanel.description
				}
			]//end items
		
		};
		
		
		if(tabPanel.vehicleId!= 0){
			
	       var citationStore = new Ext.data.JsonStore({
				root: 'citations',
				url: _contextPath + '/citation/list',
				baseParams: {filter_vehicle_id: tabPanel.vehicleId},
				totalProperty: 'count',
				fields: [ 'citation_id', 'citation_number','officer_id', 'violation_id', 'violation_description', {name:'citation_date', type: 'date', dateFormat:'Y-m-dTH:i:s'}, 'comment', 'location_id'],
				remoteSort: true,
				autoLoad: true,
				sortInfo: {
						field: 'citation_date',
						direction: 'DESC' 
					},
				listeners: {
		            load: function(p1, p2, p3){
			            Ext.getCmp("CitationGrid"+tabPanel.vehicleId).setTitle("Citations ("+ p2.length +")");
		            }
	            }

			});
			
			config.items.push(
			
					{
						
						xtype: 'grid',
				        title: 'Citations',
				        store: citationStore,
				        id:"CitationGrid"+tabPanel.vehicleId,
				        columns: [
				            {id:'citation_number',header: 'Number', width: 100, sortable: true, dataIndex: 'citation_number'},
				            {header: 'Officer ID', width: 75, sortable: true, dataIndex: 'officer_id'},
				            {header: 'Location', width: 50, sortable: true, dataIndex: 'location_id'},
				            {header: 'Violation', width: 100, sortable: true, dataIndex: 'violation_id', renderer: function(value, p1, p2){ return p2.data.violation_description; }},
				            {header: 'Date', width: 150,sortable: true, dataIndex: 'citation_date', renderer: function(value){ return value.format('F j, Y g:i A');}},
				            {header: 'Comments', width: 100, sortable: false, dataIndex: 'comment'}
				            
				        ],
				        stripeRows: true,
				        border: false,
				        loadMask: true,
				        layout: 'fit',
				        viewConfig: { forceFit:true }
				    }
			
			);
		}
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		SearchPanelResult.superclass.initComponent.apply(this, arguments);
	}
			
});