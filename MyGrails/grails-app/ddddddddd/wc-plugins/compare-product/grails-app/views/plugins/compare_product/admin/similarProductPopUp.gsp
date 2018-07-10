<form action="${app.relativeBaseUrl()}compareProductAdmin/importProduct" method="post" class="edit-popup-form">
    <g:radioGroup name="similarProductGroup" values="${similarProductList.product.id}" labels="${similarProductList.product.name}" value="${similarProductList.first().product.id}">
        <div class="form-row">
            <label>${it.label} </label> ${it.radio}
        </div>
    </g:radioGroup>
    <input type="hidden" name="matchedLabel" value="${matchedLabel}">
    <input type="hidden" name="productId" value="${productId}">
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="import"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>
