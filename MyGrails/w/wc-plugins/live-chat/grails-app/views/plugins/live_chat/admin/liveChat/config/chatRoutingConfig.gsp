<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.live_chat.constants.DomainConstants as Live_Chat_DomainConstants; com.webcommander.plugin.live_chat.constants.NamedConstants" %>
<form class="live-chat-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="liveChatRoutingSettingForm">
    <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT}"/>
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="chat.routing"/></h3>
            <div class="info-content"><g:message code="configure.how.the.chats.from.visitors.will.be.assigned"/></div>
        </div>
        <g:set var="chat_routing" value="${Live_Chat_DomainConstants.CHAT_ROUTING_CONFIG.CHAT_ROUTING}"/>
        <g:set var="is_busy" value="${Live_Chat_DomainConstants.CHAT_ROUTING_CONFIG.BROADCAST_CHAT_IF_EVERY_OPERATOR_IS_A_PARTICULAR_DEPARTMENT_IS_BUSY}"/>
        <g:set var="chat_limit" value="${Live_Chat_DomainConstants.CHAT_ROUTING_CONFIG.CHAT_LIMIT}"/>
        <g:set var="to_everyone" value="${Live_Chat_DomainConstants.CHAT_ROUTING_OPTION.BROADCAST_TO_EVERYONE}"/>
        <g:set var="on_department" value="${Live_Chat_DomainConstants.CHAT_ROUTING_OPTION.ASSIGN_BASED_ON_DEPARTMENT}"/>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="chat.routing"/></label>
                    <ui:namedSelect name="${type}.${chat_routing}"  key="${NamedConstants.CHAT_ROUTING_OPTION}" value="${config[chat_routing]}"/>
                 </div>
                <div class="form-row">
                    <label><g:message code="chat.limit"/></label>
                    <input type="text" name="${type}.${chat_limit}" restrict="signed_numeric" value="${config[chat_limit]}"  class="medium" validation="number maxlength[2]">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" name="${type}.${is_busy}" class="single" value="true" uncheck-value="false" ${config[is_busy] == "true" ? "checked" : ""}>
                    <span><g:message code="broadcast.chat.if.every.operator.is.a.particular.department.is.busy"/></span>
                </div>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button live-chat-setting-form-submit"><g:message code="update"/></button>
            </div>
         </div>
    </div>
</form>
