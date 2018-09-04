<form action="${app.relativeBaseUrl()}newsletter/loadAppView" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="title"/></label>
            <input name="title" type="text" class="large" value="${params.searchText ?: ''}">
        </div><div class="form-row">
            <label><g:message code="subject"/></label>
            <input name="subject" type="text" class="large">
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo">
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="scheduled.between"/></label>
        <input type="text" class="datefield-from smaller" name="scheduledFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="scheduledTo">
    </div>
    <div class="form-row">
        <label><g:message code="status"/></label>
        <g:select class="large" name="status" from="${[ g.message(code: "any"), g.message(code: "sent"), g.message(code: "not.sent")]}" keys="${['', true, false]}" />
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>