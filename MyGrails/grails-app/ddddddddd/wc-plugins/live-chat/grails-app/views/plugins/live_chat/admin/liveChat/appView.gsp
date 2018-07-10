<%@ page import="com.webcommander.plugin.PluginManager" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
</div>
<div class="bmui-tab left-side-header" ${params.active ? "active='" + params.active + "'" : ""}>
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="chat" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadChat?chatId=' + params.chatId }">
            <span class="title"><g:message code="chat"/></span>
        </div>
        <g:if test="${PluginManager.isInstalled("visitor-listing")}">
            <div class="bmui-tab-header" data-tabify-tab-id="visitor" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadVisitor'}">
                <span class="title"><g:message code="visitors"/></span>
            </div>
        </g:if>
        <div class="bmui-tab-header" data-tabify-tab-id="archive" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadArchive'}">
            <span class="title"><g:message code="archives"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="agent" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadAgent'}">
            <span class="title"><g:message code="active.supporter.names"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="report" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadReport'}">
            <span class="title"><g:message code="reports"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="tag" data-tabify-url="${app.relativeBaseUrl() + 'liveChatAdmin/loadTag'}">
            <span class="title"><g:message code="chat.related"/></span>
        </div>
    </div><div class="bmui-tab-body-container">
        <div id="bmui-tab-chat">
        </div>
        <g:if test="${PluginManager.isInstalled("visitor-listing")}">
            <div id="bmui-tab-visitor">
            </div>
        </g:if>
        <div id="bmui-tab-archive">
        </div>
        <div id="bmui-tab-agent">
        </div>
        <div id="bmui-tab-report">
        </div>
        <div id="bmui-tab-tag">
        </div>
    </div>
</div>
