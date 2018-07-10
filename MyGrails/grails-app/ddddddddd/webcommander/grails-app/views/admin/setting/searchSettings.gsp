<%@ page import="com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="searchSettingForm" class="create-edit-form">
    <input type="hidden" name="type" value="${DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
            <div class="info-content"><g:message code="section.text.setting.search.general"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <input type="checkbox" class="single" name="search_page.submit_restricted_item" value="true" uncheck-value="false" ${searchConfig.submit_restricted_item == "true" ? "checked='checked'" : ""}>
                <span><g:message code="submit.restricted.item"/></span>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="items.per.page"/><span class="suggestion"><g:message code="suggestion.setting.search.items"/></span></label>
                <input type="text" name="search_page.item_per_page" class="smaller" validation="required digits min[5] maxlength[9]" maxlength="9" restrict="numeric" value="${searchConfig?.item_per_page}">
            </div>
        </div>
        <div class="section-separator"></div>
        <div class="form-section-info">
            <h3><g:message code="product"/></h3>
            <div class="info-content"><g:message code="section.text.setting.search.product"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="search.result.view"/></label>
                <input type="radio" name="search_page.search_result_view" value="image" ${searchConfig.search_result_view == "image" ? "checked='true'" : ""}><span><g:message code="image.view"/></span>
                <input type="radio" name="search_page.search_result_view" value="list"  ${searchConfig ? (searchConfig?.search_result_view == "list" ? "checked='true'" : "") : "checked='true'"}><span><g:message code="list.view"/></span>
            </div>
            <div class="form-row">
                <label><g:message code="show.entities"/></label>
                <input type="radio" name="search_page.entities" value="all" ${searchConfig ? (searchConfig.entities=="all" ? "checked='true'" : "") : "checked='true'"}><span><g:message code="all"/></span>
                <input type="radio" name="search_page.entities" value="added_to_widget" ${searchConfig?.entities=="added_to_widget" ? "checked='true'" : ""}><span><g:message code="added.to.widget"/></span>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="search_page.description" value="true" uncheck-value="false" ${searchConfig.description == "true" ? "checked='checked'" : ""}>
                <span><g:message code="show.description"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="search_page.add_to_cart" value="true" uncheck-value="false" ${searchConfig.add_to_cart == "true" ? "checked='checked'" : ""}>
                <span><g:message code="add.to.cart"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="search_page.show_on_hover" value="true" uncheck-value="false" ${searchConfig.show_on_hover == "true" ? "checked" : ""}>
                <span><g:message code="show.on.hover"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="search_page.show_view_switcher" value="true" uncheck-value="false" ${searchConfig.show_view_switcher == "true" ? "checked" : ""}>
                <span><g:message code="show.view.switching.buttons"/></span>
            </div>
            <div class="form-row">
                <input type="checkbox" class="single" name="search_page.price" value="true" uncheck-value="false" ${searchConfig.price == "true" ? "checked='checked'" : ""} toggle-target="price-toggle">
                <span><g:message code="show.price"/></span>
            </div>
            <div class="form-row">
                <label><g:message code="label.for.call.for.price"/></label>
                <input type="text" name="search_page.label_for_call_for_price" value="${searchConfig.label_for_call_for_price}" validation="required">
            </div>
            <div class="price-toggle">
                <div class="form-row">
                    <label><g:message code="label.for.price" /></label>
                    <input type="text" name="search_page.label_for_price" value="${searchConfig.label_for_price}" >
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="search_page.expect_to_pay_price" value="true" uncheck-value="false" ${searchConfig.expect_to_pay_price == "true" ? "checked='checked'" : ""} toggle-target="label-for-expect">
                        <span><g:message code="show.expect.to.pay.price"/></span>
                    </div><div class="form-row label-for-expect">
                        <input type="checkbox" class="single" name="search_page.expect_to_pay_price_with_tax" ${searchConfig.expect_to_pay_price_with_tax == "true" ? "checked" : ""} value="true" uncheck-value="false">
                        <span><g:message code="show.tax.with.expect.to.pay.price"/></span>
                    </div>
                </div>
                <div class="form-row label-for-expect">
                    <label><g:message code="label.for.expect.to.pay" /></label>
                    <input type="text" name="search_page.label_for_expect_to_pay" value="${searchConfig.label_for_expect_to_pay}">
                </div>
            </div>
            <plugin:hookTag hookPoint="configSearchPage"/>

            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>