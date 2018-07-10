<form class="product-selection-panel">
    <g:include view="/admin/item/product/selection.gsp" model="${[products: products, preventSort: true, fieldName: params.fieldName]}"/>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="done"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>