<%@ page import="com.webcommander.admin.Operator" %>
<form  class="edit-popup-form" action="${app.relativeBaseUrl()}liveChatAdmin/sendTransferRequest" method="post">
    <input type="hidden" name="chatId" value="${params.chatId}">
    <div class="form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="supporter"/></label>
        <ui:domainSelect domain="${Operator}" name="agent" class="medium" text="fullName" validation="required" filter="${filter}"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="message"/></label>
        <textarea class="medium" validation="required maxlength[200]" name="message"></textarea>
    </div>
    <div class="button-line">
        <button class="submit-button" type="submit"><g:message code="send.request"/></button>
        <button class="cancel-button" type="button"><g:message code="cancel"/></button>
    </div>
</form>