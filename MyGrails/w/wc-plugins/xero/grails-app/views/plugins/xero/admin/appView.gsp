<%@ page import="com.webcommander.plugin.xero.XeroService; com.webcommander.admin.ConfigService" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
    </div>
</div>
<div class="bmui-tab left-side-header" ${params.active ? "active='" + params.active + "'" : ""}>
    <g:set var="tabs" value="${XeroService.tabs}"/>
    <div class="bmui-tab-header-container">
        <g:each in="${tabs.keySet()}" var="tabId">
            <div class="bmui-tab-header" data-tabify-tab-id="${tabId}" data-tabify-url="${app.relativeBaseUrl()}${tabs[tabId].url}">
                <span class="title"><g:message code="${tabs[tabId].message_key}"/></span>
            </div>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <g:if test="${tabId != "order"}">
            <div id="bmui-tab-${tabId}"></div>
        </g:if>
    </div>
</div>