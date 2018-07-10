<%@ page import="com.webcommander.plugin.live_chat.constants.DomainConstants" %>
<div class="chat-message-container">
    <g:each in="${chat.messages}" var="message">
        <div class="info-message ${message.senderType}">
            <g:if test="${message.isNotification}">
                <span class="notification"><g:message code="${"notification." + message.notificationType + ".for.admin"}" args="${message.notificationArgs}"/> </span>
            </g:if>
            <g:else>
                <span class="name">
                    <g:if test="${message.senderType == DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT}">
                        <g:message code="admin"/>
                    </g:if>
                    <g:else>${chat.name}</g:else>
                </span>
                <div class="message-wrap">
                    <span class="date">${message.created.toAdminFormat(true, false, session.timezone)}</span>
                    <div class="message">${message.message}</div>
                </div>
            </g:else>
        </div>
    </g:each>
</div>
