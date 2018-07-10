<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.admin.Operator; com.webcommander.webcommerce.Category;" %>
<form action="${app.relativeBaseUrl()}itemAdmin/loadProductView" class="edit-popup-form">
    <div class="form-row">
        <input type="checkbox" class="single" name="isDisposable" value="true">
        <span><g:message code="disposable"/> </span>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="product.name"/></label>
            <input name="name" type="text" class="large" value="${params.searchText ?: ''}">
        </div><div class="form-row">
            <label><g:message code="sku"/></label>
            <input name="sku" type="text" class="large">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="product.type"/></label>
            <ui:namedSelect class="large" key="${["": g.message(code: "any")] + NamedConstants.PRODUCT_TYPE}" name="productType"/>
        </div><div class="form-row">
            <label><g:message code="parent.category"/></label>
            <ui:hierarchicalSelect class="large category-selector" name="parent" domain="${Category}" prepend="${["":g.message(code: "any.category")]}"/>
        </div>
    </div>
    %{--<div class="double-input-row">
        <div class="form-row">
            <label><g:message code="brand"/></label>
            <ui:domainSelect prepend="${['':g.message(code: "any.brand")]}" name="brand" class="large brand-selector" domain="${Brand}" />
        </div><div class="form-row">
            <label><g:message code="manufacturer"/></label>
            <ui:domainSelect prepend="${['':g.message(code: "any.manufacturer")]}" name="manufacturer" class="large manufacturer-selector" domain="${Manufacturer}" />
        </div>
    </div>--}%
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="availability"/></label>
            <g:select name="isAvailable" class="large" from="[g.message(code: 'all'), g.message(code: 'available'), g.message(code: 'not.available')]" keys="${['',true, false]}"/>
        </div><div class="form-row">
            <label><g:message code="track.inventory"/></label>
            <g:select name="isInventoryEnabled" class="large" from="[g.message(code: 'all'), g.message(code: 'enabled'), g.message(code: 'disabled')]" keys="${['',true, false]}"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="stock"/></label>
            <g:select name="stock" class="large" from="[g.message(code: 'any.stock'), g.message(code: 'in.stock'), g.message(code: 'low.stock'), g.message(code: 'out.of.stock')]" keys="${['','in', 'low', 'out']}"/>
        </div><div class="form-row">
            <label><g:message code="created.by"/></label>
            <ui:domainSelect class="large user-selector" name="createdBy" domain="${com.webcommander.admin.Operator}" text="fullName" prepend="${["":g.message(code: "any.one")]}"/>
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="base.price.range"/></label>
        <div class="twice-input-row">
            <input type="text" validation="number" name="priceFrom" class="smaller"><span>-</span><input type="text" validation="number" name="priceTo" class="smaller">
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="cost.price.range"/></label>
        <div class="twice-input-row">
            <input type="text" validation="number" name="costPriceFrom" class="smaller"><span>-</span><input type="text" validation="number" name="costPriceTo" class="smaller">
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