<%@ page import="com.webcommander.plugin.live_chat.constants.DomainConstants" %>
<div class="live-chat-popup chat-popup">
    <div class="header">
        <span class="title"><g:message code="live.chat.support"/></span>
        <span class="btn close close-button"></span>
        <span class="btn minimize"></span>
    </div>
    <div class="leave-chat-window" style="display: none">
        <div class="content-wrap">
            <div class="content leave-chat">
                <div class="leaveChat-confirm-message"><g:message code="confirm.leave.chat"/></div>
                <div class="button-row">
                    <span class="button leave"><g:message code="leave"/></span>
                    <span class="button cancel"><g:message code="cancel"/></span>
                </div>
            </div>
        </div>
    </div>
    <div class="chat-window" chat-id="${session.live_chat_ref}">
        <input type="hidden" name="encryptedId" value="${encryptedId}">
        <div class="content-wrap">
            <div class="notification-container"></div>
            <div class="content">
                <div class="chat-area message-container">

                </div>
            </div>
        </div>
        <form class="chat-message-form" action="${app.relativeBaseUrl()}liveChat/sendChatMessage" method="post">
            <div class="message-pad-wrap">
                <div class="message-pad chat-box">
                    <textarea class="chat-message" name="message" validation="maxlength[500]" maxlength="500"></textarea>
                </div>
            </div>
            <div class="action-button-panel button-holder">
                <span class="act-button leave-chat-button"><g:message code="leave.chat"/> </span>
                <g:if test="${!chatData.historyRecipient}">
                    <span class="act-button send-chat-to-mail-button"><g:message code="send.chat.to.mail"/></span>
                </g:if>
                <span class="act-button send-file-button"><g:message code="send.file"/></span>
            </div>
            <div class="button-panel">
                <span class="more-option" tabindex="1"></span>
                <span class="rating-button thumbs-up ${chat.rating == DomainConstants.CHAT_RATING_TYPE.UP ? "active" : ""}" value="${DomainConstants.CHAT_RATING_TYPE.UP}"></span>
                <span class="rating-button thumbs-down ${chat.rating == DomainConstants.CHAT_RATING_TYPE.DOWN ? "active" : ""}" value="${DomainConstants.CHAT_RATING_TYPE.DOWN}"></span>
                <button class="send submit-button" type="submit"><g:message code="send"/> </button>
            </div>
        </form>
    </div>
</div>