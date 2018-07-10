<%@ page import="com.webcommander.constants.DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="visitor.information"/></h3>
            <div class="info-content"><g:message code="section.text.visitor.information.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="ask.for.info.before.chat.start"/></label>
                <input type="checkbox" class="single" name="${type}.ask_for_info" value="true" uncheck-value="false" ${config["ask_for_info"] == "true" ? "checked" : ""}>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="agent.offline.setup"/></h3>
            <div class="info-content"><g:message code="section.text.agent.offline.setup.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="offline.email.recipient"/><span class="suggestion">e.g. peter.smith@abc.com</span></label>
                <input type="text" name="${type}.offline_message_recipient" class="medium" value="${config.offline_message_recipient}" validation="required email">
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="welcome.message"/></h3>
            <div class="info-content"><g:message code="section.text.welcome.message.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="default.message.for.visitor"/><span class="suggestion">e.g. enter detail message here</span></label>
                <textarea name="${type}.welcome_message" class="medium" validation="maxlength[500]">${config.welcome_message}</textarea>
                <span class="note">(%customer_name%, %time_greetings%)</span>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>