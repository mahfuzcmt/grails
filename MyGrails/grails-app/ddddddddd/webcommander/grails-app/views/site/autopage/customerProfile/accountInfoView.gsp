<div class="name">${address.fullName.encodeAsBMHTML()}</div>
<div class="account-short-details">
    <g:if test="${address.addressLine1}">
        <p class="address-line-1">${address.addressLine1}</p>
    </g:if>
    <g:if test="${address.addressLine2}">
        <p class="address-line-2">${address.addressLine2}</p>
    </g:if>
    <p class="city-state">${((address.city ? address.city + " " : "") + (address.postCode ? address.postCode + ", " : "") + (address.state ? address.state.name + ", " : "") + (address.country.name)).encodeAsBMHTML()}</p>
    <g:if test="${address.email}">
        <p class="email">${address.email}</p>
    </g:if>
</div>
<div class="button-line">
    <span class="link-btn account-details-edit-link"><g:message code="edit"/></span>
    <span class="link-btn password-edit-link"><g:message code="change.password"/></span>
</div>