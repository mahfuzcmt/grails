<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<g:set var="customerProfile" value="${DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE}"/>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST" class="fields-setting customer-profile-settings create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES['CUSTOMER_PROFILE_PAGE']}"/>
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <input type="hidden" name="type" value="${configType}">
    <div>
        <div class="accordion-panel scroll-item-wrapper">
            <g:each in="${fields}" var="field">
                <g:set var="active" value="${configs[field + '_active'].toBoolean(null)}"/>
                <g:set var="order" value="${configs[field + '_order']}"/>
                <g:set var="label" value="${configs[field + '_label']}"/>
                <g:if test="${ (ecommerce == 'true') && ((field == "my_carts") || (field == "my_lists"))}">
                    <g:set var="CONFIG_KEY" value="${field.toString().toUpperCase()}"/>
                    <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES."${CONFIG_KEY}").size() > 0 }">
                        <div class="label-bar collapsed  row-wcui-checkbox-right customer-profile-settings">
                            <span class ="default_label" hidden>${configs[field + '_default_label']}</span>
                            <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                            <span class="float-menu-navigator" is-default="false"/>
                            <span class="inline-editable customer-profile-settings">${label}</span>
                            <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                                   value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                            <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                        </div>
                        <div class="accordion-item customer-profile-settings">
                            <g:include view="/admin/setting/customerProfilePageSettings/${field}_setting.gsp" model="[]" />
                        </div>
                    </g:if>
                </g:if>
                <g:else>
                    <g:if test="${ (ecommerce == 'true') && (field == "my_wallet") }">
                        <div class="label-bar wallet-item collapsed  row-wcui-checkbox-right customer-profile-settings">
                            <span class ="default_label" hidden>${configs[field + '_default_label']}</span>
                            <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                            <span class="float-menu-navigator" is-default="false"/>
                            <span class="inline-editable customer-profile-settings">${label}</span>
                            <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                                   value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                            <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                        </div>
                        <div class="accordion-item customer-profile-settings"></div>
                    </g:if>
                    <g:elseif test="${field == "assigned_pages"}">
                        <div class="label-bar wallet-item collapsed  row-wcui-checkbox-right customer-profile-settings">
                            <span class ="default_label" hidden>${configs[field + '_default_label']}</span>
                            <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                            <span class="float-menu-navigator" is-default="false"/>
                            <span class="inline-editable customer-profile-settings">${label}</span>
                            <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                                   value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                            <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                        </div>
                        <div class="accordion-item customer-profile-settings"></div>
                    </g:elseif>
                    <g:else>
                        <g:if test="${((ecommerce == 'true') && (DomainConstants.ECOMMERCE_CUSTOMER_PROFILE_CHECKLIST[field] == true)) || (DomainConstants.ECOMMERCE_CUSTOMER_PROFILE_CHECKLIST[field] == null)}">
                            <div class="label-bar collapsed  row-wcui-checkbox-right customer-profile-settings">
                                <span class ="default_label" hidden>${configs[field + '_default_label']}</span>
                                <input type="hidden"  class="label-customer-profile-settings" name="${configType}.${field}_label" value="${label.toString()}">
                                <span class="float-menu-navigator" is-default="false"/>
                                <span class="inline-editable customer-profile-settings">${label}</span>
                                <input class="required single customer-field-switch" name="${configType}.${field}_active"  type="checkbox"
                                       value="true" uncheck-value="false"    ${active ? 'checked="checked"' : ''} >
                                <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                            </div>
                            <div class="accordion-item customer-profile-settings">
                                <g:include view="/admin/setting/customerProfilePageSettings/${field}_setting.gsp" model="[]" />
                            </div>
                        </g:if>
                    </g:else>
                </g:else>
            </g:each>
            <br>
        </div>
        <div class="form-row">
            <button class="submit-button" type="submit"><g:message code="update"/></button>
        </div>
    </div>
</form>

