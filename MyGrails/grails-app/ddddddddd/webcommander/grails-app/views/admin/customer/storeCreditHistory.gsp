<div class="table-view">
    <table class="body">
        <colgroup>
            <col style="width: 20%">
            <col style="width: 30%">
            <col style="width: 25%">
            <col style="width: 25%">
        </colgroup>
        <tr>
            <th><g:message code="store.credit"/></th>
            <th><g:message code="note"/></th>
            <th><g:message code="date"/></th>
            <th><g:message code="adjusted.by"/></th>
        </tr>

        <g:each in="${histories}" var="history">
            <tr>
                <td>${history.deltaAmount}</td>
                <td>${history.note?.encodeAsBMHTML() ?: ""}</td>
                <td>${history.created.toAdminFormat(true, false, session.timezone)}</td>
                <td>${history.createdBy ? history.createdBy.fullName.encodeAsBMHTML() : ""}</td>
            </tr>
        </g:each>
    </table>
    <div class="footer">
        <paginator total="${count}" offset="${offset}" max="${max}"></paginator>
    </div>
</div>