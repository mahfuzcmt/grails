<div class="form-row venue-location chosen-wrapper">
    <label><g:message code="select.venue.location"/></label>
    <g:if test="${locations.size() > 0}">
        <g:select from="${locations.name}" keys="${locations.id}" name="location" class="large location-selector"/>
    </g:if>
    <g:else>
        <div class="info-row">
            <span class="info-message"><g:message code="no.venue.location.created"/>
                <span class="link activate-venue-location-tab"><g:message code='create.venue.location.first'/></span></span>
        </div>
    </g:else>
</div>