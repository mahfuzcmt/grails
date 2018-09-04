<form action="${app.relativeBaseUrl()}user/loadAppView" class="edit-popup-form">
    <div class="form-row">
        <label><g:message code="full.name"/></label>
        <input type="text" class="large" name="fullName" value="${params.searchText}">
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="email"/></label>
            <input type="text" class="large" name="email">
        </div><div class="form-row">
            <label><g:message code="status"/></label>
            <select class="large" name="status">
                <option value=""><g:message code="no.filter"/></option>
                <option value="active"><g:message code="active"/></option>
                <option value="inactive"><g:message code="inactive"/></option>
            </select>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="updated.between"/></label>
        <input type="text" class="datefield-from smaller" name="updatedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="updatedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>