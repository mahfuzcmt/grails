<%--
  Created by IntelliJ IDEA.
  User: arman
  Date: 3/31/2016
  Time: 2:51 PM
--%>

<div class="header">
    <span class="item-group entity-count title">
        <g:message code="event.custom.field.data"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="table-view no-paginator-table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="name-column">
                <col class="value-column">
                <col class="ticket-column">
                <col class="order-column">
            </colgroup>
            <tr>
                <th><g:message code="field.name"/></th>
                <th><g:message code="field.value"/></th>
                <th><g:message code="ticket.no"/></th>
                <th><g:message code="order.id"/></th>
            </tr>
            <g:if test="${fieldData}">
                <g:each in="${fieldData}" var="data" status="i">
                    <tr>
                        <td>${data.fieldName.encodeAsBMHTML()}</td>
                        <td>${data.fieldValue.encodeAsBMHTML()}</td>
                        <td><g:message code="${data.ticket}"/></td>
                        <td><g:message code="${data.order.id}"/></td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="5"><g:message code="no.field.value.created"/> </td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${0}" max="${0}"></paginator>
    </div>
</div>