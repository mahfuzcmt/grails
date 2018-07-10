<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="filter" value="${DomainConstants.SITE_CONFIG_TYPES.FILTER_PAGE}"/>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${filter}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="filter.page"/></h3>
            <div class="info-content"><g:message code="section.text.filter.page.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="display.type"/><span class="suggestion">e. g. Scrollable</span></label>
                <ui:namedSelect class="medium display-type" toggle-target="pagination-props" name="${filter}.display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${config["display-type"]}"/>
            </div>
            <div class="form-row chosen-wrapper pagination-row pagination-props-image pagination-props-list">
                <label><g:message code="show.pagination"/></label>
                <ui:namedSelect class="medium show-pagination" toggle-target="item-per-page" name="${filter}.show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pa" + "gination"]}"/>
            </div>
            <div class="form-row mandatory item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <label><g:message code="number.of.products"/></label>
                <input type="text" class="medium" maxlength="9" restrict="numeric" name="${filter}.item-per-page" value="${config["item-per-page"]}" validation="skip@if{self::hidden} required gt[0]"/>
            </div>
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filter}.price" value="true" uncheck-value="false" ${config["price"] == "true" ? "checked" : ""}>
                <span><g:message code="price"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filter}.description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                <span><g:message code="short.description"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filter}.add_to_cart" value="true" uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                <span><g:message code="add.to.cart"/></span>
            </div>
            <div class="form-row item-par-page-selection item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <input type="checkbox" class="single" name="${filter}.item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                <span><g:message code="item.per.page.selection"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filter}.sortable" value="true" uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                <span><g:message code="sortable"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="${filter}.is_rating_active" value="true" uncheck-value="false" ${config["is_rating_active"] == "true" ? "checked" : ""}>
                <span><g:message code="rating"/></span>
            </div>
            <plugin:hookTag hookPoint="configProductInFilterPage"/>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
