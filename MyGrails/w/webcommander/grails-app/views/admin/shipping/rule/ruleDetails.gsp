<div class="rule-details">
    <div class="rule-top">
        <h3 class="title">${shippingRule.name}</h3>
        <p class="description">${shippingRule.description}</p>
        <input type="hidden" class="rule-id" value="${shippingRule.id}" name="${shippingRule.description}"/>
    </div>
    <div class="body">
        <div class="rate-section">
            <span class="table-side-title rate-title"><span><g:message code="rate"/></span></span>
            <table class="rate-table">
                <tr class="table-header">
                    <th><g:message code="rate.name"/></th>
                    <th><g:message code="method"/></th>
                    <g:if test="${classEnabled}"><th><g:message code="class"/></th></g:if>
                    <th><g:message code="shipping.cost"/></th>
                    <th><g:message code="handling.cost"/></th>
                    <th class="action-col"></th>
                </tr>
                <g:if test="${shippingRule.shippingPolicy}">
                    <g:include view="admin/shipping/rate/rateDivRow.gsp" model="${[rule: shippingRule]}"/>
                </g:if>
                <tr class="table-add-button add-rate-row ${shippingRule.shippingPolicy ? 'hidden' : ''}">
                    <td colspan="6"><button class="add-rate" type="button"><g:message code="add.rate"/></button></td>
                </tr>
            </table>
        </div>

        <div class="zone-section">
            <span class="table-side-title zone-title"><span><g:message code="zone"/></span></span>
            <table class="zone-table">
            <tr class="table-header">
                <th><g:message code="zone.name"/></th>
                <th><g:message code="country"/></th>
                <th><g:message code="status"/>/<g:message code="province"/></th>
                <th><g:message code="post.code"/></th>
                <th class="action-col"></th>
            </tr>
            <g:each in="${shippingRule.zoneList}" var="zone">
                <tr>
                    <td>${zone.name}</td>
                    <td>${zone.countries?.name?.join(", ")}</td>
                    <td>${zone.states?.name?.join(", ")}</td>
                    <td>${zone.postCodes?.join(", ")}</td>
                    <td class="column actions-column">
                        <span class="action-navigator collapsed" entity-rule_id="${shippingRule?.id}" entity-rule_name="${shippingRule?.name.encodeAsBMHTML()}" entity-zone_id="${zone.id}" entity-zone_name="${zone.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
            <tr class="table-add-button">
                <td colspan="5"><button class="add-zone" type="button"><g:message code="add.zone"/></button></td>
            </tr>
        </table>
        </div>
    </div>
</div>