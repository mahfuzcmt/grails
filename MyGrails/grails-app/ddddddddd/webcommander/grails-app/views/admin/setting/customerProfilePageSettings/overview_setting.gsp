<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<g:set var="customerProfile" value="${DomainConstants.SITE_CONFIG_TYPES.OVERVIEW}"/>
<g:set var="fields" bean="configService"/>
<g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES['OVERVIEW']}"/>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.OVERVIEW)}"/>
<input type="hidden" name="type" value="${configType}">
<div class="form-section no-item">
    <div class="form-section-info">
        <h3><g:message code="overview"/></h3>
        <div class="info-content"><g:message code="section.text.customer.profile.overview.info"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row">
            <div class="child accordion-panel child full-width scroll-item-wrapper">
                <g:each in="${fields.getSortedFields(config)}" var="field">
                    <g:set var="active" value="${config[field + '_active'].toBoolean(null)}"/>
                    <g:set var="order" value="${config[field + '_order']}"/>
                    <g:set var="label" value="${config[field + '_label']}"/>
                    <div class="child no-item label-bar row-wcui-checkbox-right customer-profile-settings collapsed">
                        <span class ="default_label" hidden>${config[field + '_default_label']}</span>
                        <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                        <span class="float-menu-navigator" is-default="false"/>
                        <span class="inline-editable customer-profile-settings">${label}</span>
                        <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                               value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                        <input class="order-child" type="hidden" name="${configType}.${field}_order" value="${order}">
                    </div>
                </g:each>
            </div>
        </div>
   </div>
</div><div class="form-section">
    <div class="form-section-info">
        <h3>Configure display preference</h3>
        <div class="info-content"><g:message code="section.text.customer.profile.overview.info"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row">
            <input class="single customer-field-switch" name=""  type="checkbox" value="true" uncheck-value="false">
        </div>
   </div>
</div>
