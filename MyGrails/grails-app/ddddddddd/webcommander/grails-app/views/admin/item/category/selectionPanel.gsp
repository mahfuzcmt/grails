<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="category.name"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:if test="${categories}">
            <g:each in="${categories}" var="category">
                <tr>
                    <td>${category.name.encodeAsBMHTML()}</td>
                    <td class="actions-column" category=${category.id}>
                        <input type="checkbox" class="multiple">
                        <input type="hidden" value="${category.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td class="no-data" colspan="2"><g:message code="no.category.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>