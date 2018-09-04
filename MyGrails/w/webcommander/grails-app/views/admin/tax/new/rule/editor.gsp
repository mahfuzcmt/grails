<div class="rule-details">
    <div class="rule-top">
        <h3 class="title">${rule.name}</h3>
        <p class="description">${rule.description}</p>
        <input type="hidden" class="rule-id" value="${rule.id}" name="${rule.description}"/>
    </div>
    <div class="body">
        <div class="code-section">
            <span class="table-side-title code-title"><span><g:message code="code"/></span></span>
            <table class="code-table">
                <tr class="table-header">
                    <th><g:message code="rate.name"/></th>
                    <th><g:message code="label"/></th>
                    <th><g:message code="tax.rate"/></th>
                    <th class="action-column"></th>
                </tr>
                <g:if test="${rule.code}">
                    <tr>
                        <td>${rule.code.name}</td>
                        <td>${rule.code.label}</td>
                        <td>${rule.code.rate}%</td>
                        <td class="column actions-column">
                            <span class="action-navigator collapsed" entity-rule_id="${rule.id}" entity-id="${rule.code.id}" entity-name="${rule.name.encodeAsBMHTML()}"></span>
                        </td>
                    </tr>
                </g:if>
                <tr class="table-add-button add-code-row ${rule.code ? 'hidden' : ''}">
                    <td colspan="6"><button class="add-code" type="button"><g:message code="add.code"/></button></td>
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
            <g:each in="${rule.zones}" var="zone">
                <tr>
                    <td>${zone.name}</td>
                    <td>${zone.countries?.name?.join(", ")}</td>
                    <td>${zone.states?.name?.join(", ")}</td>
                    <td>${zone.postCodes?.join(", ")}</td>
                    <td class="column actions-column">
                        <span class="action-navigator collapsed" entity-rule_id="${rule?.id}" entity-rule_name="${rule?.name.encodeAsBMHTML()}" entity-zone_id="${zone.id}" entity-zone_name="${zone.name.encodeAsBMHTML()}"></span>
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