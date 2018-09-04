<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form class="e-commerce-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="eCommerceSettingsForm">
    <input type="hidden" name="type" value="e_commerce">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="continue.shopping"/></h3>
            <div class="info-content"><g:message code="section.text.setting.continue.shopping"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="medium single" name="e_commerce.enable_continue_shopping" value="true" ${config.enable_continue_shopping == "true" ? "checked" : ""} uncheck-value="false" toggle-target="show-continue-shopping">
                <span><g:message code="enable.continue.shopping"/></span>
            </div>
            <div class="form-row mandatory show-continue-shopping">
                <label><g:message code="label"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.label"/></span></label>
                <input type="text" class="medium" name="e_commerce.continue_shopping_label" value="${config.continue_shopping_label?.encodeAsBMHTML()}" maxlength="50" validation="maxlength[50] required@if{self::visible}">
            </div>
            <div class="form-row show-continue-shopping chosen-wrapper">
                <label><g:message code="link.target"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.link.target"/></span></label>
                <g:select class="medium" name="e_commerce.continue_shopping_target" from="${[g.message(code: "home.page"), g.message(code: "previous.page"), g.message(code: "specified")]}"
                          value="${config.continue_shopping_target}" keys="${['home', 'previous', 'specified']}" toggle-target="link-target"/>
            </div>
            <div class="form-row thicker-row link-target-specified chosen-wrapper">
                <label><g:message code="target.page"/></label>
                <g:select class="medium" name="e_commerce.continue_shopping_specified_target" from="${pageNames}" keys="${pageUrls}" value="${config.continue_shopping_specified_target}"/>
            </div>
        </div>
    </div>

    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.inventory"/></h3>
            <div class="info-content"><g:message code="section.text.setting.product.inventoory"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" name="e_commerce.show_out_of_stock_products" value="true" uncheck-value="false" ${config.show_out_of_stock_products == "true" ? "checked='checked'" : ""}>
                <span><g:message code="show.out.of.stock.products"/></span>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="e_commerce.low_stock_notification" value="true" uncheck-value="false" ${config.low_stock_notification == "true" ? "checked='checked'" : ""}>
                    <span><g:message code="out.of.stock.notification"/></span>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="single" name="e_commerce.out_of_stock_notification" value="true" uncheck-value="false" ${config.out_of_stock_notification == "true" ? "checked='checked'" : ""}>
                    <span><g:message code="low.stock.notification"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row thicker-row">
                    <label><g:message code="available.stock.message"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.available.message"/></span></label>
                    <input type="text" class="medium" name="e_commerce.available_stock_message" value="${config.available_stock_message.encodeAsBMHTML()}" maxlength="50" validation="maxlength[50]">
                </div><div class="form-row thicker-row">
                    <label><g:message code="low.stock.message"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.low.stock.message"/></span></label>
                    <input type="text" class="medium" name="e_commerce.low_stock_message" value="${config.low_stock_message.encodeAsBMHTML()}" maxlength="50" validation="maxlength[50]">
                </div>
            </div>

            <div class="double-input-row">
                <div class="form-row thicker-row">
                    <label><g:message code="out.stock.message"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.outofstock.message"/></span></label>
                    <input type="text" class="medium" name="e_commerce.out_stock_message" value="${config.out_stock_message.encodeAsBMHTML()}" maxlength="50" validation="maxlength[50]">
                </div><div class="form-row thicker-row chosen-wrapper">
                    <label><g:message code="update.stock"/><span class="suggestion"><g:message code="suggestion.setting.ecommerce.update.stock.message"/></span></label>
                    <ui:namedSelect class="medium" key="${NamedConstants.UPDATE_STOCK}" name="e_commerce.update_stock" value="${config.update_stock}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="if.stock.less.then.quantity"/></h3>
            <div class="info-content"><g:message code="section.text.setting.low.stock"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="radio" toggle-target="can-buy-message" name="e_commerce.order_quantity_over_stock"
                       value="${DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.ADD_AVAILABLE}" ${config.order_quantity_over_stock == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.ADD_AVAILABLE ? "checked='true'": ""}>
                <span><g:message code="add.purchasable.quantity.cart"/> </span>
            </div>
            <div class="form-row can-buy-message">
                <label><g:message code="message"/></label>
                <textarea name="e_commerce.add_available_message" class="large">${config.add_available_message}</textarea> &nbsp; <span class="note"><g:message code="supported.macros.are"/> (%requested_quantity%, %available_quantity%)</span>
            </div>
            <div class="form-row">
                <input type="radio" name="e_commerce.order_quantity_over_stock"
                       value="${DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY}" ${config.order_quantity_over_stock == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY ? "checked='true'": ""}>
                <span><g:message code="sell.away"/> </span>
            </div>
            <div class="form-row">
                <input type="radio" toggle-target="not-sell-message" name="e_commerce.order_quantity_over_stock"
                       value="${DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.DO_NOT_SELL}" ${config.order_quantity_over_stock == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.DO_NOT_SELL ? "checked='true'": ""}>
                <span><g:message code="do.not.sell.out.of.stock"/> </span>
            </div>
            <div class="form-row not-sell-message">
                <label><g:message code="message"/> <span class="suggestion">e.g. s:requested.quantity.not.available</span></label>
                <textarea name="e_commerce.do_not_sell_message" class="large">${config.do_not_sell_message}</textarea> &nbsp; <span class="note"><g:message code="supported.macros.are"/> (%requested_quantity%, %maximum_quantity%)</span>
            </div>

        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="minimum.purchase.amount"/></h3>
            <div class="info-content"><g:message code="minimum.purchase.amount.message"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="medium single" name="e_commerce.enable_minimum_purchase_amount" value="true" ${config.enable_minimum_purchase_amount == "true" ? "checked" : ""} uncheck-value="false" toggle-target="minimum-purchase-amount">
                <span><g:message code="enable.minimum.purchase.amount"/></span>
            </div>
            <div class="form-row mandatory minimum-purchase-amount">
                <label><g:message code="minimum.purchase.amount"/> <span class="suggestion">e.g. 100</span></label>
                <input type="text" class="medium" name="e_commerce.minimum_purchase_amount" value="${config.minimum_purchase_amount?.encodeAsBMHTML()}" maxlength="50" validation="skip@if{self::hidden} maxlength[50] number required@if{self::visible}">
            </div>
        </div>
    </div>
    <plugin:hookTag hookPoint="eCommerceSettingsTab" attrs="${[config:config]}"/>

    <div class="form-section">
        <div class="form-section-container">
            <div class="form-row">
                <button type="submit" class="submit-button e-commerce-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
