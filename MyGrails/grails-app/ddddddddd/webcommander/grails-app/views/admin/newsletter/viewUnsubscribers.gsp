<div class="header">
    <span class="item-group entity-count title">
        <g:message code="unsubscriber"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="email"/></th>
            <th><g:message code="subscription.time"/></th>
            <th><g:message code="unsubscription.time"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${unsubscribers}">
            <g:each in="${unsubscribers}" var="unsubscriber">
                <g:set var="subscriber" value="${unsubscriber.subscriber}"/>
                <tr>
                    <td>${(subscriber.title? g.message(code: subscriber.title) + " " : "") + (subscriber.firstName ? " " + subscriber.firstName.encodeAsBMHTML() : "") +  (subscriber.lastName ? " " + subscriber.lastName.encodeAsBMHTML() : "")}</td>
                    <td>${subscriber.email.encodeAsBMHTML()}</td>
                    <td>${unsubscriber.subscribed.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${unsubscriber.unsubscribed.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${unsubscriber.id}" entity-name="${subscriber.email.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.unsubscriber"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>