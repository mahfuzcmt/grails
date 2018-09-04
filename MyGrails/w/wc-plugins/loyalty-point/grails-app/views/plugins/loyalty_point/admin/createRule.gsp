<%@ page import="com.webcommander.plugin.loyalty_point.constants.DomainConstants" %>
<form class="loyalty-points-rule-form create-edit-form edit-popup-form" id="loyaltyPointsRuleForm"  action="${app.relativeBaseUrl()}loyaltyPointAdmin/saveRule">
    <div class="loyalty-point-rule-panel">
        <div class="triple-input-row rule-detail">
            <div class="form-row rule-name">
                <label><g:message code="name"/></label>
                <input class="smaller" type="text" name="rule_name" value="${params.name}" validation="required"/>
            </div>
            <div class="form-row rule-details">
                <label><g:message code="set.rule"/></label>
                <loyaltyPoint:namedSelection class="small rule-type" name="rule_type" target="${DomainConstants.RULE_TYPE}" value="${params.ruleType}"/>
            </div>
            <div class="form-row rule-details">
                <label><g:message code="points"/></label>
                <input type="text" class="smaller rule-point" maxlength="9" name="rule_point" restrict="numeric" validation="required number" value="${params.point}">
            </div>
        </div>
        <div class="customer-detail">
            <g:include controller="customerAdmin" action="selectCustomerAndGroups"/>
        </div>
    </div>
</form>