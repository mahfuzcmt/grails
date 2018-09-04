<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <div class="body">
                    <table class="content">
                        <colgroup>
                            <col style="width: 80%">
                            <col style="width: 20%">
                        </colgroup>
                        <tr>
                            <th><g:message code="event.name"/></th>
                            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
                        </tr>
                        <g:each in="${events}" var="event">
                            <tr>
                                <td>${event.name.encodeAsBMHTML()}</td>
                                <td class="actions-column" event=${event.id}>
                                    <input type="checkbox" class="multiple">
                                    <input type="hidden" value="${event.id}">
                                </td>
                            </tr>
                        </g:each>
                        <g:set var="loopCount" value="${params.int("max") - events.size()}"/>
                        <g:if test="${loopCount > 0}">
                            <g:each in="${1..loopCount}" var="i">
                                <tr>
                                    <td>&nbsp;</td>
                                    <td class="actions-column"></td>
                                </tr>
                            </g:each>
                        </g:if>
                    </table>
                </div>
                <div class="footer">
                    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
                </div>
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
                        <th><g:message code="${eventOrVenueType}.name"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${eventsOrVenues}" var="eventOrVenue">
                        <tr>
                            <td>${eventOrVenue.name.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${eventOrVenue.id}" type="${eventOrVenueType}">
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden" name="${eventOrVenueType}" value="${eventOrVenue.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${eventsOrVenues.size()}" offset="0" max="${10}"></paginator>
            </div>
            <div class="hidden">
                <div class="action-column-dice-content">
                    <table>
                        <tr>
                            <td></td>
                            <td class="actions-column">
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