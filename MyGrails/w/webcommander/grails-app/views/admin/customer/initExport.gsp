<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}customerExportImport/export" class="edit-popup-form" method="post" no-ajax="" target="_blank">
    <input type="hidden" name="filter" value='${params.filter}'>
    <div class="form-row">
        <label><g:message code="export"/></label>
        <input type="radio" name="type" value="active" checked>
        <span><g:message code="active.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></span>
    </div>
    <div class="form-row">
        <label>&nbsp;</label>
        <input type="radio" name="type" value="all">
        <span><g:message code="all.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/> </span>
    </div>
    <div class="form-row">
        <label>&nbsp;</label>
        <input type="radio" name="type" value="filter" ${!params.filter ? 'disabled="true"' : ''}>
        <span><g:message code="current.filtered.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/> </span>
    </div>
    <div class="group-row">
        <label>&nbsp;</label>
    </div>
    <g:set var="mandatoryFields" value="${["firstName", "lastName", "userName", "addressLine1", "customerType"]}"/>

    <g:each in="${NamedConstants.CUSTOMER_EXPORT_IMPORT_FIELDS}" var="field" status="i">
        <g:set var="isMandatory" value="${mandatoryFields.contains(field.key)}"/>
        <div class="form-row">
            <g:if test="${i == 0}">
                <label><g:message code="export.fields"/></label>
            </g:if>
            <g:else>
                <label>&nbsp;</label>
            </g:else>
            <g:if test="${isMandatory}">
                <input  type="hidden" value="true" name="${field.key}">
            </g:if>
            <span class="label">
                <input type="checkbox" class="single" value="true" uncheck-value="false" checked name="${field.key}" ${isMandatory ? "disabled" : ""}>
                <span><g:message code="${field.value}" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></span>
            </span>
        </div>
    </g:each>



    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="export"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>