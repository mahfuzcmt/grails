<%@ page import="com.webcommander.plugin.general_event.VenueLocation" %>
<div class="form-section">
    <div class="form-section-info-block">
        <h3><g:message code="venue"/></h3>
        <div class="info-content"><g:message code="form.section.text.add.venue.info"/></div>
    </div>
    <div class="form-section-container-block venue-table-container with-top-btn">
        <div class="btn-panel add-item">
            <button class="submit-button add-venue-btn" type="button">+&nbsp;<g:message code="add.venue"/></button>
        </div>
        <div class="venue-item-container">
            <g:if test="${event?.venueLocation}">
                <div class="row old-row">
                    <span class="name">${event.venueLocation.name.encodeAsBMHTML()}</span>
                    <div class="column actions-column">
                        <span class="action-navigator collapsed" ></span>
                    </div>
                </div>
            </g:if>
        </div>
    </div>
</div>