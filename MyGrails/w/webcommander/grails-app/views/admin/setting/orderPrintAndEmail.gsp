<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="order_print_and_email"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="order.print"/></h3>
            <div class="info-content"><g:message code="section.text.order.print.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="show.product.total"/><span class="suggestion"><g:message code="suggestion.setting.order.print.product.total"/></span></label>
                    <ui:namedSelect name="order_print_and_email.product_total_price" key="${NamedConstants.SHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS}" value="${configs?.product_total_price}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="subtotal.price"/><span class="suggestion"><g:message code="suggestion.setting.order.print.subtotal"/></span></label>
                    <ui:namedSelect name="order_print_and_email.subtotal_price" key="${NamedConstants.SHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS}" value="${configs?.subtotal_price}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_discount_each_item" value="true" uncheck-value="false" ${configs.show_discount_each_item == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.discount.each.item"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_tax_each_item" value="true" uncheck-value="false" ${configs.show_tax_each_item == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.tax.each.item"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_total_discount" value="true" uncheck-value="false" ${configs.show_total_discount == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.total.discount"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_sub_total_tax" value="true" uncheck-value="false" ${configs.show_sub_total_tax == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.total.tax"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_subtotal" value="true" uncheck-value="false" ${configs.show_subtotal == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.subtotal"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_shipping_cost" value="true" uncheck-value="false" ${configs.show_shipping_cost == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.shipping.cost"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_shipping_tax" value="true" uncheck-value="false" ${configs.show_shipping_tax == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.shipping.tax"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="order_print_and_email.show_handling_cost" value="true" uncheck-value="false" ${configs.show_handling_cost == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.handling.cost"/></span>
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="order_print_and_email.show_payment_surcharge" value="true" uncheck-value="false" ${configs.show_payment_surcharge == "true" ? "checked" : ""}> &nbsp;
                <span><g:message code="show.payment.surcharge"/></span>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>