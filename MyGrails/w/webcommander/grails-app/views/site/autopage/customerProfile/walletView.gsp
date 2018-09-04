<%@ page import="com.webcommander.constants.NamedConstants" %>
<h1 class="title"><g:message code="credit.&.debit.cards"/></h1>
<div class="credit-debit-card">
    <g:each in="${cards}" var="card">
        <div class="credit-card-box">
            <div class="header-line">
                <div class="card-name">${(card.cardName ?: card.cardHolderName).encodeAsBMHTML()}</div>
                <div class="card-type"><g:message code="credit.card"/> - ${g.message(code: NamedConstants.CREDIT_CARD_TYPES[card.cardType])}</div>
                <div class="actions">
                    <div class="floating-popup">
                        <div class="floating-action-dropper"></div>
                        <div class="popup-body context-menu" data-id="${card.id}" data-card-number="${card.cardNumber}">
                            <div class="action-item close-popup" data-action="remove"><g:message code="remove"/></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="card-number">${card.cardNumber}</div>
        </div>
    </g:each>
    <div class="new-card">
        <div class="link-btn link-card-btn"><g:message code="link.a.card"/></div>
    </div>
</div>
