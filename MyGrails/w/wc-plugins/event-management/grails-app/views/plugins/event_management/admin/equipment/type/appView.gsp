<div class="toolbar-share">
    <div class="toolbar toolbar-left">
        <span class="header-title event"><g:message code="equipment.types"/> (${count})</span>
    </div>
    <div class="toolbar toolbar-right before">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn create create-equipment-type"><i></i><g:message code="create"/></div>
        <div class="tool-group equipment-type-tool">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
        </div>
    </div>
</div>
<div class="event-tab equipment-type-table table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="select-column">
                <col class="name-column">
                <col class="description-column">
                <col class="actions-column">
            </colgroup>
            <tr>
                <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
                <th><g:message code="name"/></th>
                <th><g:message code="description"/></th>
                <th class="actions-column"><g:message code="actions"/></th>
            </tr>
            <g:if test="${equipmentTypes}">
                <g:each in="${equipmentTypes}" var="type">
                    <tr>
                        <td class="select-column"><input entity-id="${type.id}" type="checkbox" class="multiple"></td>
                        <td>${type.name.encodeAsBMHTML()}</td>
                        <td>${type.description.encodeAsBMHTML()}</td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${type.id}" entity-name="${type.name.encodeAsBMHTML()}"></span>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row">
                    <td colspan="4"><g:message code="no.equipment.type.created"/> </td>
                </tr>
            </g:else>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>