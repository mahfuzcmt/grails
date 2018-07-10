<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group remove-after-reload">
            <span class="toolbar-item reset reset-default" title="<g:message code="restore.default.setting"/>" ><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" class="fields-setting create-edit-form">
    <g:set var="configType" value="${DomainConstants.SITE_CONFIG_TYPES[type.toUpperCase() + '_ADDRESS_FIELD']}"/>
    <input type="hidden" name="type" value="${configType}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="${type}.address.form.fields"/></h3>
            <div class="info-content"><g:message code="section.text.${type}.address.form.field.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="sortable-container">
                <g:each in="${fields}" var="field">
                    <g:set var="active" value="${configs[field + '_active'].toBoolean(null)}"/>
                    <g:set var="required" value="${configs[field + '_required'].toBoolean(null)}"/>
                    <g:set var="order" value="${configs[field + '_order']}"/>
                    <g:set var="label" value="${configs[field + '_label']}"/>
                    <div class="configurable-row ${active == null || active ? 'active' : 'inactive'}">
                        <label><g:message code="${field.toString().replaceAll('_', '.')}"/></label>
                        <span class="edit-block">
                            <input type="text" class="small" name="${configType}.${field}_label" value="${label}">
                            <g:if test="${active != null}">
                                <input class="active" type="hidden" name="${configType}.${field}_active" value="${active}">
                                <input class="required single" type="checkbox" name="${configType}.${field}_required" value="true" uncheck-value="false"
                                    ${required ? 'checked="checked"' : ''} ${active ? '' : 'disabled="disabled"'}>
                                <g:message code="required"/> &nbsp &nbsp
                                <span class="tool-icon ${active ? 'remove' : 'add'}"></span>
                            </g:if>
                            <g:else>
                                <input class="required single" type="checkbox" name="${configType}.${field}_required" value="true" uncheck-value="false" checked="checked" disabled="disabled">
                                <g:message code="required"/> &nbsp &nbsp
                            </g:else>
                            <input class="order" type="hidden" name="${configType}.${field}_order" value="${order}">
                        </span>
                    </div>
                </g:each>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>

</form>