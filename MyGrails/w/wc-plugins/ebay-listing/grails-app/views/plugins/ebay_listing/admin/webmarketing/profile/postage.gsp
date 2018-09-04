<g:set var="postage" value="${profile.postage}"/>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/updatePostage" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="postage.info"/></h3>
            <div class="info-content"><g:message code="section.text.postage.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="shipping.cost"/><span class="suggestion"><g:message code="suggestion.ebay.shipping.cost"/></span></label>
                    <input type="text" class="large" name="shippingCost" restrict="decimal" value="${postage?.shippingCost?.encodeAsBMHTML()}" validation="required number maxlength[9]">
                </div><div class="form-row mandatory">
                    <label><g:message code="handling.cost"/><span class="suggestion"><g:message code="suggestion.ebay.handling.cost"/></span></label>
                    <input type="text" class="large" name="handlingCost" restrict="decimal" value="${postage?.handlingCost?.encodeAsBMHTML()}" validation="required number maxlength[9]">
                </div>
            </div>
            <div class="form-row">
                <label></label>
                <input type="checkbox" class="single" name="enableGetItFast" value="true" uncheck-value="false" ${postage?.enableGetItFast ? 'checked' : ''}>
                <span><g:message code="enable.get.it.first"/></span>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>