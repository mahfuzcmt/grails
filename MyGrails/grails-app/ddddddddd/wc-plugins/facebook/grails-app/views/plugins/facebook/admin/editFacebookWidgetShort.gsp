<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show.hide"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="tab_enabled" value="true" uncheck-value="false" ${config.tab_enabled == "true" ? "checked='checked'" : "" }>
            <label><g:message code="add.to.page"/> </label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="share_enabled" value="true" uncheck-value="false" ${config.share_enabled == "true" ? "checked='checked'" : "" }>
            <label><g:message code="fb.share"/> </label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="invite_enabled" value="true" uncheck-value="false" ${config.invite_enabled == "true" ? "checked='checked'" : "" }>
            <label><g:message code="fb.invite"/> </label>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="fb.invite.message"/> </div>
        <textarea class="sidebar-input" name="invite_message">${config.invite_message.encodeAsBMHTML()}</textarea>
    </div>
</g:applyLayout>