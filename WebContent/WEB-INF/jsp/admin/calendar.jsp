<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cambiolabs.citewrite.data.*" %>

<%! private int Contador = 0; %>
<meta charset="utf-8">

<link href="<c:url value="/static/js/calendar/css/jquery-ui.css" />" rel="stylesheet" type="text/css" />
<link href="<c:url value="/static/js/calendar/css/jquery.ui.theme.css" />" rel="stylesheet" type="text/css" />

<link href="<c:url value="/static/js/calendar/css/timelineScheduler.css" />" rel="stylesheet" type="text/css" />
<link href="<c:url value="/static/js/calendar/css/timelineScheduler.styling.css" />" rel="stylesheet" type="text/css" />
<link href="<c:url value="/static/js/calendar/css/calendar.css" />" rel="stylesheet" type="text/css" />
        

<div class="calendar"></div>
<div class="realtime-info"></div>

<script type="text/javascript">

$jQuery(document).ready(Calendar.Init);
        
</script>					