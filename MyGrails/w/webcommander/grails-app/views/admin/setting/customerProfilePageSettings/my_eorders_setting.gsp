<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="configs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_ORDERS)}"/>
<g:set var="fields" bean="configService"/>
<g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES['MY_ORDERS']}"/>
<input type="hidden" name="type" value="${configType}">
<div class="form-section no-item">
    <div class="form-section-info">
        <h3><g:message code="my.orders"/></h3>
        <div class="info-content"><g:message code="my.orders.details"/></div>
    </div>

    <div class="form-section-container">
        <div class="form-row">
            <div class="child accordion-panel child full-width scroll-item-wrapper">
                <g:each in="${fields.getSortedFields(configs, "_orderkey")}" var="field">
                    <g:if test="${field == "pending_order" || field == "completed_order"}">
                        <g:set var="active" value="${configs[field + '_active'].toBoolean(null)}"/>
                        <g:set var="order" value="${configs[field + '_orderkey']}"/>
                        <g:set var="label" value="${configs[field + '_label']}"/>
                        <g:if test="${active != null}">
                            <div class="child no-item label-bar row-wcui-checkbox-right customer-profile-settings collapsed">
                                <span class="default_label" hidden>${configs[field + '_default_label']}</span>
                                <input type="hidden" class="label-customer-profile-settings"
                                       name="${configType}.${field}_label" value="${label.toString()}">
                                <span class="float-menu-navigator" is-default="false"/>
                                <span class="inline-editable customer-profile-settings">${label}</span>
                                <input class="required single customer-field-switch"
                                       name="${configType}.${field}_active" type="checkbox"
                                       value="true" uncheck-value="false" ${active ? 'checked="checked"' : ''}>
                                <input class="order-child" type="hidden" name="${configType}.${field}_orderkey" value="${order}">
                            </div>
                        </g:if>
                    </g:if>
                </g:each>
            </div>
        </div>
    </div>
</div>