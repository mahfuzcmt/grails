<%@ page import="com.webcommander.util.AppUtil; com.webcommander.conversion.MassConversions; com.webcommander.constants.*" %>
<div class="right-panel grid-view">
    <div class="body">
        <g:if test="${rule}">
            <div class="shipping-rule-container">
                <g:include view="admin/shipping/rule/ruleDetails.gsp" model="[shippingRule: rule]"/>
            </div>
        </g:if>
        <g:else>
            <g:include view="admin/shipping/profile/blankProfile.gsp"/>
        </g:else>
    </div>
</div>