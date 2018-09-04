<form action="${app.relativeBaseUrl()}giftWrapperAdmin/save" method="post" class="create-edit-form"
      enctype="multipart/form-data">
    <input type="hidden" name="id" value="${giftWrapper.id}">

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="gift.wrapper.info"/></h3>

            <div class="info-content"><g:message code="section.text.gift.wrapper.info"/></div>
        </div>

        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="gift.wrapper.name"/><span class="suggestion">e.g. WebAlive</span></label>
                <input type="text" class="medium unique" name="name" value="${giftWrapper.name.encodeAsBMHTML()}"
                       validation="required maxlength[255]" maxlength="255" unique-action="isGiftWrapperUnique">
            </div>

            <div class="form-row mandatory">
                <label><g:message code="price"/><span class="suggestion">e.g. 100</span>
                </label>
                <input type="text" class="medium" name="price"  validation="required maxlength[100] number"
                       value="${giftWrapper.price.encodeAsBMHTML()}">
            </div>

            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion">e.g. This Company Creates Websites</span>
                </label>
                <textarea class="medium" name="description" maxlength="200"
                          validation="maxlength[199]">${giftWrapper.description}</textarea>
            </div>

            <div class="form-row drop-file thicker-row">
                <label><g:message code="gift.wrapper.logo"/></label>

                <div class="form-image-block">
                    <input type="file"  name="manufacturerLogo" file-type="image" size-limit="2097152"
                           previewer="manufacturer-logo-preview" class="medium"
                        ${giftWrapper.image ? 'remove-support="true"' : 'reset-support="true"'}
                           remove-option-name="remove-image">

                    <div class="preview-image">
                        <img id="manufacturer-logo-preview"
                             src="${appResource.getGiftWrapperImageURL(image: giftWrapper, sizeOrPrefix: "thumb")}"
                             class="preview-image">
                    </div>
                </div>
            </div>

            <div class="form-row">
                <input class="required single" type="checkbox" name="isAllowGiftMessage"
                       value="true" uncheck-value="false" ${giftWrapper.isAllowGiftMessage ? 'checked="checked"' : ''}>
                <span><g:message code="allow.gift.message"/>
            </div>

            <div class="form-row">
                <input class="required single" type="checkbox" name="isVisibleToCustomer"
                       value="true" uncheck-value="false" ${giftWrapper.isVisibleToCustomer ? 'checked="checked"' : ''}>
                <span><g:message code="show.this.wrapping.option.to.customers"/>
            </div>
        </div>
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${giftWrapper.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>