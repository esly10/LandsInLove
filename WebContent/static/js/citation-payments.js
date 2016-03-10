CitationPayments = Ext.extend(Ext.Panel, {
		citation: null,
		initComponent: function()
	    {
			var panel = this;
			var config = 
			{
				xtype: 'panel',
			    title: 'Notes',
			    id: 'citationtab-notes-' + this.citation_id,
			    padding: 5,
			    bodyCssClass: 'x-citewrite-panel-body',
			    autoScroll: true,
			    buttonAlign: 'left',
			    autoLoad : { url : _contextPath + '/citation/notes', scripts : true, params: {citation_id: this.citation_id} },
			    buttons:  [{xtype:'button',
					handler: function(){
						panel.editNote(0);
					},
					text: 'Add'}]
			};
			
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			CitationPayments.superclass.initComponent.apply(this, arguments);
	    },
	    editNote: function(note_id)
	    {
			var edithtml = new Ext.form.HtmlEditor({
				xtype: 'htmleditor',
				id: 'edit-note-note',
				name: 'note',
				bodyStyle:'overflowY: auto',
				overflowY: 'auto',
				maximizable: true,
				enableColors: false,
				enableAlignments: false,
				enableSourceEdit: false,
				enableLists: false,
				enableFont: false,
				enableFontSize: false,	
				enableLinks:true,
				defaultValue:'',
				enableFormat:false,
				width: 600,
                height: 300				
			});
			
	    	Ext.QuickTips.init();
	    	var formPanel = new Ext.form.FormPanel({
	    		bodyBorder: false,
	    		border: false,
	    		frame: false,
	    		padding: '10px',
	    		bodyCssClass: 'x-citewrite-panel-body',
				width: 600,
                height: 300,
	    		layout: {
	                type: 'fit'
	            },
	    		items: [{
	    				xtype: 'hidden',
	    				id: 'edit-note-id',
	    				name: 'note_id',
	    				value: note_id
				},edithtml]
	    	});
	      
	    	var title = "Add ";
	    	if(note_id > 0)
	    	{
	    		title = "Edit ";
	    	}
	    	
	    	title += " Note";
	    	
	    	var panel = this;
	    	var citationWindow = new Ext.Window({
	            renderTo: document.body,
	            title: title,
	            plain: true,
	            resizable: true,
	            autoScroll: true,
	            modal: true,
	            id: 'editcitationNoteFormWindow',
	            items: formPanel,
	            buttons: [{
	                text:'Save',
	                handler: function()
	                {   
	                	//validate form
	                	formPanel.getForm().submit({
	                	    url: _contextPath + '/citation/notes',
	                	    scope: this,
	                	    params: {xaction: 'save', citation_id: panel.citation_id },
	                	    success: function(form, action) {
	                	    	panel.load({url : _contextPath + '/citation/notes#note-'+action.result.citation.citation_note_id, scripts : true, params: {citation_id: panel.citation_id }});
	                	    	
	                	    	var parent = action.options.scope.findParentByType('window'); 
	                	    	parent.close();
	                	       
	                	    	Ext.growl.message('Success', 'Citation Notes have been saved.');
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
				width: 650,
                height: 350,
				listeners : {
					resize: function(obj,width, height){
							this.doLayout();
							var w = this.getWidth();
							var h = this.getHeight();
							formPanel.setWidth((w - 30));
							edithtml.setWidth((w - 30));
							formPanel.setHeight((h - 70));
							edithtml.setHeight((h - 70));
							this.doLayout(); 
							
					},
					afterrender: function(obj)	{
							this.doLayout();
							var w = this.getWidth();
							var h = this.getHeight();
							formPanel.setWidth((w - 30));
							edithtml.setWidth((w - 30));
							formPanel.setHeight((h - 70));
							edithtml.setHeight((h - 70));
							this.doLayout(); 
							
					}				
				}
	        });
	    	
	    	citationWindow.show();
	    	citationWindow.center();
	    	if(note_id > 0)
	    	{
	    		var n = Ext.get('note-text-'+note_id);
	    		Ext.getCmp('edit-note-note').setValue(n.dom.innerHTML);
	    	}
	    },
	    bindActions: function()
	    {
	    	var panel = this;
	    	var container = Ext.get('citation-notes-'+this.citation_id);
	    	var edits = container.query('.edit');
	    	Ext.each(edits, function(button, index){
	    		button.on("click", function(){
	    			var note_id = this.getAttribute("note-id");
	    			panel.editNote(note_id);
	    		});
	    	});
	    	var deletes = container.query('.delete');
	    	Ext.each(deletes, function(button, index){
	    		button.on("click", function(){
	    			var note_id = this.getAttribute("note-id");
	    			panel.deleteNote(note_id);
	    		});
	    	});
	    	
	    },
	    deleteNote: function(note_id)
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


