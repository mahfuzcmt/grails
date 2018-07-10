<form action="${app.relativeBaseUrl()}order/updateOrderAddress" method="post" class="edit-popup-form">
    <input name="id" type="hidden" value="${address?.id}">
        <div class="double-input-row">
            <div class="mandatory form-row">
                <label><g:message code="first.name"/><span class="suggestion"><g:message code="create.customer.fname.suggestion"/></span></label>
                <input type="text" class="large" name="firstName" maxlength="100" validation="required rangelength[2,100]" value="${address?.firstName.encodeAsBMHTML()}">
            </div><div class="form-row">
                <label><g:message code="last.name.surname"/><span class="suggestion"><g:message code="create.customer.lname.suggestion"/></span></label>
                <input type="text" class="large" validation="maxlength[255]" value="${address?.lastName.encodeAsBMHTML()}" name="lastName" maxlength="100">
            </div>
        </div>
        <div class="double-input-row">
            <div class="form-row mandatory">
                <label><g:message code="email"/><span class="suggestion"><g:message code="create.customer.email.suggestion"/></span></label>
                <input type="text" class="large" validation="required email" value="${address?.email}" name="email">
            </div><div class="form-row mandatory">
                <label><g:message code="address.line.1"/><span class="suggestion"><g:message code="create.customer.address.suggestion"/></span></label>
                <input type="text" class="large" name="addressLine1" maxlength="500" validation="required maxlength[500]" value="${address?.addressLine1.encodeAsBMHTML()}" >
            </div>
        </div>
        <div class="form-row trash-row" style="display: none;">
            <label><g:message code="what.to.do"/></label>
            <div>
                <a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/>
                <br/>
                <g:message code="or"/>
                <br/>
                <input type="checkbox" name="deleteTrashItem.userName" class="trash-duplicate-delete single"> &nbsp;<g:message code="delete.and.save"/>
            </div>
        </div>
        <div class="double-input-row">
            <div class="form-row">
                <label><g:message code="address.line.2"/><span class="suggestion"><g:message code="suggestion.setting.store.address2"/></span></label>
                <input type="text" name="addressLine2" maxlength="500" validation="maxlength[500]" value="${address?.addressLine2.encodeAsBMHTML()}" class="large">
            </div><div class="form-row city-selector-row">
                <label><g:message code="suburb/city"/><span class="suggestion"><g:message code="create.customer.city.suggestion"/></span></label>
                <input type="text" class="large" name="city" validation="maxlength[40]" value="${address?.city}" maxlength="40"/>
            </div>
        </div>
        <div class="form-row country-selector-row chosen-wrapper">
            <label><g:message code="country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
            <ui:countryList id="countryId" name="country.id"  class="large" value="${address?.country?.id ?: defaultCountryId.toLong()}"/>
        </div>
        <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states : states, stateId: address?.state?.id]" params="[stateName: 'state.id']"/>
        <div class="double-input-row">
            <div class="form-row post-code">
                <label><g:message code="post.code"/><span class="suggestion"><g:message code="create.customer.postcode.suggestion"/></span></label>
                <input type="text" class="large" name="postCode" validation="maxlength[7]" maxlength="7" value="${address?.postCode}"/>
            </div><div class="form-row">
                <label><g:message code="phone"/><span class="suggestion"><g:message code="create.customer.phone.suggestion"/></span></label>
                <input type="text" class="large" name="phone" maxlength="40" validation="maxlength[40] phone" value="${address?.phone}"/>
            </div>
        </div>

        <div class="double-input-row">
            <div class="form-row">
                <label><g:message code="mobile"/><span class="suggestion"><g:message code="create.customer.mobile.suggestion"/></span></label>
                <input type="text" class="large" name="mobile" maxlength="40" validation="maxlength[40] phone" value="${address?.mobile}"/>
            </div><div class="form-row">
                <label><g:message code="fax"/><span class="suggestion"><g:message code="create.customer.fax.suggestion"/></span></label>
                <input type="text" class="large" name="fax" maxlength="40" validation="maxlength[40]" value="${address?.fax}"/>
            </div>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="save"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
       </div>
</form>