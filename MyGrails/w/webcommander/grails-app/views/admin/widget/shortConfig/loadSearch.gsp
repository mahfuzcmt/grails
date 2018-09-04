<%@ page import="com.webcommander.util.StringUtil" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="button.text"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="buttonText" value="${config.buttonText ?: "s:search"}">
        </div>
        <div class="sidebar-group-label"><g:message code="search.placeholder"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="placeholderText" value="${config.placeholderText}">
        </div>
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