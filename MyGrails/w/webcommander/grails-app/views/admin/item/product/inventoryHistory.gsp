<%--
Created by IntelliJ IDEA.
User: sajed
Date: 3/25/14
Time: 3:53 PM
--%>

<div class="header">
    <span class="item-group entity-count title">
    </span>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 20%">
            <col style="width: 30%">
            <col style="width: 25%">
            <col style="width: 25%">
        </colgroup>
        <tr>
            <th><g:message code="change.quantity"/></th>
            <th><g:message code="note"/></th>
            <th><g:message code="date"/></th>
            <th><g:message code="created.by"/></th>
        </tr>

        <g:each in="${histories}" var="history">
            <tr>
                <td>${history.changeQuantity}</td>
                <td>${history.note?.encodeAsBMHTML() ?: ""}</td>
                <td>${history.created.toAdminFormat(true, false, session.timezone)}</td>
                <td>${history.createdBy ? history.createdBy.fullName.encodeAsBMHTML() : ""}</td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${offset}" max="${max}"></paginator>
</div>