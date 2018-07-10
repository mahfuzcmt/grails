<%@ page import="com.webcommander.converter.json.JSON; com.webcommander.manager.HookManager; com.webcommander.models.ProductData" %>
<input type="hidden" class="not-available" value="${notAvailableCount ?: ''}">
<g:each in="${products as List<ProductData>}" var="product" status="i">
    <tr class="tr">
        <input class="pId" type="hidden" value="${product.id}" name="products.product_${i}.id">
        <g:if test="${product.isCombined}">
            <input class="included" type="hidden" name="products.product_${i}.included" value='${!product.isCombinationPriceFixed ? product.getIncludedProducts().collect {[(it.id): it.quantity]} as JSON : '' }'>
        </g:if>
        <td class="product-name">${names[i]?.encodeAsBMHTML()}</td>
        <td>${product.sku}</td>
        <td class="editable" restrict="decimal">
            <g:set var="price" value="${product.isOnSale ? product.salePrice : product.basePrice}"/>
            <input class="pPrice" type="hidden" name="products.product_${i}.price" value="${price.toCurrency().toPrice()}">
            <span class="value">${price.toCurrency().toPrice()}</span>
        </td>
        <td class="stock" spin-min="${product.supportedMinOrderQuantity ?: 1}" spin-max="${product.supportedMaxOrderQuantity ?: ''}" spin-step="${product.isMultipleOrderQuantity ? product.multipleOfOrderQuantity : '1'}">
            ${product.isInventoryEnabled ? product.availableStock : g.message(code: "n.a")}
        </td>
        <td class="quantity" restrict="numeric">
            <input class="quantity tiny" type="text" name="products.product_${i}.quantity" value="${quantities[i]}"/>
        </td>
        <td><span class="tool-icon remove"></span></td>
        <g:each in="${HookManager.hook("optionFromVariation", [], product.attrs.selectedVariation)}" var="option">
            <input type="hidden" name="products.product_${i}.config.options" value="${option}">
        </g:each>
    </tr>
</g:each>