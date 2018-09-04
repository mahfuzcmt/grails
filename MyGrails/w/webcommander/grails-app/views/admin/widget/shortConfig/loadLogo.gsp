<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="type"/></div>
        <div class="sidebar-group-body">
            <ui:namedSelect name="type" class="sidebar-input" key="${[image: "image", text: "text"]}" value="${config.type}" toggle-target="type"/>
        </div>
    </div>
    <div class="sidebar-group type-text">
        <div class="sidebar-group-label"><g:message code="text"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="text" value="${config.text.encodeAsBMHTML()}">
        </div>
    </div>
    <div class="sidebar-group type-image">
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
