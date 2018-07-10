<%@ page import="com.webcommander.plugin.ebay_listing.constants.NamedConstants;" %>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/updatePaymentMethod" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="payment.method.info"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.payment.method.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="safe.payment.method"/></label>
                    <input type="checkbox" class="single" name="safePaymentMethod" value="${profile.safePaymentMethod.name}"
                           checked="checked" disabled="disabled"> <span><g:message code="paypal"/></span>
                </div><div class="form-row mandatory">
                    <label><g:message code="paypal.email"/><span class="suggestion"><g:message code="suggestion.ebay.email"/></span></label>
                    <input type="text" class="large" name="payPalEmail" value="${profile.safePaymentMethod.metaValues ? profile.safePaymentMethod.metaValues.first().value : ""}"
                           validation="required email">
                </div>
            </div>
            <g:each in="${NamedConstants.PAYMENT_METHODS}" var="method" status="i">
                <div class="form-row">
                    <label>
                        <g:if test="${i == 0}"><g:message code="other.payment.methods"/><span class="suggestion"><g:message code="suggestion.ebay.other.payment.method"/></span></g:if>
                    </label>
                    <input type="checkbox" class="multiple" name="paymentMethod" value="${method.key}" ${profile.availablePaymentMethods?.name?.contains(method.key) ? 'checked="checked"' : ''}>
                    <g:message code="${method.value}"/>
                </div>
            </g:each>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>