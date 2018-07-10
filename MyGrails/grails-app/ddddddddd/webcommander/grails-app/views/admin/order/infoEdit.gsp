<%@ page import="com.webcommander.webcommerce.Order; com.webcommander.manager.HookManager; com.webcommander.webcommerce.ShippingClass" %>
<g:set var="customerId" value="${Order.get(params.orderId)?.customerId}"/>
<form action="${app.relativeBaseUrl()}order/save" method="post" class="edit-popup-form order-create create-edit-form">
    <input type="hidden" name="customerId" value="${customerId ?: ''}">
    <div class="form-section order-create-first-view" step="1">
        <div class="form-section-info">
            <h3><g:message code="please.select.customer"/></h3>
            <div class="info-content"><g:message code="section.text.select.customer.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="select-customer-for-order table-view">
                <g:include controller="order" action="loadCustomer" params="${[id: customerId]}"/>
            </div>
            <div class="button-line">
                <button type="button" class="nextStep"><g:message code="next"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

    <div class="form-section select-product-view" step="2">
        <div class="form-section-info">
            <h3><g:message code="please.select.product"/></h3>
            <div class="info-content"><g:message code="section.text.select.product.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="select-product-for-order table-view">
                <g:include controller="order" action="loadProduct"/>
            </div>
            <div class="selected-products table-view">
                <div class="body">
                    <span class="header-title"><g:message code="selected.product.list"/></span>
                    <table>
                        <colgroup>
                            <col style="width: 18%">
                            <col style="width: 18%">
                            <col style="width: 18%">
                            <col style="width: 18%">
                            <col style="width: 18%">
                            <col style="width: 10%">
                        </colgroup>
                        <thead>
                        <tr>
                            <th><g:message code="name"/></th>
                            <th><g:message code="sku"/></th>
                            <th><g:message code="price"/></th>
                            <th><g:message code="available.stock"/></th>
                            <th><g:message code="quantity"/></th>
                            <th><span class="tool-icon remove-all"></span></th>
                        </tr>
                        </thead>
                        <tbody class="table-body">
                            <plugin:hookTag hookPoint="adminAddToCartAddedProduct" attrs="${[params: params]}"/>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="button-line">
                <button type="button" class="previousStep"><g:message code="previous"/></button>
                <button type="button" class="nextStep"><g:message code="next"/></button>
                <button type="button" class="modify-billing-shipping-address"><g:message code="change.address"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

    <div class="form-section" step="3">
        <div class="form-section-info">
            <h3><g:message code="please.select.payment.method"/></h3>
            <div class="info-content"><g:message code="section.text.select.payment.method.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="payment.gateway"/></label>
                <g:select from="${paymentGateways.collect{g.message(code: it.name)}}" class="medium" name="paymentGateway"
                          keys="${paymentGateways.code}" value="${payment ? payment.gatewayCode : ""}"/>
            </div>
            <div class="button-line">
                <button type="button" class="previousStep"><g:message code="previous"/></button>
                <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="order.now"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>