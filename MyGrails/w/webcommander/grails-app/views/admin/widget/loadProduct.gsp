<%@ page import="com.webcommander.constants.NamedConstants;" %>
<g:applyLayout name="_editWidget">
    <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.product.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div>
                    <div class="form-row">
                        <label><g:message code="title"/></label>
                        <input type="text" class="medium" name="title" value="${widget.title}">
                    </div>
                    <div class="form-row">
                        <label><g:message code="display.type"/></label>
                        <ui:namedSelect class="medium display-type" toggle-target="pagination-props" name="display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${config["display-type"]}"/>
                    </div>
                    <div class="form-row pagination-row pagination-props pagination-props-image pagination-props-list">
                        <label><g:message code="show.pagination"/></label>
                        <ui:namedSelect class="medium show-pagination" toggle-target="item-per-page" name="show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
                    </div>
                    <div class="form-row item-per-page item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                        <label><g:message code="number.of.products"/></label>
                        <input type="text" class="medium" name="item_per_page" value="${config["item_per_page"]}"/>
                    </div>
                    <div class="form-row">
                        <label><g:message code="show.hide"/></label>
                        <input class="single" type="checkbox" name="price" value="true" uncheck-value="false" ${config.price == "true" ? "checked" : ""}>
                        <span><g:message code="price"/></span>
                    </div>
                    <div class="form-row">
                        <label>&nbsp;</label>
                        <input class="single" type="checkbox" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                        <span><g:message code="short.description"/></span>
                    </div>
                    <div class="form-row">
                        <label>&nbsp;</label>
                        <input type="checkbox" class="single" name="add_to_cart" value="true" uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                        <span><g:message code="add.to.cart"/></span>
                    </div>
                    <div class="form-row item-par-page-selection item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                        <label>&nbsp;</label>
                        <input type="checkbox" class="single" name="item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                        <span><g:message code="item.per.page.selection"/></span>
                    </div>
                    <div class="form-row">
                        <label>&nbsp;</label>
                        <input type="checkbox" class="single" name="sortable" value="true" uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                        <span><g:message code="sortable"/></span>
                    </div>
                    <div class="form-row">
                        <label><g:message code="label.for.call.for.price"/></label>
                        <input type="text" class="medium" name="label_for_call_for_price" value="${config.label_for_call_for_price ?: "s:call.for.price"}" validation="required">
                    </div>
                    <plugin:hookTag hookPoint="productWidgetConfigurationPanel" attrs="[configs: config]"/>
                </div>
            </div>
            <g:include view="/admin/item/product/selection.gsp" model="${[products: products, fieldName: "product"]}"/>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:applyLayout>
