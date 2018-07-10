<g:applyLayout name="_widgetShortConfig">
    <input class="sidebar-input" type="hidden" name="latitude" value="${config.latitude}">
    <input class="sidebar-input" type="hidden" name="longitude" value="${config.longitude}">
    <input class="sidebar-input" type="hidden" name="address" value="${config.address}">
    <input class="sidebar-input" type="hidden" name="pin_url" value="${config.pin_url}">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="api.key"/></div>
        <input class="sidebar-input" type="text" name="api_key" validation="required"  placeholder="<g:message code="api.key"/>" value="${config.api_key}">
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="address"/></div>
        <input class="pac-input" type="text" value="${config.address}">
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="zoom"/></div>
        <input class="sidebar-input" type="text" name="zoom" validation="number" restrict="numeric" placeholder="<g:message code="zoom"/>" value="${config.zoom}">
    </div>

    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="radius"/></div>
        <input class="sidebar-input" type="text" name="radius" validation="number" restrict="numeric" placeholder="<g:message code="radius"/>" value="${config.radius}">
        <span class="unit"><g:message code="meters"/></span>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="popup.text"/></div>
        <textarea class="sidebar-input" name="popup_text">${config.popup_text}</textarea>
    </div>
    <div class="config-btns">
        <span class="advance-config-btn"><input type="button" value="<g:message code="upload.custom.pin"/>"></span>
        <span class="reset-pin" title="<g:message code="reset.pin"/>"></span>
    </div>
</g:applyLayout>