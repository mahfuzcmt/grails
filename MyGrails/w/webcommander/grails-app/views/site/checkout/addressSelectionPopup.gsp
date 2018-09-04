<div class="addresses">
    <g:each in="${addresses}" var="_address">
        <div class="address select-item ${_address.id == address.id ? 'selected' : ''}" entity-id="${_address.id}">
            <div class="name">${(_address.firstName + ( _address.lastName ? " " + _address.lastName : "")).encodeAsBMHTML()}</div>
            <div class="address">${(_address.addressLine1 +", "+ ( _address.addressLine2 ? _address.addressLine2 + ", " : "")).encodeAsBMHTML()}${_address.state?.name.encodeAsBMHTML()}, ${_address.city.encodeAsBMHTML()} ${_address.postCode}</div>
        </div>
    </g:each>
</div>
<div class="create-address" data-type="${addressType}">
    <span><g:message code="${addressType == "billing" ? "bill" : "ship"}.different.address"/></span>
</div>
