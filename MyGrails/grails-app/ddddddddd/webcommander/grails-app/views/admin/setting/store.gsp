<%@ page import="com.webcommander.admin.Country; com.webcommander.admin.State" %>
<form method="post" class="create-edit-form" enctype="multipart/form-data" action="${app.relativeBaseUrl()}setting/saveStoreDetails">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="store.detail"/></h3>
            <div class="info-content"><g:message code="section.text.store.detials"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="company.name"/><span class="suggestion"><g:message code="suggestion.setting.store.company.name"/></span></label>
                    <input type="text" class="medium" name="name" value="${storeDetail? storeDetail.name.encodeAsBMHTML() : ''}" validation="required rangelength[5, 100]">
                </div><div class="form-row mandatory">
                    <label><g:message code="email"/><span class="suggestion"><g:message code="suggestion.setting.store.email"/></span></label>
                    <input type="text" class="medium" name="address.email" value="${storeDetail? storeDetail.address.email.encodeAsBMHTML() : ''}" validation="required email">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="address.line.1"/><span class="suggestion"><g:message code="suggestion.setting.store.address1"/></span></label>
                <input type="text" class="medium" name="address.addressLine1" value="${storeDetail? storeDetail.address.addressLine1.encodeAsBMHTML() : ''}" maxlength="500" validation="required maxlength[500]">
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="address.line.2"/><span class="suggestion"><g:message code="suggestion.setting.store.address2"/></span></label>
                    <input type="text" class="medium" name="address.addressLine2" value="${storeDetail? storeDetail.address.addressLine2.encodeAsBMHTML() : ''}" maxlength="500" validation="maxlength[500]">
                </div><div class="form-row">
                    <label><g:message code="suburb/city"/><span class="suggestion"><g:message code="suggestion.setting.store.city"/></span></label>
                    <input type="text" class="medium" name="address.city" value="${storeDetail? storeDetail.address.city.encodeAsBMHTML() : ''}">
                </div>
            </div>
            <div class="form-row country-selector-row chosen-wrapper">
                <label><g:message code="country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
                <ui:countryList id="countryId" name="address.country" validation="required" class="medium" value="${storeDetail? storeDetail.address.country.id : ''}"/>
            </div>
            <g:if test="${storeDetail}">
                <g:include view="/admin/customer/stateFormFieldView.gsp" model="${[states : states, stateId: storeDetail.address.state?.id]}" params="[inputClass: 'medium', stateName: 'address.state']"/>
            </g:if>
            <div class="form-row">
                <label><g:message code="post.code"/><span class="suggestion"><g:message code="suggestion.setting.store.post.code"/></span></label>
                <input type="text" class="medium" name="address.postCode" validation="digits maxlength[7]" restrict="numeric" value="${storeDetail? storeDetail.address.postCode : ''}">
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="phone"/><span class="suggestion"><g:message code="suggestion.setting.store.phone"/></span></label>
                    <input type="text" class="medium" name="address.phone" value="${storeDetail? storeDetail.address.phone : ''}">
                </div><div class="form-row">
                    <label><g:message code="mobile"/><span class="suggestion"><g:message code="suggestion.setting.store.mobile"/></span></label>
                    <input type="text" class="medium" name="address.mobile" value="${storeDetail? storeDetail.address.mobile : ''}">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="fax"/><span class="suggestion"><g:message code="suggestion.setting.store.fax"/></span></label>
                    <input type="text" class="medium" name="address.fax" value="${storeDetail? storeDetail.address.fax : ''}">
                </div><div class="form-row">
                    <label><g:message code="abn"/><span class="suggestion"><g:message code="suggestion.setting.store.abn"/></span></label>
                    <input type="text" class="medium" name="abn" value="${storeDetail? storeDetail.abn.encodeAsBMHTML() : ''}">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="store.logo"/><span class="suggestion"><g:message code="suggestion.setting.store.logo"/></span> </label>
                <div class="form-image-block">
                    <input type="file" name="image" file-type="image" size-limit="51200" previewer="store-image-preview" remove-option-name="remove-image"
                       ${storeDetail?.image ? "" : "style='display: none'"} ${storeDetail?.image ? 'remove-support="true"' : 'reset-support="true"'}>
                    <div class="preview-image">
                        <img id="store-image-preview" src='${ appResource.getStoreLogoURL(storeDetails: storeDetail, isDefault: true)}'>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="additional.information"/><span class="suggestion"><g:message code="suggestion.setting.store.detail"/></span></label>
                <textarea class="medium" name="additionalInfo">${storeDetail? storeDetail.additionalInfo.encodeAsBMHTML() : ''}</textarea>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>