<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="checkout_page"/>

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="checkout.page.info"/></h3>
            <div class="info-content"><g:message code="section.text.settings.checkout.page"/></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="checkoutPageSettings">
                <div class="double-input-row">
                    <div class="form-row">
                    <label><g:message code="confirm.order.button.text"/><span class="suggestion"><g:message code="suggestion.setting.chkout.button.text"/></span></label>
                    <input type="text" class="large" name="checkout_page.confirm_order_button_text" value="${configs.confirm_order_button_text}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="price.column.label"/><span class="suggestion"><g:message code="suggestion.setting.chkout.price.label"/></span></label>
                        <input class="large" name="checkout_page.price_column_text" type="text" value="${configs.price_column_text}">
                    </div><div class="form-row">
                    <label><g:message code="item.total.label"/><span class="suggestion"><g:message code="suggestion.setting.chkout.item.total"/></span></label>
                    <input type="text"name="checkout_page.item_total_label" class="large" value="${configs.item_total_label}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="show.discount.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.discount.each.item.label"/></span></label>
                        <input class="large" name="checkout_page.show_discount_for_each_item_label" type="text" value="${configs.show_discount_for_each_item_label}">
                    </div><div class="form-row">
                    <label><g:message code="show.tax.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.tax.each.item.label"/></span></label>
                    <input type="text"name="checkout_page.show_tax_for_each_item_label" class="large" value="${configs.show_tax_for_each_item_label}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="show.total.discount.label"/><span class="suggestion"><g:message code="suggestion.show.total.discount.label"/></span></label>
                        <input class="large" name="checkout_page.show_total_discount_label" type="text" value="${configs.show_total_discount_label}">
                    </div><div class="form-row">
                    <label><g:message code="show.subtotal.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.label"/></span></label>
                    <input type="text"name="checkout_page.show_sub_total_label" class="large" value="${configs.show_sub_total_label}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="show.subtotal.tax.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.tax.label"/></span></label>
                        <input class="large" name="checkout_page.show_sub_total_tax_label" type="text" value="${configs.show_sub_total_tax_label}">
                    </div><div class="form-row">
                    <label><g:message code="show.shipping.tax.label"/><span class="suggestion"><g:message code="suggestion.show.shipping.tax.label"/></span></label>
                    <input type="text"name="checkout_page.show_shipping_tax_label" class="large" value="${configs.show_shipping_tax_label}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="show.shipping.cost.label"/><span class="suggestion"><g:message code="suggestion.show.shipping.cost.label"/></span></label>
                        <input class="large" name="checkout_page.show_shipping_cost_label" type="text" value="${configs.show_shipping_cost_label}">
                    </div><div class="form-row">
                    <label><g:message code="show.handling.cost.label"/><span class="suggestion"><g:message code="suggestion.show.handling.cost.label"/></span></label>
                    <input type="text"name="checkout_page.show_handling_cost_label" class="large" value="${configs.show_handling_cost_label}"/>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="show.product.total"/><span class="suggestion"><g:message code="suggestion.setting.chkout.product.total"/></span></label>
                        <ui:namedSelect class="large" name="checkout_page.individual_total_price" key="${NamedConstants.SHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS}" value="${configs.individual_total_price}"/>
                    </div><div class="form-row chosen-wrapper">
                    <label><g:message code="subtotal.price"/><span class="suggestion"><g:message code="suggestion.setting.chkout.sub.total"/></span></label>
                    <ui:namedSelect class="large" name="checkout_page.subtotal_price" key="${NamedConstants.SHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS}" value="${configs.subtotal_price}"/>
                </div>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="handling.tax.label"/><span class="suggestion">e.g. s:handling.tax</span></label>
                    <input type="text"name="checkout_page.show_handling_tax_label" class="large" value="${configs.show_handling_tax_label}" validation="required maxlength[250]" maxlength="250"/>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_product_thumbnail" value="true" uncheck-value="false" ${configs.show_product_thumbnail == "true" ? "checked" : ""}>
                        <span><g:message code="show.product.thumbnail.image"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.enable_comment" value="true" uncheck-value="false" ${configs.enable_comment == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="enable.order.comment"/></span>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_total_discount" value="true" uncheck-value="false" ${configs.show_total_discount == "true" ? "checked" : ""}> &nbsp;
                        <span><g:message code="show.total.discount"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.show_sub_total_tax" value="true" uncheck-value="false" ${configs.show_sub_total_tax == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.total.tax"/></span>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_subtotal" value="true" uncheck-value="false" ${configs.show_subtotal == "true" ? "checked" : ""}> &nbsp;
                        <span><g:message code="show.subtotal"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.show_shipping_cost" value="true" uncheck-value="false" ${configs.show_shipping_cost == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.shipping.cost"/></span>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_shipping_tax" value="true" uncheck-value="false" ${configs.show_shipping_tax == "true" ? "checked" : ""}> &nbsp;
                        <span><g:message code="show.shipping.tax"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.show_handling_cost" value="true" uncheck-value="false" ${configs.show_handling_cost == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.handling.cost"/></span>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_payment_surcharge" value="true" uncheck-value="false" ${configs.show_payment_surcharge == "true" ? "checked" : ""}> &nbsp;
                        <span><g:message code="show.payment.surcharge"/></span>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.price_enter_with_tax" value="true" uncheck-value="false" ${configs.price_enter_with_tax == "true" ? "checked" : ""}>
                        <span><g:message code="price.entered.with.tax"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.show_handling_tax" value="true" uncheck-value="false" ${configs.show_handling_tax == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.handling.tax"/></span>
                </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_discount_each_item" value="true" uncheck-value="false" ${configs.show_discount_each_item == "true" ? "checked" : ""}> &nbsp;
                        <span><g:message code="show.discount.each.item"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.show_tax_each_item" value="true" uncheck-value="false" ${configs.show_tax_each_item == "true" ? "checked" : ""}> &nbsp;
                    <span><g:message code="show.tax.each.item"/></span>
                </div>
                </div>

                <div class="double-input-row">

                </div>

                <div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.terms_and_condition" ${configs.terms_and_condition == "on" ? "checked" : ""} uncheck-value="false" toggle-target="term-condition-field"/>
                    <span><g:message code="show.terms.condition"/></span>
                </div>
                <div class="form-row term-condition-field chosen-wrapper">
                    <label><g:message code="terms.condition.type"/><span class="suggestion">e.g. Specific Text</span></label>
                    <ui:namedSelect key="${NamedConstants.TERMS_AND_CONDITION_TYPE}" name="checkout_page.terms_and_condition_type" class="large" value="${configs.terms_and_condition_type}"/>
                </div>
                <g:include controller="setting" action="loadReferenceSelectorBasedOnType" params="${[type: configs.terms_and_condition_type, ref: configs.terms_and_condition_ref]}"/>
            </plugin:hookTag>
        </div>
    </div>
</form>