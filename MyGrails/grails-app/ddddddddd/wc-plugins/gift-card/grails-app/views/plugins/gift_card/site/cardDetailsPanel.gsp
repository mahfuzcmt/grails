<div class="card-details-panel">
    <g:if test="${giftCard}">
        <div class="balance-section">
            <span class="title"><g:message code="gift.card.balance"/></span>
            <span class="balance">${currencySymbol} ${giftCard.getAvailableBalance()}</span>
        </div>
    </g:if>
    <g:else>
        <span class="error"><g:message code="invalid.gift.card"/></span>
    </g:else>
</div>