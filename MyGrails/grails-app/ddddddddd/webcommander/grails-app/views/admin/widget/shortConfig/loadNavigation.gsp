<%@ page import="com.webcommander.util.StringUtil" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="navigation"/></div>
        <div class="sidebar-group-body">
            <g:select name="navigation" class="sidebar-input" from="${navigations}" optionKey="id" optionValue="name" value="${navigation}"/>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="orientation"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="orientation" value="H" ${config.orientation ? (config.orientation == "H" ? "checked" : "") : "checked"}>
                <label><g:message code="horizontal"/></label>
            </div>
            <div>
                <input type="radio" name="orientation" value="V" ${config.orientation ? (config.orientation == "V" ? "checked" : "") : ""}>
                <label><g:message code="vertical"/></label>
            </div>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showImage" uncheck-value="hide" value="show" ${config.showImage == 'show' ? 'checked' : ''}>
            <label><g:message code="show.image"/></label>
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
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="transition.type" /></div>
            <div class="sidebar-group-body">
                <ui:namedSelect key="${[inline: g.message(code: "inline"), drawer: g.message(code: "drawer")]}" name="transition_type" value="${config.transition_type}"/>
            </div>
        </div>
    </div>
</g:applyLayout>