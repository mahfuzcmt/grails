<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="filter.title"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:if test="${filters}">
            <g:each in="${filters}" var="filter">
                <tr>
                    <td>${filter.title.encodeAsBMHTML()}</td>
                    <td class="actions-column" filterGroupItem=${filter.id}>
                        <input type="checkbox" class="multiple">
                        <input type="hidden" value="${filter.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td class="no-data" colspan="2"><g:message code="no.filter.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>