<%@ page import="com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}layout/cloneSection" class="create-edit-form" method="post">
    <input type="hidden" name="destId" value="${destId}">
    <input type="hidden" name="section" value="${section}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="layout.${section}.information"/></h3>
            <div class="info-content"><g:message code="section.text.layout.${section}.info"/></div>
        </div>
        <div class="form-section-container layout-list blocktype-list">
            <g:set var="wiMsgKey" value="${NamedConstants.WIDGET_MESSAGE_KEYS}"/>
            <g:each in="${layouts}" var="layout">
                <div class="layout-thumb blocklist-item" layout-id="${layout.id}" layout-name="${layout.name.encodeAsBMHTML()}" title="${layout[section + 'Widgets'].widgetType.collect{g.message(code: wiMsgKey[it + '.label'])}.join(", ")} widget">
                    <span class="layout-title listitem-title">${layout.name.encodeAsBMHTML()}</span>
                    <g:set var="attached" value="${layout.attachedPageForLayout(5)}"/>
                    <g:set var="pages" value="${attached.pages}"/>
                    <g:set var="autoPages" value="${attached.autoPages}"/>
                    <span class="attach-page blocklist-subitem-summary-view">${(pages ? pages.join(", ") : "") + (autoPages ? (pages ? ", " : "") + autoPages.join(", ") : "")}</span>
                </div>
            </g:each>
            <div class="form-row btn-row">
                <button type="submit" class="submit-button create-edit-form-submit"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>