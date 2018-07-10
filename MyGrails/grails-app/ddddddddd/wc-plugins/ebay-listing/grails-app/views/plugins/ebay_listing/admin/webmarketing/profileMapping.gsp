<%@ page import="com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile" %>
<form class="create-edit-form" action="${app.relativeBaseUrl()}ebayListingAdmin/mapProductProfile">
    <input type="hidden" name="productId" value="${params.productId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="ebay.listing.profile"/></h3>
            <div class="info-content"><g:message code="section.text.product.ebay"/> </div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="ebay.listing.profile" /></label>
                <ui:domainSelect domain="${EbayListingProfile}" name="profile" value="${profile.id}"/>
            </div>
            <div class="form-row">
                <label></label>
                <button class="list-on-ebay"><g:message code="list.on.ebay"/></button>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
            </div>
        </div>
    </div>
</form>