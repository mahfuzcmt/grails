<form action="${app.relativeBaseUrl()}newsletter/loadUnsubscriber" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="name"/></label>
            <input name="fullName" type="text" class="large" validation="maxlength[250]" value="${params.searchText ?: ''}">
        </div><div class="form-row">
            <label><g:message code="email"/></label>
            <input name="unsubscriberEmail" type="text" class="large" validation="maxlength[250]" value="${params.unsubscriberEmail ?: ''}">
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="subscription.between"/></label>
        <input type="text" class="datefield-from smaller" name="subscribedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="subscribedTo">
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="unsubscription.between"/></label>
        <input type="text" class="datefield-from smaller" name="unsubscribedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="unsubscribedTo">
    </div>
    <div class="form-row">
        <label><g:message code="reason"/></label>
        <input name="reason" type="text" class="large" validation="maxlength[255]" value="${params.reason ?: ''}">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>