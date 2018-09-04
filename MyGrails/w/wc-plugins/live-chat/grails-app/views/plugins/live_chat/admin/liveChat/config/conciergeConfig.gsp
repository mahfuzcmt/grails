<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants; com.webcommander.plugin.live_chat.constants.NamedConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatConciergeSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="concierge"/></h3>
            <div class="info-content"><g:message code="configure.visitor's.chat.window"/></div>
        </div>
        <g:set var="online_title" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_TITLE}"/>
        <g:set var="offline_title" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.OFFLINE_TITLE}"/>
        <g:set var="online_button_text" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_BUTTON_TEXT}"/>
        <g:set var="offline_button_text" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.OFFLINE_BUTTON_TEXT}"/>
        <g:set var="online_title" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_TITLE}"/>
        <g:set var="online_title" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_TITLE}"/>
        <g:set var="message_style" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.MESSAGE_STYLE}"/>
        <g:set var="position" value="${Live_Chat_DomainConstants.CHAT_CONCIERGE_CONFIG.POSITION}"/>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="online.title"/></label>
                    <input type="text" name="${type}.${online_title}" class="medium" value="${config[online_title]}" validation="required">
                </div>
                <div class="form-row">
                    <label><g:message code="offline.title"/></label>
                    <input type="text" name="${type}.${offline_title}" class="medium" value="${config[offline_title]}" validation="required">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="message.style"/></label>
                    <ui:namedSelect name="${type}.${message_style}"  key="${NamedConstants.CHAT_MESSAGE_STYLE}" value="${config[message_style]}"/>
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="position"/></label>
                    <ui:namedSelect name="${type}.${position}"  key="${NamedConstants.CHAT_MESSAGE_BOX_POSITION}" value="${config[position]}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="online.button.text"/></label>
                    <input type="text" name="${type}.${online_button_text}" class="medium" value="${config[online_button_text]}" validation="required">
                </div>
                <div class="form-row">
                    <label><g:message code="offline.button.text"/></label>
                    <input type="text" name="${type}.${offline_button_text}" class="medium" value="${config[offline_button_text]}" validation="required">
                </div>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
