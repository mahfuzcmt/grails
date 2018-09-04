<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatVisitorInfoSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="visitor.information"/></h3>
            <div class="info-content"><g:message code="configure.chat.initiate.settings.to.organise.visitor.information"/></div>
        </div>
        <g:set var="visitor_info" value="${Live_Chat_DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG}"/>
        <g:set var="ask_info" value="${visitor_info.ASK_INFO_BEFORE_CHAT_START}"/>
        <g:set var="name" value="${visitor_info.ASK_NAME}"/>
        <g:set var="is_name_mandatory" value="${visitor_info.IS_NAME_ASKING_MANDATORY}"/>
        <g:set var="email" value="${visitor_info.ASK_EMAIL}"/>
        <g:set var="is_email_mandatory" value="${visitor_info.IS_EMAIL_ASKING_MANDATORY}"/>
        <g:set var="contact" value="${visitor_info.ASK_CONTACT}"/>
        <g:set var="is_contact_mandatory" value="${visitor_info.IS_CONTACT_ASKING_MANDATORY}"/>
        <g:set var="department" value="${visitor_info.ASK_DEPARTMENT}"/>
        <g:set var="is_department_mandatory" value="${visitor_info.IS_DEPARTMENT_ASKING_MANDATORY}"/>
        <g:set var="message" value="${visitor_info.ASK_MESSAGE}"/>
        <g:set var="is_message_mandatory" value="${visitor_info.IS_MESSAGE_ASKING_MANDATORY}"/>
        <g:set var="allow_social_media_login" value="${visitor_info.ALLOW_SOCIAL_MEDIA_LOGIN}"/>
        <g:set var="facebook_login" value="${visitor_info.FACEBOOK_LOGIN}"/>
        <g:set var="google_plus_login" value="${visitor_info.GOOGLE_PLUS_LOGIN}"/>

        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" name="${type}.${ask_info}" class="single" value="true" uncheck-value="false" ${config[ask_info] == "true" ? "checked" : ""} toggle-target="information">
                <span><g:message code="ask.information.before.chat.start"/></span>
            </div>
            <div class="form-row information">
                <table>
                    <tr>
                        <th><g:message code="information"/></th>
                        <th><g:message code="name"/></th>
                        <th><g:message code="contact"/></th>
                        <th><g:message code="email"/></th>
                        <th><g:message code="department"/></th>
                        <th><g:message code="message"/></th>
                    </tr>
                    <tr class="data-row">
                        <td><g:message code="ask.infromation"/></td>
                        <td><input type="checkbox" name="${type}.${name}" class="multiple" value="true" uncheck-value="false" ${config[name] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${contact}" class="multiple" value="true" uncheck-value="false" ${config[contact] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${email}" class="multiple" value="true" uncheck-value="false" ${config[email] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${department}" class="multiple" value="true" uncheck-value="false" ${config[department] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${message}" class="multiple" value="true" uncheck-value="false" ${config[message] == "true" ? "checked" : ""}></td>
                    </tr>
                    <tr class="data-row">
                        <td><g:message code="mandatory.infromation"/></td>
                        <td><input type="checkbox" name="${type}.${is_name_mandatory}" class="multiple" value="true" uncheck-value="false" ${config[is_name_mandatory] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${is_contact_mandatory}" class="multiple" value="true" uncheck-value="false" ${config[is_contact_mandatory] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${is_email_mandatory}" class="multiple" value="true" uncheck-value="false" ${config[is_email_mandatory] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${is_department_mandatory}" class="multiple" value="true" uncheck-value="false" ${config[is_department_mandatory] == "true" ? "checked" : ""}></td>
                        <td><input type="checkbox" name="${type}.${is_message_mandatory}" class="multiple" value="true" uncheck-value="false" ${config[is_message_mandatory] == "true" ? "checked" : ""}></td>
                    </tr>
                </table>
            </div>
            <div class="form-row">
                <input type="checkbox" name="${type}.${allow_social_media_login}" class="single" value="true" uncheck-value="false" ${config[allow_social_media_login] == "true" ? "checked" : ""} toggle-target="social-media-login">
                <span><g:message code="allow.social.media.login"/></span>
            </div>
            <div class="social-media-login">
                <div class="form-row">
                    <input type="checkbox" name="${type}.${facebook_login}" class="multiple" value="true" uncheck-value="false" ${config[facebook_login] == "true" ? "checked" : ""}>
                    <span><g:message code="facebook"/></span>
                </div>
                <div class="form-row">
                    <input type="checkbox" name="${type}.${google_plus_login}" class="multiple" value="true" uncheck-value="false" ${config[google_plus_login] == "true" ? "checked" : ""}>
                    <span><g:message code="google.plus"/></span>
                </div>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
