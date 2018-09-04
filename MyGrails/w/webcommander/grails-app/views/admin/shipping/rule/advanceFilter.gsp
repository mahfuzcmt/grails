<div class="filter-body">
    <div class="content">
        <div class="tool-group">
            <label><g:message code="method"/></label>
            <ui:namedSelect class="small" name="policyType" key="${methodKeys}" value="${params.policyType}"/>
        </div>
        <g:if test="${params.ratePanel?.toBoolean() == false}">
            <g:if test="${classEnabled}">
                <div class="tool-group">
                    <label><g:message code="class"/></label>
                    <g:select noSelection="${['':"${g.message(code: 'all')}"]}" class="small" name="shippingClass" from="${classList}" optionKey="id" optionValue="name" value="${params.shippingClass}"/>
                </div>
            </g:if>
            <div class="tool-group">
                <label><g:message code="zone"/></label>
                <g:select noSelection="${['':"${g.message(code: 'all')}"]}" class="small" name="zone" from="${zoneList}" optionKey="id" optionValue="name" value="${params.zone}"/>
            </div>
        </g:if>
        <div class="tool-group">
            <label><g:message code="handling.cost"/></label>
            <ui:namedSelect class="small" name="handlingCost" key="${handlingCostFilterKeys}" value="${params.handlingCost}"/>
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="submit-button filter"><g:message code="filter"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>