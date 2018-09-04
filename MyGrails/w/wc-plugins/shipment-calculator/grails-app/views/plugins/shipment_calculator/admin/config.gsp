<div class="form-row">
    <label><g:message code="shipment.calculator"/></label>
    <input type="checkbox" class="single" name="shipping.shipment_calculator_cart_details_page" ${shippingSettings.shipment_calculator_cart_details_page.toBoolean() ? "checked" : ""} uncheck-value="false">
    <span><g:message code="cart.details.page"/> </span>
</div>
<div class="form-row">
    <label>&nbsp;</label>
    <input type="checkbox" class="single" name="shipping.shipment_calculator_checkout_page" ${shippingSettings.shipment_calculator_checkout_page.toBoolean() ? "checked" : ""} uncheck-value="false">
    <span><g:message code="checkout.page"/> </span>
</div>