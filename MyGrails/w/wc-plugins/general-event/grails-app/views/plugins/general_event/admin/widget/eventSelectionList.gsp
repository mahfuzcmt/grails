<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="event.name"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:if test="${events}">
            <g:each in="${events}" var="event">
                <tr>
                    <td>${event.name.encodeAsBMHTML()}</td>
                    <td class="actions-column" event=${event.id}>
                        <input type="checkbox" class="multiple">
                        <input type="hidden" value="${event.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td class="no-data" colspan="2"><g:message code="no.event.found"/> </td>
            </tr>
        </g:else>
        <g:set var="loopCount" value="${params.int("max") - events.size()}"/>
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