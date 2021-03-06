<%@ page import="com.webcommander.admin.ConfigService" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="bmui-tab left-side-header" ${params.active ? "active='" + params.active + "'" : ""}>
    <g:set var="tabs" value="${ConfigService.tabs}"/>
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="calendar" data-tabify-url="${app.relativeBaseUrl() + 'simplifiedEventAdmin/loadCalenderView'}">
            <span class="title"><g:message code="calendars"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="event" data-tabify-url="${app.relativeBaseUrl() + 'simplifiedEventAdmin/loadEventView'}">
            <span class="title"><g:message code="events"/></span>
        </div>
    </div><div class="bmui-tab-body-container">
        <div id="bmui-tab-calendar">
        </div>
        <div id="bmui-tab-event">
        </div>
    </div>
</div>
