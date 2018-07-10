<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="configs" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MANAGE_MY_ACCOUNT)}"/>
<g:set var="fields" bean="configService"/>
<g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES['MANAGE_MY_ACCOUNT']}"/>
<g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
<input type="hidden" name="type" value="${configType}">
    <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="manage.my.account"/></h3>
                <div class="info-content"><g:message code="manage.my.account.details"/></div>
            </div>
            <div class="form-section-container">
                <div class="form-row">
                    <div class="child accordion-panel child full-width scroll-item-wrapper">
                        <g:each in="${fields.getSortedFields(configs)}" var="field">
                            <g:set var="active" value="${configs[field + '_active'].toBoolean(null)}"/>
                            <g:set var="order" value="${configs[field + '_order']}"/>
                            <g:set var="label" value="${configs[field + '_label']}"/>
                            <g:if test="${((ecommerce == 'true') && (DomainConstants.ECOMMERCE_CUSTOMER_PROFILE_CHECKLIST[field] == true)) || (DomainConstants.ECOMMERCE_CUSTOMER_PROFILE_CHECKLIST[field] == null)}">
                                <div class="child label-bar row-wcui-checkbox-right  collapsed customer-profile-settings">
                                    <span class ="default_label" hidden>${configs[field + '_default_label']}</span>
                                    <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                                    <span class="float-menu-navigator" is-default="false"/>
                                    <span class="inline-editable customer-profile-settings">${label}</span>
                                    <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                                           value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                                    <input class="order-child" type="hidden" name="${configType}.${field}_order" value="${order}">
                                </div>
                                <div class="accordion-item customer-profile-settings">
                                    <g:include view="/admin/setting/customerProfilePageSettings/${field}_setting.gsp" model="[]" />
                                </div>
                            </g:if>

                        </g:each>
                    </div>
                </div>
            </div>
    </div>