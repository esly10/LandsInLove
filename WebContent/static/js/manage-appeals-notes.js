AppealNotesPanel = Ext.extend(Ext.Panel, {
		appeal: null,
		initComponent: function()
	    {
			var panel = this;
			var config = 
			{
				xtype: 'panel',
			    title: 'Notes',
			    id: 'citationtab-notes-' + this.appeal.citation_appeal_id,
			    padding: 5,
			    bodyCssClass: 'x-citewrite-panel-body',
			    autoScroll: true,
			    buttonAlign: 'left',
			    autoLoad : { url : _contextPath + '/citation/appealNotes', scripts : true, params: {citation_appeal_id: this.appeal.citation_appeal_id} },
			    buttons:  [{xtype:'button',
					handler: function(){
						panel.editNote(0);
					},
					text: 'Add'}]
			};
			
			Ext.apply(this, Ext.apply(this.initialConfig, config));
	        
			AppealNotesPanel.superclass.initComponent.apply(this, arguments);
	    },
	    editNote: function(note_id)
	    {
	    	var edit = "";
			var edithtml = new Ext.form.HtmlEditor({
				xtype: 'htmleditor',
				id: 'edit-appeal-note',
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
						},
						{
							xtype: 'label',
							id:"label_email_to_officer",
							name:"label_email_to_officer",
							//bodyStyle: 'padding: 10px; padding-right: 10px; padding-top: 5px;',
							text: 'As Email to Officer'			
						},
						{
							xtype: 'checkbox',
							id:'isEmail' ,
							name : 'email',
							fieldLabel: 'As Email',
							trueText: 1,
							falseText: 0  ,
							handler: function(val){
								if(Ext.getCmp("isEmail").getValue()){
									Ext.getCmp("subject").show();
									Ext.getCmp("subject_label").show();
									Ext.getCmp("to").show();
									Ext.getCmp("to-label").show();
								}else {
									Ext.getCmp("subject").hide();
									Ext.getCmp("subject_label").hide();
									Ext.getCmp("to").hide();
									Ext.getCmp("to-label").hide();
								}
							}
						 },			
						 
						 {
								xtype: 'box',
								id:"box1",
								name:"box1",
								height: 5
						 },
						 {
								xtype: 'label',
								name:"to_label",
								id:"to-label",
								hidden:true,
								//bodyStyle: 'padding: 10px; padding-right: 10px; padding-top: 5px;',
								text: 'To'			
						},
						{
							xtype: 'box',
							id:"box2",
							name:"box2",
							height: 5
						},
						{
							xtype: 'textfield',
							id: 'to',
							name: 'to',
							fieldLabel: 'To',
							hidden:true,
							value: "",
							width: 120,
							regex: /^([\w\-\'\-]+)(\.[\w-\'\-]+)*@([\w\-]+\.){1,5}([A-Za-z]){2,4}$/,
							regexText:'This field should be an e-mail address in the format "user@example.com"',
							allowBlank: false						
						},
						{
							xtype: 'box',
							height: 5,
							id:"box13",
							name:"box3",
						},
			    		 {
							xtype: 'label',
							text: 'Subject:',
							hidden:true,
							name:"subject_label",
							id:"subject_label",
							width: 130,
							listeners:{}
						},
						{
							xtype: 'box',
							height: 5,
							id:"box4",
							name:"box4",
						},
						{
							xtype: 'textfield',
							id: 'subject',
							name: 'subject',
							fieldLabel: 'Subject',
							hidden:true,
							value: "",
							width: 120,
							listeners:{keyup: function(field, e)
								{
								
								}
							}
						},
						{
							xtype: 'box',
							height: 5,
							id:"box5",
							name:"box5",
						},
						edithtml
	    		]
	    	});
	    	
	    	
	    	//formPanel.add(edithtml);
	      
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
	            id: 'appealNoteFormWindow',
	            items: formPanel,
	            //bbar:   [],
	            buttons: [{
	                text:'Save',
	                handler: function()
	                {   
	                	
	                	if(Ext.getCmp("isEmail").getValue()){
	                		if(!formPanel.getForm().isValid()){
	                			Ext.Msg.show({
         	                	   title:'Error',
         	                	   msg: 'Invalid form, please check the information.',
         	                	   buttons: Ext.Msg.OK,
         	                	   icon: Ext.MessageBox.ERROR
         	                	});
	                			return false;
	                		}
	                	}
	                	
	                	var params = formPanel.getForm().getValues();
	                	params.xaction = 'save';
	                	params.citation_appeal_id= panel.appeal.citation_appeal_id;
	                	if(params.note.trim() == ""){
	                		Ext.Msg.show({
      	                	   title:'Error',
      	                	   msg: 'Note is required.',
      	                	   buttons: Ext.Msg.OK,
      	                	   icon: Ext.MessageBox.ERROR
      	                	});
	                		
	                		return false;
	                	}
	                	
	                	Ext.Ajax.request({
	                		 url: _contextPath + '/citation/appealNotes',
	                		 scope: this,
	                		 params: params,
	                		 success: function(form, action) {
		                	    	panel.load({ url : _contextPath + '/citation/appealNotes', scripts : true, params: {citation_appeal_id: panel.appeal.citation_appeal_id} });
		                	    	Ext.getCmp("appealNoteFormWindow").close(); 
		                	    	Ext.growl.message('Success', 'Appeal Notes have been saved.');
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
	                	
	                	
	                	//validate form
	                	/*
	                	formPanel.getForm().submit({
	                	    url: _contextPath + '/citation/appealNotes',
	                	    scope: this,
	                	    params: {xaction: 'save', citation_appeal_id: panel.appeal.citation_appeal_id },
	                	    success: function(form, action) {
	                	    	panel.load({ url : _contextPath + '/citation/appealNotes', scripts : true, params: {citation_appeal_id: panel.appeal.citation_appeal_id} });
	                	    	
	                	    	var parent = action.options.scope.findParentByType('window'); 
	                	    	parent.close();
	                	       
	                	    	Ext.growl.message('Success', 'Appeal Notes have been saved.');
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
	                	});*/
	                }
	            },{
	                text: 'Close',
	                handler: function(){
	                	this.findParentByType('window').close();
	                }
	            }],
				width: 650,
                height:350,
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
	    	var container = Ext.get('citation-appeal-notes-'+this.appeal.citation_appeal_id);
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
					   url: _contextPath + '/citation/appealNotes',
					   success: function(response, opts){
						   panel.load({ url : _contextPath + '/citation/appealNotes', scripts : true, params: {citation_appeal_id: panel.appeal.citation_appeal_id} });
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
					   params: { note_id: note_id, citation_appeal_id: panel.appeal.citation_appeal_id, xaction: 'delete' }
					});
				}
			});
	    }
});


