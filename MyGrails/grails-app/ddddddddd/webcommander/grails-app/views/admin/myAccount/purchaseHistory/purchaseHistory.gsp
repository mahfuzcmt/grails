<div class="toolbar-share">
</div>
<div class="table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="service-column">
                <col class="purchased-on-column">
                <col class="package-name-column">
                <col class="total-column">
                <col class="status-column">
            </colgroup>
            <tr>
                <th><g:message code="service"/></th>
                <th><g:message code="purchased.on"/></th>
                <th><g:message code="package.name"/></th>
                <th><g:message code="total"/></th>
                <th><g:message code="status"/></th>
            </tr>
            <g:if test="${history.size()}">

            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="5"><g:message code="no.purchase.history.found"/></td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
