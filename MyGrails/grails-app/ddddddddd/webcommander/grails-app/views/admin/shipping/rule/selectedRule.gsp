<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="profile-editor-panel">
    <div class="edit-popup-form">
        <div class="app-tab-content-container">
            <div class="header">
                <div class="left-header">
                    <span class="title"><g:message code="selected.rule"/></span>
                </div>
            </div>
            <table class="content">
                <tr>
                    <th><g:message code="rule.name"/></th>
                    <th><g:message code="method"/></th>
                    <g:if test="${classEnabled}"><th><g:message code="shipping.class"/></th></g:if>
                    <th><g:message code="shipping.cost"/></th>
                    <th><g:message code="handling.cost"/></th>
                </tr>
                <g:if test="${selectedRule}">
                    <g:include view="admin/shipping/rule/ruleRow.gsp" model="[rule: selectedRule, selectRulePopup: true]"/>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row">
                        <td colspan="6"><g:message code="no.rule.created"/></td>
                    </tr>
                </g:else>
            </table>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button next"><g:message code="next"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>