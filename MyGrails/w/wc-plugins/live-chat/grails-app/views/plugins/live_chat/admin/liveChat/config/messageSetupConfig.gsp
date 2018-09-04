<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatMessageSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="message.setup"/></h3>
            <div class="info-content"><g:message code="provide.default.message.for.visitor.to.organize.welcome.message.for.visitor"/></div>
        </div>
        <g:set var="message_setup" value="${Live_Chat_DomainConstants.CHAT_MESSAGE_SETUP_CONFIG}"/>
        <g:set var="online_message" value="${message_setup.ONLINE_MESSAGE}"/>
        <g:set var="offline_message" value="${message_setup.OFFLINE_MESSAGE}"/>
        <g:set var="is_enabled_default_dept_message" value="${message_setup.IS_ENABLED_DEFAULT_DEPARTMENT_MESSAGE}"/>
        <g:set var="is_enabled_default_msg_when_no_dept_selected" value="${message_setup.IS_ENABLED_DEFAULT_MESSAGE_WHEN_NO_DEPARTMENT_SELECTED}"/>
        <g:set var="default_msg_text_when_no_dept_selected" value="${message_setup.DEFAULT_MESSAGE_TEXT_WHEN_NO_DEPARTMENT_SELECTED}"/>
        <g:set var="idle_time_out_message_setting" value="${message_setup.IDLE_TIME_OUT_MESSAGE_SETTING}"/>
        <g:set var="inactivity_time_period" value="${message_setup.INACTIVITY_TIME_PERIOD}"/>
        <g:set var="no_of_times_message_to_be_sent" value="${message_setup.NUMBER_OF_TIMES_MESSAGE_TO_BE_SENT}"/>
        <g:set var="inactivity_message" value="${message_setup.INACTIVITY_MESSAGE}"/>
        <g:set var="is_enable_busy_message_for_customer" value="${message_setup.IS_ENABLED_BUSY_MESSAGE_FOR_CUSTOMER}"/>
        <g:set var="busy_message" value="${message_setup.BUSY_MESSAGE_TEXT_FOR_CUSTOMER}"/>

        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="online.message"/></label>
                    <input type="text" name="${type}.${online_message}" class="medium" value="${config[online_message]}">
                </div>
                <div class="form-row">
                    <label><g:message code="offline.message"/></label>
                    <input type="text" name="${type}.${offline_message}" class="medium" value="${config[offline_message]}">
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" name="${type}.${is_enabled_default_dept_message}" class="single" value="true" uncheck-value="false" ${config[is_enabled_default_dept_message] == "true" ? "checked" : ""}>
                <span><g:message code="default.department.message"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" name="${type}.${is_enabled_default_msg_when_no_dept_selected}" class="single" value="true" uncheck-value="false" ${config[is_enabled_default_msg_when_no_dept_selected] == "true" ? "checked" : ""} toggle-target="default-msg-if-no-dept-selected">
                <span><g:message code="default.message.when.no.department.selected"/></span>
            </div>
            <div class="form-row default-msg-if-no-dept-selected">
                <input type="text" name="${type}.${default_msg_text_when_no_dept_selected}" class="medium" value="${config[default_msg_text_when_no_dept_selected]}">
             </div>
            <div class="form-row">
                <input type="checkbox" name="${type}.${idle_time_out_message_setting}" class="single" value="true" uncheck-value="false" ${config[idle_time_out_message_setting] == "true" ? "checked" : ""} toggle-target="idle-time-out-message-setting">
                <span><g:message code="idle.time.out.message.setting"/></span>
            </div>

            <div class="idle-time-out-message-setting">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="inactivity.time.period"/></label>
                        <input type="text" name="${type}.${inactivity_time_period}" placeholder="<g:message code="mins"/>" restrict="signed_numeric" value="${config[inactivity_time_period]}"  class="medium" validation="number maxlength[2]">
                    </div>
                    <div class="form-row">
                        <label><g:message code="no.of.times.message.to.be.sent"/></label>
                        <input type="text" name="${type}.${no_of_times_message_to_be_sent}" restrict="signed_numeric" value="${config[no_of_times_message_to_be_sent]}"  class="medium" validation="number maxlength[2]">
                    </div>
                </div>
                <div class="form-row">
                    <label><g:message code="message"/></label>
                    <input type="text" name="${type}.${inactivity_message}" class="medium" value="${config[inactivity_message]}">
                </div>
            </div>
             <div class="form-row">
                 <input type="checkbox" name="${type}.${is_enable_busy_message_for_customer}" class="single" value="true" uncheck-value="false" ${config[is_enable_busy_message_for_customer] == "true" ? "checked" : ""}>
                 <span><g:message code="busy.message.for.customer"/></span>
             </div>
             <div class="form-row">
                 <input type="text" name="${type}.${busy_message}"  class="medium" value="${config[busy_message]}">
             </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
