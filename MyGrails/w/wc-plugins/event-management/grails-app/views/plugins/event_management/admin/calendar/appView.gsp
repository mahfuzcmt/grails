<%@ page import="com.webcommander.util.AppUtil" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-left">
        <span class="header-title event"><g:message code="calendar"/></span>
    </div>
</div>
<div class="calendar-type-wrap">
    <span class="calendar-type link active" type='public'><g:message code="public.calendar"/></span>
    <span class="calendar-type link" type='venue'><g:message code="venue.calendar"/></span>
    <span class="calendar-type link" type='equipment'><g:message code="equipment.calendar"/></span>
    <span class="selector"></span>
</div>
<div class="calendar-wrap">
    <div class="calendar-container"></div>
    <input type="hidden" class="dateFormat" value="${AppUtil.getConfig('locale', 'admin_date_format')}">
    <input type="hidden" class="timeFormat" value="${AppUtil.getConfig('locale', 'admin_time_format')}">
</div>