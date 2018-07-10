<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="news.title"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:each in="${newses}" var="news">
            <tr>
                <td>${news.title.encodeAsBMHTML()}</td>
                <td class="actions-column" news=${news.id}>
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${news.id}">
                </td>
            </tr>
        </g:each>
        <g:set var="loopCount" value="${params.int("max") - newses.size()}"/>
        <g:if test="${loopCount > 0}">
            <g:each in="${1..loopCount}" var="i">
                <tr>
                    <td>&nbsp;</td>
                    <td class="actions-column"></td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>