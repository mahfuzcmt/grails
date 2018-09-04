<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="venueAdmin" action="loadVenueLocationsForSelection"/>
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
                        <th><g:message code="venue.location"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${selectedLocations}" var="location">
                        <tr>
                            <td>${location.name.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${location.id}" type="venueLocation">
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden" name="venueLocation" value="${location.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${selectedLocations.size()}" offset="0" max="${10}"></paginator>
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