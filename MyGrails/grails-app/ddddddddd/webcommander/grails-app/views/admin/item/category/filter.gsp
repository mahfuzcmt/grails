<%@ page import="com.webcommander.admin.Operator; com.webcommander.webcommerce.Category;" %>
<form action="${app.relativeBaseUrl()}itemAdmin/loadCategoryView" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="category.name"/></label>
            <input name="name" type="text" class="medium" value="${params.searchText ?: ''}">
        </div><div class="form-row">
            <label><g:message code="sku"/></label>
            <input name="sku" type="text" class="medium">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="parent.category"/></label>
            <ui:hierarchicalSelect class="medium category-selector" name="parent" domain="${Category}" prepend="${["":g.message(code: "any.category")]}"/>
        </div><div class="form-row">
            <label><g:message code="created.by"/></label>
            <ui:domainSelect class="medium user-selector" name="createdBy" domain="${com.webcommander.admin.Operator}" text="fullName" prepend="${["":g.message(code: "any.one")]}"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo">
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="updated.between"/></label>
        <input type="text" class="datefield-from smaller" name="updatedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="updatedTo">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>