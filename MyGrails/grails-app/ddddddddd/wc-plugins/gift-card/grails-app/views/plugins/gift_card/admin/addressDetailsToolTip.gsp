<div class="gift-card-address-details">
    <div class="item">
        <span class="title"><g:message code="recipient.name"/>:</span>
        <span class="value">${(giftCard.firstName + " " + (giftCard.lastName ?: "")).encodeAsBMHTML()}</span>
    </div>
    <div class="item">
        <span class="title"><g:message code="recipient.email"/>:</span>
        <span class="value">${giftCard.email}</span>
    </div>
    <g:if test="${giftCard.senderName}">
        <div class="item">
            <span class="title"><g:message code="sender.name"/>:</span>
            <span class="value">${giftCard.senderName}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.phone}">
        <div class="item">
            <span class="title"><g:message code="phone"/>:</span>
            <span class="value">${giftCard.phone}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.mobile}">
        <div class="item">
            <span class="title"><g:message code="mobile"/>:</span>
            <span class="value">${giftCard.mobile}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.address}">
        <div class="item">
            <span class="title"><g:message code="recipient.address.line"/>:</span>
            <span class="value">${giftCard.address}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.city}">
        <div class="item">
            <span class="title"><g:message code="city"/>:</span>
            <span class="value">${giftCard.city}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.postCode}">
        <div class="item">
            <span class="title"><g:message code="post.code"/>:</span>
            <span class="value">${giftCard.postCode}</span>
        </div>
    </g:if>

    <g:if test="${giftCard.country}">
        <div class="item">
            <span class="title"><g:message code="country"/>:</span>
            <span class="value">${giftCard.country.name}</span>
        </div>
    </g:if>

</div>