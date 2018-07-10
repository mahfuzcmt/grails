<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form action="${app.relativeBaseUrl()}customerAdmin/loadAppView" class="edit-popup-form create-edit-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
            <input type="text" class="large" name="name" value="${params.searchText}"/>
        </div><div class="form-row">
            <label><g:message code="email"/></label>
            <input type="text" class="large" name="email"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="customer.type" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
            <g:select class="large" name="isCompany" from='${[ g.message(code: "any"), g.message(code: "individual"), g.message(code: "company")]}' keys="['', 'false', 'true']"/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="status"/></label>
            <g:select class="large" name="status" from='${[  g.message(code: "any"), g.message(code: "active"), g.message(code: "inactive"), g.message(code: "pending")]}' keys="['', 'A', 'I', 'W']"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="address" /></label>
            <input type="text" class="large" name="address"/>
        </div><div class="form-row">
            <label><g:message code="suburb/city"/></label>
            <input type="text" class="large" name="city"/>
        </div>
    </div>
    <div class="form-row country-selector-row chosen-wrapper">
        <label><g:message code="country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
        <ui:countryList id="countryId" name="country" class="large" noSelection="${["": g.message(code: "all.countries")]}"/>
    </div>
    <div class="double-input-row">
        <div class="form-row post-code">
            <label><g:message code="post.code"/></label>
            <input type="text" class="large" name="postCode"/>
        </div><div class="form-row">
            <label><g:message code="phone"/></label>
            <input type="text" class="large" name="phone"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="mobile"/></label>
            <input type="text" class="large" name="mobile"/>
        </div><div class="form-row">
            <label><g:message code="fax"/></label>
            <input type="text" class="large" name="fax"/>
        </div>
    </div>
    <div class="form-row storecredit-between">
        <label><g:message code="store.credit"/></label>
        <div class="twice-input-row">
            <input type="text" class="store-credit-from smaller" restrict="decimal" name="storeCreditFrom"><span>-</span><input type="text" restrict="decimal" class="store-credit-to smaller" name="storeCreditTo"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="updated.between"/></label>
        <input type="text" class="datefield-from smaller" name="updatedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="updatedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>