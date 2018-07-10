<div class="rate-bulk-edit-panel">
    <table class="bulk-edit-table">
        <tr>
            <th class="header-value"><g:message code="rate.name"/></th>
            <th class="header-value"><g:message code="method"/></th>
            <th class="header-value"><g:message code="shipping.cost"/></th>
            <th class="header-value"><g:message code="handling.cost"/></th>
        </tr>
        <tbody class="table-body">
            <g:each in="${rateList}" var="rate">
                <g:include view="admin/shipping/rate/bulkedit/bulkEditRow.gsp" model="[rate: rate]"/>
            </g:each>
        </tbody>
    </table>
</div>