CiteConfiguration = Class.create();
CiteConfiguration.prototype = {
	currentRow: null,
	initialize: function(options) 
	{
		this.options = {
			container: null,
			btnAdd: null,
			url: null
		};
		
		Object.extend(this.options, options || {});
		
		var object = this;
		this.options.btnAdd.onclick = this.add.bind(this);
		this.doBind();
	},
	doBind: function()
	{
		var object = this;
		this.options.container.select('.btn-up').each(function(div){div.onclick = object.move.bind(object, div, 'up');});
		this.options.container.select('.btn-down').each(function(div){div.onclick = object.move.bind(object, div, 'down');});
		this.options.container.select('.btn-edit').each(function(div){div.onclick = object.edit.bind(object, div);});
		this.options.container.select('.btn-remove').each(function(div){div.onclick = object.remove.bind(object, div);});
	},
	add: function(icon)
	{
		this.currentRow = null;
		this.addCiteField();
	},
	edit: function(icon)
	{
		var object = this;
		this.currentRow = $(icon.parentNode.parentNode.parentNode);
		
		var name = this.getFieldName(this.currentRow);
		var data = { xaction: 'get', name: name };
		Ext.Ajax.request({
			   url: _contextPath + '/administration/citation',
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   object.addCiteField(response.field);
				   }
				   else
				   {
					   Ext.Msg.show({
	    				   title:'Error',
	    				   msg: response.msg,
	    				   buttons: Ext.Msg.OK,
	    				   icon: Ext.MessageBox.ERROR
	    				});
				   }
			   },
			   failure: function()
			   {
				   Ext.Msg.show({
 				   title:'Error',
 				   msg: 'Error saving configuration.',
 				   buttons: Ext.Msg.OK,
 				   icon: Ext.MessageBox.ERROR
 				});
			   },
			   params: data
			});
	},
	remove: function(icon)
	{
		var parent = $(icon.parentNode.parentNode.parentNode);
		var name = this.getFieldName(parent);
		
		 Ext.Msg.confirm("Remove?", "Remove field?.", function(bid, p2){ // <br> if the field exist in view devise will be delete too.
			  if(bid == "yes")
			  {
				  Ext.Ajax.request({
					   url: _contextPath + '/administration/citation',
					   success: function(p1, p2)
					   {
						   
						   var response = Ext.decode(p1.responseText);
						   if(response.success)
						   {
							   parent.remove();
							   if(response.msg2){
								   
								   var task = new Ext.util.DelayedTask(function(){
										  Ext.growl.message(' ', 'The field has been removed in Device View too.');
								   });
								   
								   Ext.growl.message('Success', 'Field has been removed.');
								   task.delay(3100);
								   
							   }else {
								   Ext.growl.message('Success', 'Field has been removed.');
								   
							   }
							   
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
           				   msg: 'Error deleting device.',
           				   buttons: Ext.Msg.OK,
           				   icon: Ext.MessageBox.ERROR
           				});
					   },
					   params: { xaction: 'remove', 'field-name': name }
					}); 
			  }
		  });
	},
	getFieldName: function(row)
	{
		var inputs = row.select("input[name='field_name']");
		if(inputs.length > 0)
		{
			return inputs[0].value;
		}
		
		return "";
	},
	move: function(icon, dir)
	{
		var object = this;
		this.currentRow = $(icon.parentNode.parentNode.parentNode);
		var name = this.getFieldName(this.currentRow);
		if(dir == 'up')
		{
			var sibling = this.currentRow.previous('.form-row');
		}
		else
		{
			var sibling = this.currentRow.next('.form-row');
		}
		
		if(sibling != undefined)
		{
			Ext.Ajax.request({
			   url: _contextPath + '/administration/citation',
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   object.currentRow.remove();
					   if(dir == 'up')
					   {
						   sibling.insert({before: object.currentRow});
					   }
					   else
						{
						   sibling.insert({after: object.currentRow});
						}	
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
				   msg: 'Error deleting device.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
			   },
			   params: { xaction: 'move', 'field-name': name, dir: dir }
			}); 
		}
	},
	addRow: function(field)
	{
		var html = new Array(
				'<div class="form-row">',
					'<dl class="config-form">',
						'<dt>',field.label,'<input type="hidden" id="field_name" name="field_name" value="',field.name,'" /></dt>');
		
			html.push(	'<dd>',
							'<div class="x-tool-btn btn-up" title="Move Field Up"></div>',
							'<div class="x-tool-btn btn-down" title="Move Field Down"></div>',
							'<div class="x-tool-btn btn-edit" title="Field Settings"></div>',
							'<div class="x-tool-btn btn-remove" title="Remove Field"></div>',
						'</dd>',
					'</dl>',
				'</div>'	
		);
		
		if(this.currentRow == null) //this is a new row we are adding
		{
			this.options.container.insert(html.join(''));
		}
		else
		{
			this.currentRow.insert({before: html.join('')});
			this.currentRow.remove();
		}
		this.doBind();
	},
	optionCount: 0,
	addOptionValue: function(event, id, value)
	{
		if(id == undefined)
		{
			id = "";
		}
		if(value == undefined)
		{
			value = "";
		}
		
		this.optionCount++;
		var html = new Array(
				'<div class="form-row">',
					'<dl class="config-form" style="border: 0px;">',
						'<dt style="width: 100px;"><input type="text" class="x-form-text" id="option_id_',this.optionCount,'" name="option_id_',this.optionCount,'" value="',id,'" style="width: 90px;" /></dt>',
						'<dt style="width: 100px;"><input type="text" class="x-form-text" id="option_value_',this.optionCount,'" name="option_value_',this.optionCount,'" value="',value,'" style="width: 90px;" /></dt>',
						'<dd style="width: 40px;">',
							//'<div class="x-tool-btn btn-option-up" title="Move Option Up"></div>',
							//'<div class="x-tool-btn btn-option-down" title="Move Option Down"></div>',
							'<div class="x-tool-btn btn-option-remove" title="Remove Option"></div>',
						'</dd>',
					'</dl>',
					'<div style="clear: left;"></div>',
				'</div>'	
		);
		
		$('option-count').value = this.optionCount;
		$('list-options-container').insert(html.join(''));
		this.doOptionBind();
	},
	doOptionBind: function()
	{
		var object = this;
		var container = $('list-options-container');
		//container.select('.btn-option-up').each(function(div){div.onclick = object.moveOption.bind(object, div, 'up');});
		//container.select('.btn-option-down').each(function(div){div.onclick = object.moveOption.bind(object, div, 'down');});
		container.select('.btn-option-remove').each(function(div){div.onclick = object.removeOption.bind(object, div);});
	},
	moveOption: function(icon, dir)
	{
		var object = this;
		var row = $(icon.parentNode.parentNode.parentNode);
		if(dir == 'up')
		{
			var sibling = this.currentRow.previous();
		}
		else
		{
			var sibling = this.currentRow.next();
		}
	},
	removeOption: function(icon, dir)
	{
		var row = $(icon.parentNode.parentNode.parentNode);
		row.remove();
	},
	addCiteField: function(field)
	{
		var fp = {
				bodyBorder: false,
				border: false,
				frame: false,
				defaultType:'textfield',
				bodyStyle: 'padding: 10px 10px 10px 10px;',
				bodyCssClass: 'x-citewrite-panel-body',
				defaults: { width: '95%' },
				
				items: [{
			    	   xtype: 'combo',
			    	   id: 'form_field_name',
			    	   hiddenName: 'field-name',
			    	   fieldLabel: 'Field Type',
			    	   width: 150,
					 	lazyRender: false,
					 	store: new Ext.data.ArrayStore({
					        autoDestroy: true,
					        fields: ['name', 'label'],
					        data : [
					            ['officer_id', 'Officer ID'],
					            ['date_time', 'Date and Time'],
					            ['license', 'License'],
					            ['vin', 'VIN'],
					            ['state', 'State'],
					            ['violation', 'Violation'],
					            ['make', 'Make'],
					            ['color', 'Color'],
					            ['location', 'Location'],
					            ['comment', 'Comments'],
					            ['ot', 'Custom']
					        ]
					    }),
					    displayField: 'label',
					    valueField: 'name',
						triggerAction: 'all',
						forceSelection: true,
						mode: 'local'
						,listeners: {
							select: function(combo, record, index)
							{
								var details = Ext.getCmp('cite-field-details');
								if(record.data.name == 'ot')
								{
									details.show();
								}
								else
								{
									details.hide();
								}
								
								if((record.data.name == "license")||(record.data.name == "vin")||(record.data.name == "state")||(record.data.name == "violation")||(record.data.name == "officer_id")){
									if(record.data.name == "violation" || record.data.name == "officer_id" ){
										Ext.getCmp('field_required').setValue(true);
									}else{
										Ext.getCmp('field_required').setValue(false);
									}
									Ext.getCmp('field_required').disable();
								}else{
									Ext.getCmp('field_required').enable();
								}
								
							}
						}
			       },{
		    		   	xtype: 'checkbox',
						id: 'field_required',
						name: 'field-required',
			    	   boxLabel: 'Required',
			    	   hideLabel: false
			       },{
			    	   id: 'cite-field-details',
			    	   xtype: 'panel',
			    	   layout: 'form',
			    	   hidden: true,
			    	   padding: 5,
			    	   items: [{
			    		   	xtype: 'textfield',
							id: 'field_label',
							name: 'field-label',
				    	   fieldLabel: 'Field Name',
				    	   maskRe: /^[a-zA-Z0-9- ]*$/,
			               tabIndex: 1
				       },{
				    	   xtype: 'combo',
				    	   id: 'field_value_type',
				    	   hiddenName: 'field-value-type',
				    	   fieldLabel: 'Value Type',
			               tabIndex: 2,
			               width: 150,
						 	lazyRender: false,
						 	store: new Ext.data.ArrayStore({
						        autoDestroy: true,
						        fields: ['id', 'description'],
						        data : [
						            ['text', 'Text'],
						            ['list', 'List'],
						            ['database', 'Database']
						        ]
						    }),
						    displayField: 'description',
						    valueField: 'id',
							triggerAction: 'all',
							forceSelection: true,
							mode: 'local'
							,listeners: {
								select: function(combo, record, index)
								{
									var options = Ext.getCmp('cite-field-value-options');
									var textOptions = Ext.getCmp('cite-field-text-options');
									var dbOptions = Ext.getCmp('cite-field-database-options');
									if(record.data.id == 'list')
									{
										options.show();
										textOptions.hide();
										dbOptions.hide();
									}
									else if(record.data.id == 'database')
									{
										options.hide();
										textOptions.hide();
										dbOptions.show();
									}
									else
									{
										options.hide();
										textOptions.show();
										dbOptions.hide();
									}
								}
							}
				       },{
				    	   id: 'cite-field-value-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   padding: 5,
				    	   hidden: true,
				    	   border: false,
				    	   frame: false,
				    	   html: '<div class="form-header">'+
				    	   		'	<input type="hidden" name="option-count" id="option-count" value=0" />'+
								'	<dl class="config-form" style="border: 0px;">'+
								'		<dt style="width: 100px;"><b>Option ID</b></dt>'+
								'		<dt style="width: 100px;"><b>Option Value</b></dt>'+
								'		<dd style="width: 40px;"><div class="x-tool-btn btn-option-add" title="Add Option" id="citeAddValueOption"></div></dd>'+
								'	</dl>'+
								'</div>'+
								'<div id="list-options-container"></div>'
				       },{
				    	   id: 'cite-field-text-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   hidden: true,
				    	   border: false,
				    	   frame: false,
				    	   layout: 'form',
				    	   items: [{
						    		   	xtype: 'textfield',
										id: 'field_validation',
										name: 'field-validation',
							    	   fieldLabel: 'Validation',
							    	   allowBlank: true
							       }]
				       },{
				    	   id: 'cite-field-database-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   hidden: true,
				    	   border: false,
				    	   frame: false,
				    	   layout: 'form',
				    	   items: [{
						    		   	xtype: 'textfield',
										id: 'field_table_name',
										name: 'field-table-name',
										fieldLabel: 'Table Name',
										maskRe: /^[a-zA-Z0-9-_]*$/,
							       },{
						    		   	xtype: 'textfield',
										id: 'field_id_field',
										name: 'field-id-field',
							    	   fieldLabel: 'ID Field',
										maskRe: /^[a-zA-Z0-9-_]*$/,
							       },{
						    		   	xtype: 'textfield',
										id: 'field_description_field',
										name: 'field-description-field',
							    	   fieldLabel: 'Description Field',
										maskRe: /^[a-zA-Z0-9-_]*$/,
							       },{
						    		   	xtype: 'textfield',
										id: 'field_where',
										name: 'field-where',
							    	   fieldLabel: 'Where'
							       }]
				       }]
			       }]
			};
		
		
		var formPanel = new Ext.FormPanel(fp);
	  
		var ajaxParams = { xaction: 'add'};
		var title = "Add ";
		if(field != null)
		{
			title = "Edit ";
			ajaxParams.xaction = 'update';
			ajaxParams.orig_name = field.name;
		}
		
		title += 'Citation Field';
		
			var object = this;
			var codeWindow = new Ext.Window({
	            renderTo: document.body,
	            title: title,
	            width: 325,
	            height: 300,
	            closeAction:'close',
	            plain: true,
	            resizable: false,
	            modal: true,
	            id: 'citeFieldWindow',
	            items: formPanel,
	            autoScroll: true,

	            buttons: [{
	                text:'Save',
	                handler: function()
	                {                	
	                	//validate form
	                	formPanel.getForm().submit({
	                	    url: _contextPath + '/administration/citation',
	                	    scope: this,
	                	    params: ajaxParams,
	                	    success: function(form, action) {
	                	    	
	                	    	var response = Ext.decode(action.response.responseText);
	                	    	
	                	    	if(response.success)
	                	    	{
	                	    		object.addRow(response.field);
	                	    		
		                	    	var parent = action.options.scope.findParentByType('window'); 
		                	    	parent.close();
		                	       
		                	    	Ext.growl.message('Success', 'Field has been saved.');
	                	    	}
	                	    	else
	                	    	{
	                	    		Ext.Msg.show({
                	    			   title:'Error',
                	    			   msg: response.msg,
                	    			   buttons: Ext.Msg.OK,
                	    			   icon: Ext.MessageBox.ERROR
                	    			});
	                	    	}
	                	    },
	                	    failure: function(form, action) {
	                	    var parent = action.options.scope.findParentByType('window'); 
	                	        switch (action.failureType) {
	                	            case Ext.form.Action.CLIENT_INVALID:
	                	            	Ext.Msg.show({
	                 	    			   title:'Failure',
	                 	    			   msg: 'Form fields may not be submitted with invalid values',
	                 	    			   buttons: Ext.Msg.OK,
	                 	    			   icon: Ext.MessageBox.ERROR
	                 	    			});
	                	            	parent.close();
	                	                break;
	                	            case Ext.form.Action.CONNECT_FAILURE:
	                	            	Ext.Msg.show({
	                 	    			   title:'Failure',
	                 	    			   msg: 'Ajax communication failed',
	                 	    			   buttons: Ext.Msg.OK,
	                 	    			   icon: Ext.MessageBox.ERROR
	                 	    			});
	                	            	parent.close();
	                	                break;
	                	            case Ext.form.Action.SERVER_INVALID:
	                	            	Ext.Msg.show({
	                 	    			   title:'Failure',
	                 	    			   msg: action.result.msg,
	                 	    			   buttons: Ext.Msg.OK,
	                 	    			   icon: Ext.MessageBox.ERROR
	                 	    			});
	                	            	parent.close();
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
			
			var plus = $('citeAddValueOption');
			plus.onclick =  this.addOptionValue.bind(this);			
			
			if(field != null)
			{
				var fn = Ext.getCmp('form_field_name');
				
				if(fn.store.find('name', field.name) == -1)
				{
					fn.setValue('ot');
					Ext.getCmp('cite-field-details').show();
					
					Ext.getCmp('field_label').setValue(field.label);
					Ext.getCmp('field_value_type').setValue(field.type);
					Ext.getCmp('field_required').setValue(field.required);
					
					if(field.type == 'list')
					{
						//add option fields
						if(field.options.length > 0)
						{
							Ext.getCmp('cite-field-value-options').show();
							Ext.getCmp('cite-field-text-options').hide();
							Ext.getCmp('cite-field-database-options').hide();
							
							for(var i = 0; i < field.options.length; i++)
							{
								var option = field.options[i];
								this.addOptionValue(null, option.codeid, option.description);
							}
						}
					}
					else if(field.type == 'database')
					{
						Ext.getCmp('cite-field-value-options').hide();
						Ext.getCmp('cite-field-text-options').hide();
						Ext.getCmp('cite-field-database-options').show();
						
						Ext.getCmp('field_table_name').setValue(field.tableName);
						Ext.getCmp('field_id_field').setValue(field.idField);
						Ext.getCmp('field_description_field').setValue(field.descField);
						Ext.getCmp('field_where').setValue(field.where);
					}
					else if(field.type == 'text')
					{
						Ext.getCmp('cite-field-value-options').hide();
						Ext.getCmp('cite-field-text-options').show();
						Ext.getCmp('cite-field-database-options').hide();
						
						Ext.getCmp('field_validation').setValue(field.validation);
					}
				}
				else
				{
					fn.setValue(field.name);
					
					if((field.name == "license") || (field.name == "vin") || (field.name == "state") ||(field.name == "violation")||(field.name == "officer_id")){
						if(field.name == "violation" ||(field.name == "officer_id")){
							Ext.getCmp('field_required').setValue(true);
						}else{
							Ext.getCmp('field_required').setValue(false);
						}
						Ext.getCmp('field_required').disable();
					}else{
						Ext.getCmp('field_required').setValue(field.required);
					}
		
				}
			}
	}
};

