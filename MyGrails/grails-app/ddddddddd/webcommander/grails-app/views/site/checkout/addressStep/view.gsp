<%@ page import="com.webcommander.constants.DomainConstants;" %>
<input type="hidden" name="mode" value="view">
<span class="share-toolbar toolbar hidden">
    <span class="toolbar-btn edit edit-section"><g:message code="edit"/></span>
</span>
<g:if test="${shouldHaveShipping && cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING}">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content">
                <div class="address-view">
                    <div class="header">
                        <span class="title"><g:message code="billing.address"/></span>
                    </div>
                    <div class="address">
                        <div class="name">${effectiveBilling.fullName.encodeAsBMHTML()}</div>
                        <div class="address-line-1">${effectiveBilling.addressLine.encodeAsBMHTML()}</div>
                        <div class="address-line-2">${effectiveBilling.stateName}, ${effectiveBilling.city.encodeAsBMHTML()} ${effectiveBilling.postCode}, ${effectiveBilling.countryName}</div>
                    </div>
                </div>
            </div>
        </div><div class="columns last-column">
        <div class="column-content">
            <div class="address-view shipping">
                <div class="header">
                    <span class="title"><g:message code="shipping.address"/></span>
                </div>
                <div class="address">
                    <div class="name">${effectiveShipping.fullName.encodeAsBMHTML()}</div>
                    <div class="address-line-1">${effectiveShipping.addressLine.encodeAsBMHTML()}</div>
                    <div class="address-line-2">${effectiveShipping.stateName}, ${effectiveShipping.city.encodeAsBMHTML()} ${effectiveShipping.postCode}, ${effectiveShipping.countryName}</div>
                </div>
            </div>
        </div>
    </div>
    </div>
</g:if>
<g:else>
    <div class="address-view">
        <div class="header">
            <span class="title"><g:message code="billing.address"/></span>
        </div>
        <div class="address">
            <div class="name">${effectiveBilling.fullName.encodeAsBMHTML()}</div>
            <div class="address-line-1">${effectiveBilling.addressLine.encodeAsBMHTML()}</div>
            <div class="address-line-2">${effectiveBilling.stateName}, ${effectiveBilling.city.encodeAsBMHTML()} ${effectiveBilling.postCode}, ${effectiveBilling.countryName}</div>
        </div>
    </div>
</g:else>


