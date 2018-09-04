<form action="${app.relativeBaseUrl()}manufacturer/loadAppView" method="post" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="searchText" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>