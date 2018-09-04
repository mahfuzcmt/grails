<div class="header">
    <span class="item-group entity-count title">
        <g:message code="newsletters"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "administrative.status"), g.message(code: "send.to"), g.message(code: "remove")]}" keys="['', 'administrativeStatus', 'sendTo', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 20%">
            <col style="width: 30%">
            <col style="width: 15%">
            <col style="width: 15%">
            <col style="width: 10%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="title"/></th>
            <th><g:message code="subject"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="schedule.time"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${newsletters}">
            <g:each in="${newsletters}" var="newsletter">
                <tr>
                    <td class="select-column"><input entity-id="${newsletter.id}" type="checkbox" class="multiple"></td>
                    <td>${newsletter.title.encodeAsBMHTML()}</td>
                    <td>${newsletter.subject.encodeAsBMHTML()}</td>
                    <td>${newsletter.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${newsletter.scheduleTime ? newsletter.scheduleTime.toAdminFormat(true, false, session.timezone) : g.message(code: "unscheduled")}</td>
                    <td class="status-column"><span class="status ${newsletter.isSent ? 'positive' : 'negative'}" title="${newsletter.isSent ? g.message(code: "sent") : g.message(code: "not.sent")}"></span></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${newsletter.id}" entity-name="${newsletter.title.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.newsletter.created"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>