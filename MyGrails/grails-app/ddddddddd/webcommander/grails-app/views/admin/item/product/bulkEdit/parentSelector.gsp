<%@ page import="com.webcommander.webcommerce.Category" %>
<g:each in="${products}" var="product">
    <ui:hierarchicalSelect name="${product.id}.categories" class="form-full-width parent-${product.id} parents-selector special-select-chosen" domain="${Category}" custom-attrs="${[multiple: 'true', 'chosen-highlighted': product.parent?.id ?: 0, 'data-placeholder': g.message(code: "select.categories"), 'chosen-hiddenfieldname': "${product.id}.parent"]}" values="${product.parents.id ?: 0}"/>
</g:each>