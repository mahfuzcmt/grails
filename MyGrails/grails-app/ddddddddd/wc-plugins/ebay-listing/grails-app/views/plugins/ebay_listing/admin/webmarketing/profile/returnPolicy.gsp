<g:set var="returnPolicy" value="${profile.returnPolicy}"/>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/updateReturnPolicy" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="return.policy"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.return.policy.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="accept.return"/><span class="suggestion"><g:message code="suggestion.ebay.accept.return"/></span></label>
                <ui:namedSelect class="large" name="acceptReturn" value="${returnPolicy?.acceptReturn?.toString()}" key="[true: 'yes', false: 'no']" toggle-target="accept-return"/>
            </div>
            <div class="accept-return-true">
                <div class="double-input-row">
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="return.within"/></label>
                        <ui:namedSelect class="large" name="returnWithin" value="${returnPolicy?.returnWithin}" key="${[3, 7, 14, 30, 60].collect{ 'Days_' + it  }}"/>
                    </div><div class="form-row chosen-wrapper">
                        <label><g:message code="refund.type"/></label>
                        <ui:namedSelect class="large" name="refundType" value="${returnPolicy?.refundType}" key="${[Exchange: 'exchange', MoneyBack: 'money.back', MerchandiseCredit: 'merchandise.credit']}"/>
                    </div>
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="return.shipping.paid.by"/></label>
                    <ui:namedSelect class="large" name="returnShippingPaidBy" value="${returnPolicy?.returnShippingPaidBy}" key="['Buyer', 'Seller']"/>
                </div>
                <div class="form-row">
                    <label><g:message code="additional.return.policy.note"/></label>
                    <textarea class="large" name="additionalReturnPolicyNote">${returnPolicy?.additionalReturnPolicyNote?.encodeAsBMHTML()}</textarea>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>