<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatDepartmentsSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="departments"/></h3>
            <div class="info-content"><g:message code="add.departments.to.assign.chats.automatically"/></div>
        </div>
        <g:set var="offline_email_recipient" value="${Live_Chat_DomainConstants.OFFLINE_EMAIL_RECIPIENT}"/>
        <div class="form-section-container live-chat-department-settings">
            <div class="form-row">
                <button class="button add-department" type="button">+ <g:message code="add.department"/></button>
                <g:include controller="LiveChatAdmin" action="loadChatDepartments"/>
            </div>
        </div>
    </div>
</form>