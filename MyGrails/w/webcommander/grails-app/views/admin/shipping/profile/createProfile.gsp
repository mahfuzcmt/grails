<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="profile-editor-panel">
    <form class="edit-popup-form" action="${app.relativeBaseUrl()}shippingAdmin/saveProfile">
        <input type="hidden" name="id" value="${profile.id}">
        <div class="form-row mandatory">
            <label><g:message code="name"/> </label>
            <input type="text" value="${profile.name}" name="name" class="unique" validation="required maxlength[100]" maxlength="100" unique-action="isShippingProfileUnique">
        </div>
        <div class="form-row">
            <label><g:message code="rule.precedence"/> </label>
            <ui:namedSelect class="medium" name="rulePrecedence" key="${NamedConstants.SHIPPING_PROFILE_RULE_PRECEDENCE_MESSAGE_KEYS}" value="${profile.rulePrecedence}"/>
        </div>
        <div class="form-row">
            <label toggle-target="description" row-expanded="false"><g:message code="description"/></label>
            <textarea class="description" name="description" validation="maxlength[500]" maxlength="500">${profile.description}</textarea>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="${profile.id ? 'update' : 'add'}"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </form>
</div>