Ext.onReady(function(){
		var citationFormPanel = new Ext.FormPanel({
			bodyBorder: false,
			border: false,
			frame: false,
			labelAlign: 'top',
			buttonAlign:'center',
			bodyStyle: 'padding: 10px; ',
			id: 'citeDetailsForm',
			bodyCssClass: 'x-citewrite-panel-body',
            layout:'column',
            labelAlign: 'top',
			items:[
			       {
			    	   xtype: 'container',
			    	   columnWidth: .5,
			    	   layout: 'form',
			    	   items: [
						       {
						    	   xtype: 'textfield',
						    	   id: 'cite_officer_id',
						    	   fieldLabel: 'Officer ID',
						    	   width: 160,
						    	   value: _officerID
						    		  
						       },
						       {
						    	   xtype: 'datefield',
						    	   id: 'cite_date',
						    	   fieldLabel: 'Date',
								   width: 160
						       },
						       {
						    	   xtype: 'textfield',
						    	   id: 'cite_license',
						    	   fieldLabel: 'License',
							    	width: 160,
							    	maxLength: 7,
					                maskRe: /^[a-zA-Z0-9_]*$/
						       },
						       {
						    	   xtype: 'combo',
						    	   id: 'citeColor',
						    	   hiddenName: 'cite_color',
						    	   fieldLabel: 'Color',
						    	   width: 160,
								 	lazyRender: false,
								 	store: new Ext.data.JsonStore({
											root: 'codes',
											url: _contextPath + '/citation/codes?type=color',
											totalCount: 'count',
											id: 0,
											fields: ['codeid', 'description'],
											autoLoad: true,
											remoteSort: true
								 		}),
									valueField: 'codeid',
									displayField: 'description',
									triggerAction: 'all',
									forceSelection: false,
									mode: 'local'
						       },
						       {
						    	   xtype: 'combo',
						    	   hiddenName: 'cite_location',
						    	   id: 'citeLocation',
						    	   fieldLabel: 'Location',
						    	   width: 160,
								 	lazyRender: false,
								 	store: new Ext.data.JsonStore({
										root: 'codes',
										url: _contextPath + '/citation/codes?type=location',
										totalCount: 'count',
										id: 0,
										fields: ['codeid', 'description', { 
										       name: 'display', 
										       convert: function(v, rec) { return rec.codeid +' - '+ rec.description; }
										    }],
										autoLoad: true,
										remoteSort: true
								 		}),
									valueField: 'codeid',									
									displayField: 'display',
									triggerAction: 'all',
									forceSelection: false,
									mode: 'local'
						       },
						       {
						    	   xtype: 'datefield',
						    	   id: 'chalk_start_date',
						    	   fieldLabel: 'Chalk Start Date',
							    	width: 160,
							    	hidden: true
						       },
						       {
						    	   xtype: 'datefield',
						    	   id: 'chalk_end_date',
						    	   fieldLabel: 'Chalk End Date',
						    	   width: 160,
							    	hidden: true
						       },
						       {
						    	   xtype: 'hidden',
						    	   id: 'cite_read_id'
						       }]
			       },
			       {
			    	   xtype: 'container',
			    	   columnWidth: .5,
			    	   layout: 'form',
			    	   items: [
								{
									xtype: 'combo',
									hiddenName: 'cite_violation',
									id: 'citeViolation',
									fieldLabel: 'Violation',
								 	width: 160,
								 	lazyRender: false,
								 	store: new Ext.data.JsonStore({
										root: 'codes',
										url: _contextPath + '/citation/codes?type=violation',
										totalCount: 'count',
										id: 0,
										fields: ['codeid', 'description', 'is_overtime', { 
										       name: 'display', 
										       convert: function(v, rec) { return rec.codeid +' - '+ rec.description; }
										    }],
										autoLoad: true,
										remoteSort: true,
										sortInfo: {
											field: 'codeid',
											direction: 'ASC'
										}
								 		}),
									valueField: 'codeid',
									displayField: 'display',
									triggerAction: 'all',
									forceSelection: true,
									mode: 'local',
									listeners:{
								         'select': function(p1, p2)
								         {
								        	 if(p2.data.is_overtime == 1)
								        	 {
								        		 Ext.getCmp('chalk_start_date').show();
								        		 Ext.getCmp('chalk_start_time').show();
								        		 Ext.getCmp('chalk_end_date').show();
								        		 Ext.getCmp('chalk_end_time').show();
								        	 }
								        	 else
								        	 {
								        		 Ext.getCmp('chalk_start_date').hide();
								        		 Ext.getCmp('chalk_start_time').hide();
								        		 Ext.getCmp('chalk_end_date').hide();
								        		 Ext.getCmp('chalk_end_time').hide();
								        	 }
								         }
								    }
								},
						       {
						    	   xtype: 'timefield',
						    	   id: 'cite_time',
						    	   fieldLabel: 'Time',
							    	width: 160
						       },
					       {
					    	   xtype: 'combo',
					    	   id: 'citeState',
					    	   hiddenName: 'cite_state',
					    	   fieldLabel: 'State',
						    	width: 160,
							 	lazyRender: false,
							 	store: new Ext.data.JsonStore({
									root: 'codes',
									url: _contextPath + '/citation/codes?type=state',
									totalCount: 'count',
									id: 0,
									fields: ['codeid', 'description'],
									autoLoad: true,
									remoteSort: true
							 		}),
								valueField: 'codeid',
								displayField: 'description',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
					       },
					       {
					    	   xtype: 'combo',
					    	   id: 'citeMake',
					    	   hiddenName: 'cite_make',
					    	   fieldLabel: 'Make',
					    	   width: 160,
							 	lazyRender: false,
							 	store: new Ext.data.JsonStore({
									root: 'codes',
									url: _contextPath + '/citation/codes?type=make',
									totalCount: 'count',
									id: 0,
									fields: ['codeid', 'description'],
									autoLoad: true,
									remoteSort: true
							 		}),
								valueField: 'codeid',
								displayField: 'description',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
					       },
					       {
					    	   xtype: 'combo',
					    	   id: 'citeComment',
					    	   hiddenName: 'cite_comment',
					    	   fieldLabel: 'Comment',
					    	   width: 160,
							 	lazyRender: false,
							 	store: new Ext.data.JsonStore({
									root: 'codes',
									url: _contextPath + '/citation/codes?type=comment',
									totalCount: 'count',
									id: 0,
									fields: ['codeid', 'description', { 
									       name: 'display', 
									       convert: function(v, rec) { return rec.codeid +' - '+ rec.description; }
									    }],
									autoLoad: true,
									remoteSort: true
							 		}),
								valueField: 'codeid',
								displayField: 'display',
								triggerAction: 'all',
								forceSelection: true,
								mode: 'local'
					       },
					       {
					    	   xtype: 'timefield',
					    	   id: 'chalk_start_time',
					    	   fieldLabel: 'Chalk Start Time',
						    	width: 160,
						    	hidden: true
					       },
					       {
					    	   xtype: 'timefield',
					    	   id: 'chalk_end_time',
					    	   fieldLabel: 'Chalk End Time',
					    	   width: 160,
						    	hidden: true
					       }
					      ]
			       }]
		});
		
		//device dialog
		citationWindow = new Ext.Window({
			title: 'New Citation',
            renderTo: document.body,
            id: 'citeWindow',
            width: 400,
            height:400,
            closeAction:'hide',
            plain: true,
            resizable: true,
            modal: true,
            stateful: false,
            autoScroll: true,
            
            items: citationFormPanel,

            buttons: [{
                text:'OK',
                handler: function()
                {
                	//validate form
                	citationFormPanel.getForm().submit({
                	    url: _contextPath + '/citation/add',
                	    success: function(form, action) {
                	       Ext.growl.message('Success', 'Citation has been created.');
                	       citationWindow.hide();
                	       launchPrintDialog(action.result.citation);
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
                	               Ext.growl.message('Error', action.result.msg);
                	       }
                	    }
                	});
                }
            },{
                text: 'Cancel',
                handler: function(){
                	citationWindow.hide();
                }
            }]
        });
		
		citationPrintDialog = new Ext.Window({
			title: 'Print Citation',
            renderTo: document.body,
            id: 'citePrintWindow',
            width: 415,
            height:400,
            closeAction:'hide',
            plain: true,
            resizable: true,
            modal: true,
            stateful: false,
            autoScroll: true,
            bodyCssClass: 'x-citewrite-panel-body',
            layout: 'form',
            padding: '5px',
            items: [{
		    	   xtype: 'combo',
		    	   id: 'printFormat',
		    	   hiddenName: 'print_format',
		    	   fieldLabel: 'Print Format',
		    	   width: 150,
				 	lazyRender: false,
				 	store: new Ext.data.ArrayStore({
				        autoDestroy: true,
				        fields: ['type', 'display'],
				        data : [
				            ['desktop', 'Street Sweeper'],
				            ['mobile', 'Mobile']
				        ]
				    }),
				    displayField: 'display',
					triggerAction: 'all',
					forceSelection: false,
					mode: 'local'
		       },{
            	id: 'citation_print_details',
            	padding: '5px',
            	margin: '10px 0px 0px 0px',
            	autoWidth: true
            }],

            buttons: [{
                text:'Print',
                handler: function()
                {
                	/*
                	//validate form
                	citationFormPanel.getForm().submit({
                	    url: _contextPath + '/citation/add',
                	    success: function(form, action) {
                	       
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
                	               Ext.growl.message('Error', action.result.msg);
                	       }
                	    }
                	});*/
                }
            },{
                text: 'Close',
                handler: function(){
                	citationPrintDialog.hide();
                }
            }]
        });
});

function launchCitaionCreate(license, read_id, read_date)
{
	Ext.getCmp('cite_license').setValue(license);
	if(read_id != undefined)
	{
		Ext.getCmp('cite_read_id').setValue(read_id);
		Ext.getCmp('cite_date').setValue(read_date);
		Ext.getCmp('cite_time').setValue(read_date);
		Ext.getCmp('chalk_start_date').setValue(read_date);
		Ext.getCmp('chalk_start_time').setValue(read_date);
		Ext.getCmp('chalk_end_date').setValue(read_date);
		Ext.getCmp('chalk_end_time').setValue(read_date);
	}
	else
	{
		read_date = new Date();
		Ext.getCmp('cite_read_id').setValue(0);
		Ext.getCmp('cite_date').setValue(read_date);
		Ext.getCmp('cite_time').setValue(read_date);
		Ext.getCmp('chalk_start_date').setValue(read_date);
		Ext.getCmp('chalk_start_time').setValue(read_date);
		Ext.getCmp('chalk_end_date').setValue(read_date);
		Ext.getCmp('chalk_end_time').setValue(read_date);
	}
	Ext.getCmp('citeWindow').show();
}

function launchPrintDialog(citation)
{
	var html = new Array(
			  '<dl class="details" style="maring: 0px;">',
			  		'<dt>Citation Number</dt>',
			  		'<dd>',citation.citation_number,'</dd>',
			  		'<dt>Date</dt>');
			if(citation.citation_date instanceof Date)
			{
			  	html.push('<dd>',citation.citation_date.format('F j, Y g:i A'),'</dd>');
			}
			else
			{
				html.push('<dd>',citation.citation_date,'</dd>');
			}
		html.push(	'<dt>Officer ID</dt>',
			  		'<dd>',citation.officer_id,'</dd>',
			  		'<dt>License</dt>',
			  		'<dd>',citation.license,'</dd>',
			  		'<dt>VIN</dt>',
			  		'<dd>',citation.vin,'</dd>',
			  		'<dt>Color</dt>',
			  		'<dd>',citation.color,'</dd>',
			  		'<dt>Make</dt>',
			  		'<dd>',citation.make,'</dd>',
			  		'<dt>State</dt>',
			  		'<dd>',citation.state,'</dd>',
			  		'<dt>Violation</dt>',
			  		'<dd>',citation.violation_id,' - ', citation.violation_description, '</dd>',
			  		'<dt>Amount</dt>',
			  		'<dd>$',citation.violation_amount,'</dd>');
	  
	  if(citation.violation_start != null)
	  {
		  if(citation.violation_start instanceof Date)
		  {
			  html.push('<dt>Chalk Start</dt>',
			  		'<dd>',citation.violation_start.format('F j, Y g:i A'),'</dd>');
		  }
		  else
		  {
			  html.push('<dt>Chalk Start</dt>',
				  		'<dd>',citation.violation_start,'</dd>');
		  }
	  }
	  
	  if(citation.violation_end != null)
	  {
		  if(citation.violation_end instanceof Date)
		  {
			  html.push('<dt>Chalk End</dt>',
			  		'<dd>',citation.violation_end.format('F j, Y g:i A'),'</dd>');
		  }
		  else
		  {
			  html.push('<dt>Chalk End</dt>',
				  		'<dd>',citation.violation_end,'</dd>');
		  }
	  }
	  
			  		html.push('<dt>Location</dt>',
			  		'<dd>',citation.location_id, ' - ', citation.location_description,'</dd>',
			  		'<dt>Comments</dt>',
			  		'<dd>',citation.comments,'</dd>',
			  		'<dt>Exported</dt>',
			  		'<dd>',((citation.exported == 1)?'Yes':'No'),'</dd>',
			  '</dl>');
	  
	  Ext.getCmp('citation_print_details').update(html.join(''));
	Ext.getCmp('citePrintWindow').show();
}