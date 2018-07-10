<g:if test="${venues.size() > 0}">
    <form action="${app.relativeBaseUrl()}eventAdmin/saveVenueBooking" method="post" class="edit-popup-form create-edit-form">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="book.venue"/> </h3>
                <div class="info-content"><g:message code="form.section.text.event.book.venue"/></div>
            </div>
            <div class="form-section-container">
                <input type="hidden" name="eventId" value="${eventId}">
                <input type="hidden" name="sessionId" value="${sessionId}">
                <div class="form-row venue chosen-wrapper">
                    <label><g:message code="select.venue"/></label>
                    <g:select from="${venues.name}" keys="${venues.id}" name="venue" class="large venue-selector"/>
                </div>
                <g:include view="plugins/event_management/admin/event/venueLocationListing.gsp" model="[locations: venues[0].locations]"/>
                <div class="form-row">
                    <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </div>
            </div>
        </div>
    </form>
</g:if>
<g:else>
    <div class="info-row">
        <span class="info-message"><g:message code="no.venue.created"/> <span class="link activate-venue-tab"><g:message code='create.venue.first'/></span></span>
    </div>
</g:else>