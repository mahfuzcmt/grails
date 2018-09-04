<div class="header">
    <div class="toolbar-share">
        <div class="toolbar toolbar-left">
            <span class="header-title event"><g:message code="equipments"/> (${count})</span>
        </div>
        <div class="toolbar-right before toolbar">
            <div class="tool-group action-header" style="display: none">
                <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
            </div>
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
            <div class="tool-group toolbar-btn create create-equipment"><i></i><g:message code="create"/></div>
            <div class="tool-group">
                <span class="toolbar-item switch-menu collapsed equipment-tool"><i></i></span>
                <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
            </div>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="event-tab equipment-table table-view">
        <div class="body">
            <table class="content">
                <colgroup>
                    <col class="select-column">
                    <col class="name-column">
                    <col class="description-column">
                    <col class="created-column">
                    <col class="updated-column">
                    <col class="actions-column">
                </colgroup>
                <tr>
                    <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
                    <th><g:message code="name"/></th>
                    <th><g:message code="description"/></th>
                    <th><g:message code="created"/></th>
                    <th><g:message code="updated"/></th>
                    <th class="actions-column"><g:message code="actions"/></th>
                </tr>
                <g:if test="${equipments}">
                    <g:each in="${equipments}" var="equipment">
                        <tr>
                            <td class="select-column"><input entity-id="${equipment.id}" type="checkbox" class="multiple"></td>
                            <td>${equipment.name.encodeAsBMHTML()}</td>
                            <td>${equipment.description.encodeAsBMHTML()}</td>
                            <td>${equipment.created.toAdminFormat(true, false, session.timezone)}</td>
                            <td>${equipment.updated.toAdminFormat(true, false, session.timezone)}</td>
                            <td class="actions-column">
                                <span class="action-navigator collapsed" entity-id="${equipment.id}" entity-name="${equipment.name.encodeAsBMHTML()}"></span>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row">
                        <td colspan="7"><g:message code="no.equipment.created"/> </td>
                    </tr>
                </g:else>
            </table>
        </div>
    </div>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>