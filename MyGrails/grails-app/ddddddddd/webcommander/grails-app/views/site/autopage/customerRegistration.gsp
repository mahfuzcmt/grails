<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:if test="${configs["registration_status_type"] == "closed"}">
    <div class="registration-type-closed">
        <site:message code="${configs.close_registration_message}"/>
    </div>
</g:if>
<g:else>
    <g:set var="defaultCountryId" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country')}"></g:set>
    <form class="registration-form valid-verify-form" action="${app.relativeBaseUrl()}customer/saveRegistration" method="post" validation-config-key="customer_registration">
        <plugin:hookTag hookPoint="customerRegistrationForm">
        <g:if test="${params.referer}">
            <input type="hidden" name="referer" value="${params.referer}">
        </g:if>
        <span class="title"><g:message code="registration"/></span>
        <g:each in="${fields}" var="field">
            <g:set var="fieldRequired" value="${fieldsConfigs[field + '_required']}"/>
            <g:set var="requiredClass" value="${fieldRequired ? 'required' : ''}"/>
            <g:set var="mandatoryClass" value="${fieldRequired ? 'mandatory' : ''}"/>
            <g:set var="label" value="${fieldsConfigs[field + "_label"]}"/>
            <g:if test="${field == 'first_name'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:first.name"}"/>:</label>
                    <input type="text" class="large" name="firstName" validation="${requiredClass} rangelength[2,100]" value="${customer.firstName}">
                </div>
            </g:if>
            <g:if test="${field == 'last_name'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:last.name"}"/>:</label>
                    <input type="text" class="large" name="lastName" validation="${requiredClass} rangelength[2,100]" value="${customer.lastName}">
                </div>
            </g:if>
            <g:if test="${field == 'email'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:email"}"/>:</label>
                    <input type="text" id="customer-registration-email" class="large" name="email" validation="${requiredClass} single_email" value="${customer.email}">
                </div>
            </g:if>
            <g:if test="${field == 'confirm_email'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:confirm.email"}"/>:</label>
                    <input type="text" class="large" validation="${requiredClass} single_email compare[customer-registration-email, string, eq]">
                </div>
            </g:if>
            <g:if test="${field == 'password'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:password"}"/>:</label>
                    <input type="password" id="customer-new-password" class="password-strength-meter" name="password" validation="${requiredClass} rangelength[6,50]"/>
                </div>
            </g:if>
            <g:if test="${field == 'retype_password'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:retype.password"}"/>:</label>
                    <input type="password" class="match-password" validation="${requiredClass} compare[customer-new-password, string, eq]" message_params="(password above)"/>
                </div>
            </g:if>
            <g:if test="${field == 'address_line_1'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:address.line.1"}"/>:</label>
                    <input type="text" class="large" validation="${requiredClass} maxlength[500]" maxlength="500" name="addressLine1" value="${customer.addressLine1}">
                </div>
            </g:if>
            <g:if test="${field == 'address_line_2'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:address.line.2"}"/>:</label>
                    <input type="text" name="addressLine2" validation="${requiredClass} maxlength[500]" maxlength="500" value="${customer.addressLine2}" class="large">
                </div>
            </g:if>
            <g:if test="${field == 'country'}">
                <div class="form-row country-selector-row">
                    <label><site:message code="${label ?: "s:country"}"/>:</label>
                    <ui:countryList id="countryId" name="country.id" class="large" value="${customer.country?.id ?: defaultCountryId.toLong()}"/>
                </div>
                <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states: states, stateId: customer.state?.id, postCode: customer.postCode]"/>
            </g:if>
            <g:if test="${field == 'post_code'}">
                <div class="form-row post-code-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:post.code"}"/>:</label>
                    <input type="text" class="large" name="postCode" validation="${requiredClass} maxlength[5]" value="${customer.postCode}"/>
                </div>
            </g:if>
            <g:if test="${field == 'city'}">
                <div class="form-row city-selector-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:suburb/city"}"/>:</label>
                    <g:include controller="app" action="loadCities" params="${[validation: requiredClass, state: customer.state?.id ?: (states.size() ? states[0].id : null), postCode: customer.postCode, city: customer.city]}"/>
                </div>
            </g:if>
            <g:if test="${field == 'customer_type'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:customer.type"}"/>:</label>
                    <input type="radio" name="isCompany" class="radio" value="true" toggle-target="company-row" toggle-target="abn" ${customer.isCompany ? "checked" : ""}>
                    <g:message code="company"/>
                    <input type="radio" name="isCompany" class="radio" value="false" toggle-target="sex-row" ${customer.isCompany  ? "" : "checked"}>
                    <g:message code="individual"/>
                </div>
            </g:if>
            <g:if test="${field == 'sex'}">
                <div class="form-row sex-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:gender"}"/>:</label>
                    <input type="radio" name="sex" class="radio" value="Male" ${customer.sex ? (customer.sex == "Male" ? "checked" : "") : "checked"}>
                    <g:message code="male"/>
                    <input type="radio" name="sex" class="radio" value="Female" ${customer.sex == "Female" ? "checked" : ""}>
                    <g:message code="female"/>
                </div>
            </g:if>
            <g:if test="${field == 'company_name'}">
                <div class="form-row company-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:company.name"}"/>:</label>
                    <input type="text" class="large" name="companyName" validation="skip@if{self::hidden} ${requiredClass} rangelength[2,100]" value="${customer.companyName}">
                </div>
            </g:if>
            <g:if test="${field == 'phone'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:phone"}"/>:</label>
                    <input type="text" class="large" name="phone" validation="${requiredClass} phone maxlength[40]" value="${customer.phone}"/>
                </div>
            </g:if>
            <g:if test="${field == 'mobile'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:mobile"}"/>:</label>
                    <input type="text" class="large" name="mobile" validation="${requiredClass} phone maxlength[40]" value="${customer.mobile}"/>
                </div>
            </g:if>
            <g:if test="${field == 'fax'}">
                <div class="form-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:fax"}"/>:</label>
                    <input type="text" class="large" name="fax" validation="${requiredClass} maxlength[40]" value="${customer.fax}"/>
                </div>
            </g:if>
            <g:if test="${field == 'abn'}">
                <div class="form-row company-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:abn"}"/>:</label>
                    <input type="text" class="large abn-validation" value="${customer.abn}" name="abn" validation="skip@if{self::hidden} ${requiredClass} match[^\d{2}\s\d{3}\s\d{3}\s\d{3}$, <site:message code="s:abn.format"/>]" maxlength="14">
                </div>
            </g:if>
            <g:if test="${field == 'abn_branch'}">
                <div class="form-row company-row ${mandatoryClass}">
                    <label><site:message code="${label ?: "s:abn.branch"}"/>:</label>
                    <input type="text" class="large" value="${customer.abnBranch}" name="abnBranch" validation="skip@if{self::hidden} ${requiredClass} maxlength[3] digits">
                </div>
            </g:if>
        </g:each>
        <g:if test="${configs["registration_captcha"] == 'yes'}">
            <ui:captcha/>
        </g:if>
        <g:if test="${fields.contains("registration_terms")}">
            <g:if test="${configs["registration_terms_text"]}">
                <div class="form-row mandatory">
                    <label><site:message code="${label ?: "s:registration.terms"}"/>:</label>
                    <textarea class="large" readonly >${configs["registration_terms_text"]}</textarea>
                </div>
            </g:if>
            <div class="form-row" validation="least_selection" message_template="${g.message(code: "required.to.proceed")}">
                <label>&nbsp;</label>
                <input class="checkbox" type="checkbox" name="registrationTerms"><g:message code="i.agree"/>
            </div>
        </g:if>
        </plugin:hookTag>
        <div class="form-row submit-row">
            <label>&nbsp;</label>
            <button><g:message code="register"/></button>
        </div>
    </form>
</g:else>