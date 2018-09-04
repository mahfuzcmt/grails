<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="checkout_page"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
            <div class="info-content"></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="page.title"/><span class="suggestion"><g:message code="suggestion.setting.chkout.page.title"/></span></label>
                <input type="text" class="large" name="checkout_page.page_title" value="${configs.page_title}"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="customer.registration"/></h3>
            <div class="info-content"></div>
        </div>
        <div class="form-section-container nested-panel" validation="skip@if{this:.auth-option:checked} fail" message_template="at.least.option.required">
            <div class="form-row">
                <input type="checkbox" class="single auth-option" name="checkout_page.allow_sign_up" value="true" uncheck-value="false" ${configs.allow_sign_up == "true" ? "checked" : ""} toggle-target="allow-sign-up-dependents">
                <span><g:message code="allow.sing.up"/></span>
            </div>
            <div class="nested-content allow-sign-up-dependents">
                <div class="form-row">
                    <input type="checkbox" class="single" name="checkout_page.is_sign_up_required" value="true" ${configs.is_sign_up_required == "true" ? "checked" : ""} uncheck-value="false">
                    <span><g:message code="is.sign.up.required"/></span>
                </div>
            </div>
            <div class="form-row allow-sign-up-dependents" do-reverse-toggle>
                <input type="checkbox" class="single" name="checkout_page.show_login" value="true" ${configs.show_login == "true" ? "checked" : ""} uncheck-value="false">
                <span><g:message code="show.login"/></span>
            </div>
            <div class="form-row checkout-guest">
                <input type="checkbox" class="single auth-option" name="checkout_page.show_checkout_as_a_guest" value="true" uncheck-value="false" ${configs.show_checkout_as_a_guest == "true" ? "checked" : ""}>
                <span><g:message code="checkout.guest"/></span>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="billing.and.shipping.info"/></h3>
            <div class="info-content"></div>
        </div>
        <div class="form-section-container nested-panel" validation="skip@if{this:.shipping-option:checked} fail" message_template="at.least.option.required">
            <div class="form">
                <input type="checkbox" class="single shipping-option" name="checkout_page.enable_shipping" value="true" uncheck-value="false" ${configs.enable_shipping == "true" ? "checked" : ""} toggle-target="enable-shipping-dependents">
                <label><g:message code="enable.shipping" /></label>
            </div>
            <div class="double-input-row nested-content enable-shipping-dependents">
                <div class="form-row">
                    <label><g:message code="display.text"/></label>
                    <input type="text" name="checkout_page.shipping_display_text" value="${configs.shipping_display_text}" validation="skip@if{self::hidden} required" maxlength="200">
                </div><div class="form-row">
                    <label><g:message code="display.subtext"/></label>
                    <input type="text" name="checkout_page.shipping_display_subtext" value="${configs.shipping_display_subtext}" maxlength="200">
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single shipping-option" name="checkout_page.enable_store_pickup" value="true" uncheck-value="false" ${configs.enable_store_pickup == "true" ? "checked" : ""} toggle-target="enable-store-pickup-dependents">
                <span><g:message code="enable.store.pickup"/></span>
            </div>
            <div class="double-input-row nested-content enable-store-pickup-dependents">
                <div class="form-row">
                    <label><g:message code="display.text"/></label>
                    <input type="text" name="checkout_page.store_pickup_display_text" value="${configs.store_pickup_display_text}" validation="skip@if{self::hidden} required" maxlength="200">
                </div><div class="form-row">
                    <label><g:message code="display.subtext"/></label>
                    <input type="text" name="checkout_page.store_pickup_display_subtext" value="${configs.store_pickup_display_subtext}" maxlength="200">
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single shipping-option" name="checkout_page.enable_others_shipping" value="true" uncheck-value="false" ${configs.enable_others_shipping == "true" ? "checked" : ""} toggle-target="enable-shipping-quote-dependents">
                <span><g:message code="enable.shipping.quote"/></span>
            </div>
            <div class="double-input-row nested-content enable-shipping-quote-dependents">
                <div class="form-row">
                    <label><g:message code="display.text"/></label>
                    <input type="text" name="checkout_page.others_shipping_display_text" value="${configs.others_shipping_display_text}" validation="skip@if{self::hidden} required" maxlength="200">
                </div><div class="form-row">
                    <label><g:message code="display.subtext"/></label>
                    <input type="text" name="checkout_page.others_shipping_display_subtext" value="${configs.others_shipping_display_subtext}" maxlength="200">
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="shipping.and.handling"/></h3>
            <div class="info-content"></div>
        </div>
        <div class="form-section-container nested-panel">
            <div class="form-row">
                <label><g:message code="shipping.and.handling.subtext"/></label>
                <input type="text" name="checkout_page.shipping_handling_subtext" value="${configs.shipping_handling_subtext}" validation="skip@if{self::hidden} required" maxlength="200">
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="order.confirmation"/></h3>
            <div class="info-content"></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="checkoutPageConfirmSectionSettings">
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_product_thumbnail" value="true" uncheck-value="false" ${configs.show_product_thumbnail == "true" ? "checked" : ""}>
                        <span><g:message code="show.product.thumbnail.image"/></span>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.price_enter_with_tax" value="true" uncheck-value="false" ${configs.price_enter_with_tax == "true" ? "checked" : ""}>
                        <span><g:message code="all.price.displayed.tax.inclusive"/></span>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_product_total_with_discount" value="true" uncheck-value="false" ${configs.show_product_total_with_discount == "true" ? "checked" : ""}>
                        <span><g:message code="show.product.total.with.discount"/></span>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_subtotal_with_discount" value="true" uncheck-value="false" ${configs.show_subtotal_with_discount == "true" ? "checked" : ""}>
                        <span><g:message code="show.subtotal.with.discount"/></span>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_total_discount" value="true" uncheck-value="false" ${configs.show_total_discount == "true" ? "checked" : ""} toggle-target="show-total-discount">
                        <span><g:message code="show.total.discount"/></span>
                    </div>
                    <div class="nested-content show-total-discount">
                        <div class="form-row">
                            <label><g:message code="show.total.discount.label"/><span class="suggestion"><g:message code="suggestion.show.total.discount.label"/></span></label>
                            <input class="large" name="checkout_page.show_total_discount_label" type="text" value="${configs.show_total_discount_label}">
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_discount_each_item" value="true" uncheck-value="false" ${configs.show_discount_each_item == "true" ? "checked" : ""} toggle-target="show-discount-each-item">
                        <span><g:message code="show.discount.each.item"/></span>
                    </div>
                    <div class="nested-content show-discount-each-item">
                        <div class="form-row">
                            <label><g:message code="show.discount.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.discount.each.item.label"/></span></label>
                            <input class="large" name="checkout_page.show_discount_for_each_item_label" type="text" value="${configs.show_discount_for_each_item_label}">
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_tax_each_item" value="true" uncheck-value="false" ${configs.show_tax_each_item == "true" ? "checked" : ""} toggle-target="show-tax-each-item">
                        <span><g:message code="show.tax.each.item"/></span>
                    </div>
                    <div class="nested-content show-tax-each-item">
                        <div class="form-row">
                            <label><g:message code="show.tax.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.tax.each.item.label"/></span></label>
                            <input type="text"name="checkout_page.show_tax_for_each_item_label" class="large" value="${configs.show_tax_for_each_item_label}"/>
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_subtotal" value="true" uncheck-value="false" ${configs.show_subtotal == "true" ? "checked" : ""} toggle-target="show-subtotal">
                        <span><g:message code="show.subtotal"/></span>
                    </div>
                    <div class="nested-content show-subtotal">
                        <div class="form-row">
                            <label><g:message code="show.subtotal.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.label"/></span></label>
                            <input type="text"name="checkout_page.show_sub_total_label" class="large" value="${configs.show_sub_total_label}"/>
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_sub_total_tax" value="true" uncheck-value="false" ${configs.show_sub_total_tax == "true" ? "checked" : ""} toggle-target="show-sub-total-tax">
                        <span><g:message code="show.total.tax"/></span>
                    </div>
                    <div class="nested-content show-sub-total-tax">
                        <div class="form-row">
                            <label><g:message code="show.subtotal.tax.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.tax.label"/></span></label>
                            <input class="large" name="checkout_page.show_sub_total_tax_label" type="text" value="${configs.show_sub_total_tax_label}">
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_shipping_cost" value="true" uncheck-value="false" ${configs.show_shipping_cost == "true" ? "checked" : ""} toggle-target="show-shipping-cost">
                        <span><g:message code="show.shipping.cost"/></span>
                    </div>
                    <div class="nested-content show-shipping-cost">
                        <div class="form-row">
                            <label><g:message code="show.shipping.cost.label"/><span class="suggestion"><g:message code="suggestion.show.shipping.cost.label"/></span></label>
                            <input class="large" name="checkout_page.show_shipping_cost_label" type="text" value="${configs.show_shipping_cost_label}">
                        </div>
                        <div class="nested-panel">
                            <div class="form-row">
                                <input type="checkbox" class="single" name="checkout_page.show_shipping_and_handling_cost_separately" value="true" uncheck-value="false" ${configs.show_shipping_and_handling_cost_separately == "true" ? "checked" : ""} toggle-target="show-shipping-and-handling-cost-separately">
                                <span><g:message code="show.shipping.and.handling.cost.separately"/></span>
                            </div>
                            <div class="nested-content show-shipping-and-handling-cost-separately">
                                <div class="form-row">
                                    <label><g:message code="show.handling.cost.label"/><span class="suggestion"><g:message code="suggestion.show.handling.cost.label"/></span></label>
                                    <input class="large" name="checkout_page.show_handling_cost_label" type="text" value="${configs.show_handling_cost_label}">
                                </div>
                            </div>
                        </div>
                        <div class="nested-panel">
                            <div class="form-row">
                                <input type="checkbox" class="single" name="checkout_page.show_shipping_tax" value="true" uncheck-value="false" ${configs.show_shipping_tax == "true" ? "checked" : ""} toggle-target="show-shipping-tax">
                                <span><g:message code="show.shipping.tax"/></span>
                            </div>
                            <div class="nested-content show-shipping-tax">
                                <div class="form-row">
                                    <label><g:message code="show.shipping.tax.label"/><span class="suggestion"><g:message code="suggestion.show.shipping.tax.label"/></span></label>
                                    <input type="text"name="checkout_page.show_shipping_tax_label" class="large" value="${configs.show_shipping_tax_label}"/>
                                </div>
                                <div class="form-row show-shipping-and-handling-cost-separately">
                                    <label><g:message code="show.handling.tax.label"/><span class="suggestion"><g:message code="suggestion.show.handling.tax.label"/></span></label>
                                    <input type="text"name="checkout_page.show_handling_tax_label" class="large" value="${configs.show_handling_tax_label}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_shipping_discount" value="true" uncheck-value="false" ${configs.show_shipping_discount == "true" ? "checked" : ""} toggle-target="show-shipping-discount">
                        <span><g:message code="show.shipping.discount"/></span>
                    </div>
                    <div class="nested-content show-shipping-discount">
                        <div class="form-row">
                            <label><g:message code="show.shipping.discount.label"/><span class="suggestion"><g:message code="suggestion.show.shipping.discount.label"/></span></label>
                            <input class="large" name="checkout_page.show_shipping_discount_label" type="text" value="${configs.show_shipping_discount_label}">
                        </div>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.show_payment_surcharge" value="true" uncheck-value="false" ${configs.show_payment_surcharge == "true" ? "checked" : ""}>
                        <span><g:message code="show.payment.surcharge"/></span>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.terms_and_condition" ${configs.terms_and_condition == "on" ? "checked" : ""} uncheck-value="false" toggle-target="term-condition-field"/>
                        <span><g:message code="show.terms.condition"/></span>
                    </div>
                    <div class="nested-content term-condition-field">
                        <div class="form-row form-rowchosen-wrapper">
                            <label><g:message code="terms.condition.type"/><span class="suggestion">e.g. Specific Text</span></label>
                            <ui:namedSelect key="${NamedConstants.TERMS_AND_CONDITION_TYPE}" name="checkout_page.terms_and_condition_type" class="large" value="${configs.terms_and_condition_type}"/>
                        </div>
                        <g:include controller="setting" action="loadReferenceSelectorBasedOnType" params="${[type: configs.terms_and_condition_type, ref: configs.terms_and_condition_ref]}"/>
                    </div>
                </div>
                <div class="nested-panel">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="checkout_page.enable_comment" value="true" uncheck-value="false" ${configs.enable_comment == "true" ? "checked" : ""}>
                        <span><g:message code="enable.order.comment"/></span>
                    </div>
                </div>
            </plugin:hookTag>
        </div>
    </div>
    <div class="form-section">
        <div class="form-section-container">
           <div class="form-row btn-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>