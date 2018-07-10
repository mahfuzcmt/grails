<%@ page import="com.webcommander.util.AppUtil" %>
<form class="included-products-container" ${product.isCombinationQuantityFlexible ? 'combination-flexible="true"' : ""}>
    <div class="title"><g:message code="included.products"/></div>
    <g:if test="${product.isCombinationQuantityFlexible}">
        <g:each in="${includedProducts}" var="included">
            <div class="info-row">
                <label><a href="${app.baseUrl() + "product/" + included.includedProduct.url}">${included.label.encodeAsBMHTML()}</a></label>
                <span class="price individual-price">
                    <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span>
                    <span class="price-amount">
                        ${included.price ? included.price.toCurrency().toPrice() : (included.includedProduct.isOnSale ? included.includedProduct.salePrice.toCurrency().toPrice() : included.includedProduct.basePrice.toCurrency().toPrice())}
                    </span>
                </span>
                &nbsp; &nbsp; &nbsp;
                <input type="text" class="included-quantity-selector" value="${included.quantity}" spin-min="1" pId="${included.id}">
            </div>
        </g:each>
    </g:if>
    <g:else>
        <g:each in="${includedProducts}" var="included">
            <div class="info-row">
                <label><a href="${app.baseUrl() + "product/" + included.includedProduct.url}">${included.label.encodeAsBMHTML()}</a></label>
                <span class="quantity"> (${included.quantity})</span>
            </div>
        </g:each>
    </g:else>
</form>