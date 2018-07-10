<form action="${app.relativeBaseUrl()}blogAdmin/loadPostAppView" method="post" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="name"/></label>
            <input type="text" name="searchText" class="large" value="${params.searchText ?: ""}"/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="status"/></label>
            <g:select class="large" name="isPublished" from="${[g.message(code: "any"), g.message(code: "published"), g.message(code: "unpublished")]}" keys="${["", "true", "false"]}"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="visibility"/></label>
            <g:select class="large" name="visibility" from="${[g.message(code: "any"), g.message(code: "open"), g.message(code: "restricted"), g.message(code: "hidden")]}" keys="${["", "open", "restricted", "hidden"]}"/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="category"/></label>
            <select class="large" name="category">
                <option value=""><g:message code="any"/></option>
                <g:each in="${categories}" var="category">
                    <option value="${category.id}">${category.name}</option>
                </g:each>
            </select>
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="created.by"/></label>
        <select class="large" name="createdBy">
            <option value=""><g:message code="any"/></option>
            <g:each in="${operators}" var="operator">
                <option value="${operator.id}">${operator.fullName}</option>
            </g:each>
        </select>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="post.date"/></label>
        <input type="text" class="datefield-from" name="dateFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to" name="dateTo"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to" name="createdTo"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="updated.between"/></label>
        <input type="text" class="datefield-from" name="updatedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to" name="updatedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>