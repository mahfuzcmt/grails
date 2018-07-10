<g:if test="${venues.size() > 0}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="add.venue.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.event.add.venue"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row venue chosen-wrapper">
                <label><g:message code="select.venue"/></label>
                <g:select from="${venues.name}" keys="${venues.id}" name="venue" class="large venue-selector"/>
            </div>
            <g:include view="plugins/general_event/admin/event/addVenueLocationList.gsp" model="[locations: venues[0].locations]"/>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button add-venue-btn edit-popup-form-submit"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:if>
<g:else>
    <div class="info-row">
        <span class="info-message"><g:message code="no.venue.created"/> <span class="link switch-to-venue-tab"><g:message code='create.venue.first'/></span></span>
    </div>
</g:else>