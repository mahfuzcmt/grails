<%@ page import="com.webcommander.util.AppUtil; com.webcommander.plugin.discount.NameConstants; com.webcommander.plugin.discount.Constants" %>
<div class="discount-details">
    <span class="discount-details-title"><g:message code="get.discount.amount"/></span>
    <g:if test="${discountDetails.type == Constants.AMOUNT_DETAILS_TYPE.SINGLE}">
        <span class="single-amount-detail discount-detail-display"><g:message code="discount.amount.value" args="${[discountDetails.getDisplaySingleAmount()]}"/></span>
    </g:if>
    <g:else>
        <span class="tier-detail discount-detail-display"><g:message code="tiered.discount"/>
        <% boolean multiTier = false %>
        <g:each in="${discountDetails.tiers}" var="tier">${(multiTier ? ', (' : '(') + AppUtil.baseCurrency.symbol + tier.minimumAmount?.toConfigPrice() + '-' + tier.getDisplayAmount()})<% multiTier = true %></g:each>
        </span>
    </g:else>
</div>