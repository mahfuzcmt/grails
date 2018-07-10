<g:set var="productService" bean="productService"/>
<div class="body">
    <table>
        <colgroup>
            <col style="width: 30%">
            <col style="width: 25%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 5%">
        </colgroup>
        <thead>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="sku"/></th>
            <th><g:message code="price"/></th>
            <th><g:message code="available.stock"/></th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${products}" var="product">
            <tr class="product-row" product-id="${product.id}" product-type="${product.productType}" has-variation="${product.metaClass.getMetaMethod("hasVariation") ? product.hasVariation() : false}">
                <td class="pName">${product.name.encodeAsBMHTML()}</td>
                <td class="sku">${product.sku.encodeAsBMHTML()}</td>
                <td class="pPrice">${product.isOnSale ? product.salePrice.toCurrency().toAdminPrice() : product.basePrice.toCurrency().toAdminPrice()}</td>
                <td class="stock" value="${product.supportedMinOrderQuantity}" spin-min="${product.supportedMinOrderQuantity ?: 1}"spin-max="${product.supportedMaxOrderQuantity ?: ''}" spin-step="${product.isMultipleOrderQuantity ? product.multipleOfOrderQuantity : '1'}"
                    combined-isCombined="${product.isCombined}" combined-isFixed="${product.isCombinationPriceFixed}"
                    ${(product.isCombined && !product.isCombinationPriceFixed) ? productService.getIncludedProducts([id: product.id]).each {
                        out << "included-" + it.id + "='" + it.quantity + "'";
                    } : ""}
                >
                    ${product.availableStock ? product.availableStock.encodeAsBMHTML() : g.message(code: 'n.a')}
                </td>
                <td><span class="tool-icon add"></span></td>
            </tr>
        </g:each>
        <g:if test="${!products}">
            <tr class="table-no-entry-row">
                <td colspan="4"><g:message code="no.product.found"/> <span class="link add-product"><g:message code="add.product.now"/></span></td>
            </tr>
        </g:if>
        </tbody>
    </table>

</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${10}"></paginator>
</div>