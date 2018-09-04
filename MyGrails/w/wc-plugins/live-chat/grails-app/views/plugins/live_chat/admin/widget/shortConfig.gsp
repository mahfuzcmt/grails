<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <span class="sidebar-group-label"><g:message code="online.text"/></span>
        <div class="sidebar-group-body">
            <input type="text" name="online_text" class="sidebar-input" value="${config.online_text}" validation="required">
        </div>
    </div>
    <div class="sidebar-group">
        <span class="sidebar-group-label"><g:message code="offline.text"/></span>
        <div class="sidebar-group-body">
            <input type="text" name="offline_text" class="sidebar-input" value="${config.offline_text}" validation="required">
        </div>
    </div>
</g:applyLayout>