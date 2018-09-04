<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="create-edit-form">
    <div class="form-section">
        <input type="hidden" name="type" value="category_page">
        <div class="form-section-info">
            <h3><g:message code="category"/></h3>
            <div class="info-content"><g:message code="section.text.setting.category.page.category"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="category_page.category_image" value="true" uncheck-value="false" ${config["category_image"] == "true" ? "checked" : ""}>
                    <span><g:message code="image"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="category_page.category_description" value="true" uncheck-value="false" ${config["category_description"] == "true" ? "checked" : ""}>
                    <span><g:message code="show.description"/></span>
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="category_page.subcategory" value="true" uncheck-value="false" ${config["subcategory"] == "true" ? "checked" : ""} toggle-target="subcategory-group">
                <span><g:message code="show.subcategory"/></span>
            </div>
            <div class="form-row">
                <g:set var="category_page" value="${DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE}"/>
                <input type="checkbox" class="single" name="${category_page}.show_view_switcher" value="true" uncheck-value="false" ${config["show_view_switcher"] == "true" ? "checked" : ""}>
                <span><g:message code="show.view.switching.buttons"/></span>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="subcategory"/></h3>
            <div class="info-content"><g:message code="section.text.setting.category.page.subcategory"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="show.pagination"/><span class="suggestion"><g:message code="suggestion.setting.category.pagination"/></span></label>
                    <ui:namedSelect class="medium" toggle-target="cat-pagination" toggle-anim="slide" name="category_page.subcategory_show_pagination" key="${NamedConstants.PAGINATION_MESSAGE}"
                                    value="${config["subcategory_show_pagination"]}"/>
                </div><div class="form-row mandatory cat-pagination-top cat-pagination-bottom cat-pagination-top_and_bottom">
                    <label><g:message code="number.of.subcategories"/></label>
                    <input type="text" class="medium" name="category_page.subcategory_item_per_page" validation="skip@if{self::hidden} required gt[0]"
                           value="${config["subcategory_item_per_page"]}"/>
                </div>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="category_page.subcategory_description" value="true" uncheck-value="false" ${config["subcategory_description"] == "true" ? "checked" : ""}>
                <span><g:message code="short.description"/></span>
            </div>

        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product"/></h3>
            <div class="info-content"><g:message code="section.text.setting.category.page.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row pagination-row chosen-wrapper">
                    <label><g:message code="show.pagination"/><span class="suggestion"><g:message code="suggestion.setting.category.pagination"/></span></label>
                    <ui:namedSelect class="medium" toggle-target="pagination" toggle-anim="slide" name="category_page.show_pagination" key="${NamedConstants.PAGINATION_MESSAGE}"
                                    value="${config["show_pagination"]}"/>
                </div><div class="form-row mandatory pagination-top pagination-bottom pagination-top_and_bottom">
                    <label><g:message code="number.of.products"/></label>
                    <input type="text" class="medium" name="category_page.item_per_page" validation="skip@if{self::hidden} required gt[0]" value="${config["item_per_page"]}"/>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="category_page.sortable" value="true" uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                    <span><g:message code="sortable"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="category_page.show_on_hover" value="true" uncheck-value="false" ${config["show_on_hover"] == "true" ? "checked" : ""}>
                    <span><g:message code="show.on.hover"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="category_page.add_to_cart" value="true" uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                    <span><g:message code="add.to.cart"/></span>
                </div><div class="form-row pagination-top pagination-bottom pagination-top_and_bottom">
                    <input type="checkbox" class="single" name="category_page.item_per_page_selection" value="true" uncheck-value="false" ${config["item_per_page_selection"] == "true" ? "checked" : ""}>
                    <span><g:message code="item.per.page.selection"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="category_page.price" value="true" uncheck-value="false" ${config.price == "true" ? "checked" : ""} toggle-target="price-toggle">
                    <span><g:message code="price"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="category_page.description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                    <span><g:message code="short.description"/></span>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="label.for.call.for.price"/></label>
                <input type="text" name="category_page.label_for_call_for_price" value="${config.label_for_call_for_price}" validation="required">
            </div>
            <div class="price-toggle">
                <div class="form-row">
                    <label><g:message code="label.for.price" /></label>
                    <input type="text" name="category_page.label_for_price" value="${config.label_for_price}" >
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="category_page.expect_to_pay_price" value="true" uncheck-value="false" ${config.expect_to_pay_price == "true" ? "checked='checked'" : ""} toggle-target="label-for-expect">
                        <span><g:message code="show.expect.to.pay.price" /></span>
                    </div><div class="form-row label-for-expect">
                        <input type="checkbox" class="single" name="category_page.expect_to_pay_price_with_tax" ${config.expect_to_pay_price_with_tax == "true" ? "checked" : ""} value="true" uncheck-value="false">
                        <span><g:message code="show.tax.with.expect.to.pay.price"/></span>
                </div>
                </div>
                <div class="form-row label-for-expect">
                    <label><g:message code="label.for.expect.to.pay" /></label>
                    <input type="text" name="category_page.label_for_expect_to_pay" value="${config.label_for_expect_to_pay}">
                </div>
            </div>
            <plugin:hookTag hookPoint="configProductInCategoryPage"/>

            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>