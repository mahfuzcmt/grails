<%@ page import="com.webcommander.admin.Customer" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="abandoned.carts"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "send.notification")]}" keys="['', 'send_notification']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="cart-no-column">
            <col class="name-column">
            <col class="email-column">
            <col class="status-column">
            <col class="notification-no-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="cart.no"/></th>
            <th><g:message code="registered.customer.name"/></th>
            <th><g:message code="customer.email"/></th>
            <th><g:message code="notification.status"/></th>
            <th><g:message code="no.of.notification.sent"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${carts}">
            <g:set var="status" value="${[sent: 'positive', disabled: 'negative', pending: "diplomatic"]}"/>
            <g:each in="${carts}" var="cart">
                <tr>
                    <td class="select-column"><input entity-id="${cart.id}" type="checkbox" class="multiple"></td>
                    <td>${cart.id}</td>
                    <g:set var="customer" value="${cart.customer}"/>
                    <td>${customer.fullName().encodeAsBMHTML()}</td>
                    <td>${customer.address.email}</td>
                    <td class="status-column"><span class="status ${status[cart.notificationStatus]}" title="<g:message code="${cart.notificationStatus}"/>"></span></td>
                    <td>${cart.notificationSentCount}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${cart.id}" entity-notification="${cart.notificationStatus}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>

