<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form id="shoppingCartSettingsForm" action="${app.relativeBaseUrl()}setting/saveConfigurations" onsubmit="return false" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="cart_page"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="page.title"/><span class="suggestion"><g:message code="suggestion.setting.cart.page.title"/></span></label>
                <input type="text" class="large" name="cart_page.page_title" value="${shoppingCartSettings.page_title}"/>
            </div>
            <div class="form-row">
                <label><g:message code="checkout.button.text"/><span class="suggestion"><g:message code="suggestion.setting.cart.chkout.btn"/></span></label>
                <input type="text" class="large" name="cart_page.checkout_button_text" value="${shoppingCartSettings.checkout_button_text}"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="error.messages"/></h3>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="invalid.quantity.message"/><span class="suggestion">e.g. s:invalid.quantity</span></label>
                    <input class="large" name="cart_page.invalid_quantity_message" type="text" value="${shoppingCartSettings.invalid_quantity_message}">
                </div><div class="form-row">
                    <label><g:message code="max.quantity.message"/><span class="suggestion">e.g. s:max.quantity.allowed</span></label>
                    <input type="text"name="cart_page.max_quantity_message" class="large" value="${shoppingCartSettings.max_quantity_message}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="min.quantity.message"/><span class="suggestion">e.g. s:min.required.quantity</span></label>
                    <input class="large" name="cart_page.min_quantity_message" type="text" value="${shoppingCartSettings.min_quantity_message}">
                </div><div class="form-row">
                    <label><g:message code="multiple.quantity.message"/><span class="suggestion">e.g. s:item.quantity.should.multiple</span></label>
                    <input type="text"name="cart_page.multiple_quantity_message" class="large" value="${shoppingCartSettings.multiple_quantity_message}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="shopping.cart.page"/></h3>
        </div>
        <div class="form-section-container">
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_product_thumbnail" value="true" uncheck-value="false" ${shoppingCartSettings.show_product_thumbnail == "true" ? "checked" : ""}>
                    <span><g:message code="show.product.thumbnail.image"/></span>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.price_enter_with_tax" value="true" uncheck-value="false" ${shoppingCartSettings.price_enter_with_tax == "true" ? "checked" : ""}>
                    <span><g:message code="all.price.displayed.tax.inclusive"/></span>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <label><g:message code="price.column.label"/><span class="suggestion"><g:message code="suggestion.setting.cart.page.price.label"/></span></label>
                    <input class="large" name="cart_page.price_column_text" type="text" value="${shoppingCartSettings.price_column_text}">
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <label><g:message code="item.total.label"/><span class="suggestion"><g:message code="suggestion.setting.cart.page.item.total"/></span></label>
                    <input type="text"name="cart_page.item_total_label" class="large" value="${shoppingCartSettings.item_total_label}"/>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_product_total_with_discount" value="true" uncheck-value="false" ${shoppingCartSettings.show_product_total_with_discount == "true" ? "checked" : ""}>
                    <span><g:message code="show.product.total.with.discount"/></span>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_subtotal_with_discount" value="true" uncheck-value="false" ${shoppingCartSettings.show_subtotal_with_discount == "true" ? "checked" : ""}>
                    <span><g:message code="show.subtotal.with.discount"/></span>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_total_discount" value="true" uncheck-value="false" ${shoppingCartSettings.show_total_discount == "true" ? "checked" : ""} toggle-target="total-discount-label">
                    <span><g:message code="show.total.discount"/></span>
                </div>
                <div class="nested-content total-discount-label">
                    <div class="form-row">
                        <label><g:message code="show.total.discount.label"/><span class="suggestion"><g:message code="suggestion.show.total.discount.label"/></span></label>
                        <input class="large" name="cart_page.show_total_discount_label" type="text" value="${shoppingCartSettings.show_total_discount_label}">
                    </div>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_discount_each_item" value="true" uncheck-value="false" ${shoppingCartSettings.show_discount_each_item == "true" ? "checked" : ""} toggle-target="each-discount-label"/>
                    <span><g:message code="show.discount.each.item"/></span>
                </div>
                <div class="nested-content each-discount-label">
                    <div class="form-row">
                        <label><g:message code="show.discount.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.discount.each.item.label"/></span></label>
                        <input class="large" name="cart_page.show_discount_for_each_item_label" type="text" value="${shoppingCartSettings.show_discount_for_each_item_label}">
                    </div>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_tax_each_item" value="true" uncheck-value="false" ${shoppingCartSettings.show_tax_each_item == "true" ? "checked" : ""} toggle-target="each-item-tax-label">
                    <span><g:message code="show.tax.each.item"/></span>
                </div>
                <div class="nested-content each-item-tax-label">
                    <div class="form-row">
                        <label><g:message code="show.tax.each.item.label"/><span class="suggestion"><g:message code="suggestion.show.tax.each.item.label"/></span></label>
                        <input type="text"name="cart_page.show_tax_for_each_item_label" class="large" value="${shoppingCartSettings.show_tax_for_each_item_label}"/>
                    </div>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_subtotal" value="true" uncheck-value="false" ${shoppingCartSettings.show_subtotal == "true" ? "checked" : ""} toggle-target="subtotal-label">
                    <span><g:message code="show.subtotal"/></span>
                </div>
                <div class="nested-content subtotal-label">
                    <div class="form-row">
                        <label><g:message code="show.subtotal.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.label"/></span></label>
                        <input type="text"name="cart_page.show_sub_total_label" class="large" value="${shoppingCartSettings.show_sub_total_label}"/>
                    </div>
                </div>
            </div>
            <div class="nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cart_page.show_sub_total_tax" value="true" uncheck-value="false" ${shoppingCartSettings.show_sub_total_tax == "true" ? "checked" : ""} toggle-target="subtotal-tax-label">
                    <span><g:message code="show.total.tax"/></span>
                </div>
                <div class="nested-content subtotal-tax-label">
                    <div class="form-row">
                        <label><g:message code="show.subtotal.tax.label"/><span class="suggestion"><g:message code="suggestion.show.sub.total.tax.label"/></span></label>
                        <input class="large" name="cart_page.show_sub_total_tax_label" type="text" value="${shoppingCartSettings.show_sub_total_tax_label}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>