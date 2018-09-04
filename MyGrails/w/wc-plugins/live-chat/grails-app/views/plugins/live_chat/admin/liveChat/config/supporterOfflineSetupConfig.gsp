<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatSupporterOfflineSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="supporter.offline.setup"/></h3>
            <div class="info-content"><g:message code="provide.offline.email.recipient.to.organize.offline.message"/></div>
        </div>
        <g:set var="offline_email_recipient" value="${Live_Chat_DomainConstants.OFFLINE_EMAIL_RECIPIENT}"/>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="offline.email.recipient"/></label>
                <input type="text" name="${type}.${offline_email_recipient}" class="medium" value="${config[offline_email_recipient]}" validation="required email">
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
