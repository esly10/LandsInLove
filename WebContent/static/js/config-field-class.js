FieldConfiguration = Class.create({
	currentRow: null,
	initialize: function(options) 
	{
		this.options = {
			container: null,
			btnAdd: null,
			url: null
		};
		
		Object.extend(this.options, options || {});
		
		if(this.options.btnAdd != null)
		{
			this.options.btnAdd.onclick = this.add.bind(this);
		}
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
		this.addField();
	},
	edit: function(icon)
	{
		var object = this;
		this.currentRow = $(icon.parentNode.parentNode.parentNode);
		
		var name = this.getFieldName(this.currentRow);
		var data = { xaction: 'get', name: name };
		Ext.Ajax.request({
			   url: object.options.url,
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   object.addField(response.field);
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
	 				   msg: 'Error retrieving field information.',
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
		var object = this;
		 Ext.Msg.confirm("Remove?", "Remove field?", function(bid, p2){
			  if(bid == "yes")
			  {
				  Ext.Ajax.request({
					   url: object.options.url,
					   success: function(p1, p2)
					   {
						   var response = Ext.decode(p1.responseText);
						   if(response.success)
						   {
							   parent.remove();
							   Ext.growl.message('Success', 'Field has been removed.');
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
			   url: object.options.url,
			   params: { xaction: 'move', 'field-name': name, dir: dir },
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
				   msg: 'Error moving field.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
			   }
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
						'<dt style="width: 100px;"><input type="text" class="x-form-text id-input" id="option_id_',this.optionCount,'" name="option_id_',this.optionCount,'" value="',id,'" style="width: 90px;" /></dt>',
						'<dt style="width: 100px;"><input type="text" class="x-form-text" id="option_value_',this.optionCount,'" name="option_value_',this.optionCount,'" value="',value,'" style="width: 90px;" /></dt>',
						'<dd style="width: 40px;">',
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
		container.select('.btn-option-remove').each(function(div){div.onclick = object.removeOption.bind(object, div);});
	},
	moveOption: function(icon, dir)
	{
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
	addField: function(field)
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
			    	   id: 'vehicle-field-details',
			    	   xtype: 'panel',
			    	   layout: 'form',
			    	   bodyCssClass: 'x-citewrite-panel-body',
			    	   border: false,
			    	   padding: 5,
			    	   items: [{
			    		   	xtype: 'textfield',
							id: 'field_label',
							name: 'field-label',
				    	    fieldLabel: 'Field Name',
				    	    maskRe: /^[a-zA-Z0-9- ]*$/,
				    	    maxLength:25
				       },{
			    		   	xtype: 'checkbox',
							id: 'field_required',
							name: 'field-required',
				    	   boxLabel: 'Required',
				    	   hideLabel: false
				       },{
				    	   xtype: 'combo',
				    	   id: 'field_value_type',
				    	   hiddenName: 'field-value-type',
				    	   fieldLabel: 'Value Type',
				    	   submitValue: true,
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
									var options = Ext.getCmp('field-value-options');
									var textOptions = Ext.getCmp('field-text-options');
									var dbOptions = Ext.getCmp('field-database-options');
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
				    	   id: 'field-value-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   padding: 5,
				    	   border: false,
				    	   frame: false,
				    	   hidden: true,
				    	   bodyCssClass: 'x-citewrite-panel-body',
				    	   html: '<div class="form-header">'+
				    	   		'	<input type="hidden" name="option-count" id="option-count" value="0" />'+
								'	<dl class="config-form" style="border: 0px;">'+
								'		<dt style="width: 100px;"><b>Option ID</b></dt>'+
								'		<dt style="width: 100px;"><b>Option Value</b></dt>'+
								'		<dd style="width: 40px;"><div class="x-tool-btn btn-option-add" title="Add Option" id="addValueOptionBtn"></div></dd>'+
								'	</dl>'+
								'</div>'+
								'<div id="list-options-container"></div>'
				       },{
				    	   id: 'field-text-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   hidden: true,
				    	   border: false,
				    	   frame: false,
				    	   layout: 'form',
				    	   bodyCssClass: 'x-citewrite-panel-body',
				    	   items: [{
						    		   	xtype: 'textfield',
										id: 'field_validation',
										name: 'field-validation',
							    	   fieldLabel: 'Validation',
							    	   allowBlank: true
							       }]
				       },{
				    	   id: 'field-database-options',
				    	   xtype: 'panel',
				    	   margins: '10 0 0 0',
				    	   hidden: true,
				    	   border: false,
				    	   frame: false,
				    	   layout: 'form',
				    	   bodyCssClass: 'x-citewrite-panel-body',
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
		
		title += 'Field';
		
			var object = this;
			var vehicleWindow = new Ext.Window({
	            renderTo: document.body,
	            title: title,
	            width: 325,
	            height: 300,
	            closeAction:'close',
	            plain: true,
	            resizable: false,
	            modal: true,
	            id: 'fieldWindow',
	            items: formPanel,
	            autoScroll: true,

	            buttons: [{
	                text:'Save',
	                handler: function()
	                {                	
	                	
	                	if(Ext.getCmp('field_label').getValue() == "" && Ext.getCmp('field_value_type').getValue() == ""){
	                		Ext.Msg.show({
         	    			   title:'Error!',
         	    			   msg: "The fields 'Field Name' and 'Value Type' are required",
         	    			   buttons: Ext.Msg.OK,
         	    			   icon: Ext.MessageBox.ERROR
         	    			});
	                		
	                		return false;
	                	}
     		                	
	                	//validate form
	                	formPanel.getForm().submit({
	                	    url: object.options.url,
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
                	    			   title:'Error!',
                	    			   msg: response.msg,
                	    			   buttons: Ext.Msg.OK,
                	    			   icon: Ext.MessageBox.ERROR
                	    			});
	                	    	}
	                	    },
	                	    failure: function(form, action) {
	                	    	var response = Ext.decode(action.response.responseText);
	                	    	if (typeof response.msg !== undefined){
	                	    		Ext.Msg.show({
										   title:'Failure',
										   msg: response.msg,
										   buttons: Ext.Msg.OK,
										   icon: Ext.MessageBox.ERROR
										});
	                	    	}
	                	    		
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
			
			vehicleWindow.show();
			
			var plus = $('addValueOptionBtn');
			plus.onclick =  this.addOptionValue.bind(this);			
			
			if(field != null)
			{
				Ext.getCmp('field_label').setValue(field.label);
				Ext.getCmp('field_value_type').setValue(field.type);
				Ext.getCmp('field_required').setValue(field.required);
				
				var list = Ext.getCmp('field-value-options');
				var text = Ext.getCmp('field-text-options');
				var db = Ext.getCmp('field-database-options');
				list.hide();
				text.hide();
				db.hide();
				
				if(field.type == 'list')
				{
					//add option fields
					if(field.options.length > 0)
					{
						list.show();
						
						for(var i = 0; i < field.options.length; i++)
						{
							var option = field.options[i];
							this.addOptionValue(null, option.id, option.name);
						}
					}
				}
				else if(field.type == 'database')
				{
					db.show();
					
					Ext.getCmp('field_table_name').setValue(field.tableName);
					Ext.getCmp('field_id_field').setValue(field.idField);
					Ext.getCmp('field_description_field').setValue(field.descField);
					Ext.getCmp('field_where').setValue(field.where);
				}
				else if(field.type == 'text')
				{
					text.show();
					
					Ext.getCmp('field_validation').setValue(field.validation);
				}
			}
	}
});