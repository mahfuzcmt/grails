<div class="newsletter unsubscribe valid-verify-form">
    <h3 class="title"><g:message code="newsletter.unsubscription"/></h3>
    <span class="message"><g:message code="to.unsubscribe.please.provide.reason"/>.</span>
    <input type="hidden" name="sid" value="${sid}">
    <div class="form-row mandatory">
        <label><g:message code="reason"/>:</label>
        <textarea class="medium message-area" name="message" validation="required maxlength[2000]" maxlength="2000"></textarea>
    </div>
    <div class="form-row button-container">
        <label>&nbsp</label>
        <button class="newsletter-unsubscription submit-button"><g:message code="unsubscribe"/></button>
    </div>
</div>