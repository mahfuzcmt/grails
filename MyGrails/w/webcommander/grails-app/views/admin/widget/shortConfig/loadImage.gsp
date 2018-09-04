<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="alt.text"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="alt_text" value="${config.alt_text.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="hyperlink.url"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="hype_url" value="${config.hype_url.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="link.target"/></div>
        <div class="sidebar-group-body">
            <g:select class="sidebar-input" name="link_target" from="${['_self', '_blank', '_parent', '_top']}" keys="${['_self', '_blank', '_parent', '_top']}" value="${config.link_target}" />
        </div>
    </div>
</g:applyLayout>
