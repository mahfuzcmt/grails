<form action="${app.relativeBaseUrl()}plugin/loadAppView" method="post" class="edit-popup-form">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="searchText" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="plugins.between"/></label>
        <input type="text" class="datefield-from smaller" name="pluginFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="pluginTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>