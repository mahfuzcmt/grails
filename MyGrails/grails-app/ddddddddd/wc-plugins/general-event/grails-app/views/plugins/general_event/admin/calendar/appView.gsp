<%@ page import="com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="header-title event"><g:message code="calendar"/></span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
        </div>
    </div>
</div>

<div class="calendar-wrap">
    <div class="calendar-container"></div>
    <input type="hidden" class="dateFormat" value="${AppUtil.getConfig('locale', 'admin_date_format')}">
    <input type="hidden" class="timeFormat" value="${AppUtil.getConfig('locale', 'admin_time_format')}">
</div>