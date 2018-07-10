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
        <g:if test="${customerList}">
        <g:each in="${customerList}"  var="customer">
            <tr>
                <td>${customer.fullName.encodeAsBMHTML() + (customer.isCompany ? "&nbsp;<span class='mark-icon company'></span>" : "") }</td>
                <td class="actions-column" customer="${customer.id}">
                    <input type="checkbox" class="multiple">
                    <input type="hidden" value="${customer.id}">
                </td>
            </tr>
        </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td class="no-data" colspan="2"><g:message code="no.customer.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset ?: 0}" max="${params.max ?: 10}"></paginator>
</div>