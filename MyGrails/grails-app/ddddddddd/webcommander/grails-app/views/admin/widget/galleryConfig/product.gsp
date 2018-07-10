<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="gallery-content-config product">
    <div class="form-row">
        <label><g:message code="filter.by"/></label>
        <ui:namedSelect class="sidebar-input" name="filter-by" key="${NamedConstants.PRODUCT_WIDGET_FILTER}" value="${config["filter-by"]}" toggle-target="filter-by"/>
    </div>
    <div class="filter-by-none">
        <g:include view="/admin/item/product/selection.gsp" model="${[products: products, fieldName: "product"]}"/>
    </div>
</div>