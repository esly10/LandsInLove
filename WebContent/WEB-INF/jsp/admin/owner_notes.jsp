<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>


<div class="owner-notes" id="owner-notes-<c:out value="${owner.ownerId}" />">
	<c:forEach items="${owner.notes}" var="note">
		<a id="note-<c:out value="${note.id}" />"></a>
		<div class="owner-note">
			<div class="note" id="note-text-<c:out value="${note.id}" />">${note.note}</div>
			<div class="updated"><b>Updated:</b> <c:out value="${note.updated}" /> by <c:out value="${note.updatedBy}" /></div>
			<div class="created"><b>Created:</b> <c:out value="${note.created}" /> by <c:out value="${note.createdBy}" /></div>
			
			<div class="button edit" note-id="<c:out value="${note.id}" />"></div>
			<div class="button delete" note-id="<c:out value="${note.id}" />"></div>
		</div>
	</c:forEach>
</div>
<script type="text/javascript">
	var tabs = Ext.getCmp('ownertabs');
	var ownertab = tabs.getComponent('ownertab-<c:out value="${owner.ownerId}" />');
	var cmp = ownertab.getComponent('ownertab-notes-<c:out value="${owner.ownerId}" />')
	cmp.bindActions();
</script>