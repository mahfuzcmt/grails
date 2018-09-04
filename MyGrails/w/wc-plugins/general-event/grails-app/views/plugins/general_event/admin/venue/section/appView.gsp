<div class="content create-edit-form section-panel-view">
    <div class="form-section">
        <div class="form-section-info-block">
            <h3><g:message code="${sections.size() > 1 ? 'sections' : 'section'}"/> (${sections.size()})</h3>
        </div>
        <div class="form-section-container-block section-table">
            <table class="content">
                <colgroup>
                    <col style="width: 20%">
                    <col style="width: 20%">
                    <col style="width: 20%">
                    <col style="width: 15%">
                    <col style="width: 15%">
                    <col style="width: 10%">
                </colgroup>
                <tr>
                    <th><g:message code="section.name"/></th>
                    <th><g:message code="location.name"/></th>
                    <th><g:message code="venue.name"/></th>
                    <th><g:message code="number.of.seats"/></th>
                    <th><g:message code="ticket.price"/></th>
                    <th class="actions-column"><g:message code="actions"/></th>
                </tr>
                <g:if test="${sections}">
                    <g:each in="${sections}" var="section">
                        <tr>
                            <td>${section.name.encodeAsBMHTML()}</td>
                            <td>${section.venueLocation?.name.encodeAsBMHTML()}</td>
                            <td>${section.venueLocation.venue?.name.encodeAsBMHTML()}</td>
                            <td>${section.rowCount * section.columnCount}</td>
                            <td>${section.ticketPrice}</td>
                            <td class="actions-column"><span class="action-navigator collapsed" entity-sectionId="${section.id}" entity-locationId="${section.venueLocation.id}" entity-name="${section.name.encodeAsBMHTML()}"></span></td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row">
                        <td colspan="6"><g:message code="no.section.created"/></td>
                    </tr>
                </g:else>
            </table>
        </div>
    </div>
</div>