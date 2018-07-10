<%@ page import="com.webcommander.util.AppUtil" %>
<g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
<div class="card-details">
    <div class="balance-section">
        <span class="title"><g:message code="gift.card.balance"/></span>
        <span class="balance">${currencySymbol}${giftCard.getAvailableBalance()}</span>
    </div>
    <div class="expiry-section">
        <span class="title"><g:message code="card.expiry"/>:</span>
        <span class="balance">${giftCard.availableTo}</span>
    </div>
    <div class="cartitem-btn-wrapper">
        <g:if test="${eCommerceConfig.enable_continue_shopping == "true"}">
            <a href="${continueShoppingUrl}?#overview" class="continue-shopping-btn cartitem-btn button et_cartp_continue_shopping" et-category="button"><site:message code="${eCommerceConfig.continue_shopping_label}"/></a>
        </g:if>
    </div>
</div>