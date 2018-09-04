<div class="edit-${section}-address">
    <g:each in="${fields}" var="field" status="idx">
        <g:set var="fieldRequired" value="${fieldsConfigs[field + '_required']}"/>
        <g:set var="required" value="${fieldRequired.toBoolean(true) ? 'required' : ''}"/>
        <g:set var="mandatory" value="${required == 'required' ? 'mandatory' : ''}"/>
        <g:set var="label" value="${fieldsConfigs[field + '_label']}"/>
        <g:if test="${field == 'first_name'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:first.name"}"/></label>
                <input type="text" class="large" name="firstName" validation="${required} rangelength[2,100]" value="${address.firstName}">
            </div>
        </g:if>
        <g:if test="${field == 'last_name'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:last.name"}"/></label>
                <input type="text" class="large" name="lastName" validation="${required} rangelength[2,100]" value="${address.lastName}">
            </div>
        </g:if>
        <g:if test="${field == 'email'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:email"}"/></label>
                <input type="text" class="large" name="email" validation="${required} email" value="${address.email}">
            </div>
        </g:if>
        <g:if test="${field == 'confirm_email'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:confirm.email"}"/></label>
                <input type="text" class="large" name="confirmEmail" validation="${required} email">
            </div>
        </g:if>
        <g:if test="${field == 'address_line_1'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:address.line.1"}"/></label>
                <input type="text" class="large" validation="${required} maxlength[1000]" maxlength="500" name="addressLine1" value="${address.addressLine1}">
            </div>
        </g:if>
        <g:if test="${field == 'address_line_2'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:address.line.2"}"/></label>
                <input type="text" name="addressLine2" validation="${required} maxlength[1000]" maxlength="500" value="${address.addressLine2}" class="large">
            </div>
        </g:if>
        <g:if test="${field == 'country'}">
            <div class="form-row country-selector-row">
                <label><site:message code="${label ?: "s:country"}"/></label>
                <ui:countryList name="countryId" id="countryId" value="${address.countryId}" validation="required"/>
            </div>
            <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states : states, stateId: address.stateId]" params="[stateName: 'stateId']"/>
        </g:if>
        <g:if test="${field == 'post_code'}">
            <div class="form-row post-code-row ${mandatory}">
                <label><site:message code="${label ?: "s:post.code"}"/></label>
                <input type="text" class="large" name="postCode" validation="${required} maxlength[5]" value="${address.postCode}">
            </div>
        </g:if>
        <g:if test="${field == 'city'}">
            <div class="form-row city-selector-row chosen-wrapper ${mandatory}">
                <label><site:message code="${label ?: "s:suburb/city"}"/></label>
                <g:include controller="app" action="loadCities" params="[validation: required, state: address.stateId, postCode: address.postCode, city: address.city]"/>
            </div>
        </g:if>
        <g:if test="${field == 'phone'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:phone"}"/></label>
                <input type="text" class="large" name="phone" validation="${required} maxlength[40]" value="${address.phone}">
            </div>
        </g:if>
        <g:if test="${field == 'mobile'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:mobile"}"/></label>
                <input type="text" class="large" name="mobile" validation="${required} maxlength[40]" value="${address.mobile}">
            </div>
        </g:if>
        <g:if test="${field == 'fax'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:fax"}"/></label>
                <input type="text" class="large" name="fax" validation="${required} maxlength[40]" value="${address.fax}">
            </div>
        </g:if>
    </g:each>
</div>