<div class="header">
    <span class="item-group entity-count title">
        <g:message code="tickets"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group toolbar-btn create create-ticket"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col>
            <col style="width: 30%">
            <col style="width: 200px">
            <col style="width: 150px">
            <col style="width: 100px">
        </colgroup>
        <tr>
            <th><g:message code="seat.numbers"/></th>
            <th><g:message code="section"/></th>
            <th><g:message code="date"/></th>
            <th><g:message code="purchased"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:each in="${tickets}" var="ticket">
            <tr>
                <td>${ticket.seats.join(", ")}</td>
                <td>${ticket.section.name}</td>
                <td>${ticket.purchased.toAdminFormat(true, false, session.timezone)}</td>
                <td>${ticket.isHonorable ? g.message(code: 'complementary') : "Order#" + ticket.orderRef + (ticket.isReserved ? "("+ g.message(code: "reserved")+ ")" : "")}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-id="${ticket.ticketNumber}"></span>
                </td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>