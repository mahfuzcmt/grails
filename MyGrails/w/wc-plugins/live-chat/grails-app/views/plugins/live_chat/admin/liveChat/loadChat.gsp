<%@ page import="com.webcommander.util.DateUtil; com.webcommander.plugin.live_chat.ChatTag; com.webcommander.plugin.live_chat.constants.NamedConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="chat-switch"><g:message code="activate.chat"/>
                <input type="checkbox" class="single availability-switch" data-isactive="${isAgent}" value="true" uncheck-value="false" ${isAgent == true ? "checked" : ""}>
            </span>
        </div>
    </div>
</div>
<g:if test="${isAgent}">
    <div class="no-active-chat">
        <span class="message"><g:message code="waiting.for.connection"/></span>
    </div>
    <div class="chat-window" style="display: none">
        <ui:namedSelect class="medium action-selector" prepend="${[actions: "Actions"]}" key="${NamedConstants.CHAT_ACTION_TYPE}" name="actionType"/>
        <div class="right-side">
            <form class="chat-message-form" action="${app.baseUrl()}liveChatAdmin/updateChat" method="post">
                <div class="column-content">
                    <div class="info-row">
                        <label><g:message code="customer.details"/></label>
                    </div>
                    <div class="info-row">
                        <label><g:message code="name"/></label>
                        <span class="value">${chat?.name}</span>
                    </div>
                    <div class="info-row">
                        <label><g:message code="phone"/></label>
                        <span class="value">${chat?.phone}</span>
                    </div>
                    <div class="info-row">
                        <label><g:message code="email"/></label>
                        <span class="value">${chat?.email}</span>
                    </div>
                    <div class="info-row">
                        <label><g:message code="department"/></label>
                        <span class="value">${chat?.chatDepartment?.name}</span>
                    </div>
                    <hr>
                    <div class="info-row">
                        <label><g:message code="start.time"/></label>
                        <span class="value">${chat?.created?.toAdminFormat(true, false, session.timezone)}</span>
                    </div>
                    <div class="info-row">
                        <label><g:message code="chat.duration"/></label>
                        <span class="value"><liveChat:toDuration millis="${duration}"/></span>
                    </div>
                </div>
            </form>
         </div>
        <div class="chat-area">
        </div>
        <form class="chat-message-form" action="${app.baseUrl()}liveChatAdmin/sendChatMessage" method="post">
            <div class="chat-box" >
                <textarea name="message" class=" no-auto-size" validation="maxlength[500]" error-position="before" maxlength="500"></textarea>
            </div>
            <div class="chat-button-line">
                <button class="send-file" type="button"><g:message code="send.file"/></button>>
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
