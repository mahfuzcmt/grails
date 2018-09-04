<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="profile-editor-panel">
    <form class="edit-popup-form"  action="${app.relativeBaseUrl()}taxAdmin/addRuleToProfile">
        <input type="hidden" name="ruleId" value="${params.ruleId}"/>
        <input type="hidden" name="profileId" value="${params.profileId}"/>
        <div class="app-tab-content-container">
            <div class="header">
                <div class="left-header">
                    <span class="title"><g:message code="selected.rule"/></span>
                </div>
            </div>
            <g:include view="admin/tax/new/rule/ruleTable.gsp" model="[hideActionCol: true]"/>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button next"><g:message code="submit"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </form>
</div>