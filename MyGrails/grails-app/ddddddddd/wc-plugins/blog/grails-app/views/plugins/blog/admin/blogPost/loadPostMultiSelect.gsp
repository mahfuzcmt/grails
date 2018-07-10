<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 80%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th class="actions-column">
                <input class="check-all multiple" type="checkbox">
            </th>
        </tr>
        <g:each in="${postList}"  var="post">
            <tr>
                <td>${post.name.encodeAsBMHTML()}</td>
                <td class="actions-column" post="${post.id}">
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${post.id}">
                </td>
            </tr>
        </g:each>
        <g:set var="loopCount" value="${10 - postList.size()}"/>
        <g:if test="${loopCount}">
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