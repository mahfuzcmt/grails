<div class="header">
    <span class="item-group entity-count title">
        <g:message code="venue.locations"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
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
            <col class="collapse-controller-column" style="width: 5%">
            <col style="width: 5%">
            <col style="width: 45%">
            <col style="width: 20%">
            <col style="width: 15%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th></th>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="organiser"/></th>
            <th><g:message code="capacity"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${locations}">
            <g:each in="${locations}" var="location"  status="i">
                <tr>
                    <td><span class="${location.venueLocationInvitation?'tool-icon collapsed toggle-cell' : ''}" ></span></td>
                    <td class="select-column"><input entity-id="${location.id}" type="checkbox" class="multiple"></td>
                    <td>${location?.name.encodeAsBMHTML()}</td>
                    <td>${location?.organiser?.fullName}</td>
                    <td>${capacity[i]}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-url="${location.url}" entity-id="${location.id}" entity-name="${location.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
                <tr class="toggle-table-row" style="display: none">
                    <g:if test="${location.venueLocationInvitation}">
                        <td colspan="6">
                            <g:include view="plugins/event_management/admin/venue/venueLocationInvitationView.gsp" model="[location: location]"/>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.venue.location.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>