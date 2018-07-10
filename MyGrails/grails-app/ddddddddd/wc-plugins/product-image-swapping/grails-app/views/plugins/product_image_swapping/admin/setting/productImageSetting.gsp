<%@ page import="com.webcommander.constants.DomainConstants" %>
<g:set var="imageSwapping" value="${DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE_SWAPPING}"></g:set>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" onsubmit="return false" method="POST" class="create-edit-form">
    <input type="hidden" name="type" value="${imageSwapping}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.image.swapping"/></h3>
            <div class="info-content"><g:message code="section.text.product.image.swaping.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="product.image.swapping"/></label>&nbsp;
                <input type="checkbox" class="single" name="${imageSwapping}.enable_swapping" value="true" uncheck-value="false" ${config.enable_swapping == "true" ? "checked" : ""}>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>