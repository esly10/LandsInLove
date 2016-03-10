<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>


<div class="citation-notes" id="citation-appeal-notes-<c:out value="${citationAppeal.citationAppealId}" />">
	<c:if test="${empty citationAppeal.notes}">
	    <div class="note" id="note-text" /> No notes found.</div>
	</c:if>
	<c:forEach items="${citationAppeal.notes}" var="note">
		<a id="note-<c:out value="${note.id}" />"></a>
		<div class="citation-note">
			<div class="note" id="note-text-<c:out value="${note.id}" />">${note.note}</div>
			<div class="updated"><b>Updated:</b> <c:out value="${note.updated}" /> by <c:out value="${note.updatedBy}" /></div>
			<div class="created"><b>Created:</b> <c:out value="${note.created}" /> by <c:out value="${note.createdBy}" /></div>
			
			<div class="button edit" note-id="<c:out value="${note.id}" />"></div>
			<div class="button delete" note-id="<c:out value="${note.id}" />"></div>
		</div>
	</c:forEach>
</div>
<script type="text/javascript">
	var tabs = Ext.getCmp('appealtabs');
	var appealtab = tabs.getComponent('appealtab-<c:out value="${citationAppeal.citationAppealId}" />');
	var cmp = appealtab.getComponent('citationtab-notes-<c:out value="${citationAppeal.citationAppealId}" />');
	cmp.bindActions();
</script>
