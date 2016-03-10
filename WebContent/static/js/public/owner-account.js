(function ($)
{
	
	$.fn.vTableController = function (array)
	{
		_elem = $(this);
		
		_elem.children().remove();
		for ( var i = 0; i < array.length; i++) 
		{
			var vehicle = array[i];
			var vin = vehicle.vin;
			var license = vehicle.license;
			var html = new Array(
				'<ul id="vehicle-',vehicle.vehicle_id,'" ', ((i%2==0)?'':'class="alt-row"'), '>',
					'<li style="overflow: hidden;" title="',vehicle.make,'">',vehicle.make,'</li>',
					'<li style="overflow: hidden;" title="',vehicle.color,'">',vehicle.color,'</li>',
					'<li style="overflow: hidden;" title="',vin,'">',vin.substr(0, 8),'</li>',
					'<li style="overflow: hidden;" title="',license,'">',license.substr(0, 8),'</li>',
					'<li style="width: 50px;">',((vehicle.has_permit==true)?'Yes':'No'),'</li>',
					'<li class="buttons"> <div title="Edit Vehicle" id="update" onclick="editVehicle(',i,')" class="update"></div> <div title="Delete Vehicle" onclick="deleteVehicle(',i,')" class="delete"></div></li>',
				
				'</ul>'
			);
			
			
			_elem.append(html.join(''));
		}
	
	};
	
	
	$.fn.pTableController = function (array)
	{
		_elem = $(this);
		
		_elem.children().remove();
		for ( var i = 0; i < array.length; i++) 
		{
			var permit = array[i];
			var html = new Array(
				'<ul id="permit-',permit.permit_number,'" ', ((i%2==0)?'':'class="alt-row"'), '>',	
					'<li style="width: 60px; overflow: hidden;">',permit.permit_number,'</li>',
					'<li style="width: 80px; overflow: hidden;"title="',permit.permit_description,'">',permit.permit_description,'</li>',
					'<li style="width: 50px;">',permit.permit_max_vehicles,'</li>',
					'<li style="width: 90px;overflow: hidden" title="',permit.date_expired,'">',permit.date_expired,'</li>',
					'<li class="buttons">',
						'<div title="view Permit" id="view" onclick="viewPermit(',i,')" class="view"></div>');
			if(permit.is_valid)
			{
				html.push('<div title="Edit Permit" id="update" onclick="editPermit(',permit.mpermit_id,')" class="update"></div>',
						'<div class="delete inactive"></div>');
			}
			else
			{ 
				html.push('<div id="update" class="update inactive"></div>',
						 '<div title="Delete Permit"class="delete" onclick="deletePermit(',i,')"></div>');
			}
			
			html.push('</li>',
						'</ul>');
		
			
			_elem.append(html.join(''));
		}
	
	};
	
	$.fn.pCars = function (array)
	{
		_elem = $(this);
		
		_elem.children().remove();
		for ( var i = 0; i < array.length; i++) 
		{
			var vehicle = array[i];
			var vehicleinfo = vehicle.license + ' ('+vehicle.make+')';
			var html = new Array('<div style="width:250px"> <input type="checkbox" name="mpermit-vehicle-list" value="',vehicle.vehicle_id,'"   id="mpermit-vehicle-',vehicle.vehicle_id,'" >',vehicleinfo,'</div>');
			
			_elem.append(html.join(''));
		}
	};
	
	$.fn.reset = function () 
	{
		  $(this).each (function() { this.reset(); });
	};
	
	
	
})(jQuery);



function printPermit(id)
{	
	if(!!navigator.userAgent.match(/Trident.*rv\:11\./)){ // ONLY FOR IE 11 fix  
		 	$.ajax({
				  method: "POST",
				  url: _contextPath+'/permit/print?'+id,
				}).done(function( data ) {
					var mywindow = window.open('', 'to_print', 'height=600,width=800');
				    mywindow.document.write(data);
				    mywindow.document.close();
				    mywindow.close();
				    
				});
		
	}else {
		$(document.body).append('<iframe name="print-framed" id="printf" width="0" height="0" style="margin-left: -10px;" src= "'+_contextPath+'/permit/print?'+id+'"></iframe>');
	}
	  
}



function sendForm(form_id)//add vehicle
{
	var form = $('#'+form_id);
	
	 $.ajax({type: 'post', url: _contextPath + '/vehicle/save', data: form.serialize(), success: sendFormCB,dataType: 'json' });
	
}
		
function sendFormCB(data)
{
	
	if(data.success)
	{
	
		updateVehicles(data.vehicle);
		$('#vehicle-table.body').vTableController(_vehicles);
		
		alert(data.msg);
		
		$('#form-vehicle').reset();
		cancel();
		
	}else
	{
		alert(data.msg);
	}
	
	
}



function updatePermit(form_id)
{
	var form = $('#'+form_id);
	
	 $.ajax({type: 'post', url: _contextPath + '/permit/update', data: form.serialize(), success: updatePermitCB,dataType: 'json' });
	
}
		
function updatePermitCB(data)
{
	
	if(data.success)
	{
	
		alert(data.msg);
		$('#update-vehicle-permit').children().remove();
		cancel();
		
	}else
	{
		alert(data.msg);
		$('#update-vehicle-permit').children().remove();
		cancel();
	}
	
	
}
		
		
function deleteVehicle(index)
{
	var vehicle = _vehicles[index];
	var license = vehicle.license;
	var msg = "";
	if(license.length > 0)
	{
		msg = "Do you want to remove the vehicle \""+license+"\"?";
	}
	else
	{
		msg = "Do you want to remove the vehicle \""+vehicle.vin+"\"?";
	}
	if (confirm(msg))
	{
		var data = {xaction: 'delete', vehicle_id: vehicle.vehicle_id};
		
		 $.ajax({type: 'post', url: _contextPath + '/vehicle/delete', data: data, success: function(data){ deleteVehicleCB(data, index); },dataType: 'json'});
	}
	
}
		
function deleteVehicleCB(data, index)
{
	
	if(data.success)
	{
			$('#vehicle-table.li').remove(); 
			_vehicles.splice(index,1);
			$('#vehicle-table.body').vTableController(_vehicles);
			alert(data.msg);
			$('#form-vehicle').reset();
			cancel();
			
	}
	else
	{
		alert(data.msg);
	}

}

function deletePermit(index)
{
	var permit = _permits[index];
	var permit_id = permit.mpermit_id;
	if (confirm("Do you want to delete the permit  \""+permit.permit_number+"\"?")) 
	{
		var data = {mpermit_id: permit_id};
		$.ajax({type: 'post', url: _contextPath + '/permit/delete', data: data , success: function(data){ deletePermitCB(data,index);},dataType: 'json'});
		
	}
	
}
		
function deletePermitCB(data, index)
{
	
	if(data.success)
	{
			$('#permit-table li').remove(); 
			_permits.splice(index,1);
			$('#permit-table.body').pTableController(_permits);
			alert(data.msg);
			$('#form-permit').reset();
			$('#update-vehicle-permit').children().remove();
			cancel();
			
	}
	else
	{
		alert(data.msg);
		$('#form-permit').reset();
		$('#update-vehicle-permit').children().remove();
		cancel();
	}

}
		
		
	
function editVehicle(index)
{
	
	var pvehicle = $('#popup-vehicle');
	
	var center_x = $(document).width()/2;
	var center_y = $(document).height()/2;
	
	var vehicle = _vehicles[index];
	var vpopup_x = pvehicle.width()/2;
	var vpopup_y = pvehicle.height()/2;

	$("#update-cmd").html("Update");

	$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
	$('#popup-vehicle').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
	$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
	
	
	$('#vehicle_id').val(vehicle.vehicle_id);
	$('#license').val(vehicle.license);
	$('#vin').val(vehicle.vin);
	$('#make').val(vehicle.make_id);
	$('#color').val(vehicle.color_id);
	$('#state').val(vehicle.state_id);
	$('#license').val(vehicle.license);
	
	
	
	for ( var i = 0; i < vehicle.extra.length; i++) 
	{
		var vehicleAttr = vehicle.extra[i];
		var field = vehicleAttr.name;
		$('#'+field).val(vehicleAttr.value);
	}
}
				
		
function updateVehicles(nvehicle)
{
	for ( var i = 0; i < _vehicles.length; i++) 
	{
		var vehicle = _vehicles[i];
		if (vehicle.vehicle_id == nvehicle.vehicle_id) 
		{
			var has_permit = _vehicles[i]['has_permit'];
			_vehicles[i]=nvehicle;
			nvehicle['has_permit'] = has_permit;
			return;
		}
	}
	_vehicles[_vehicles.length] = nvehicle;
}

var _editPermitMutex = false;	
function editPermit(index)
{
	if(_editPermitMutex){		
		return;
	}

	try {
	   _editPermitMutex = true;
		var permit_id = index;
		var data = {xaction: 'delete', mpermit_id: permit_id};
		$.ajax({type: 'post', url: _contextPath + '/permit/load', data: data , success: editPermitCB,dataType: 'json'});
    }finally {
		window.setTimeout(setEditPermitMutex,1000);
    }
}

function editPermitCB(data)
{
			
   
	var pPermit = $('#update-permit-popup');
	
	var center_x = $(document).width()/2;
	var center_y = $(document).height()/2;
	
	 _permit = data.permitJSON;
	var vpopup_x = pPermit.width()/2;
	var vpopup_y = pPermit.height()/2;


	$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
	$('#update-permit-popup').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
	$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
	
	$('#permit-name-update').html(_permit.types.name+' - '+_permit.types.description);
	$('#permit_id').val(_permit.mpermit_id);
	
	for ( var i = 0; i < _vehicles.length; i++) 
	{
			
		var vehicle = _vehicles[i];
		var vehicle_id = vehicle.vehicle_id;
		if(vehicle.license.length > 1)
		{
			var vehicleinfo = vehicle.license + ' ('+vehicle.make+')';
		}
		else
		{
			var vehicleinfo = vehicle.vin + ' ('+vehicle.make+')';
		}
		if(searchVehicle(vehicle_id))
		{
			var html = new Array('<div> <input type="checkbox"checked name="mpermit-vehicle-',vehicle.vehicle_id,'" id="mpermit-vehicle-',vehicle.vehicle_id,'" >',vehicleinfo,'</div>');
		}else
		{
			var html = new Array('<div> <input type="checkbox" name="mpermit-vehicle-',vehicle.vehicle_id,'" id="mpermit-vehicle-',vehicle.vehicle_id,'" >',vehicleinfo,'</div>');
			
		}
		
		
		$('#update-vehicle-permit').append(html.join(''));
		
	}
	
	
	for ( var i = 0; i < _permit.extra.length; i++) 
	{
		var permitAttr = _permit.extra[i];
		var field = $('#extra-'+permitAttr.name);
		field.val(permitAttr.value);
		
		
			
	}
	

	
}
function  searchVehicle(vehicle_id)
{
	for ( var cont = 0; cont < _permit.vehicles.length; cont++) 
	{
		var vehiclePermit = _permit.vehicles[cont];
		if(vehicle_id == vehiclePermit.vehicle_id)
		{
			return true;
		};
			
	};
	return false;
}
				

function viewPermit(index)
{
	var permit = _permits[index];
	var permit_id = permit.mpermit_id;
	
	$('#popup-permit-view').load( _contextPath + '/permit/view', {mpermit_id: permit_id},function(){ viewPermitCB();});
	
}


function viewPermitCB(data)
{
	var center_x = $(document).width()/2;
	var center_y = $(document).height()/2;
	
	var pPermit = $('#popup-permit-view');
	var vpopup_x = pPermit.width()/2;
	var vpopup_y = pPermit.height()/2;


	$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
	$('#popup-permit-view').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
	$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
	

}




	
$(document).ready(function(){
	
	$("#license").limitkeypress({ rexp: /^[^\W]*$/});
	
	$("#vin").keydown(function(event) {
		
		var charCode = event.which;
		var vinFirstGroup = /^([a-h,A-H,j-n,J-N,p-z,P-Z,0-9])*$/;	
		var vinSecondGroup = /^([a-h,A-H,j-n,J-N,p,P,r-t,R-T,v-z,V-Z,0-9])*$/;	
		var vinThirdGroup = /^([a-h,A-H,j-n,J-N,p-z,P-Z,0-9])*$/;
		var vinFourthGroup = /^([0-9])+$/;
		var content = $("#vin").val();
		
		if ( event.which == 13 || event.which > 105) {
		     event.preventDefault();
		 }
			
	    if (!charCode || (content == undefined)) {
	        return;// return false, optionally
	    }
	
		try{
			var code = event.keyCode ? event.keyCode : event.which;
			if(code!==46 && code!==8 && code!==37 && code!==39){
			
				if(charCode>95 && charCode<106){
					charCode = charCode - 48;
				}
			
	 			content = content + String.fromCharCode(charCode).toLowerCase();
				if(content != undefined && content.length < 9){
					if(content.substr(0,content.length).match(vinFirstGroup) == undefined){
						return false;
					}
				}else if(content != undefined && content.length == 9){
					if(content.substr(9-1,content.length).match(vinSecondGroup) == undefined){
						return false;
					}	
				}else if(content != undefined && content.length == 10){
					if(content.substr(10-1,content.length).match(vinThirdGroup) == undefined){
						return false;
					}	
				}else if(content != undefined && (content.length >= 11 && content.length<= 16)){
					if(content.substr(11-1,content.length).match(vinFourthGroup) == undefined){
						return false;
					}
				}	
			}
		}catch(e){}

	});
	
	$('#vehicle-table').vTableController(_vehicles);
	$('#permit-table').pTableController(_permits);
	$('#vehicle-list').pCars(_vehicles);
	
	
	$(document.body).append($('#popup-vehicle').detach());
	
	$('#add-Vehicle').click(function(){
		
		
		var pvehicle = $('#popup-vehicle');
		
		var center_x = $(document).width()/2;
		var center_y = $(document).height()/2;
				
		var vpopup_x = pvehicle.width()/2;
		var vpopup_y = pvehicle.height()/2;
		
		$('#form-vehicle').reset();
		$('#vehicle_id').val("0");
		$(document.body).append('<div class="popup-mask" id="popup-mask"></div>');
		$('#popup-vehicle').show().css({top:center_y-vpopup_y , left: center_x-vpopup_x});
		$('#popup-mask').css({width:center_x *2 +'px',height:center_y*2+'px'});
		$("#update-cmd").html("Submit");
		
	});
	
	$('#vehicle-close').click(function(){
		$('#popup-vehicle').hide();
		$('#popup-mask').remove();
	});
	
	$('#permit-close').click(function(){
		$('#popup-permit').hide();
		$('#popup-mask').remove();
	});
	$('#update-permit-close').click(function(){
		$('#update-permit-popup').hide();
		$('#update-vehicle-permit').children().remove();
		$('#popup-mask').remove();
	});
	
	$('#view-permit-close').click(function(){
		$('#popup-permit-view').hide();
		$('#permit_extra').children().remove();
		$('#popup-mask').remove();
	});
	
	$('#cancel').click(function(){
		$('#popup-permit').hide();
		$('#popup-mask').remove();
	});
	
		
	$('#mpermit-type-id').change (function (){
		
		var option = this.options[this.selectedIndex];
		var cost = option.getAttribute("cost");
		$('#permit-amount').html(cost);
	});	
	
});
	
function cancel()
{

	if($('#update-vehicle-permit').length> 0 && $('#update-vehicle-permit').css('display') !== 'none'){
		$('#update-vehicle-permit').children().remove();
	}

	$('#popup-vehicle').hide();
	$('#popup-permit').hide();
	$('#popup-permit-view').hide();
	$('#update-permit-popup').hide();
	$('#popup-mask').remove();
}	

function setEditPermitMutex(){
	_editPermitMutex = false;
}