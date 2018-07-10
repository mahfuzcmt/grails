<h1><g:message code="loyalty.points"/></h1>
<div class="loyalty-history">
    <div class="loyality_point_quentity"><g:message code="loyalty.point.store.message" args="${[totalPoints]}"/></div>
    <table>
        <colgroup>
            <col class="id-col">
            <col class="order-date-col">
            <col class="loyalty-point-col">
        </colgroup>
        <thead>
            <tr>
                <th><g:message code="date"/></th>
                <th><g:message code="loyalty.points"/></th>
                <th><g:message code="comment"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${historyList}" var="orderHistory">
                <tr>
                    <td><div class="wrapper" data-label="<g:message code="date"/>:">${orderHistory.created.toSiteFormat(true, false, session.timezone)}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="loyalty.point"/>:">${orderHistory.pointCredited - orderHistory.pointDebited}</div></td>
                    <td>
                        <div class="wrapper" data-label="<g:message code="comment"/>:">
                            <g:message code="${orderHistory.type}"/>
                            <g:if test="${orderHistory.type == com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.ORDER}">#${orderHistory.comment}</g:if>
                        </div>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <div class="button-line">
        <span class="button loyalty-button back" target="customerProfile"><g:message code="back"/> </span>
    </div>
</div>