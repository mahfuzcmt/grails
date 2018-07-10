<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="post.title"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:if test="${posts}">
            <g:each in="${posts}" var="post">
                <tr>
                    <td>${post.name.encodeAsBMHTML()}</td>
                    <td class="actions-column" post=${post.id}>
                        <input type="checkbox" class="multiple">
                        <input type="hidden" value="${post.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td class="no-data" colspan="2"><g:message code="no.post.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>