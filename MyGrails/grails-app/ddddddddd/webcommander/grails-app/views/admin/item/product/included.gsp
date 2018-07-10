<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.Product" %>
<form action="${app.relativeBaseUrl()}productAdmin/saveIncluded" method="post" class="edit-popup-form included-product-form">
    <input type="hidden" name="id" value="${product.id}">
    <input type="hidden" name="isFixed" value="${product.isCombinationPriceFixed}">
    <input type="hidden" name="isDownloadable" value="${product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE ? "true" : "false"}">
    <g:include view="/admin/item/product/includedSelection.gsp" model="${[product: product, products: products, fieldName: "included", isFixed: product.isCombinationPriceFixed]}"/>
    <div class="button-line">
        <div class="button-line">
            <button type="submit" class="submit-button included-product-form-submit"><g:message code="update"/></button>
        </div>
    </div>
</form>