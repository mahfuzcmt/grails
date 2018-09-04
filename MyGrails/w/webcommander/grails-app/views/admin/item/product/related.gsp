<%@ page import="com.webcommander.webcommerce.Product" %>
<form action="${app.relativeBaseUrl()}productAdmin/saveRelated" method="post" class="edit-popup-form related-product-form">
    <input type="hidden" name="id" value="${product.id}">
    <g:include view="/admin/item/product/selection.gsp" model="${[products: products, fieldName: "related"]}"/>
    <div class="button-line">
        <div class="button-line">
            <button type="submit" class="submit-button related-product-form-submit"><g:message code="update"/></button>
        </div>
    </div>
</form>