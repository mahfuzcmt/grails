<div class="column-content">
    <table>
        <colgroup>
            <col>
            <col style="width: 80px">
        </colgroup>
        <tr>
            <th><g:message code="product"/></th>
            <th class="actions-column">
                <g:message code="action"/>
                <input class="check-all multiple" type="checkbox">
            </th>
        </tr>
        <g:each in="${productList}"  var="product">
            <tr>
                <td title="${product.name.encodeAsBMHTML()}">${product.name.encodeAsBMHTML()}</td>
                <td class="actions-column" product="${product.id}">
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${product.id}">
                </td>
            </tr>
        </g:each>
        <g:set var="loopCount" value="${10 - productList.size()}"/>
        <g:each in="${1..loopCount}" var="i">
            <tr>
                <td>&nbsp;</td>
                <td class="actions-column"></td>
            </tr>
        </g:each>
    </table>
    <div class="pagination-line">
        <paginator total="${productList.size()}" offset="${0}" max="${10}"></paginator>
    </div>
</div>