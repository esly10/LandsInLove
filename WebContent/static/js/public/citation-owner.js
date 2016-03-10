function sendCitation (citationId)
{
	
	var data = { citation_id: citationId};
	$.ajax({type: 'post', url: _contextPath + '/citation/details', data: data });
}