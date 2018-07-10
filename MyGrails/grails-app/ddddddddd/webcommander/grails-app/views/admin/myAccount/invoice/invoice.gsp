<div class="toolbar-share">
</div>
<div class="table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="invoice-no-column">
                <col class="invoiced-on-column">
                <col class="invoice-due-column">
                <col class="total-column">
                <col class="status-column">
                <col class="download-column">
                <col class="action-column">
            </colgroup>
            <tr>
                <th><g:message code="invoice.no"/></th>
                <th><g:message code="invoiced.on"/></th>
                <th><g:message code="invoice.due"/></th>
                <th><g:message code="total"/></th>
                <th><g:message code="status"/></th>
                <th><g:message code="download"/></th>
                <th></th>
            </tr>
            <g:if test="${invoices.size()}">

            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="7"><g:message code="no.invoice.found"/></td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
