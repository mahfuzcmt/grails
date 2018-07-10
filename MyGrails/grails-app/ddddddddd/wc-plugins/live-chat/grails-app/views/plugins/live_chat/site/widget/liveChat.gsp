<%@ page import="com.webcommander.plugin.live_chat.constants.DomainConstants" %>
<g:applyLayout name="_widget">
    <span class="live-chat-init-button live-chat-status ${hasAgent ? "online" : "offline"}"><site:message code="${config[(hasAgent ? "online" : "offline") + '_text']}"/></span>
</g:applyLayout>
