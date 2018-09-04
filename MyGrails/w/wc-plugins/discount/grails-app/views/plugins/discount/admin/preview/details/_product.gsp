<%@ page import="com.webcommander.util.AppUtil; com.webcommander.plugin.discount.NameConstants; com.webcommander.plugin.discount.Constants" %>
<div class="discount-details">
    <span class="discount-details-title"><g:message code="get.discount.product"/></span>
    <g:if test="${discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT}">
        <span class="free-product-detail discount-detail-display"><g:message code="discount.product.free" args="${discountDetails.freeProductMaxQty}"/></span>
    </g:if>
    <g:elseif test="${discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP}">
        <span class="capped-product-detail discount-detail-display"><g:message code="discount.product.price.capped" args="${[AppUtil.baseCurrency.symbol, discountDetails.capPrice?.toConfigPrice()]}"/></span>
    </g:elseif>
    <g:else>
        <g:if test="${discountDetails.amountType == Constants.AMOUNT_DETAILS_TYPE.SINGLE}">
            <span class="single-amount-detail discount-detail-display"><g:message code="discount.amount.value" args="${[discountDetails.getDisplaySingleAmount()]}"/></span>
        </g:if>
        <g:else>
            <span class="tier-detail discount-detail-display"><g:message code="tiered.discount"/>
            <%boolean multiTier = false%>
                <g:each in="${discountDetails.tiers}" var="tier">${(multiTier ? ', (' : '(') + tier.minimumQty + 'qty' + '-' + tier.getDisplayAmount()})<%multiTier = true%></g:each>
            </span>
        </g:else>
    </g:else>
</div>