<%@ page import="com.webcommander.plugin.live_chat.constants.DomainConstants; com.webcommander.plugin.live_chat.ChatDepartment" %>
<div class="live-chat-popup initial-popup">
    <div class="header">
        <span class="title">
            ${agentCount ? config[DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_TITLE] : config[DomainConstants.CHAT_CONCIERGE_CONFIG.OFFLINE_TITLE]}
         </span>
        <span class="btn close close-button"></span>
    </div>
    <div class="init-live-chat content-wrap">
        <g:if test="${!agentCount}">
            <div class="content offline-send-message">
                <div class="system-message login">${config[DomainConstants.CHAT_MESSAGE_SETUP_CONFIG.OFFLINE_MESSAGE]}</div>
                <div class="notification-container"></div>
                <form class="live-chat-offline-message-form" action="${app.baseUrl()}liveChat/sendOfflineMessage" method="post" validation-config-key="chat_offline_message">
                    <label><g:message code="name"/>:</label><span class="required">*</span>
                    <input type="text" name="name" validation="required maxlength[100]">
                    <label><g:message code="subject"/>:</label><span class="required">*</span>
                    <input type="text" name="subject" validation="required maxlength[200]" maxlength="200">
                    <label><g:message code="email"/>:</label><span class="required" >*</span>
                    <input type="text" name="email" validation="required email" maxlength="200">
                    <label><g:message code="phone"/>:</label><span class="required">*</span>
                    <input type="text" name="phone" validation="required phone" maxlength="200">
                    <label><g:message code="message"/>:</label><span class="required">*</span>
                    <textarea validation="required maxlength[1000]" name="message"></textarea>
                    <button type="submit" class="start-chat submit-button">${config[DomainConstants.CHAT_CONCIERGE_CONFIG.OFFLINE_BUTTON_TEXT]}</button>
                </form>
            </div>
        </g:if>
        <g:else>
            <div class="content login">
                <div class="system-message login">${config[DomainConstants.CHAT_MESSAGE_SETUP_CONFIG.ONLINE_MESSAGE]}</div>
                    <form class="chat-info-form" action="${app.relativeBaseUrl()}liveChat/enterChat" validation-config-key="chat_init">
                        <g:if test="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.ASK_NAME] == 'true'}">
                            <div class="form-row">
                                <label><g:message code="name"/>:</label>
                                <input type="text" name="name" validation="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.IS_NAME_ASKING_MANDATORY] == 'true' ? 'required' : ''} maxlength[100]" maxlength="100">
                            </div>
                        </g:if>
                        <g:if test="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.ASK_CONTACT] == 'true'}">
                            <div class="form-row">
                                <label><g:message code="contact"/>:</label>
                                <input type="text" name="phone" validation="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.IS_CONTACT_ASKING_MANDATOR] == 'true' ? 'required' : ''} phone" maxlength="200">
                            </div>
                        </g:if>
                        <g:if test="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.ASK_EMAIL] == 'true'}">
                            <div class="form-row">
                                <label><g:message code="email"/>:</label>
                                <input type="text" name="email" validation="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.IS_EMAIL_ASKING_MANDATORY] == 'true' ? 'required' : ''} email" maxlength="200">
                            </div>
                        </g:if>
                        <g:if test="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.ASK_DEPARTMENT] == 'true'}">
                            <div class="form-row">
                                <label><g:message code="department"/>:</label>
                                <ui:domainSelect domain="${ChatDepartment}" name="department" class="large" text="name" validation="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.IS_DEPARTMENT_ASKING_MANDATORY] == 'true' ? 'required' : ''}" prepend="${['': g.message(code: 'none')]}"/>
                            </div>
                        </g:if>
                        <g:if test="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.ASK_DEPARTMENT] == 'true'}">
                            <div class="form-row">
                                <label><g:message code="message"/></label>
                                <input type="text" name="message" validation="${config[DomainConstants.CHAT_VISITOR_INFORMATION_CONFIG.IS_MESSAGE_ASKING_MANDATORY] == 'true' ? 'required' : ''} maxlength[200]" maxlength="200">
                            </div>
                        </g:if>
                        <div class="form-row">
                            <button class="submit-button start-chat" type="submit">${config[DomainConstants.CHAT_CONCIERGE_CONFIG.ONLINE_BUTTON_TEXT]}</button>
                        </div>
                    </form>
            </div>
        </g:else>
    </div>
</div>
