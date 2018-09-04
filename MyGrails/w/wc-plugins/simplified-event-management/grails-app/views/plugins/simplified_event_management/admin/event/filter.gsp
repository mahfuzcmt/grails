<form action="${app.relativeBaseUrl()}simplifiedEventAdmin/loadEventView" method="post" class="edit-popup-form">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="name" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="date.range"/></label>
        <input type="text" class="datefield-from smaller" name="startTime"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="endTime"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>