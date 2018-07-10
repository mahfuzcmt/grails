<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}itemExport/export" class="edit-popup-form" method="post" no-ajax="" target="_blank">
    <div class="double-input-row">
        <div class="form-row">
            <input name="exportProduct" checked value="true" type="checkbox" class="single">
            <span><g:message code="product"/> </span>
        </div><div class="form-row">
            <input name="exportCategory" checked value="true"  type="checkbox" class="single">
            <span><g:message code="category"/> </span>
        </div>
    </div>
    <hr class="tiny-seperator">
    <div class="bmui-tab ">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="product">
                <span class="title"><g:message code="product"/></span>
            </div>
            <div class="bmui-tab-header" data-tabify-tab-id="category">
                <span class="title"><g:message code="category"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-product">
                <g:include view="/admin/item/export/fields.gsp" model="${[fields: NamedConstants.PRODUCT_IMPORT_FIELDS + NamedConstants.PRODUCT_IMPORT_EXTRA_FIELDS, mandatoryFields: DomainConstants.PRODUCT_EXPORT_MANDATORY_FIELDS.keySet(), prefix: "product"]}"/>
            </div>

            <div id="bmui-tab-category">
                <g:include view="/admin/item/export/fields.gsp" model="${[fields: NamedConstants.CATEGORY_IMPORT_FIELDS, mandatoryFields: ["name", "sku", "basePrice"], prefix: "category"]}"/>
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="export"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>