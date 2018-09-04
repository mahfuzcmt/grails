<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatSoundNotificationSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="sound.notification"/></h3>
            <div class="info-content"><g:message code="manage.sound.notifications.of.new.chats.and.messages"/></div>
        </div>
        <g:set var="sound" value="${Live_Chat_DomainConstants.CHAT_SOUND_NOTIFICATION}"/>
        <g:set var="new_incomming_chat" value="${sound.NEW_INCOMING_CHAT_SOUND}"/>
        <g:set var="operator_joins_a_chat" value="${sound.OPERATOR_JOINS_A_CHAT_SOUND}"/>
        <g:set var="new_chat_message" value="${sound.NEW_CHAT_MESSAGE_SOUND}"/>
        <g:set var="chat_is_transferred_to_another_operator" value="${sound.CHAT_IS_TRANSFERRED_TO_ANOTHER_OPERATOR_SOUND}"/>
        <g:set var="disconnect" value="${sound.DISCONNECT_SOUND}"/>

        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="multiple" name="${type}.${new_incomming_chat}" value="true" uncheck-value="false" ${config[new_incomming_chat] == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="new.incoming.chat"/></span>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="multiple" name="${type}.${operator_joins_a_chat}" value="true" uncheck-value="false" ${config[operator_joins_a_chat] == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="operator.joins.a.chat"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="multiple" name="${type}.${new_chat_message}" value="true" uncheck-value="false" ${config[new_chat_message] == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="new.chat.message"/></span>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="multiple" name="${type}.${chat_is_transferred_to_another_operator}" value="true" uncheck-value="false" ${config[chat_is_transferred_to_another_operator] == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="chat.is.transferred.to.another.operator"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="multiple" name="${type}.${disconnect}" value="true" uncheck-value="false" ${config[disconnect] == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="disconnect"/></span>
                </div>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>

    </div>
</form>
