<%@ page import="com.webcommander.util.StringUtil" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <input type="checkbox" name="quick_cart" class="sidebar-input single" value="true" uncheck-value="false" ${config.quick_cart == "true" ? "checked" : ""}>
        <label>
            <g:message code="enable.quick.cart"/>
        </label>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label">
            <g:message code="text"/>
        </div>
        <input type="text" name="text" value="${config.text.encodeAsBMHTML()}" class="sidebar-input">
        <span class="note"><g:message code="supported.macros.are"/> (%distinct_item_count%, %item_count%, %total_amount%)</span>
    </div>
    <g:set var="uuid" value="${StringUtil.uuid}"/>
    <div class="sidebar-group">
        <div class="sidebar-group-body">
            <input type="checkbox" id="${uuid}" class="single" name="responsive_menu" uncheck-value="false" value="true" ${config.responsive_menu == 'true' ? 'checked' : ''} toggle-target="responsive-menu-prop">
            <label><g:message code="responsive.menu"/></label>
        </div>
    </div>
    <div class="responsive-menu-prop">
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="resolution"/></div>
            <div class="sidebar-group-body">
                <ui:namedSelect key="${resolutions}" name="resolutions" multiple="true" values="${config.resolutions instanceof List ? config.resolutions : [config.resolutions ]}" validation="required@if{global:#${uuid}:checked}"/>
            </div>
        </div>
    </div>
</g:applyLayout>