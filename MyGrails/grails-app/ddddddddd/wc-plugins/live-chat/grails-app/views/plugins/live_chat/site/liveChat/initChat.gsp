<div class="live-chat-popup initial-popup">
    <div class="header">
        <span class="title"><g:message code="live.chat.support"/></span>
        <span class="btn close close-button"></span>
    </div>
    <div class="init-live-chat content-wrap">
        <g:if test="${!agentCount}">
            <div class="content offline-send-message">
                <div class="system-message login"><g:message code="live.chat.no.agent.message"/></div>
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
                    <button type="submit" class="start-chat submit-button"><g:message code="send"/></button>
                </form>
            </div>
        </g:if>
        <g:else>
            <div class="content login">
                <div class="system-message login"><g:message code="chat.init.form.welcome.message"/></div>
                <form class="chat-info-form" action="${app.relativeBaseUrl()}liveChat/enterChat" validation-config-key="chat_init">
                    <label><g:message code="name"/>:</label><span class="required">*</span>
                    <input type="text" name="name" validation="required maxlength[100]" maxlength="100">
                    <label><g:message code="subject"/>:</label><span class="required">*</span>
                    <input type="text" name="subject" validation="required maxlength[200]" maxlength="200">
                    <label><g:message code="email"/>:</label><span class="required">*</span>
                    <input type="text" name="email" validation="required email" maxlength="200">
                    <label><g:message code="phone"/>:</label><span class="required">*</span>
                    <input type="text" name="phone" validation="required phone" maxlength="200">
                    <label><g:message code="message"/></label>
                    <input type="text" name="message" validation="maxlength[200]" maxlength="200">
                    <button class="submit-button start-chat" type="submit"><g:message code="start.chat"/></button>
                </form>
            </div>
        </g:else>
    </div>
</div>