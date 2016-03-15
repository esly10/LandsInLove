var agencyStore = null;	
var paymentStore = null;
var guestsStore = null;
var roomsStore = null;
var chargesStore = null;
agencyStore= new Ext.data.JsonStore({
		url: _contextPath + '/agency/list',
		root: 'agencies',		
        totalProperty: 'count',
        remoteSort: true,
        autoLoad: true,
        fields: [ 
                  'agency_id',
                  'agency_name', 
                  'agency_identification', 
                  'agency_address', 
                  'agency_zip', 
                  'agency_country', 
                  'agency_phone', 
                  'agency_email',
                  'agency_fax', 
                  'agency_type', 
                  'agency_web_site', 
                  'agency_notes'
                 ],
	     sortInfo: {
			field: 'agency_id',
			direction: 'ASC'
		}
    });
	
	


	paymentStore = new Ext.data.JsonStore({
		url: _contextPath + '/payments/list',     
		root: 'payments',     
        totalProperty: 'count',
        remoteSort: true,
        fields: [ 
                'payment_id',
                'reservation_id',
                'payment_date',
                'payment_method',
                 'receive_date',
                 'transaction_no',
                 'back_account',
                 'amount',
                 'bill_to',
                 'payment_notes',
        ],
        sortInfo: {
			field: 'payment_date',
			direction: 'DESC'
		},
		autoLoad: {}
    });
	
	guestsStore = new Ext.data.JsonStore({
		root: 'guests',
		url: _contextPath + '/guests/list',
		totalProperty: 'count',
		remoteSort: true,
		autoLoad: true,
		fields: [ 
		          	'guest_id',
		          	'name', 
		          	'dni', 
		          	'title', 
		          	'address', 
		          	'zip', 
		          	'country', 
		          	'phone',
		          	'email', 
		          	'mobile', 
		          	'fax', 
		          	'notes',
		          	'market', 
		          	'creation_date', 
		          	'type'
		        ],
		
		sortInfo: {
				field: 'guest_id',
				direction: 'ASC'
			}
	});

	
	  // create the Data Store
    roomsStore = new Ext.data.JsonStore({
		url: _contextPath + '/rooms/availableList',
		root: 'rooms',
        totalProperty: 'count',
        remoteSort: true,
        fields: [
            'ROOM_ID',
            'ROOM_NO',
            'ROOM_TYPE',
            'STATUS',
            'LOCATION_X',
            'LOCATION_Y'		            		            
        ],
		sortInfo: {
			field: 'ROOM_NO',
			direction: 'DESC'
		},
		baseParams: {IS_DELETE: 0 }
    });
    
    
    arrayRoomsStore = new Array();
    /*roomsStore.load({
		params:{start:0, limit: 1000},
		callback: function () {
			roomsStore.each(function(record,id){
				 var dataRoom = new Array(record.data.ROOM_ID.toString(), record.data.ROOM_NO.toString(), record.data.ROOM_TYPE.toString());
				arrayRoomsStore.push(dataRoom);
			});
			
        }
     });*/
    
	
	 // create the Data Store
    chargesStore = new Ext.data.JsonStore({
        // destroy the store if the grid is destroyed
       // autoDestroy: true,
        root: 'charges',
        url: _contextPath + '/reservation/chargeList',
        fields: [
                 // the 'name' below matches the tag name to read, except 'availDate'
                 // which is mapped to the tag 'availability'
                 'charge_id',
                 'charge_reservation_id',
                 'charge_date',
                 'charge_item_name',
                 'charge_item_desc',
                 'charge_qty',
                 'charge_rate',
                 'charge_total',
                 'charge_folio'
        ]
    });
    
