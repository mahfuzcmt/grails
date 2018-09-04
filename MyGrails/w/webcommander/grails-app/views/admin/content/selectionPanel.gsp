<div class="body">
    <table>
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="article.name"/></th>
            <th class="actions-column"><input class="check-all" type="checkbox" class="multiple"></th>
        </tr>
        <g:each in="${articles}" var="article">
            <tr>
                <td>${article.name.encodeAsBMHTML()}</td>
                <td class="actions-column" article=${article.id}>
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${article.id}">
                </td>
            </tr>
        </g:each>
        <g:if test="${!articles}">
            <tr>
                <td></td>
                <td class="actions-column"></td>
            </tr>
        </g:if>
        <g:set var="loopCount" value="${params.int("max") - articles.size()}"/>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>