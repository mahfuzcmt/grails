<g:if test="${taxRules.size() > 0}">
    <h4 class="group-label"><g:message code="tax.rule"/></h4>
    <div class="view-content-block">
    <g:each in="${taxRules}" var="taxRule">
        <div class="view-list-row">
            ${taxRule.name.encodeAsBMHTML()}
        </div>
    </g:each>
    </div>
</g:if>
<g:if test="${shippingRules.size()}">
    <h4 class="group-label"><g:message code="shipping.rule"/></h4>
    <g:each in="${shippingRules}" var="shippingRule">
        <div class="view-content-block">
            ${shippingRule.name.encodeAsBMHTML()}
        </div>
    </g:each>
</g:if>
<g:if test="${paymentGateways.size()}">
    <h4 class="group-label"><g:message code="payment.gateways"/></h4>
    <g:each in="${paymentGateways}" var="paymentGateway">
        <div class="view-content-block">
            ${paymentGateway.name.encodeAsBMHTML()}
        </div>
    </g:each>
</g:if>
