<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="giftRegistry" value="${DomainConstants.SITE_CONFIG_TYPES.GIFT_REGISTRY}"/>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_REGISTRY)}"/>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${giftRegistry}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="gift.registry"/></h3>
            <div class="info-content"><g:message code="section.text.setting.gift.registry"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="display.type"/></label>
                <ui:namedSelect class="medium display-type" toggle-target="pagination-props" name="${giftRegistry}.display-type"
                                key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}"
                                value="${config["display-type"]}"/>
            </div>
            <div class="form-row pagination-row pagination-props pagination-props-image pagination-props-list chosen-wrapper">
                <label><g:message code="show.pagination"/></label>
                <ui:namedSelect class="medium show-pagination" toggle-target="item-per-page" name="${giftRegistry}.show-pagination"
                                key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
            </div>
            <div class="form-row mandatory item-per-page item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <label><g:message code="number.of.products"/><span class="suggestion">e. g. 20</span></label>
                <input type="text" class="medium" restrict="numeric" maxlength="9" name="${giftRegistry}.item-per-page" value="${config["item-per-page"]}" validation="skip@if{self::hidden} required"/>
            </div>
            <div class="form-row">
                <label><g:message code="show.hide"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${giftRegistry}.price" value="true"
                           uncheck-value="false" ${config["price"] == "true" ? "checked" : ""}>
                    <span><g:message code="price"/></span>
                </div><div class="form-row">
                <input type="checkbox" class="single" name="${giftRegistry}.description" value="true"
                       uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                <span><g:message code="short.description"/></span>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${giftRegistry}.add_to_cart" value="true"
                           uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                    <span><g:message code="add.to.cart"/></span>
                </div><div class="form-row">
                <input type="checkbox" class="single" name="${giftRegistry}.sortable" value="true"
                       uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                <span><g:message code="sortable"/></span>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="${giftRegistry}.show_on_hover" value="true"
                           uncheck-value="false" ${config["show_on_hover"] == "true" ? "checked" : ""}>
                    <span><g:message code="show.on.hover"/></span>
                </div><div class="form-row item-par-page-selection item-per-page-top item-per-page-bottom item-per-page-top_and_bottom">
                <input type="checkbox" class="single" name="${giftRegistry}.item-per-page-selection" value="true"
                       uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                <span><g:message code="item.per.page.selection"/></span>
            </div>
            </div>
            <div class="double-input-row">
                <plugin:isInstalled identifier="wish-list"><div class="form-row">
                    <input type="checkbox" class="single" name="${giftRegistry}.add_to_wish_list" value="true"
                           uncheck-value="false" ${config["add_to_wish_list"] == "true" ? "checked" : ""}>
                    <span><g:message code="add.to.wish.list"/></span>
                </div></plugin:isInstalled><plugin:isInstalled identifier="compare-product"><div class="form-row">
                <input type="checkbox" class="single" name="${giftRegistry}.add_to_compare" value="true" uncheck-value="false" ${config["add_to_compare"] == "true" ? "checked" : ""}>
                <span><g:message code="add.to.compare"/></span>
            </div></plugin:isInstalled>
            </div>
            <div class="double-input-row">
                <div class="form-row pagination-props-image pagination-props-list">
                    <input type="checkbox" class="single" name="${giftRegistry}.show_view_switcher" value="true"
                           uncheck-value="false" ${config["show_view_switcher"] == "true" ? "checked" : ""}>
                    <span><g:message code="show.view.switching.buttons"/></span>
                </div>
            </div>

            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>