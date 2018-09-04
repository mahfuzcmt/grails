<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="filterGroup" value="${DomainConstants.SITE_CONFIG_TYPES.FILTER_GROUP_PAGE}"/>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${filterGroup}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="filter.group.page"/></h3>
            <div class="info-content"><g:message code="section.text.filter.group.page.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="thumbnail.image"/></label>
                <div class="twice-input-row">
                    <input type="text" name="${filterGroup}.thumbnail_width" class="smaller" validation="required digits max[400] min[150] maxlength[9]" restrict="numeric" maxlength="9" value="${configs.thumbnail_width}"><span>x</span><input type="text" name="${filterGroup}.thumbnail_height" class="smaller" validation="required digits max[400] min[60] maxlength[9]" restrict="numeric" maxlength="9" value="${configs.thumbnail_height}">
                </div>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="display.type"/><span class="suggestion">e. g. Scrollable</span></label>
                <ui:namedSelect class="medium display-type" toggle-target="pagination-props" name="${filterGroup}.display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${configs["display-type"]}"/>
            </div>
            <div class="form-row chosen-wrapper pagination-row pagination-props-image pagination-props-list">
                <label><g:message code="show.pagination"/></label>
                <ui:namedSelect class="medium show-pagination" toggle-target="item-per-page" name="${filterGroup}.show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${configs["show-pa" + "gination"]}"/>
            </div>
            <div class="form-row mandatory item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <label><g:message code="number.of.products"/></label>
                <input type="text" class="medium" name="${filterGroup}.item-per-page" value="${configs["item-per-page"]}" validation="skip@if{self::hidden} required digits gt[0] maxlength[9]" restrict="numeric" maxlength="9"/>
            </div>
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filterGroup}.price" value="true" uncheck-value="false" ${configs["price"] == "true" ? "checked" : ""} toggle-target="price-toggle">
                <span><g:message code="price"/></span>
            </div>
            <div class="form-row">
                <label><g:message code="label.for.call.for.price"/></label>
                <input type="text" name="${filterGroup}.label_for_call_for_price" value="${configs.label_for_call_for_price}" validation="required">
            </div>
            <div class="price-toggle">
                <div class="form-row">
                    <label><g:message code="label.for.price" /></label>
                    <input type="text" name="${filterGroup}.label_for_price" value="${configs.label_for_price}" >
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="${filterGroup}.expect_to_pay_price" value="true" uncheck-value="false" ${configs.expect_to_pay_price == "true" ? "checked='checked'" : ""} toggle-target="label-for-expect">
                        <span><g:message code="show.expect.to.pay.price" /></span>
                    </div><div class="form-row label-for-expect">
                        <input type="checkbox" class="single" name="${filterGroup}.expect_to_pay_price_with_tax" ${configs.expect_to_pay_price_with_tax == "true" ? "checked" : ""} value="true" uncheck-value="false">
                        <span><g:message code="show.tax.with.expect.to.pay.price"/></span>
                    </div>
                </div>

                <div class="form-row label-for-expect">
                    <label><g:message code="label.for.expect.to.pay" /></label>
                    <input type="text" name="${filterGroup}.label_for_expect_to_pay" value="${configs.label_for_expect_to_pay}">
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filterGroup}.description" value="true" uncheck-value="false" ${configs["description"] == "true" ? "checked" : ""}>
                <span><g:message code="short.description"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filterGroup}.add_to_cart" value="true" uncheck-value="false" ${configs["add_to_cart"] == "true" ? "checked" : ""}>
                <span><g:message code="add.to.cart"/></span>
            </div>
            <div class="form-row pagination-props-image pagination-props-list">
                <input type="checkbox" class="single" name="${filterGroup}.show_view_switcher" value="true" uncheck-value="false" ${configs["show_view_switcher"] == "true" ? "checked" : ""}>
                <span><g:message code="show.view.switching.buttons"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filterGroup}.show_on_hover" value="true" uncheck-value="false" ${configs["show_on_hover"] == "true" ? "checked" : ""}>
                <span><g:message code="show.on.hover"/></span>
            </div>
            <div class="form-row item-par-page-selection item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <input type="checkbox" class="single" name="${filterGroup}.item-per-page-selection" value="true" uncheck-value="false" ${configs["item-per-page-selection"] == "true" ? "checked" : ""}>
                <span><g:message code="item.per.page.selection"/></span>
            </div>
            <div class="form-row chosen-wrapper pagination-row pagination-props-image pagination-props-list">
                <input type="checkbox" class="single" name="${filterGroup}.sortable" value="true" uncheck-value="false" ${configs["sortable"] == "true" ? "checked" : ""}>
                <span><g:message code="sortable"/></span>
            </div>
            <plugin:hookTag hookPoint="configProductInfilterGroupPage" attrs="${[configType: filterGroup]}"/>
        </div>
    </div>

    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="filter.group.page.details"/></h3>
            <div class="info-content"><g:message code="section.text.setting.filter.group.page.details"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${filterGroup}.show_image" value="true" uncheck-value="false" ${configs["show_image"] == "true" ? "checked" : ""}>
                    <span><g:message code="image"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="${filterGroup}.show_description" value="true" uncheck-value="false" ${configs["show_description"] == "true" ? "checked" : ""}>
                    <span><g:message code="description"/></span>
                </div>
            </div>
        </div>
    </div>
    <div class="form-section">
        <div class="form-section-container">
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>

</form>
