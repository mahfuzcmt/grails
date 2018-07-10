<form action="${app.relativeBaseUrl()}news/loadAppView" method="post" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="form-row">
        <label><g:message code="title"/></label>
        <input type="text" name="searchText" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row">
        <label><g:message code="article.name"/></label>
        <input type="text" name="article" class="large" value="${params.article ?: ""}"/>
    </div>
    <div class="form-row datefield-between">
            <label><g:message code="news.between"/></label>
            <input type="text" class="datefield-from smaller" name="newsFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="newsTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>