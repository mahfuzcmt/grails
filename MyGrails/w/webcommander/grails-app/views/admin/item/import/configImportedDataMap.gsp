<g:form controller="itemImport" action="initImport" method="post" class="edit-popup-form">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="product">
                <span class="title"><g:message code="product"/></span>
            </div>
            <div class="bmui-tab-header" data-tabify-tab-id="category">
                <span class="title"><g:message code="category"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-category">
                <g:include view="/admin/item/import/categoryMappingTab.gsp" model="[sheetNames: sheetNames, categorySheet: categorySheet, categoryFields: categoryFields, categoryColumns: categoryColumns, sheetRows: sheetRows]"/>
            </div>
            <div id="bmui-tab-product">
                <g:include view="/admin/item/import/productMappingTab.gsp" model="[sheetNames: sheetNames, productSheet: productSheet, productFields: productFields, productColumns: productColumns, sheetRows: sheetRows]"/>
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="init.import"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>