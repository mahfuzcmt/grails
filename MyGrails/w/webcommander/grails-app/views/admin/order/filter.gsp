<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>

<form action="${app.relativeBaseUrl()}order/loadAppView" method="post" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
            <input type="text" name="customerName" class="large"/>
        </div><div class="form-row">
            <label><g:message code="product.name"/></label>
            <input type="text" name="productName" class="large"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="order.id"/></label>
            <input type="text" name="orderId" class="large">
        </div><div class="form-row">
            <label><g:message code="product.sku"/></label>
            <input type="text" name="productSku" class="large" />
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="order.date"/></label>
        <input type="text" class="datefield-from smaller" name="orderFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="orderTo"/>
    </div>
    <div class="form-row">
        <label><g:message code="order.total"/></label>
        <ui:namedSelect class="smaller" key="${["": g.message(code: "none")] << NamedConstants.ORDER_TOTAL}" name="orderTotalStatus" toggle-target="order-status"/>
    </div>
    <div class="form-row order-status-" do-reverse-toggle>
        <input type="text" name="total" class="smaller" validation="number" restrict="decimal" placeholder="<g:message code="suggestion.order.total"/>"/>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="order.status"/></label>
            <ui:namedSelect class="large" key="${["": g.message(code: "none")] << NamedConstants.ORDER_STATUS}" name="orderStatus"/>
        </div><div class="form-row">
            <label><g:message code="shipment.status"/></label>
            <ui:namedSelect class="large" key="${["": g.message(code: "none")] << NamedConstants.SHIPPING_STATUS}" name="shippingStatus"/>
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="payment.status"/></label>
        <ui:namedSelect class="large" key="${["": g.message(code: "none")] << NamedConstants.ORDER_PAYMENT_STATUS}" name="paymentStatus"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>