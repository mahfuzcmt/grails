<%@ page import="com.webcommander.constants.NamedConstants;com.webcommander.webcommerce.Category" %>
<g:applyLayout name="_widgetShortConfig">
    <plugin:hookTag hookPoint="productWidgetConfig">
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="display.type"/></div>
            <div class="sidebar-group-body">
                <ui:namedSelect class="sidebar-input display-type" toggle-target="pagination-props" name="display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${config["display-type"]}"/>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="filter.by"/></div>
            <div class="sidebar-group-body">
                <ui:namedSelect class="sidebar-input filter-by" toggle-target="filter-fields" name="filter-by" key="${NamedConstants.PRODUCT_WIDGET_FILTER}" value="${config["filter-by"]}"/>
            </div>
        </div>
        <div class="sidebar-group filter-fields-category">
            <div class="sidebar-group-label"><g:message code="category"/></div>
            <div class="sidebar-group-body">
                <ui:hierarchicalSelect name="category" class="medium category-selector" value="${config.category?.toLong()}" domain="${Category}"/>
            </div>
        </div>
        <div class="sidebar-group pagination-row pagination-props pagination-props-image pagination-props-list">
            <div class="sidebar-group-label"><g:message code="show.pagination"/></div>
            <div class="sidebar-group-body">
                <ui:namedSelect class="sidebar-input show-pagination" toggle-target="item-per-page" name="show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
            </div>
        </div>
        <div class="sidebar-group item-per-page item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
            <div class="sidebar-group-label"><g:message code="number.of.products"/></div>
            <div class="sidebar-group-body">
                <input type="text" class="sidebar-input" name="item_per_page" value="${config["item_per_page"]}"/>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="price" value="true" uncheck-value="false" ${config.price == "true" ? "checked" : ""} toggle-target="toggle-on-price">
                <label><g:message code="show.price"/></label>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-label">
                <g:message code="label.for.call.for.price"/>
            </div>
            <div class="sidebar-group-body">
                <input type="text" name="label_for_call_for_price" class="sidebar-input" value="${config["label_for_call_for_price"] ?: "s:call.for.price"}" validation="required">
            </div>
        </div>
        <div class="toggle-on-price">
            <div class="sidebar-group">
                <div class="sidebar-group-label">
                    <g:message code="label.for.price"/>
                </div>
                <div class="sidebar-group-body">
                    <input type="text" name="label_for_price" class="sidebar-input" value="${config["label_for_price"]}">
                </div>
            </div>
            <div class="sidebar-group">
                <div class="sidebar-group-body">
                    <input type="checkbox" class="single" name="expect_to_pay_price" value="true" uncheck-value="false" ${config.expect_to_pay_price == "true" ? "checked" : ""} toggle-target="expect-to-pay-toggle">
                    <label><g:message code="show.expect.to.pay.price"/></label>
                </div>
            </div>
            <div class="sidebar-group expect-to-pay-toggle">
                <div class="sidebar-group-label">
                    <g:message code="label.for.expect.to.pay"/>
                </div>
                <div class="sidebar-group-body">
                    <input type="text" name="label_for_expect_to_pay" class="sidebar-input" value="${config["label_for_expect_to_pay"]}">
                </div>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                <label><g:message code="short.description"/></label>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="add_to_cart" value="true" uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                <label><g:message code="add.to.cart"/></label>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="show_on_hover" value="true" uncheck-value="false" ${config["show_on_hover"] == "true" ? "checked" : ""}>
                <label><g:message code="show.on.hover"/></label>
            </div>
        </div>
        <div class="sidebar-group item-par-page-selection item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                <label><g:message code="item.per.page.selection"/></label>
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body pagination-props pagination-props-image pagination-props-list">
                <input type="checkbox" class="single" name="sortable" value="true" uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                <label><g:message code="sortable"/></label>
            </div>
        </div>
        <div class="sidebar-group pagination-props-image pagination-props-list">
            <div class="sidebar-group-body">
                <input type="checkbox" class="single" name="show_view_switcher" value="true" uncheck-value="false" ${config["show_view_switcher"] == "true" ? "checked" : ""}>
                <label><g:message code="show.view.switching.buttons"/></label>
            </div>
        </div>
    </plugin:hookTag>
    <div class="advance-config-btn filter-fields-none"><input type="button" value="${advanceText}"></div>
</g:applyLayout>