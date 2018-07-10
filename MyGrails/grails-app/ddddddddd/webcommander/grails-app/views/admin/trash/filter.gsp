<form action="${app.relativeBaseUrl()}trash/loadAppView" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="entity.type"/></label>
            <g:select class="large selected-entity" from="${domains}" keys="${domains}"  noSelection="['': 'All']" name="advancedSearchDomain"/>
        </div><div class="form-row">
            <label><g:message code="entity.name"/></label>
            <input type="text" class="large" name="searchText" value="${params.searchText}"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="deleted.between"/></label>
        <input type="text" class="datefield-from smaller" name="deletedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="deletedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>