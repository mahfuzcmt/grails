<%@ page import="com.webcommander.plugin.live_chat.ChatTag" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <g:if test="${isAgent}">
                <span class="toolbar-item deactivate" title="<g:message code="deactivate.chat"/>"><i></i></span>
            </g:if>
            <g:else>
                <span class="toolbar-item activate" title="<g:message code="activate.chat"/>"><i></i></span>
            </g:else>
        </div>
    </div>
</div>
<g:if test="${isAgent}">
    <div class="no-active-chat">
        <span class="message"><g:message code="waiting.for.connection"/></span>
    </div>
    <div class="chat-window" style="display: none">
        <div class="chat-area">
        </div>
        <form class="chat-message-form" action="${app.baseUrl()}liveChatAdmin/sendChatMessage" method="post">
            <div class="chat-box" >
                <textarea name="message" class=" no-auto-size" validation="maxlength[500]" error-position="before" maxlength="500"></textarea>
            </div>
            <div class="chat-button-line">
            <span class="add-tag-label"><g:message code="chat.related"/></span>
            <ui:domainSelect domain="${ChatTag}" custom-attrs="${[multiple: true, "data-placeholder": g.message(code: "select.tag"), "min-width": 140]}" class="tag-selector" values="${chat ? chat.tags.id : []}"/>
                <button class="terminate-chat" type="button"><g:message code="terminate.chat"/></button>
                <button class="transfer-chat" type="button"><g:message code="transfer.chat"/></button>
                <button class="send-file" type="button"><g:message code="send.file"/></button>
                <button class="submit-button" type="submit"><g:message code="send"/></button>
            </div>
        </form>
    </div>
</g:if>
<g:else>
    <div class="agent-not-active app-tab-text-info">
        <span><g:message code="chat.not.activated"/></span><span class="link activate-chat"><g:message code="activate"/></span>
    </div>
</g:else>
