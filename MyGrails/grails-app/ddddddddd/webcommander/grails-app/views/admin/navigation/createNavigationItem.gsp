<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="itemTypes" value="${DomainConstants.NAVIGATION_ITEM_TYPE}"></g:set>
<form class="navigation-item-form fade-in-down" action="${app.relativeBaseUrl()}navigation/saveNavigationItems" method="post">
    <span class="title"><g:message code="${params.title}"/></span>
    <div class="mandatory form-row">
        <label><g:message code="label"/></label>
        <input type="text" class="medium" name="label" validation="required maxlength[100]" value="${navigationItem.label}" maxlength="100">
    </div>
    <div class="form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="type"/></label>
        <select id="itemType" class="medium" name="itemType" validation="required">
            <g:each in="${itemTypes}" status="i" var="type">
                <option value="${type.value}" ${navigationItem?.itemType == type.value ? 'selected' : ''}><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type.value]}"/></option>
            </g:each>
        </select>
    </div>
    <g:if test="${navigationItem?.itemType}">
        <g:include controller="navigation" action="loadReferenceSelectorBasedOnType" params="${[type: navigationItem?.itemType, ref: navigationItem?.itemRef]}"/>
    </g:if>
    <div class="form-row chosen-wrapper">
        <label><g:message code="navigation.target"/></label>
        <g:select name="target" from="${['_self', '_blank', '_parent', '_top']}" value="${navigationItem?.target}" keys="${['_self', '_blank', '_parent', '_top']}" class="medium"/>
    </div>
    <div class="form-row chosen-wrapper">
        <label><g:message code="parent"/></label>
        <select class="medium" name="parent">
            <g:each in="${parents}" var="parent">
                <option value="${parent.id}" ${parent.id == navigationItem?.parent?.id.toString() ? "selected" : ""} class="${"depth-" + parent.depth}">${parent.name}</option>
            </g:each>
        </select>
    </div>
    <div class="form-row thicker-row">
        <label><g:message code="item.image"/></label>
        <div class="form-image-block">
            <input type="file" name="item-image" file-type="image" size-limit="20480" submit-data="${navigationItem.id}"
                 ${navigationItem.image ? 'remove-support="true"' :
                    'reset-support="' + 'true"'} previewer="navigation-logo-preview" remove-option-name="remove-image">
            <div class="preview-image">
                <g:set var="logoPath" value="${appResource.getNavigationItemImageURL(navigationItem: navigationItem)}"/>
                <img id="navigation-logo-preview" src="${logoPath}">
            </div>
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="image.alt"/></label>
        <input type="text" class="medium" name="imageAlt"  value="${navigationItem.imageAlt}" maxlength="100">
    </div>
    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${navigationItem.label ? "update" : "save"}"/></button>
        <g:if test="${navigationItem.label}"><button type="button" class="cancel-button"><g:message code="cancel"/></button></g:if>
    </div>
</form>
