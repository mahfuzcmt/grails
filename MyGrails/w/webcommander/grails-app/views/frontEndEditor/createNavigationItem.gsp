<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="itemTypes" value="${DomainConstants.NAVIGATION_ITEM_TYPE}"></g:set>
<div class="fee-nav-add-edit-panel fee-row">
    <div class="fee-nav-header">
        <div class="fee-form-row fee-multi-input-row">
            <div class="fee-form-element mandatory fee-nav-label">
                <label><g:message code="label"/></label>
                <input type="text" class="medium" name="label" validation="required maxlength[100]" value="${navigationItem.label}" maxlength="100">
            </div>
            <div class="fee-form-element fee-nav-action">
                <button type="button" class="fee-common fee-item-cancel"><g:message code="cancel"/></button>
                <button type="button" class="fee-blue fee-item-save"><g:message code="${navigationItem.label ? "update" : "save"}"/></button>
            </div>
        </div>
    </div>
    <div class="fee-nav-body">
        <div class="fee-form-row fee-multi-input-row">
            <div class="fee-form-element mandatory mandatory-chosen-wrapper">
                <label><g:message code="type"/></label>
                <select id="itemType" class="medium" name="itemType" validation="required">
                    <g:each in="${itemTypes}" status="i" var="type">
                        <option value="${type.value}" ${navigationItem?.itemType == type.value ? 'selected' : ''}><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type.value]}"/></option>
                    </g:each>
                </select>
            </div>
            <div class="fee-form-element typeRef-column">
                <g:if test="${navigationItem?.itemType}">
                    <g:include controller="navigation" action="loadReferenceSelectorBasedOnType" params="${[type: navigationItem?.itemType, ref: navigationItem?.itemRef]}"/>
                </g:if>
            </div>
        </div>
    </div>
</div>
%{--<div class="fee-row">
    <div class="fee-col fee-col-25 form-row mandatory mandatory-chosen-wrapper">
        <label><g:message code="type"/></label>
        <select id="itemType" class="medium" name="itemType" validation="required">
            <g:each in="${itemTypes}" status="i" var="type">
                <option value="${type.value}" ${navigationItem?.itemType == type.value ? 'selected' : ''}><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type.value]}"/></option>
            </g:each>
        </select>
    </div>
    <div class="fee-col fee-col-25 typeRef-column">
        <g:if test="${navigationItem?.itemType}">
            <g:include controller="navigation" action="loadReferenceSelectorBasedOnType" params="${[type: navigationItem?.itemType, ref: navigationItem?.itemRef]}"/>
        </g:if>
    </div>
    <div class="fee-col fee-col-25 mandatory form-row">
        <label><g:message code="label"/></label>
        <input type="text" class="medium" name="label" validation="required maxlength[100]" value="${navigationItem.label}" maxlength="100">
    </div>
    <div class="fee-col fee-col-25 form-row">
        <label>&nbsp;</label>
        <button type="button" class="fee-blue fee-item-save"><g:message code="${navigationItem.label ? "update" : "save"}"/></button>
        <button type="button" class="fee-common fee-item-cancel"><g:message code="cancel"/></button>
    </div>
</div>--}%
