<h3><g:message code="${giftRegistry.id ? "edit" : "create"}.gift.registry"/></h3>
<form class="gift-registry-create-edit-form" action="${app.baseUrl()}giftRegistry/save" method="post">
    <input type="hidden" name="id" value="${giftRegistry.id}">
    <div class="form-row mandatory">
        <label><g:message code="name"/>:</label>
        <input name="name" type="text" validation="required maxlength[100]" maxlength="100" value="${giftRegistry.name}">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="event.name"/>:</label>
        <input name="eventName" type="text" validation="required maxlength[100]" maxlength="100" value="${giftRegistry.eventName}">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="event.date"/>:</label>
        <input name="eventDate" type="text" class="time-field" validation="required" value="${giftRegistry.eventDate?.toDatePickerFormat(true, session.timezone)}">
    </div>
    <div class="form-row">
        <label><g:message code="event.details"/>:</label>
        <textarea name="eventDetails" validation="maxlength[100]" maxlength="1000">${giftRegistry.eventDetails}</textarea>
    </div>
    <div class="group-row">
        <h3><b><g:message code="shipping.address"/></b></h3>
    </div>
    <g:each in="${fields}" var="field">
        <g:set var="fieldRequired" value="${fieldsConfigs[field + '_required']}"/>
        <g:set var="required" value="${fieldRequired.toBoolean(true) ? 'required' : ''}"/>
        <g:set var="mandatory" value="${required == 'required' ? 'mandatory' : ''}"/>
        <g:set var="label" value="${fieldsConfigs[field + '_label']}"/>
        <g:if test="${field == 'first_name'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:first.name"}"/>:</label>
                <input type="text" class="large" name="firstName" validation="${required} rangelength[2,100]"
                       value="${address.firstName}">
            </div>
        </g:if>
        <g:if test="${field == 'last_name'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:last.name"}"/>:</label>
                <input type="text" class="large" name="lastName" validation="${required} rangelength[2,100]"
                       value="${address.lastName}">
            </div>
        </g:if>
        <g:if test="${field == 'email'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:email"}"/>:</label>
                <input type="text" class="large" id="address_email_field" name="email" validation="${required} email" value="${address.email}">
            </div>
        </g:if>
        <g:if test="${field == 'confirm_email'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:confirm.email"}"/>:</label>
                <input type="text" class="large" name="confirmEmail" validation="${required} email compare[address_email_field, string, eq]">
            </div>
        </g:if>
        <g:if test="${field == 'address_line_1'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: ('address_line_2' in fields ? "s:address.line.1" : "s:address.line")}"/>:</label>
                <input type="text" class="large" validation="${required} maxlength[1000]" maxlength="500" name="addressLine1" value="${address.addressLine1}">
            </div>
        </g:if>
        <g:if test="${field == 'address_line_2'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:address.line.2"}"/>:</label>
                <input type="text" name="addressLine2" validation="${required} maxlength[1000]" maxlength="500" value="${address.addressLine2}" class="large">
            </div>
        </g:if>
        <g:if test="${field == 'country'}">
            <div class="form-row country-selector-row ${mandatory}">
                <label><site:message code="${label ?: "s:country"}"/>:</label>
                <ui:countryList name="countryId" value="${country?.id}" validation="required"/>
            </div>
            <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states : states, stateId: address?.state?.id]"/>
        </g:if>
        <g:if test="${field == 'post_code'}">
            <div class="form-row post-code-row ${mandatory}">
                <label><site:message code="${label ?: "s:post.code"}"/>:</label>
                <input type="text" class="large" name="postCode" validation="${required} maxlength[5]" value="${address.postCode}"/>
            </div>
        </g:if>
        <g:if test="${field == 'city'}">
            <div class="form-row city-selector-row ${mandatory}">
                <label><site:message code="${label ?: "s:suburb/city"}"/>:</label>
                <g:include controller="app" action="loadCities" params="${[validation: required, state: address.state?.id, postCode: address.postCode, city: address.city]}"/>
            </div>
        </g:if>
        <g:if test="${field == 'phone'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:phone"}"/>:</label>
                <input type="text" class="large" name="phone" validation="${required} maxlength[40] phone" value="${address.phone}"/>
            </div>
        </g:if>
        <g:if test="${field == 'mobile'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:mobile"}"/>:</label>
                <input type="text" class="large" name="mobile" validation="${required} maxlength[40] phone" value="${address.mobile}"/>
            </div>
        </g:if>
        <g:if test="${field == 'fax'}">
            <div class="form-row ${mandatory}">
                <label><site:message code="${label ?: "s:fax"}"/>:</label>
                <input type="text" class="large" name="fax" validation="${required} maxlength[40]" value="${address.fax}"/>
            </div>
        </g:if>
    </g:each>
    <div class="form-row btn-row">
        <label></label>
        <button type="submit" class="submit-button"><g:message code="${giftRegistry.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>