<div class="inner-table child-page-table">
    <h2><g:message code="store.pages"/>(${childPages.size()})</h2>
    <table>
        <tr>
            <td class="table-header"></td>
            <td class="table-header status-column"><g:message code="visibility"/></td>
            <td class="table-header"><g:message code="store"/></td>
            <td class="table-header"><g:message code="page.name"/></td>
            <td class="table-header"><g:message code="page.title"/></td>
            <td class="table-header"><g:message code="page.layout"/></td>
            <td class="table-header"><g:message code="created"/></td>
            <td class="table-header"><g:message code="updated"/></td>
            <td class="table-header"><g:message code="created.by"/></td>
            <td class="table-header actions-column"><g:message code="actions"/></td>
        </tr>
        <g:if test="${childPages}">
            <g:each in="${childPages}" var="childPage">
                <tr class="${childPage.url == landingPage ? "landing highlighted" : ""}">
                    <g:set var="status" value="${[open: 'positive', hidden: 'diplomatic', restricted: 'negative']}"/>
                    <g:set var="tooltipNotification" value="${[open: 'Open', hidden: 'Hidden', restricted: 'Restricted']}"/>
                    <td class="select-column"><input entity-id="${childPage.id}" type="checkbox" class="multiple"></td>
                    <td><span class="status ${status[childPage.visibility]}" title="${tooltipNotification[childPage.visibility]}"></span></td>
                    <td>${childPage.store.location.encodeAsBMHTML()}</td>
                    <td>${childPage.name.encodeAsBMHTML()}</td>
                    <td>${childPage.title.encodeAsBMHTML()}</td>
                    <td>${childPage.layout?.name?.encodeAsBMHTML()}</td>
                    <td>${childPage.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td >${childPage.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${childPage.createdBy?.fullName?.encodeAsBMHTML()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${childPage.id}" entity-url="${childPage.url.encodeAsBMHTML()}" entity-name="${childPage.name.encodeAsBMHTML()}" entity-owner_id="${childPage.createdBy?.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>