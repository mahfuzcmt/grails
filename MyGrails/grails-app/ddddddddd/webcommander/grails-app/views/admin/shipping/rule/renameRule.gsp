<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<div class="profile-editor-panel">
    <form class="edit-popup-form" action="${app.relativeBaseUrl()}shippingAdmin/copyRule">
        <input type="hidden" name="ruleId" value="${id}"/>
        <input type="hidden" name="profileId" value="${profileId}"/>
        <div class="form-row mandatory">
            <label><g:message code="rule.name"/> </label>
            <input type="text" name="name" class="unique" validation="required maxlength[100]" maxlength="100"  unique-action="isShippingRuleUnique">
        </div>
        <g:if test="${classEnabled}">
            <div class="form-row">
                <label><g:message code="class"/> </label>
                <g:select class="medium" name="shippingClass" optionKey="id" optionValue="name" from="${classList}"/>
            </div>
        </g:if>
        <div class="form-row">
            <label toggle-target="description" row-expanded="false"><g:message code="rule.description"/> </label>
            <textarea name="description" class="description" validation="maxlength[500]" maxlength="500"></textarea>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="add"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </form>
</div>