<%@ page import="com.webcommander.content.Section" %>
<form action="${app.relativeBaseUrl()}snippetAdmin/loadSnippetView" method="post" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="name" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row">
        <label><g:message code="section"/></label>
        <ui:hierarchicalSelect name="section" class="large section-selector" domain="${Section}" prepend="${["" : g.message(code: "all.sections")]}"/>
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