<%@ page import="com.webcommander.webcommerce.Product" %>
<form action="${app.relativeBaseUrl()}categoryAdmin/saveLinked" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${category.id}">
    <g:include view="/admin/item/product/selection.gsp" model="${[fieldName: "linked", preventSort: true]}"/>
    <div class="form-row">
        <label>&nbsp;</label>
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
    </div>
</form> 