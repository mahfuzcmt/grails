<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES['CUSTOMER_ACCOUNT_INFORMATION']}"/>
<g:set var="configs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_ACCOUNT_INFORMATION)}"/>
<input type="hidden" name="type" value="${configType}">
<g:set var="fields" bean="configService"/>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="account.information"/></h3>
        <div class="info-content"><g:message code="account.information.details"/></div>
    </div>
    <div class="form-section-container">
        <div class="sortable-container">
            <g:each in="${fields.getSortedFields(configs)}" var="field">

                <g:set var="active" value="${configs[field + '_active'].toBoolean(null)}"/>
                <g:set var="required" value="${configs[field + '_required'].toBoolean(null)}"/>
                <g:set var="order" value="${configs[field + '_order']}"/>
                <g:set var="label" value="${configs[field + '_label']}"/>

                <div class="configurable-row  row-wcui-checkbox-right child-child  ${active == null || active ? 'active' : 'active'}">
                    <label><g:message code="${field.toString().replaceAll('_', '.')}"/></label>
                    <span class="edit-block">
                        <g:if test="${active != null}">
                            <input class="active single" type="checkbox" name="${configType}.${field}_active" value="true" uncheck-value="false"
                                ${active ? 'checked="checked"' : ''}>
                        </g:if>
                        <g:else>
                            <input class="required single" type="checkbox" name="${configType}.${field}_active" value="true" uncheck-value="false" checked="checked" disabled="disabled">
                        </g:else>
                        <input class="order-child-child" type="hidden" name="${configType}.${field}_order" value="${order}">
                    </span>
                </div>

            </g:each>
        </div>
    </div>
</div>