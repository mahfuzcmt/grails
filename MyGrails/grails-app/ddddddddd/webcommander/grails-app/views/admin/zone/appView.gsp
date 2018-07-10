<div class="header">
    <input type="hidden" id="session-owner-id" value="${session.admin}"/>
    <span class="item-group entity-count title">
        <g:message code="zone"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
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
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container zone-table">
    <table class="content">
        <colgroup>
            <col style="width: 5%">
            <col style="width: 22%">
            <col style="width: 21%">
            <col style="width: 21%">
            <col style="width: 21%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th><g:message code="zone.name"/></th>
            <th><g:message code="country"/></th>
            <th><g:message code="state"/></th>
            <th><g:message code="post.code"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${zones}">
            <g:each in="${zones}" var="zone">
                <tr>
                    <td class="select-column"><input entity-id="${zone.id}" type="checkbox" class="multiple"></td>
                    <td>${zone.name.encodeAsBMHTML()}</td>
                    <td>
                        <g:if test="${zone.countries.size() == 1}">
                            <span class="flag-icon ${zone.countries[0].code.toLowerCase()}"></span>
                        </g:if>
                        <g:else>
                            <span class="flag-icon default"></span>
                        </g:else>
                        <g:set var="countries" value="${zone.countries}"/>
                        ${countries?.name.join(", ")}
                    </td>
                    <td>
                        <g:set var="states" value="${zone.states}"/>
                        ${states?.name.join(", ")}
                    </td>
                    <td>
                        <g:set var="postCodes" value="${zone.postCodes}"/>
                        ${postCodes?.join(", ")}
                    </td>

                    <g:if test="${!zone.isDefault}">
                        <td class="actions-column"><span class="action-navigator collapsed" entity-id="${zone.id}" entity-name="${zone.name.encodeAsBMHTML()}" entity-system-generated="${zone.isSystemGenerated}"></span></td>
                    </g:if>
                    <g:else>
                        <td></td>
                    </g:else>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.zone.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
