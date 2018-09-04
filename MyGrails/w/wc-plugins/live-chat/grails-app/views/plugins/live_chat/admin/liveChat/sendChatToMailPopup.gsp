<form  class="edit-popup-form" action="${app.relativeBaseUrl()}liveChatAdmin/sendChatToMail" method="post">
    <input type="hidden" name="chatId" value="${params.chatId}">
    <div class="form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="recipient.email"/>:</label><span class="required">*</span>
        <input name="recipient" type="text" class="medium" validation="required email" value="${chatData?.email}">
    </div>
    <div class="button-line">
        <button class="submit-button" type="submit"><g:message code="send"/></button>
        <button class="cancel-button" type="button"><g:message code="cancel"/></button>
    </div>
</form>