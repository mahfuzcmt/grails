<form action="${app.relativeBaseUrl()}ebayListingAdmin/updateBasic" method="post">
    <input type="hidden" name="profileId" value="${profile.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="basic.info"/></h3>
            <div class="info-content"><g:message code="section.text.ebay.basic.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.ebay.name"/></span></label>
                    <input type="text" class="large" name="name" value="${profile.name.encodeAsBMHTML()}" maxlength="255" validation="required maxlength[255]">
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="item.condition"/><span class="suggestion"><g:message code="suggestion.ebay.item.condition"/></span></label>
                    <ui:namedSelect class="large" name="itemCondition" value="${profile.itemCondition}"
                                    key="[0: 'use.product.condition', 1000: 'brand.new', 2750: 'like.new', 4000: 'very.good', 5000: 'good', 6000: 'acceptable']"/>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="note"/><span class="suggestion"><g:message code="suggestion.ebay.note"/></span></label>
                <textarea class="large" name="note" maxlength="500" validation="maxlength[500]">${profile.note.encodeAsBMHTML()}</textarea>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="primary.category"/><span class="suggestion"><g:message code="suggestion.ebay.primary.category"/></span></label>
                    <div class="category-selector primary">
                        <input type="hidden" class="selected-category" name="primaryCategory" value="${profile.primaryCategory}">
                        <span class="category-breadcrumb primary">
                            <ebayListing:renderCategoryBreadcrumb profile="${profile}" isPrimaryCategory="${true}"/>
                        </span>
                    </div>
                </div><div class="form-row">
                    <label><g:message code="secondary.category"/><span class="suggestion"><g:message code="suggestion.ebay.secondary.category"/></span></label>
                    <div class="category-selector secondary">
                        <input type="hidden" class="selected-category" name="secondaryCategory" value="${profile.secondaryCategory}">
                        <span class="category-breadcrumb secondary">
                            <ebayListing:renderCategoryBreadcrumb profile="${profile}" isPrimaryCategory="${false}"/>
                        </span>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <label></label>
                <input type="checkbox" class="large single" name="useProductImage" uncheck-value="false" value="true" ${profile.useProductImage ? 'checked' : ''}>
                <span><g:message code="use.product.images"/></span>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>