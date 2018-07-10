<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="defaultCountryId" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country')}"></g:set>
<form class="create-edit-form customer-create-form" action="${app.relativeBaseUrl()}customerAdmin/save" method="post">
    <input name="id" type="hidden" value="${customer?.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="customer.information" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></h3>
            <div class="info-content"><g:message code="section.text.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="mandatory form-row">
                    <label><g:message code="first.name"/><span class="suggestion"><g:message code="create.customer.fname.suggestion"/></span></label>
                    <input type="text" class="large" name="firstName" maxlength="100" validation="required rangelength[2,100]" value="${customer?.firstName.encodeAsBMHTML()}">
                </div><div class="form-row">
                    <label><g:message code="last.name.surname"/><span class="suggestion"><g:message code="create.customer.lname.suggestion"/></span></label>
                    <input type="text" class="large" validation="maxlength[255]" value="${customer?.lastName.encodeAsBMHTML()}" name="lastName" maxlength="100">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="customer.type" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
                    <input type="radio" name="isCompany" class="radio" value="true" ${customer.isCompany ? "checked" : ""} toggle-target="company-row">
                    <span class="value"><g:message code="company"/></span>
                    <input type="radio" name="isCompany" class="radio" value="false" ${customer.isCompany  ? "" : "checked"} toggle-target="customer-type">
                    <span class="value"><g:message code="individual"/></span>
                </div><div class="form-row">
                    <div class="customer-type">
                        <label><g:message code="gender"/></label>
                        <input type="radio" name="sex" class="radio" value="Male" ${customer.sex ? (customer.sex == "Male" ? "checked" : "") : "checked"}>
                        <span class="value"><g:message code="male"/></span>
                        <input type="radio" name="sex" class="radio" value="Female" ${customer?.sex == "Female" ? "checked" : ""}>
                        <span class="value"><g:message code="female"/></span>
                    </div>
                </div>
            </div>
            <div class="triple-input-row">
                <div class="form-row company-row">
                    <label><g:message code="company.name"/></label>
                    <input type="text" class="large" name="companyName" validation="skip@if{self::hidden} rangelength[2,100]" value="${customer?.companyName}">
                </div><div class="form-row company-row">
                    <label><g:message code="abn"/><span class="suggestion"><g:message code="create.customer.abn.suggestion"/></span></label>
                    <input type="text" class="large abn-validation" value="${customer?.abn}" name="abn" validation="skip@if{self::hidden} match[^\d{2}\s\d{3}\s\d{3}\s\d{3}$, <site:message code="s:abn.format"/>] maxlength[14]" restrict="numeric" maxlength="14">
                </div><div class="form-row company-row">
                    <label><g:message code="abn.branch"/><span class="suggestion"><g:message code="create.customer.abnbranch.suggestion"/></span></label>
                    <input type="text" class="large" value="${customer?.abnBranch}" name="abnBranch"  validation="skip@if{self::hidden} digits maxlength[3]" restrict="numeric" maxlength="3">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="email"/><span class="suggestion"><g:message code="create.customer.email.suggestion"/></span></label>
                <input type="text" class="large unique" validation="required email" value="${customer?.userName}" name="email"  unique-field-name="email" unique-field="userName">
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
                <div class="form-row mandatory">
                    <label><g:message code="address.line.1"/><span class="suggestion"><g:message code="create.customer.address.suggestion"/></span></label>
                    <input type="text" class="large" name="addressLine1" maxlength="500" validation="required maxlength[500]" value="${address?.addressLine1.encodeAsBMHTML()}" >
                </div><div class="form-row">
                    <label><g:message code="address.line.2"/><span class="suggestion"><g:message code="suggestion.setting.store.address2"/></span></label>
                    <input type="text" name="addressLine2" maxlength="500" validation="maxlength[500]" value="${address?.addressLine2.encodeAsBMHTML()}" class="large">
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
                </div><div class="form-row city-selector-row">
                    <label><g:message code="suburb/city"/><span class="suggestion"><g:message code="create.customer.city.suggestion"/></span></label>
                    <input type="text" class="large" name="city" validation="maxlength[40]" value="${address?.city}" maxlength="40"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="mobile"/></label>
                    <input type="text" class="large" name="mobile" maxlength="40" validation="maxlength[40] phone" message_template="mobile.invalid" value="${address?.mobile}"/>
                </div><div class="form-row">
                    <label><g:message code="phone"/></label>
                    <input type="text" class="large" name="phone" maxlength="40" validation="maxlength[40] phone" value="${address?.phone}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="status"/></label>
                    <select class="large" name="status">
                        <option value="I"><g:message code="inactive"/></option>
                        <option value="A" ${customer?.status == "A" ? 'selected="selected"' : ''}><g:message code="active"/></option>
                    </select>
                </div><div class="form-row">
                    <label><g:message code="fax"/></label>
                    <input type="text" class="large" name="fax" maxlength="40" validation="maxlength[40] phone" message_template="fax.invalid" value="${address?.fax}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="tax.default.code"/><span class="suggestion"></span></label>
                    %{--<ui:domainSelect name="defaultTaxCode" class="medium tax-profile-selector" domain="${com.webcommander.webcommerce.TaxCode}" text="label" key="name" value="${customer?.defaultTaxCode}" prepend="${['': g.message(code: "select.")]}" />--}%
                    <g:select name="defaultTaxCode" class="medium tax-profile-selector" from="${codes}" optionValue="label" optionKey="name" value="${customer?.defaultTaxCode}" noSelection="['': g.message(code: 'none')]"/>
                </div>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${ customer.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

</form>