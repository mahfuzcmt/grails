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
        <g:each in="${categoryList}"  var="category">
            <tr>
                <td>${category.name.encodeAsBMHTML()}</td>
                <td class="actions-column" category="${category.id}">
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${category.id}">
                </td>
            </tr>
        </g:each>
        <g:set var="loopCount" value="${10 - categoryList.size()}"/>
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