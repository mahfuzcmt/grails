<%@ page import="com.webcommander.util.StringUtil" %>
<div class="address-edit-from-wrap">
    <div class="header">
        <span class="title"><g:message code="${addressType}.address"/></span>
    </div>
    <g:form class="" controller="shop" action="loadAddressStep" method="POST" >
        <input type="hidden" name="operation" value="saveOrSelectAddress">
        <input type="hidden" name="addressType" value="${addressType}"/>
        <input type="hidden" name="id" value="${address.id}"/>
        <g:set var="colSplitter" value="${Math.ceil(fields.size() / 2)}"/>
        <plugin:hookTag hookPoint="siteAddressEditor" attrs="${[:]}">
            <div class="multi-column two-column">
            <div class="columns first-column">
            <div class="column-content">
                <g:set var="emailId" value="${StringUtil.uuid}"/>
                <g:each in="${fields}" var="field" status="idx">
                    <g:set var="fieldRequired" value="${fieldsConfigs[field + '_required']}"/>
                    <g:set var="required" value="${fieldRequired.toBoolean(true) ? 'required' : ''}"/>
                    <g:set var="mandatory" value="${required == 'required' ? 'mandatory' : ''}"/>
                    <g:set var="label" value="${fieldsConfigs[field + '_label']}"/>
                    <g:if test="${idx == colSplitter}">
                        </div>
                    </div><div class="columns last-column">
                        <div class="column-content">
                    </g:if>
                    <g:if test="${field == 'first_name'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:first.name"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_first_Name" : "et_shipping_edit_first_Name"}" et-category="textbox" name="firstName" validation="${required} rangelength[2,100]" value="${address.firstName}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'last_name'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:last.name"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_last_Name" : "et_shipping_edit_last_Name"}" et-category="textbox" name="lastName" validation="${required} rangelength[2,100]" value="${address.lastName}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'email'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:email"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_email" : "et_shipping_edit_email"}" et-category="textbox" id="${emailId}" name="email" validation="${required} single_email" value="${address.email}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'confirm_email'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:confirm.email"}"/>:</label>
                            <input type="text" class="large" name="confirmEmail" value="${address.email}" validation="${required} single_email compare[${emailId}, string, eq]">
                        </div>
                    </g:if>
                    <g:if test="${field == 'address_line_1'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:address.line.1"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_address_line" : "et_shipping_edit_address_line"}" et-category="textbox" validation="${required} maxlength[1000]" maxlength="500" name="addressLine1" value="${address.addressLine1}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'address_line_2'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:address.line.2"}"/>:</label>
                            <input type="text" name="addressLine2" validation="${required} maxlength[1000]" maxlength="500" value="${address.addressLine2}" class="large ${addressType == "billing" ? "et_billing_edit_address_line" : "et_shipping_edit_address_line"}" et-category="textbox">
                        </div>
                    </g:if>
                    <g:if test="${field == 'country'}">
                        <div class="form-row country-selector-row">
                            <label><site:message code="${label ?: "s:country"}"/>:</label>
                            <ui:countryList name="country.id" value="${address.countryId}" validation="required" class="${addressType == "billing" ? "et_billing_edit_contry" : "et_shipping_edit_contry"}" et-category="dropdown"/>
                        </div>
                        <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states: states, stateId: address.stateId]" params="[section: addressType, inputClass: (addressType == 'billing') ? 'large et_billing_edit_state' : 'large et_shipping_edit_state']"/>
                    </g:if>
                    <g:if test="${field == 'post_code'}">
                        <div class="form-row post-code-row ${mandatory}">
                            <label><site:message code="${label ?: "s:post.code"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_post_code" : "et_shipping_edit_post_code"}" et-category="textbox" name="postCode" validation="${required} maxlength[5]" value="${address.postCode}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'city'}">
                        <div class="form-row city-selector-row  ${mandatory}">
                            <label><site:message code="${label ?: "s:suburb/city"}"/>:</label>
                            <g:include controller="app" action="loadCities" params="[validation: required, state: address.stateId, postCode: address.postCode, city: address.city]"/>
                        </div>
                    </g:if>
                    <g:if test="${field == 'phone'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:phone"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_phone" : "et_shipping_edit_phone"}" et-category="textbox" name="phone" validation="${required} maxlength[40] phone" value="${address.phone}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'mobile'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:mobile"}"/>:</label>
                            <input type="text" class="large ${addressType == "billing" ? "et_billing_edit_mobile" : "et_shipping_edit_mobile"}" et-category="textbox" name="mobile" validation="${required} maxlength[40] phone" value="${address.mobile}">
                        </div>
                    </g:if>
                    <g:if test="${field == 'fax'}">
                        <div class="form-row ${mandatory}">
                            <label><site:message code="${label ?: "s:fax"}"/>:</label>
                            <input type="text" class="large" name="fax" validation="${required} maxlength[40]" value="${address.fax}">
                        </div>
                    </g:if>
                </g:each>
            </div>
            </div>
            </div>
            <g:if test="${params.isNew}">
                <div class="form-row save-in-profile">
                    <input type="checkbox" name="saveInProfile">
                    <span class="save-in-profile-message"><g:message code="save.also.profile.address"/></span>
                </div>
            </g:if>
            <g:if test="${!params.isNew && session."effective_${addressType}_address"}">
                <input type="button" value="${g.message(code: 'cancel')}" class="button cancel-button ${addressType == "billing" ? "et_billing_cancel" : "et_shipping_cancel"}" et-category="button">
                &nbsp; &nbsp;
            </g:if>
            <input type="submit" value="${g.message(code: 'update')}" class="button submit-button ${addressType == "billing" ? "et_billing_continue" : "et_shipping_continue"}" et-category="button">
        </plugin:hookTag>
    </g:form>
</div>
