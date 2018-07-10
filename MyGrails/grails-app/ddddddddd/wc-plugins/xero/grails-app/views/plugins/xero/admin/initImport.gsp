<form class="edit-popup-form" action="${app.relativeBaseUrl()}xero/initImport">
    <div class="form-row">
        <input type="checkbox" name="use_tax" value="true" uncheck-value="false">
        <span><g:message code="tax"/></span>
    </div>
    <div class="form-row">
        <input type="checkbox" name="use_customer" value="true" uncheck-value="false">
        <span><g:message code="customer"/></span>
    </div>
    <div class="form-row">
        <input type="checkbox" name="use_product" value="true" uncheck-value="false">
        <span><g:message code="product"/></span>
    </div>

    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="Import"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>