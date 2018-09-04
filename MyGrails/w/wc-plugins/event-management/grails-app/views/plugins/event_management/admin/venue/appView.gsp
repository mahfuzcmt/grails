<div class="toolbar-share">
    <div class="toolbar toolbar-left">
        <span class="header-title event"><g:message code="venues"/> (${count})</span>
    </div>
    <div class="toolbar before toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn create create-venue"><i></i><g:message code="create"/></div>
    </div>
</div>
<div class="event-tab venue-table table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="select-column">
                <col class="name-column">
                <col class="url-column">
                <col class="capacity-column">
                <col class="manager-column">
                <col class="actions-column">
            </colgroup>
            <tr>
                <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
                <th><g:message code="name"/></th>
                <th><g:message code="site.url"/></th>
                <th><g:message code="location.capacity"/></th>
                <th><g:message code="manager"/></th>
                <th class="actions-column"><g:message code="actions"/></th>
            </tr>
            <g:if test="${venues}">
                <g:each in="${venues}" var="venue" status="i">
                    <tr>
                        <td class="select-column"><input entity-id="${venue.id}" type="checkbox" class="multiple"></td>
                        <td>${venue.name.encodeAsBMHTML()}</td>
                        <td><a href="${venue.siteUrl}" target="_blank">${venue.siteUrl}</a> </td>
                        <td>${capacity[i]}</td>
                        <td>${venue.manager.fullName}</td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${venue.id}" entity-name="${venue.name.encodeAsBMHTML()}" entity-url="${venue.url.encodeAsBMHTML()}"></span>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="6"><g:message code="no.venue.created"/> </td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
