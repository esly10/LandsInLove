PermitDeviceView = Class.create();
PermitDeviceView.prototype = {
	initialize: function(options) 
	{
		this.options = {
			container: null,
			rowCount: 0,
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
		this.options.container.select('.btn-up').each(function(div){div.onclick = object.up.bind(object, div);});
		this.options.container.select('.btn-down').each(function(div){div.onclick = object.down.bind(object, div);});
		this.options.container.select('.btn-remove').each(function(div){div.onclick = object.remove.bind(object, div);});
	},
	add: function()
	{
		this.options.rowCount++;
		var html = new Array(
				'<div class="form-row">',
					'<dl class="config-form">',
						'<dt>',
							'<select class="x-form-select" id="column_name" name="column_name" >',
								'<optgroup label="Permit">');
		for(var i = 0; i < _defaultPermitFields.length; i++)
		{
			var field = _defaultPermitFields[i];
			html.push(				'<option value="mpermit.',field.name,'">',field.label,'</option>');			
		}
		for(var i = 0; i < _permitFields.length; i++)
		{
			var field = _permitFields[i];
			html.push(				'<option value="mpermit_attribute.',field.name,'">',field.label,'</option>');			
		}
		
		html.push(				'</optgroup>',
								'<optgroup label="Vehicle">');
		
		for(var i = 0; i < _defaultVehicleFields.length; i++)
		{
			var field = _defaultVehicleFields[i];
			html.push(				'<option value="vehicle.',field.name,'">',field.label,'</option>');			
		}
		for(var i = 0; i < _vehicleFields.length; i++)
		{
			var field = _vehicleFields[i];
			html.push(				'<option value="vehicle_attribute.',field.name,'">',field.label,'</option>');			
		}
		
		html.push(				'</optgroup>',
								'<optgroup label="Owner">');
		
		for(var i = 0; i < _defaultOwnerFields.length; i++)
		{
			var field = _defaultOwnerFields[i];
			html.push(				'<option value="owner.',field.name,'">',field.label,'</option>');			
		}
		for(var i = 0; i < _ownerFields.length; i++)
		{
			var field = _ownerFields[i];
			html.push(				'<option value="owner_attribute.',field.name,'">',field.label,'</option>');			
		}
		
		html.push(				'</optgroup>');
								
		html.push(				'</select>',
						'</dt>',
						'<dt>',
						'&nbsp;',
						'\n',
							'<select name="mapping" id="mapping">',
								'<option value="">-- None --</option>');
		for(var i = 0; i < _citeFields.length; i++)
		{
			var field = _citeFields[i];
			html.push(				'<option value="',field.name,'">',field.label,'</option>');			
		}
		html.push(			'</select>',
						'</dt>',
						'<dd class="searchable"><input type="checkbox" class="x-form-text" id="column_searchable" name="column_searchable" value="1" /></dd>',		
						'<dd>',
							'<div class="x-tool-btn btn-up" title="Move Column Up"></div>',
							'<div class="x-tool-btn btn-down" title="Move Column Down"></div>',
							'<div class="x-tool-btn btn-remove" title="Remove Column"></div>',
						'</dd>',
					'</dl>',
				'</div>'	
		);
		
		this.options.container.insert(html.join(''));
		this.doBind();
	},
	remove: function(icon)
	{
		var parent = $(icon.parentNode.parentNode.parentNode);
		parent.remove();
	},
	up: function(icon)
	{
		var parent = $(icon.parentNode.parentNode.parentNode);
		var sibling = parent.previous('.form-row');
		if(sibling != undefined)
		{
			parent.remove();
			sibling.insert({before: parent});
		}
	},
	down: function(icon)
	{
		var parent = $(icon.parentNode.parentNode.parentNode);
		var sibling = parent.next('.form-row');
		if(sibling != undefined)
		{
			parent.remove();
			sibling.insert({after: parent});
		}
	},
	save: function(button, event)
	{
		var rows = this.options.container.select('.form-row');
		var data = {action: 'save', count: rows.length};
		var mappingMap = new Object();
		var fieldMap  = new Object();
				
		for(var i = 0; i < rows.length; i++)
		{
			var row = rows[i];
			var select = (row.select("select[name='column_name']")[0]);
			var name = select.options[select.selectedIndex].value;
			if(fieldMap[name] == undefined){
				fieldMap[name] = name;
			}else{
				Ext.Msg.show({
	 				   title:'Field Error',
	 				   msg: 'The Field Name is unique',
	 				   buttons: Ext.Msg.OK,
	 				   icon: Ext.MessageBox.ERROR
	 				});
				fieldMap = null;
				return false;
			}
			select = (row.select("select[name='mapping']")[0]);
			var mapping = select.options[select.selectedIndex].value;
			if(mapping != ""){
				if(mappingMap[mapping] == undefined){
					mappingMap[mapping] = mapping;
				}else{
					Ext.Msg.show({
		 				   title:'Field Error',
		 				   msg: 'The Citation Mapping is unique',
		 				   buttons: Ext.Msg.OK,
		 				   icon: Ext.MessageBox.ERROR
		 				});
					mappingMap = null;
					return false;
				}
			}			
			var searchable = (row.select("input[name='column_searchable']")[0]);
			if(searchable.checked)
			{
				data['searchable_'+i] = 1;
			}
			else
			{
				data['searchable_'+i] = 0;
			}
					
			data['order_'+i] = i+1;
			data['name_'+i] = name;
			data['mapping_'+i] = mapping;
		}
		
		Ext.Ajax.request({
			   url: this.options.url,
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					   Ext.growl.message('Success', 'Configuration has been saved.');
					   Ext.getCmp('hotlist-config-panel').doAutoLoad();
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
	}
};