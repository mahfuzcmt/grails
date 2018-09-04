<div class="content">
    <table>
        <colgroup>
            <col class="name-column">
            <col class="description-column">
            <col class="agents-column">
            <col class="actions-column">

        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="description"/></th>
            <th>#<g:message code="agents"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>

        <g:if test="${departments}">
            <g:each in="${departments}" var="department">
                <tr class="data-row">
                    <td>${department.name.encodeAsBMHTML()}</td>
                    <td>${department.description.encodeAsBMHTML()}</td>
                    <td>${department.operators.size()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${department.id}" entity-name="${department.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.department.added"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
