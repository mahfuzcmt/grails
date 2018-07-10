<%@ page import="com.webcommander.plugin.ebay_listing.constants.NamedConstants; com.webcommander.plugin.ebay_listing.constants.DomainConstants;" %>
<g:set var="pricing" value="${profile.pricing}"/>
<form action="${app.relativeBaseUrl()}ebayListingAdmin/updatePricing" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="pricing.info"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.pricing.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="pricing.type"/><span class="suggestion"><g:message code="suggestion.ebay.pricing.type"/></span></label>
                <ui:namedSelect class="large" name="type" value="${pricing?.type}" key="${NamedConstants.PRICING_TYPE}" toggle-target="pricing-type"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="buy.it.now.price"/><span class="suggestion"><g:message code="suggestion.ebay.buy.it.now.price"/></span></label>
                <ui:namedSelect class="large" name="buyItNowPrice.type" value="${pricing?.buyNowPrice?.type}" key="${NamedConstants.PRICING_PROFILE_TYPE}"
                                toggle-target="buy-it-now-price"/>
            </div>
            <div class="form-row buy-it-now-price-${DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_ADDITIONAL_PRICE}">
                <label><g:message code="additional.amount"/></label>
                <input type="text" class="small" name="buyItNowPrice.additionalAmount" value="${pricing?.buyNowPrice?.additionalAmount?.encodeAsBMHTML()}"
                       validation="skip@if{self::hidden} required maxlength[9]"> &nbsp;
            <ui:namedSelect class="tiny" name="buyItNowPrice.additionalType" value="${pricing?.buyNowPrice?.additionalType}" key="${['$': 'fixed', '%': 'percent']}"/>
            </div>
            <div class="form-row buy-it-now-price-${DomainConstants.PRICING_PROFILE_TYPE.NEW_PRICE}">
                <label><g:message code="new.amount"/></label>
                <input type="text" class="large" name="buyItNowPrice.newAmount" restrict="decimal" value="${pricing?.buyNowPrice?.newAmount?.encodeAsBMHTML()}"
                       validation="skip@if{self::hidden} required maxlength[9]">
            </div>

            <div class="form-row pricing-type-${DomainConstants.PRICING_TYPE.AUCTION} chosen-wrapper">
                <label><g:message code="starting.price"/></label>
                <ui:namedSelect class="large" name="startingPrice.type" value="${pricing?.startingPrice?.type}" key="${NamedConstants.PRICING_PROFILE_TYPE}"
                                toggle-target="starting-price"/>
            </div>
            <div class="form-row pricing-type-${DomainConstants.PRICING_TYPE.AUCTION} starting-price-${DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_ADDITIONAL_PRICE} chosen-wrapper">
                <label><g:message code="additional.amount"/></label>
                <input type="text" class="small" name="startingPrice.additionalAmount" value="${pricing?.startingPrice?.additionalAmount?.encodeAsBMHTML()}"
                       validation="skip@if{self::hidden} required maxlength[9]"> &nbsp;
                <ui:namedSelect class="tiny" name="startingPrice.additionalType" value="${pricing?.startingPrice?.additionalType}" key="${['$': 'fixed', '%': 'percent']}"/>
            </div>
            <div class="form-row pricing-type-${DomainConstants.PRICING_TYPE.AUCTION} starting-price-${DomainConstants.PRICING_PROFILE_TYPE.NEW_PRICE}">
                <label><g:message code="new.amount"/></label>
                <input type="text" class="large" name="startingPrice.newAmount" restrict="decimal" value="${pricing?.startingPrice?.newAmount?.encodeAsBMHTML()}"
                       validation="skip@if{self::hidden} required maxlength[9]">
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="sell.to.quantity"/></label>
                <ui:namedSelect name="sellToQuantityType" key="${NamedConstants.SELL_TO_QUANTITY_TYPE}" value="${pricing.sellToQuantityType}" toggle-target="sell-to-quantity"/>
            </div>
            <div class="form-row sell-to-quantity-${DomainConstants.SELL_TO_QUANTITY_TYPE.MORE_THEN_ONE_QUANTITY}">
                <label><g:message code="quantity"/></label>
                <input type="text" name="quantity" value="${pricing.quantity}" validation="skip@if{self::hidden} required digits" resrict="number">
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="duration"/><span class="suggestion"><g:message code="suggestion.ebay.duration"/></span></label>
                    <g:select class="large" name="duration" from="${[1,3,5,7,10]}" optionValue="${{it + " " + g.message(code: "days")}}" value="${pricing.duration}"/>
                </div><div class="form-row">
                    <label><g:message code="list.as.private"/></label>
                    <input type="checkbox" class="single" name="isPrivateListing" uncheck-value="false" value="true" ${pricing.isPrivateListing ? 'checked' : ''}>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>