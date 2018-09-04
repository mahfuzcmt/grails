<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="simplifiedEventAdmin" action="loadEventsForSelection"/>
            </div>
        </div><div class="columns last-column">
        <div class="column-content selected-panel table-view">
            <div class="body">
                <table>
                    <colgroup>
                        <col style="width: 80%">
                        <col style="width: 20%">
                    </colgroup>
                    <tr>
                        <th><g:message code="event.name"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${selectedEvents}" var="event">
                        <tr>
                            <td>${event.name.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${event.id}" type="event">
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden" name="event" value="${event.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${selectedEvents.size()}" offset="0" max="${10}"></paginator>
            </div>
            <div class="hidden">
                <div class="action-column-dice-content">
                    <table>
                        <tr>
                            <td></td>
                            <td class="actions-column">
                                <span class="tool-icon move-controls">
                                    <span class="move-up"></span>
                                    <span class="move-down"></span>
                                </span>
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden">
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</div>