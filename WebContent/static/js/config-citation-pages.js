
	function move(icon, direction)
	{
		var object = this;
		this.currentRow = $(icon.parentNode.parentNode.parentNode);
		
		var page = $(this.currentRow.parentNode.parentNode.parentNode);
		page = Ext.getCmp(page.id);
		 
		 
		var numPage = page.title.slice(5,7);// only 2 last characters
   	  	numPage = numPage-1;
		
		var name = this.getFieldName(this.currentRow);
		var dir = '';
		if(direction == 1)	//1 = up, 0 = down
		{
			var sibling = this.currentRow.previous('.form-row');
			dir = 'up';
		}
		else
		{
			var sibling = this.currentRow.next('.form-row');
			dir = 'down';
		}
		
		if(sibling != undefined)
		{
			
			Ext.Ajax.request({
			   url: _contextPath + '/administration/citationDevice',
			   params: { action: 'move', 'field-name': name, 'page_num': numPage,'dir': dir },
			   success: function(p1, p2)
			   {
				   var response = Ext.decode(p1.responseText);
				   if(response.success)
				   {
					  object.currentRow.remove();
					   if(direction == 1) 
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
			});  
		}
	}

	function getFieldName(row)
	{
		var inputs = row.select("input[name='field_name']");
		if(inputs.length > 0)
		{
			return inputs[0].value;
		}
		
		return "";
	}
	
	function removeFiel(icon)
	{
		
		var parent = $(icon.parentNode.parentNode.parentNode);
		var name = this.getFieldName(parent);
		
		var page = $(parent.parentNode.parentNode.parentNode);
		var page = Ext.getCmp(page.id);
		
		 Ext.Msg.confirm("Remove?", "Remove field?", function(bid, p2){
			  if(bid == "yes")
			  {
				  
				  var numPage = page.title.slice(5,7);// only 2 last characters
	        	  numPage = numPage-1;
				  
				  Ext.Ajax.request({
					   params: { action: 'deleteFild', 'page_num': numPage, 'field-name':name},
					   url: _contextPath + '/administration/citationDevice',
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
						   msg: 'Error delete fild.',
						   buttons: Ext.Msg.OK,
						   icon: Ext.MessageBox.ERROR
						});
					   },
					   
					}); 
				  
			  }
		  });
	}
	
	
function openPagesWindow(id) {
	
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
				   store: new Ext.data.JsonStore({
		                idIndex: 0,
		                autoDestroy: true,
		                fields: ['name', 'label'],
		                }),
				    displayField: 'label',
				    valueField: 'name',
					triggerAction: 'all',
					forceSelection: true,
					mode: 'local',
					listeners: {
						select: function(combo, record, index)
						{
														
						}
					}
		       }]
		};
	 
	
	 var formPanel = new Ext.FormPanel(fp);
	 var object = this;
	 var codeWindowPage = new Ext.Window({
         renderTo: document.body,
         id: 'citeFieldPageWindow',
         title: 'Add Citation Field',
         height: 150,
         width: 325,
         maxHeight: 150,
         layout: 'fit',
         closeAction:'close',
         plain: true,
         resizable: false,
         modal: true,
         autoScroll: true,
         items: formPanel,
         buttons: [{
             text:'Save',
             handler: function()
             {
            	 var page = '';
            	
            	 if(id.id == undefined){
            		 page = Ext.getCmp(id);
            	 }else {
            		 page = Ext.getCmp(id.id);
            	 }
            	 
            	  var numPage = page.title.slice(5,7);// only 2 last characters
            	  numPage = numPage-1;
            	  var fieldName = Ext.getCmp("form_field_name").getValue();
            	 
             	//validate form
             	formPanel.getForm().submit({
             	    url: _contextPath + '/administration/citationDevice',
             	    scope: this,
             	    params: { action: 'addFiled', 'page_num': numPage, 'field-name': fieldName },    	   
             	    success: function(form, action) {
             	    	
             	    	var response = Ext.decode(action.response.responseText);
             	    	
             	    	if(response.success)
             	    	{
             	    		
             	    		var field = response.field;
             	    		if(field.label == ''){
             	    			loadPages();
             	    		}else {
             	    			addRow(field); 
             	    		}
             	    		
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
		
	   codeWindowPage.show();
	   
	   
	   
	function addRow(field)
	{		
		var html = '<div class="form-row">'+
					'<dl class="config-form">'+
						'<dt>'+field.label+'<input type="hidden" id="field_name" name="field_name" value="'+field.name+'" /></dt>'+
						'<dd>'+
							'<div class="x-tool-btn btn-up" title="Move Field Up" onclick="move(this,1); return false;"></div>'+
							'<div class="x-tool-btn btn-down" title="Move Field Down" onclick="move(this, 0); return false;"></div>'+
							'<div class="x-tool-btn btn-remove" title="Remove Field" onclick="removeFiel(this); return false;"></div>'+
						'</dd>'+
					'</dl>'+
				'</div>';
				

		if(id.id == undefined){
			var content = Ext.getCmp(id).body.dom.innerHTML;
			Ext.getCmp(id).body.update(content+ html);
			Ext.getCmp("configCitationPages").doLayout();
		} else {
			var content = Ext.getCmp(id.id).body.dom.innerHTML;
			Ext.getCmp(id.id).body.update(content+ html);
			Ext.getCmp("configCitationPages").doLayout();
		}
			
	}
			 
	
	// populate combo id: form_field_name
	Ext.Ajax.request({
		url : _contextPath + '/administration/citationDevice',
		params: {action: 'listFields'}, 
		success : function(p1, p2) {

			 var response = Ext.decode(p1.responseText);
			   if(response.success)
			   {
				   var data = response.result;
				   Ext.getCmp('form_field_name').getStore().loadData(data, false);			   
				   
			   } 
			
			
		}
	});
	
	
}
	
function create_page(){
	
	 var index = Ext.getCmp("configCitationPages").items.length;
	 if(index > 14){
		 alert('You may create up to 15 pages.');
		 return false;
	 }
	 
	 
	Ext.Ajax.request({
		   params: { action: 'createPage'},
		   url: _contextPath + '/administration/citationDevice',
		   success: function(p1, p2)
		   {
			   var response = Ext.decode(p1.responseText);
			   if(response.success)
			   {
				   			   
				var d = new Date();
				var id = d.getTime();
				 
				index = ++index;
				id = id+index;
				var item = new Ext.Panel({
				    xtype: 'panel',
				    bodyStyle: 'padding-left: 10px;',
				    title: 'Page '+ index,
				    id: id,
				    border: false,
					autoHeight: true,
					bodyCssClass: 'x-citewrite-panel-body',
					tools:[{
					    type:'close',
					    qtip: 'close',
					    // hidden:true,
					    handler: function(){ //event, toolEl, panel
				   	
					    	remove_page(id);
					    	
					    }
					}],
					html:'<div class="form-header" id=""><br>'+
							'<dl class="config-form">'+
								'<dt><b>Field Name</b></dt>'+
								'<dd><div class="x-tool-btn btn-add" title="Add Column" id="" onclick="openPagesWindow('+id+'); return false;" ></div></dd>'+
							'</dl>'+
						'</div>',												
				 });
				//item.render(document.body);  // toolbar is rendered
				Ext.getCmp("configCitationPages").add(item);
				Ext.getCmp("configCitationPages").doLayout();
					
				   
	
				   Ext.growl.message('Success', 'Page has been created.');
			   }
			   
		   },
		   failure: function(){
			   Ext.Msg.show({
			   title:'Failure',
			   msg: 'Error create page.',
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});
		   },
		   
		}); 
	 
}

function create_pages(){
	
	 var index = Ext.getCmp("configCitationPages").items.length;
	 if(index > 14){
		 alert('You may create up to 15 pages.');
		 return false;
	 }
	 
	 var d = new Date();
		var id = d.getTime();
		 
		index = ++index;
		id = id+index;
		var item = new Ext.Panel({
		    xtype: 'panel',
		    bodyStyle: 'padding-left: 10px;',
		    title: 'Page '+ index,
		    id: id,
		    border: false,
			autoHeight: true,
			bodyCssClass: 'x-citewrite-panel-body',
			tools:[{
			    type:'close',
			    qtip: 'close',
			    // hidden:true,
			    handler: function(){ //event, toolEl, panel
		   	
			    	remove_page(id);
			    	
			    }
			}],
			html:'<div class="form-header" id=""><br>'+
			'<dl class="config-form">'+
				'<dt><b>Field Name</b></dt>'+
				'<dd><div class="x-tool-btn btn-add" title="Add Column" id="" onclick="openPagesWindow('+id+'); return false;" ></div></dd>'+
			'</dl>'+
			'</div>',												
		 	 });
		//item.render(document.body);  // toolbar is rendered
		Ext.getCmp("configCitationPages").add(item);
		Ext.getCmp("configCitationPages").doLayout();
		
		
		return(id);
	 
}

function remove_page(id){
	
	var page = Ext.getCmp(id);
	index = Ext.getCmp("configCitationPages").items.length;
	
	if(index <= 1 ){
		Ext.growl.message('Message', 'The first page can not be removed.');
		return;
	}
	
	Ext.Msg.confirm("Remove?", "Remove "+page.title+"?", function(bid, p2){
		  if(bid == "yes")
		  {
			  
			  var numPage = page.title.slice(5,7);// only 2 last characters
        	  numPage = numPage-1;
        	  
			  Ext.Ajax.request({
				   params: { action: 'deletePage', 'page_num': numPage},
				   url: _contextPath + '/administration/citationDevice',
				   success: function(p1, p2)
				   {
					   var response = Ext.decode(p1.responseText);
					   if(response.success)
					   {
			
						   	  Ext.getCmp("configCitationPages").remove(id, true);
				              Ext.getCmp("configCitationPages").doLayout();
				                 
				              var count =1;
				              for ( var i = 0; i < index; i++ ) {
				              	
				              	Ext.getCmp("configCitationPages").items.items[i].setTitle('page '+count);
				              	count++;
				              }
				              
				              Ext.growl.message('Success', 'Page has been removed.');
					   }
					   
				   },
				   failure: function(){
					   Ext.Msg.show({
					   title:'Failure',
					   msg: 'Error delete page.',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
					});
				   },
				   
				});
	
		  }
	  });
	
}

function loadRow(id, field)
{
	
	var html = '<div class="form-row">'+
				'<dl class="config-form">'+
					'<dt>'+field.label+'<input type="hidden" id="field_name" name="field_name" value="'+field.name+'" /></dt>'+
					'<dd>'+
						'<div class="x-tool-btn btn-up" title="Move Field Up" onclick="move(this,1); return false;"></div>'+
						'<div class="x-tool-btn btn-down" title="Move Field Down" onclick="move(this, 0); return false;"></div>'+
						'<div class="x-tool-btn btn-remove" title="Remove Field" onclick="removeFiel(this); return false;"></div>'+
					'</dd>'+
				'</dl>'+
			'</div>';
			

	if(id != undefined){
		var content = Ext.getCmp(id).body.dom.innerHTML;
		Ext.getCmp(id).body.update(content+ html);
		Ext.getCmp("configCitationPages").doLayout();
	} 
		
}

function loadPages(){
	
	Ext.Ajax.request({
		url : _contextPath + '/administration/citationDevice',
		params: {action: 'loadPages'}, 
		success : function(p1, p2) {

			 var response = Ext.decode(p1.responseText);
			   if(response.success)
			   {
				   var obj = response.result;
				   
				   Ext.getCmp('configCitationPages').removeAll(true);
				   
				   if(obj.length == ''){
					   create_pages();
				   }else {
					   for(var i = 0; i < obj.length; i++){
						   
						   var new_page = ''; 
						   new_page = create_pages();
						   
						   for(var j = 0; j < obj[i].length; j++){
							   loadRow(new_page, obj[i][j]);
						   }   
						   
		
						}
				   }
				   /*
				   $.each( obj, function( key, value ) {

					   var new_page = ''; 
					   new_page = create_pages();
					   
					   $.each(value, function( index, val ) {		 
					 		 
					   });
				   
				   });
				   	*/		
			   } 
			
			
		}
	});
	
}

CiteConfigurationPagePanel = Ext.extend(Ext.Panel, {
	initComponent : function()
	{	
		var firtPage = 'CiteFirtPage';
		var config = 
		{
			xtype: 'panel',
			id: 'panel_device_view',
			title: 'Device view',
			bodyCssClass: 'x-citewrite-panel-body',
			autoScroll: true,
			
			listeners: {
				activate: function(e, a) { //render activate
		        	loadPages();
		        }
		    },
			width:500,
			items: [
			        	{
			        		xtype: 'panel',
							id: 'configCitationPages',
							layout: 'accordion',
							border: false,
							width:500,
							tbar:[{
								text: 'Add Page',
								id:'add-page-button',	
								listeners: {
									click:{
				                        fn:function () { 
	
				                        	create_page();
				                      				                        
				                        }
				                    }
				                }
								
								}],
							layoutConfig: {
											titleCollapse: false,
											closable: true,
											animate: true
							 }/*,
							items: [
										{
											xtype: 'panel',
											title: 'Page 1',
											id:firtPage,
											border: false,  		   
											autoHeight: true,
											bodyCssClass: 'x-citewrite-panel-body',
											html:'<div class="form-header"><br>'+
											'<dl class="config-form">'+
												'<dt><b>Field Name</b></dt>'+
												'<dd><div class="x-tool-btn btn-add" title="Add Column" id="" onclick="openPagesWindow('+firtPage+'); return false;" ></div></dd>'+
											'</dl>'+
											'</div>',
										}
									] */
			        	}
			        ]
		}

		Ext.apply(this, Ext.apply(this.initialConfig, config));

		CiteConfigurationPagePanel.superclass.initComponent.apply(this, arguments);
	}
	
});


