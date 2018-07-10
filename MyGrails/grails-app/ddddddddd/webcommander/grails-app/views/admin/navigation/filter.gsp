<form action="${app.relativeBaseUrl()}navigation/loadAppView" method="post" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="searchText" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row datefield-between">
            <label><g:message code="created.between"/></label>
            <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo"/>
        </div>
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