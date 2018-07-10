<div class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info-block">
            <h3><g:message code="locations"/>(${locations ? locations.size() : venue?.locations?.size()})</h3>
            ${venue ? '<div class="info-content"><g:message code="section.text.venue.location.info"/></div>' : ''}
        </div>
        <div class="form-section-container-block location-table-container with-top-btn">
            <g:if test="${venue}">
                <div class="btn-panel add-item">
                    <button class="submit-button add-location-btn" type="button">+&nbsp;<g:message code="create.location"/></button>
                </div>
            </g:if>
            <g:else>
                &nbsp;
            </g:else>
            <g:set var="locationList" value="${venue ? venue.locations : locations}"/>
            <div class="location-item-container">
                <g:each in="${locationList.findAll{it != null}}" var="location">
                    <g:include view="/plugins/general_event/admin/venue/location/locationRow.gsp" model="[location: location, clazz: clazz]"/>
                </g:each>
            </div>
        </div>
    </div>
</div>