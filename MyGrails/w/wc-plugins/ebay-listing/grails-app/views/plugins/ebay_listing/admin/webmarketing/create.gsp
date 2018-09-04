<form action="${app.relativeBaseUrl()}ebayListingAdmin/createProfile" method="post" class="edit-popup-form create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="ebay.listing.profile"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.profile.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.ebay.name"/></span></label>
                <input type="text" class="large" name="name" validation="required maxlength[255]" maxlength="255">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>