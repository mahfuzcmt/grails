<form action="${app.relativeBaseUrl()}newsletter/loadSubscriber" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="name"/></label>
            <input name="fullName" type="text" class="large" validation="maxlength[250]" value="${params.fullName ?: ''}">
        </div><div class="form-row">
            <label><g:message code="email"/></label>
            <input name="subscriberEmail" type="text" class="large" validation="maxlength[250]" value="${params.subscriberEmail ?: ''}">
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="subscription.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>