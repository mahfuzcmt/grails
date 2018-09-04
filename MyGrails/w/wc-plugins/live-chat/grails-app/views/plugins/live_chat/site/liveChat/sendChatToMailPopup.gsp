<div class="live-chat-popup">
    <div class="header">
        <span class="title"><g:message code="live.chat.support"/></span>
        <span class="btn close"></span>
    </div>
    <div class="content-wrap">
        <div class="content send-history-mail">
            <form action="${app.relativeBaseUrl()}liveChat/sendChatToMail" validation-config-key="chat_history">
                <input type="hidden" name="encryptedId" value="${params.encryptedId}">
                <label><g:message code="recipient.email"/>:</label><span class="required">*</span>
                <input name="recipient" type="text" class="medium" validation="required email" value="${chatData?.email}">
                <div class="button-row">
                    <button class="button send" type="submit"><g:message code="send"/> </button>
                    <button class="button cancel-button" type="button"><g:message code="cancel"/></button>
                </div>
            </form>
        </div>
    </div>
</div>
