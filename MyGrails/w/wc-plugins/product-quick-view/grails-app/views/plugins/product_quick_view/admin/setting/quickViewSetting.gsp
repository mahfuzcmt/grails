<%@ page import="com.webcommander.constants.DomainConstants" %>
<g:set var="productQuickView" value="${DomainConstants.SITE_CONFIG_TYPES.PRODUCT_QUICK_VIEW}"></g:set>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" class="create-edit-form" onsubmit="return false" method="POST">
    <input type="hidden" name="type" value="${productQuickView}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.quick.view"/></h3>
            <div class="info-content"><g:message code="section.text.quick.view.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="product.quick.view"/></label>
                <input type="checkbox" class="single" name="${productQuickView}.enable_quick_view" value="true" uncheck-value="false" ${config.enable_quick_view == "true" ? "checked" : ""}>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>