<form class="edit-popup-form" action="${app.relativeBaseUrl()}myob/initExport">
    <div class="form-row">
        <input type="checkbox" name="use_customer" value="true" uncheck-value="false">
        <span><g:message code="customer"/></span>
    </div>
    <div class="form-row">
        <input type="checkbox" name="use_order" value="true" uncheck-value="false">
        <span><g:message code="order"/></span>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="Export"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>